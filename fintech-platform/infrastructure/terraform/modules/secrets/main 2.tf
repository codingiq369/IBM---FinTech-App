# DB credentials, stored so the cluster can read them without anyone
# putting them in a Kubernetes Secret by hand -- an External Secrets
# Operator deployment (not included in this scaffold yet) would sync this
# into the fintech-db-credentials Secret templated in
# infrastructure/kubernetes/base/secrets/fintech-db-credentials.yaml.

resource "aws_secretsmanager_secret" "db_credentials" {
  name = "fintech/${var.environment}/db-credentials"
}

resource "aws_secretsmanager_secret_version" "db_credentials" {
  secret_id = aws_secretsmanager_secret.db_credentials.id
  secret_string = jsonencode({
    username = var.db_username
    password = var.db_password
  })
}
