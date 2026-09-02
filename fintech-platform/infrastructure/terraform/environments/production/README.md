# environments/production

Provisions the production cluster, database, secrets, IAM roles, DNS/cert/WAF, and
CloudWatch resources this environment needs, by calling the shared modules in
`infrastructure/terraform/modules/`.

    cd infrastructure/terraform/environments/production
    terraform init
    TF_VAR_db_master_password=... terraform plan -out plan.tfout
    terraform apply plan.tfout

This has been validated as HCL syntax (balanced blocks) but not run through
`terraform validate` or `terraform plan` against real AWS credentials --
treat any error you hit running it for real the same way the root README
asks you to treat a Java compile error: a real bug report, not a surprise.
`.github/workflows/infrastructure.yml` runs `terraform fmt -check`,
`validate`, and `plan` on every change under this path.

Outputs feed `infrastructure/kubernetes/overlays/production` (DB host, IAM role
ARNs) -- see that overlay's README for how they're wired in today (as
`REPLACE_WITH_...` placeholders, patched by hand or by a future step in
`.github/workflows/cd-production.yml`).
