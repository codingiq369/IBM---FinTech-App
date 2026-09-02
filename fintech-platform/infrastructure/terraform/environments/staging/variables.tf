variable "aws_region" {
  type    = string
  default = "us-east-1"
}

variable "vpc_cidr" {
  type = string
}

variable "availability_zones" {
  type = list(string)
}

variable "node_instance_type" {
  type = string
}

variable "node_desired_size" {
  type = number
}

variable "node_min_size" {
  type = number
}

variable "node_max_size" {
  type = number
}

variable "db_instance_class" {
  type = string
}

variable "db_allocated_storage" {
  type = number
}

variable "db_multi_az" {
  type = bool
}

variable "db_deletion_protection" {
  type = bool
}

variable "db_backup_retention_period" {
  type = number
}

variable "db_master_username" {
  type    = string
  default = "fintech"
}

variable "db_master_password" {
  description = "Never set in terraform.tfvars. Supply via TF_VAR_db_master_password from the pipeline's secret store (see .github/workflows/infrastructure.yml)."
  type        = string
  sensitive   = true
}

variable "domain_name" {
  type = string
}

variable "route53_zone_id" {
  description = "Pre-existing hosted zone for fintech.example.com -- Terraform doesn't own DNS zone creation, only the records under it."
  type        = string
}

variable "alarm_email" {
  type    = string
  default = ""
}

variable "log_retention_days" {
  type = number
}
