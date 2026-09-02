# One IAM role per Kubernetes ServiceAccount (IRSA), trust-scoped to that
# exact namespace + service account name so a pod can only assume the role
# matching its own identity. Matches the `eks.amazonaws.com/role-arn`
# annotation placeholder in infrastructure/kubernetes/base/serviceaccounts/.

data "aws_iam_policy_document" "assume_role" {
  for_each = toset(var.service_names)

  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRoleWithWebIdentity"]

    principals {
      type        = "Federated"
      identifiers = [var.oidc_provider_arn]
    }

    condition {
      test     = "StringEquals"
      variable = "${var.oidc_provider_url}:sub"
      values   = ["system:serviceaccount:${var.namespace}:${each.value}"]
    }
  }
}

resource "aws_iam_role" "service" {
  for_each           = toset(var.service_names)
  name               = "fintech-${var.environment}-${each.value}"
  assume_role_policy = data.aws_iam_policy_document.assume_role[each.value].json
}

data "aws_iam_policy_document" "read_db_secret" {
  statement {
    effect    = "Allow"
    actions   = ["secretsmanager:GetSecretValue"]
    resources = [var.db_credentials_secret_arn]
  }

  statement {
    effect    = "Allow"
    actions   = ["logs:PutLogEvents", "logs:CreateLogStream"]
    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "service" {
  for_each = toset(var.service_names)
  name     = "fintech-${var.environment}-${each.value}-policy"
  role     = aws_iam_role.service[each.value].id
  policy   = data.aws_iam_policy_document.read_db_secret.json
}
