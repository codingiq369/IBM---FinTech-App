# base/serviceaccounts

One ServiceAccount per workload, annotated for IRSA. Overlays patch the role ARN to that environment's IAM role (output of the `iam` Terraform module).
