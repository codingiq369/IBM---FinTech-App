variable "environment" {
  type = string
}

variable "subnet_ids" {
  type = list(string)
}

variable "vpc_security_group_ids" {
  type = list(string)
}

variable "instance_class" {
  type = string
}

variable "allocated_storage" {
  type = number
}

variable "multi_az" {
  type    = bool
  default = false
}

variable "deletion_protection" {
  type    = bool
  default = false
}

variable "backup_retention_period" {
  type    = number
  default = 1
}

variable "master_username" {
  type    = string
  default = "fintech"
}

variable "master_password" {
  description = "Set via TF_VAR_master_password from the secret store, never committed in a .tfvars file."
  type        = string
  sensitive   = true
}
