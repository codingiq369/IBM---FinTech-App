output "cluster_name" {
  value = module.kubernetes.cluster_name
}

output "cluster_endpoint" {
  value = module.kubernetes.cluster_endpoint
}

output "db_endpoint" {
  value = module.databases.endpoint
}

output "db_credentials_secret_arn" {
  value = module.secrets.db_credentials_secret_arn
}

output "service_role_arns" {
  description = "Feed these into infrastructure/kubernetes/overlays/<env> ServiceAccount annotations."
  value       = module.iam.role_arns
}

output "acm_certificate_arn" {
  value = module.load_balancer.certificate_arn
}
