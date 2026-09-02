# The actual ALB is created by the AWS Load Balancer Controller running
# in-cluster, driven by the Ingress object in
# infrastructure/kubernetes/base/ingress/ingress.yaml -- Terraform doesn't
# own that resource directly. What Terraform owns is the DNS-validated
# certificate the controller attaches to it, the WAF web ACL, and the DNS
# record pointing at the resulting load balancer.

resource "aws_acm_certificate" "this" {
  domain_name       = var.domain_name
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.this.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  zone_id = var.route53_zone_id
  name    = each.value.name
  type    = each.value.type
  records = [each.value.record]
  ttl     = 60
}

resource "aws_wafv2_web_acl" "this" {
  name        = "fintech-${var.environment}"
  scope       = "REGIONAL"
  description = "Baseline managed-rule protection for the ${var.environment} ingress ALB."

  default_action {
    allow {}
  }

  rule {
    name     = "aws-managed-common-rules"
    priority = 0

    override_action {
      none {}
    }

    statement {
      managed_rule_group_statement {
        name        = "AWSManagedRulesCommonRuleSet"
        vendor_name = "AWS"
      }
    }

    visibility_config {
      cloudwatch_metrics_enabled = true
      metric_name                = "fintech-${var.environment}-common-rules"
      sampled_requests_enabled   = true
    }
  }

  visibility_config {
    cloudwatch_metrics_enabled = true
    metric_name                = "fintech-${var.environment}"
    sampled_requests_enabled   = true
  }
}
