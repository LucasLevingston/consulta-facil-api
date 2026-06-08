output "alb_dns_name" {
  description = "ALB DNS name — point your domain CNAME here"
  value       = aws_lb.main.dns_name
}

output "app_url" {
  description = "Application URL"
  value       = local.app_url
}

output "api_ecr_url" {
  description = "ECR repository URL for the API image"
  value       = aws_ecr_repository.api.repository_url
}

output "web_ecr_url" {
  description = "ECR repository URL for the Web image"
  value       = aws_ecr_repository.web.repository_url
}

output "rds_endpoint" {
  description = "RDS instance endpoint"
  value       = aws_db_instance.postgres.address
  sensitive   = true
}

output "ecs_cluster_name" {
  description = "ECS cluster name"
  value       = aws_ecs_cluster.main.name
}

output "api_service_name" {
  description = "ECS API service name"
  value       = aws_ecs_service.api.name
}

output "web_service_name" {
  description = "ECS Web service name"
  value       = aws_ecs_service.web.name
}

output "api_log_group" {
  description = "CloudWatch log group for API"
  value       = aws_cloudwatch_log_group.api.name
}

output "web_log_group" {
  description = "CloudWatch log group for Web"
  value       = aws_cloudwatch_log_group.web.name
}

output "route53_nameservers" {
  description = "Route53 nameservers — update your registrar to these (only when domain_name is set)"
  value       = var.domain_name != "" ? aws_route53_zone.main[0].name_servers : []
}

output "acm_certificate_arn" {
  description = "ACM certificate ARN in use (Terraform-created or BYO)"
  value       = local.certificate_arn
}

output "cloudfront_distribution_id" {
  description = "CloudFront distribution ID — used for cache invalidation on deploy"
  value       = var.domain_name != "" && var.enable_cloudfront ? aws_cloudfront_distribution.main[0].id : ""
}
