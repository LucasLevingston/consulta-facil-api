locals {
  ssm_prefix = "/${var.app_name}/${var.environment}"

  db_url = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/${var.db_name}"
}

resource "aws_ssm_parameter" "jwt_secret" {
  name  = "${local.ssm_prefix}/jwt-secret"
  type  = "SecureString"
  value = var.jwt_secret
  tags  = { Name = "jwt-secret" }
}

resource "aws_ssm_parameter" "db_url" {
  name  = "${local.ssm_prefix}/db-url"
  type  = "SecureString"
  value = local.db_url
  tags  = { Name = "db-url" }
}

resource "aws_ssm_parameter" "db_username" {
  name  = "${local.ssm_prefix}/db-username"
  type  = "SecureString"
  value = var.db_username
  tags  = { Name = "db-username" }
}

resource "aws_ssm_parameter" "db_password" {
  name  = "${local.ssm_prefix}/db-password"
  type  = "SecureString"
  value = var.db_password
  tags  = { Name = "db-password" }
}

resource "aws_ssm_parameter" "mercadopago_token" {
  name  = "${local.ssm_prefix}/mercadopago-token"
  type  = "SecureString"
  value = var.mercadopago_access_token
  tags  = { Name = "mercadopago-token" }
}

resource "aws_ssm_parameter" "mercadopago_webhook_secret" {
  name  = "${local.ssm_prefix}/mercadopago-webhook-secret"
  type  = "SecureString"
  value = var.mercadopago_webhook_secret
  tags  = { Name = "mercadopago-webhook-secret" }
}

resource "aws_ssm_parameter" "grafana_otlp_endpoint" {
  name  = "${local.ssm_prefix}/grafana-otlp-endpoint"
  type  = "SecureString"
  value = var.grafana_otlp_endpoint
  tags  = { Name = "grafana-otlp-endpoint" }
}

resource "aws_ssm_parameter" "grafana_otlp_token" {
  name  = "${local.ssm_prefix}/grafana-otlp-token"
  type  = "SecureString"
  value = var.grafana_otlp_token
  tags  = { Name = "grafana-otlp-token" }
}

resource "aws_ssm_parameter" "anthropic_api_key" {
  name  = "${local.ssm_prefix}/anthropic-api-key"
  type  = "SecureString"
  value = var.anthropic_api_key != "" ? var.anthropic_api_key : "disabled"
  tags  = { Name = "anthropic-api-key" }
}

resource "aws_ssm_parameter" "twilio_account_sid" {
  name  = "${local.ssm_prefix}/twilio-account-sid"
  type  = "SecureString"
  value = var.twilio_account_sid != "" ? var.twilio_account_sid : "disabled"
  tags  = { Name = "twilio-account-sid" }
}

resource "aws_ssm_parameter" "twilio_auth_token" {
  name  = "${local.ssm_prefix}/twilio-auth-token"
  type  = "SecureString"
  value = var.twilio_auth_token != "" ? var.twilio_auth_token : "disabled"
  tags  = { Name = "twilio-auth-token" }
}

resource "aws_ssm_parameter" "grafana_faro_url" {
  name  = "${local.ssm_prefix}/grafana-faro-url"
  type  = "SecureString"
  value = var.grafana_faro_url != "" ? var.grafana_faro_url : "disabled"
  tags  = { Name = "grafana-faro-url" }
}

resource "aws_ssm_parameter" "redis_host" {
  count = var.enable_elasticache ? 1 : 0
  name  = "${local.ssm_prefix}/redis-host"
  type  = "String"
  value = local.redis_host
  tags  = { Name = "redis-host" }
}
