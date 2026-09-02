resource "aws_cloudwatch_log_group" "service" {
  for_each          = toset(var.service_names)
  name              = "/fintech/${var.environment}/${each.value}"
  retention_in_days = var.log_retention_days
}

resource "aws_sns_topic" "alarms" {
  name = "fintech-${var.environment}-alarms"
}

resource "aws_sns_topic_subscription" "alarm_email" {
  count     = var.alarm_email == "" ? 0 : 1
  topic_arn = aws_sns_topic.alarms.arn
  protocol  = "email"
  endpoint  = var.alarm_email
}

resource "aws_cloudwatch_metric_alarm" "node_cpu_high" {
  alarm_name          = "fintech-${var.environment}-node-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods   = 3
  metric_name         = "node_cpu_utilization"
  namespace           = "ContainerInsights"
  period              = 300
  statistic           = "Average"
  threshold           = 85
  alarm_actions       = [aws_sns_topic.alarms.arn]
  dimensions = {
    ClusterName = "fintech-${var.environment}"
  }
}
