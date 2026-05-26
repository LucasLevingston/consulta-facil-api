variable "aws_region" {
  description = "AWS region for all resources"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Deployment environment (prod, staging)"
  type        = string
  default     = "prod"
}

variable "app_name" {
  description = "Application name prefix for all resources"
  type        = string
  default     = "consulta-facil"
}

# ─── VPC ─────────────────────────────────────────────────────────────────────

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets (ALB)"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets (ECS, RDS)"
  type        = list(string)
  default     = ["10.0.3.0/24", "10.0.4.0/24"]
}

# ─── RDS ─────────────────────────────────────────────────────────────────────

variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t3.micro"
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "consulta_facil_db"
}

variable "db_username" {
  description = "PostgreSQL master username"
  type        = string
  default     = "cfadmin"
  sensitive   = true
}

# ─── ECS ─────────────────────────────────────────────────────────────────────

variable "api_cpu" {
  description = "Fargate CPU units for API task (256 = 0.25 vCPU)"
  type        = number
  default     = 512
}

variable "api_memory" {
  description = "Fargate memory (MB) for API task"
  type        = number
  default     = 1024
}

variable "web_cpu" {
  description = "Fargate CPU units for Web task"
  type        = number
  default     = 256
}

variable "web_memory" {
  description = "Fargate memory (MB) for Web task"
  type        = number
  default     = 512
}

variable "api_desired_count" {
  description = "Desired number of API ECS tasks"
  type        = number
  default     = 1
}

variable "web_desired_count" {
  description = "Desired number of Web ECS tasks"
  type        = number
  default     = 1
}

# ─── ALB / DNS ───────────────────────────────────────────────────────────────

variable "domain_name" {
  description = "Base domain (e.g. consulta-facil.com). Leave empty for HTTP-only ALB."
  type        = string
  default     = ""
}

variable "acm_certificate_arn" {
  description = "ACM certificate ARN for HTTPS. Required when domain_name is set."
  type        = string
  default     = ""
}

# ─── Secrets (set via terraform.tfvars or -var flags — never commit actual values) ───

variable "jwt_secret" {
  description = "JWT signing secret (min 32 chars)"
  type        = string
  sensitive   = true
}

variable "db_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
}

variable "mercadopago_access_token" {
  description = "MercadoPago API access token"
  type        = string
  sensitive   = true
}

variable "grafana_otlp_endpoint" {
  description = "Grafana Cloud OTLP endpoint URL"
  type        = string
  sensitive   = true
}

variable "grafana_otlp_token" {
  description = "Grafana Cloud OTLP token (base64 instanceId:apiKey)"
  type        = string
  sensitive   = true
}

variable "anthropic_api_key" {
  description = "Anthropic API key (optional — leave empty to disable voice booking)"
  type        = string
  default     = ""
  sensitive   = true
}

variable "twilio_account_sid" {
  description = "Twilio account SID (optional — leave empty to disable WhatsApp)"
  type        = string
  default     = ""
  sensitive   = true
}

variable "twilio_auth_token" {
  description = "Twilio auth token"
  type        = string
  default     = ""
  sensitive   = true
}

variable "grafana_faro_url" {
  description = "Grafana Faro collector URL for web RUM (optional)"
  type        = string
  default     = ""
  sensitive   = true
}
