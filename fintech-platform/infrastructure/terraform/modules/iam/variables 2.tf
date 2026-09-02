variable "environment" {
  type = string
}

variable "oidc_provider_arn" {
  type = string
}

variable "oidc_provider_url" {
  description = "Without the leading https://, matching how Terraform's aws_iam_openid_connect_provider.url comes back."
  type        = string
}

variable "namespace" {
  description = "Kubernetes namespace the service accounts live in (fintech-<env>)."
  type        = string
}

variable "db_credentials_secret_arn" {
  type = string
}

variable "service_names" {
  type    = list(string)
  default = ["customer-service", "accounts-service", "ledger-service", "transfers-service", "api-gateway", "web-banking"]
}
