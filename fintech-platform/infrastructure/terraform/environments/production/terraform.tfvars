# production environment sizing. db_master_password is deliberately absent --
# see variables.tf. route53_zone_id is a placeholder until a real hosted
# zone for fintech.example.com exists.

vpc_cidr            = "10.3.0.0/16"
availability_zones  = ["us-east-1a", "us-east-1b", "us-east-1c"]

node_instance_type = "m6i.xlarge"
node_desired_size  = 3
node_min_size      = 3
node_max_size      = 6

db_instance_class          = "db.r6g.large"
db_allocated_storage       = 200
db_multi_az                = true
db_deletion_protection     = true
db_backup_retention_period = 30

domain_name     = "banking.fintech.example.com"
route53_zone_id = "REPLACE_WITH_ROUTE53_ZONE_ID"

alarm_email        = "platform-oncall@fintech.example.com"
log_retention_days = 90
