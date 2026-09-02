terraform {
  required_version = ">= 1.7"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Bucket and lock table are bootstrapped once, out of band, before this is
  # ever run -- Terraform can't create the backend it's about to store its
  # own state in. Same bucket for every environment, separated by key.
  backend "s3" {
    bucket         = "fintech-platform-terraform-state"
    key            = "environments/dev/terraform.tfstate"
    region         = "us-east-1"
    dynamodb_table = "fintech-platform-terraform-locks"
    encrypt        = true
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Environment = "dev"
      ManagedBy   = "terraform"
      Project     = "fintech-platform"
    }
  }
}
