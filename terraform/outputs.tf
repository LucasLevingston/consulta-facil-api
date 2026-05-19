output "api_url" {
  description = "URL pública da API (configure CORS_ALLOWED_ORIGINS no frontend)"
  value       = "http://${aws_lb.main.dns_name}/v1"
}

output "alb_dns_name" {
  description = "DNS do Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "ecr_repository_url" {
  description = "URL do repositório ECR (usado no GitHub Actions)"
  value       = aws_ecr_repository.app.repository_url
}

output "ecs_cluster_name" {
  description = "Nome do cluster ECS (GitHub Actions: ECS_CLUSTER)"
  value       = aws_ecs_cluster.main.name
}

output "ecs_service_name" {
  description = "Nome do serviço ECS (GitHub Actions: ECS_SERVICE)"
  value       = aws_ecs_service.app.name
}

output "ecs_task_family" {
  description = "Família da task definition (GitHub Actions: ECS_TASK_FAMILY)"
  value       = aws_ecs_task_definition.app.family
}

output "rds_endpoint" {
  description = "Endpoint do banco RDS"
  value       = aws_db_instance.postgres.address
  sensitive   = true
}

output "container_name" {
  description = "Nome do container ECS (GitHub Actions: CONTAINER_NAME)"
  value       = "${var.project_name}-api"
}
