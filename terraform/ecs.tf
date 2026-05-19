resource "aws_cloudwatch_log_group" "app" {
  name              = "/ecs/${local.name_prefix}"
  retention_in_days = 30
}

resource "aws_ecs_cluster" "main" {
  name = local.name_prefix

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = { Name = local.name_prefix }
}

resource "aws_ecs_task_definition" "app" {
  family                   = "${local.name_prefix}-api"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.ecs_cpu
  memory                   = var.ecs_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name  = "${var.project_name}-api"
    # Imagem placeholder: o GitHub Actions atualizará a task definition no 1º deploy
    image = "${aws_ecr_repository.app.repository_url}:latest"

    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]

    environment = [
      { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
      { name = "DB_URL",      value = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.db_name}" },
      { name = "DB_USERNAME", value = var.db_username },
      { name = "AWS_REGION",  value = var.aws_region },
      { name = "AWS_S3_BUCKET",          value = var.s3_bucket_name },
      { name = "CORS_ALLOWED_ORIGINS",   value = var.cors_allowed_origins },
      { name = "APP_URL",                value = var.app_url },
    ]

    secrets = [
      { name = "DB_PASSWORD",              valueFrom = aws_ssm_parameter.db_password.arn },
      { name = "JWT_SECRET",               valueFrom = aws_ssm_parameter.jwt_secret.arn },
      { name = "MERCADOPAGO_ACCESS_TOKEN", valueFrom = aws_ssm_parameter.mercadopago_token.arn },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.app.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "wget -qO- http://localhost:8080/v1/actuator/health || exit 1"]
      interval    = 30
      timeout     = 10
      retries     = 3
      startPeriod = 90  # Spring Boot leva tempo para iniciar
    }
  }])

  # O GitHub Actions gerencia as revisões da task definition após o 1º apply
  lifecycle {
    ignore_changes = [container_definitions]
  }
}

resource "aws_ecs_service" "app" {
  name            = "${local.name_prefix}-api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.ecs_desired_count
  launch_type     = "FARGATE"

  # ECS Fargate em subnets públicas (sem custo de NAT Gateway)
  network_configuration {
    subnets          = aws_subnet.public[*].id
    security_groups  = [aws_security_group.ecs.id]
    assign_public_ip = true
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.app.arn
    container_name   = "${var.project_name}-api"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  deployment_controller {
    type = "ECS"
  }

  depends_on = [aws_lb_listener.http]
}
