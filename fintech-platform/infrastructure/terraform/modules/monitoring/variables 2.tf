variable "environment" {
  type = string
}

variable "log_retention_days" {
  type = number
}

variable "alarm_email" {
  description = "Set to \"\" to skip creating an email subscription (e.g. in dev)."
  type        = string
  default     = ""
}

variable "service_names" {
  type    = list(string)
  default = ["customer-service", "accounts-service", "ledger-service", "transfers-service", "api-gateway", "web-banking"]
}
