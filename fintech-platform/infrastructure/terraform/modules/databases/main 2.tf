# One Postgres instance per environment, matching
# deployment/docker/postgres-init/001-create-databases.sql locally: a single
# instance, one database per service (customer_db, accounts_db, ledger_db,
# transfers_db) for real microservice data isolation without four separate
# instances. RDS only creates the database named in `db_name` on first boot
# (same limitation the postgres image has, which is why postgres-init exists
# for Docker Compose) -- deployment/scripts/migrate.sh creates the other
# three against this endpoint before each service's own Flyway migrations run.

resource "aws_db_subnet_group" "this" {
  name       = "fintech-${var.environment}"
  subnet_ids = var.subnet_ids
}

resource "aws_db_instance" "this" {
  identifier     = "fintech-${var.environment}"
  engine         = "postgres"
  engine_version = "16"
  instance_class = var.instance_class

  allocated_storage = var.allocated_storage
  storage_encrypted = true

  db_name  = "fintech"
  username = var.master_username
  password = var.master_password

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = var.vpc_security_group_ids

  multi_az                = var.multi_az
  deletion_protection     = var.deletion_protection
  backup_retention_period = var.backup_retention_period
  skip_final_snapshot     = !var.deletion_protection

  tags = {
    Environment = var.environment
  }
}
