data "aws_caller_identity" "current" {}

locals {
  account_id = data.aws_caller_identity.current.account_id

  # Resolve cert ARN: BYO > Terraform-created > none
  certificate_arn = (
    var.acm_certificate_arn != "" ? var.acm_certificate_arn :
    (var.domain_name != "" ? aws_acm_certificate_validation.main[0].certificate_arn : "")
  )

  https_enabled = local.certificate_arn != ""
  app_url       = local.https_enabled ? "https://${var.domain_name}" : "http://${aws_lb.main.dns_name}"
  api_url       = "${local.app_url}/v1"
}

# ─── ECS Cluster ──────────────────────────────────────────────────────────────

resource "aws_ecs_cluster" "main" {
  name = var.app_name

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = { Name = "${var.app_name}-cluster" }
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }
}

# ─── CloudWatch Log Groups ────────────────────────────────────────────────────

resource "aws_cloudwatch_log_group" "api" {
  name              = "/ecs/${var.app_name}/api"
  retention_in_days = 30
  tags              = { Name = "${var.app_name}-api-logs" }
}

resource "aws_cloudwatch_log_group" "web" {
  name              = "/ecs/${var.app_name}/web"
  retention_in_days = 14
  tags              = { Name = "${var.app_name}-web-logs" }
}

# ─── API Task Definition ──────────────────────────────────────────────────────

resource "aws_ecs_task_definition" "api" {
  family                   = "${var.app_name}-api"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.api_cpu
  memory                   = var.api_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "api"
    image     = "${aws_ecr_repository.api.repository_url}:latest"
    essential = true

    portMappings = [{
      containerPort = 8080
      protocol      = "tcp"
    }]

    environment = concat([
      { name = "SPRING_PROFILES_ACTIVE", value = "prod" },
      { name = "AWS_REGION",             value = var.aws_region },
      { name = "AWS_S3_BUCKET",          value = "${var.app_name}-images" },
      { name = "APP_URL",                value = local.app_url },
      { name = "CORS_ALLOWED_ORIGINS",   value = local.app_url },
      { name = "AWS_SES_FROM_EMAIL",     value = aws_ssm_parameter.ses_from_email.value },
    ], var.enable_elasticache ? [
      { name = "REDIS_HOST", value = local.redis_host },
      { name = "REDIS_PORT", value = local.redis_port },
    ] : [])

    secrets = [
      { name = "JWT_SECRET",                valueFrom = aws_ssm_parameter.jwt_secret.arn },
      { name = "DB_URL",                    valueFrom = aws_ssm_parameter.db_url.arn },
      { name = "DB_USERNAME",               valueFrom = aws_ssm_parameter.app_db_username.arn },
      { name = "DB_PASSWORD",               valueFrom = aws_ssm_parameter.app_db_password.arn },
      { name = "FLYWAY_DB_USERNAME",        valueFrom = aws_ssm_parameter.db_username.arn },
      { name = "FLYWAY_DB_PASSWORD",        valueFrom = aws_ssm_parameter.db_password.arn },
      { name = "MERCADOPAGO_ACCESS_TOKEN",   valueFrom = aws_ssm_parameter.mercadopago_token.arn },
      { name = "MERCADOPAGO_WEBHOOK_SECRET", valueFrom = aws_ssm_parameter.mercadopago_webhook_secret.arn },
      { name = "GRAFANA_OTLP_ENDPOINT",      valueFrom = aws_ssm_parameter.grafana_otlp_endpoint.arn },
      { name = "GRAFANA_OTLP_TOKEN",        valueFrom = aws_ssm_parameter.grafana_otlp_token.arn },
      { name = "ANTHROPIC_API_KEY",         valueFrom = aws_ssm_parameter.anthropic_api_key.arn },
      { name = "TWILIO_ACCOUNT_SID",        valueFrom = aws_ssm_parameter.twilio_account_sid.arn },
      { name = "TWILIO_AUTH_TOKEN",         valueFrom = aws_ssm_parameter.twilio_auth_token.arn },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.api.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "api"
      }
    }

    healthCheck = {
      command     = ["CMD-SHELL", "curl -f http://localhost:8080/v1/actuator/health || exit 1"]
      interval    = 30
      timeout     = 10
      retries     = 3
      startPeriod = 60
    }
  }])

  tags = { Name = "${var.app_name}-api-task" }
}

# ─── Web Task Definition ──────────────────────────────────────────────────────

resource "aws_ecs_task_definition" "web" {
  family                   = "${var.app_name}-web"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.web_cpu
  memory                   = var.web_memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name      = "web"
    image     = "${aws_ecr_repository.web.repository_url}:latest"
    essential = true

    portMappings = [{
      containerPort = 3000
      protocol      = "tcp"
    }]

    environment = [
      { name = "NEXT_PUBLIC_API_URL", value = local.api_url },
      { name = "NODE_ENV",            value = "production" },
    ]

    secrets = [
      { name = "ANTHROPIC_API_KEY",          valueFrom = aws_ssm_parameter.anthropic_api_key.arn },
      { name = "NEXT_PUBLIC_GRAFANA_FARO_URL", valueFrom = aws_ssm_parameter.grafana_faro_url.arn },
    ]

    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.web.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "web"
      }
    }
  }])

  tags = { Name = "${var.app_name}-web-task" }
}

# ─── ECS Services ─────────────────────────────────────────────────────────────

resource "aws_ecs_service" "api" {
  name            = "${var.app_name}-api"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.api.arn
  desired_count   = var.api_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.api_ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.api.arn
    container_name   = "api"
    container_port   = 8080
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  deployment_controller {
    type = "ECS"
  }

  lifecycle {
    ignore_changes = [task_definition]
  }

  depends_on = [aws_lb_listener.http]
  tags       = { Name = "${var.app_name}-api-service" }
}

resource "aws_ecs_service" "web" {
  name            = "${var.app_name}-web"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.web.arn
  desired_count   = var.web_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.web_ecs.id]
    assign_public_ip = false
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.web.arn
    container_name   = "web"
    container_port   = 3000
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  deployment_controller {
    type = "ECS"
  }

  lifecycle {
    ignore_changes = [task_definition]
  }

  depends_on = [aws_lb_listener.http]
  tags       = { Name = "${var.app_name}-web-service" }
}
