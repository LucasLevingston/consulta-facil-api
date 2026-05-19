# Parâmetros sensíveis armazenados como SecureString no SSM Parameter Store.
# Os valores aqui são placeholders — atualize-os ANTES do primeiro deploy:
#
#   aws ssm put-parameter --name "/consulta-facil/prod/db-password" \
#     --value "SUA_SENHA" --type SecureString --overwrite
#
# Ou preencha via terraform.tfvars (os valores vêm das variáveis Terraform).

resource "aws_ssm_parameter" "db_password" {
  name        = "/${var.project_name}/${var.environment}/db-password"
  description = "Senha do banco RDS"
  type        = "SecureString"
  value       = var.db_password

  lifecycle {
    ignore_changes = [value] # Permite rotação manual sem reescrever via Terraform
  }
}

resource "aws_ssm_parameter" "jwt_secret" {
  name        = "/${var.project_name}/${var.environment}/jwt-secret"
  description = "Segredo JWT para assinatura de tokens"
  type        = "SecureString"
  value       = var.jwt_secret

  lifecycle {
    ignore_changes = [value]
  }
}

resource "aws_ssm_parameter" "mercadopago_token" {
  name        = "/${var.project_name}/${var.environment}/mercadopago-token"
  description = "Access token MercadoPago"
  type        = "SecureString"
  value       = var.mercadopago_access_token != "" ? var.mercadopago_access_token : "placeholder"

  lifecycle {
    ignore_changes = [value]
  }
}
