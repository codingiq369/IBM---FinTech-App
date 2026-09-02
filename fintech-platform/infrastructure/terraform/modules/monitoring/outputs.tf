output "log_group_names" {
  value = { for name, lg in aws_cloudwatch_log_group.service : name => lg.name }
}

output "alarms_topic_arn" {
  value = aws_sns_topic.alarms.arn
}
