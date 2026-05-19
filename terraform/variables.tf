variable "aws_region" {
  description = "Região AWS"
  type        = string
  default     = "us-east-1"
}

variable "project_name" {
  description = "Nome base do projeto (usado nos recursos)"
  type        = string
  default     = "consulta-facil"
}

variable "environment" {
  description = "Ambiente (prod, staging)"
  type        = string
  default     = "prod"
}

# --- Banco de dados ---
variable "db_name" {
  description = "Nome do banco PostgreSQL"
  type        = string
  default     = "consulta_facil_db"
}

variable "db_username" {
  description = "Usuário do banco"
  type        = string
  default     = "consulta_facil"
}

variable "db_password" {
  description = "Senha do banco (sensível)"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "Tipo de instância RDS"
  type        = string
  default     = "db.t3.micro"
}

# --- Aplicação ---
variable "jwt_secret" {
  description = "Segredo JWT (mín. 256 bits)"
  type        = string
  sensitive   = true
}

variable "cors_allowed_origins" {
  description = "URL do frontend (ex: https://app.consultafacil.com)"
  type        = string
}

variable "app_url" {
  description = "URL do frontend para redirects MercadoPago"
  type        = string
}

variable "mercadopago_access_token" {
  description = "Access token MercadoPago"
  type        = string
  sensitive   = true
  default     = ""
}

variable "s3_bucket_name" {
  description = "Nome do bucket S3 para imagens"
  type        = string
  default     = "consulta-facil-images-prod"
}

# --- ECS ---
variable "ecs_cpu" {
  description = "CPU do task ECS (unidades)"
  type        = number
  default     = 512
}

variable "ecs_memory" {
  description = "Memória do task ECS (MB)"
  type        = number
  default     = 1024
}

variable "ecs_desired_count" {
  description = "Número de tasks ECS em execução"
  type        = number
  default     = 1
}
