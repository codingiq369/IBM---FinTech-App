output "role_arns" {
  description = "Map of service name -> IAM role ARN, for patching each overlay's ServiceAccount annotation."
  value       = { for name, role in aws_iam_role.service : name => role.arn }
}
