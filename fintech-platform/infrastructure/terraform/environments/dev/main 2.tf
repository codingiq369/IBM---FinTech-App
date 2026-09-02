module "networking" {
  source = "../../modules/networking"

  environment        = "dev"
  vpc_cidr           = var.vpc_cidr
  availability_zones = var.availability_zones
  enable_nat_gateway = true
}

module "kubernetes" {
  source = "../../modules/kubernetes"

  environment         = "dev"
  vpc_id              = module.networking.vpc_id
  private_subnet_ids  = module.networking.private_subnet_ids
  node_instance_type  = var.node_instance_type
  node_desired_size   = var.node_desired_size
  node_min_size       = var.node_min_size
  node_max_size       = var.node_max_size
}

# Allows the EKS node security group to reach Postgres on 5432. The EKS
# module's default node security group is used here rather than a
# hand-rolled one, kept in the module's own state.
resource "aws_security_group" "database" {
  name   = "fintech-dev-database"
  vpc_id = module.networking.vpc_id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

module "databases" {
  source = "../../modules/databases"

  environment              = "dev"
  subnet_ids               = module.networking.private_subnet_ids
  vpc_security_group_ids   = [aws_security_group.database.id]
  instance_class           = var.db_instance_class
  allocated_storage        = var.db_allocated_storage
  multi_az                 = var.db_multi_az
  deletion_protection      = var.db_deletion_protection
  backup_retention_period  = var.db_backup_retention_period
  master_username          = var.db_master_username
  master_password          = var.db_master_password
}

module "secrets" {
  source = "../../modules/secrets"

  environment = "dev"
  db_username = var.db_master_username
  db_password = var.db_master_password
}

module "iam" {
  source = "../../modules/iam"

  environment                = "dev"
  oidc_provider_arn          = module.kubernetes.oidc_provider_arn
  oidc_provider_url          = replace(module.kubernetes.oidc_provider_url, "https://", "")
  namespace                  = "fintech-dev"
  db_credentials_secret_arn  = module.secrets.db_credentials_secret_arn
}

module "load_balancer" {
  source = "../../modules/load-balancer"

  environment     = "dev"
  domain_name     = var.domain_name
  route53_zone_id = var.route53_zone_id
}

module "monitoring" {
  source = "../../modules/monitoring"

  environment         = "dev"
  log_retention_days  = var.log_retention_days
  alarm_email         = var.alarm_email
}

# redis, kafka, and object-storage modules exist (see
# infrastructure/terraform/modules/) but aren't called here -- nothing in
# the implemented vertical slice consumes them yet. See each module's own
# main.tf header for what would trigger adding a `module` block for it.
