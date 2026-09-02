output "certificate_arn" {
  value = aws_acm_certificate.this.arn
}

output "web_acl_arn" {
  value = aws_wafv2_web_acl.this.arn
}
