# dev environment sizing. db_master_password is deliberately absent --
# see variables.tf. route53_zone_id is a placeholder until a real hosted
# zone for fintech.example.com exists.

vpc_cidr            = "10.0.0.0/16"
availability_zones  = ["us-east-1a", "us-east-1b"]

node_instance_type = "t3.medium"
node_desired_size  = 1
node_min_size      = 1
node_max_size      = 2

db_instance_class          = "db.t3.micro"
db_allocated_storage       = 20
db_multi_az                = false
db_deletion_protection     = false
db_backup_retention_period = 1

domain_name     = "dev.banking.fintech.example.com"
route53_zone_id = "REPLACE_WITH_ROUTE53_ZONE_ID"

alarm_email        = ""
log_retention_days = 7
