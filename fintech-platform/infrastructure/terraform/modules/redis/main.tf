# Not consumed by any service in the implemented vertical slice today --
# every service talks straight to its own Postgres database, no cache or
# session store in front of it. Built so it's ready if/when one of the
# services under docs/domains needs it; no environment's root module
# (infrastructure/terraform/environments/<env>/main.tf) calls this yet.

resource "aws_elasticache_subnet_group" "this" {
  name       = "fintech-${var.environment}"
  subnet_ids = var.subnet_ids
}

resource "aws_elasticache_cluster" "this" {
  cluster_id           = "fintech-${var.environment}"
  engine               = "redis"
  engine_version       = "7.1"
  node_type            = var.node_type
  num_cache_nodes      = var.num_cache_nodes
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.this.name
  security_group_ids   = var.vpc_security_group_ids
}
