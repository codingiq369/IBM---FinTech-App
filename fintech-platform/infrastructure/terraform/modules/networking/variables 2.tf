variable "environment" {
  description = "Environment name (dev, staging, uat, production)."
  type        = string
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC."
  type        = string
}

variable "availability_zones" {
  description = "AZs to spread public/private subnets across."
  type        = list(string)
}

variable "enable_nat_gateway" {
  description = "Whether to provision a NAT gateway for private subnet egress. Off in dev to save cost."
  type        = bool
  default     = true
}
