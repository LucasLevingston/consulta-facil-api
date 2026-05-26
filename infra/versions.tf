terraform {
  required_version = ">= 1.6"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Remote state — create the bucket + table before running terraform init
  # aws s3 mb s3://consulta-facil-tfstate --region us-east-1
  # aws dynamodb create-table --table-name consulta-facil-tflock \
  #   --attribute-definitions AttributeName=LockID,AttributeType=S \
  #   --key-schema AttributeName=LockID,KeyType=HASH \
  #   --billing-mode PAY_PER_REQUEST
  backend "s3" {
    bucket         = "consulta-facil-tfstate"
    key            = "prod/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "consulta-facil-tflock"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "consulta-facil"
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}
