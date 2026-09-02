# staging environment sizing. db_master_password is deliberately absent --
# see variables.tf. route53_zone_id is a placeholder until a real hosted
# zone for fintech.example.com exists.

vpc_cidr            = "10.1.0.0/16"
availability_zones  = ["us-east-1a", "us-east-1b"]

node_instance_type = "t3.large"
node_desired_size  = 2
node_min_size      = 2
node_max_size      = 4

db_instance_class          = "db.t3.small"
db_allocated_storage       = 50
db_multi_az                = false
db_deletion_protection     = false
db_backup_retention_period = 3

domain_name     = "staging.banking.fintech.example.com"
route53_zone_id = "REPLACE_WITH_ROUTE53_ZONE_ID"

alarm_email        = ""
log_retention_days = 14
