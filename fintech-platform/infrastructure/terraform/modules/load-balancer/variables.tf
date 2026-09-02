variable "environment" {
  type = string
}

variable "domain_name" {
  description = "e.g. banking.fintech.example.com for production, staging.banking.fintech.example.com for staging."
  type        = string
}

variable "route53_zone_id" {
  type = string
}
