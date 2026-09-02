# Not consumed today -- no service in the implemented vertical slice
# generates or stores files (statements, exports, document-verification
# uploads for KYC, etc.). Built so it's ready when one does; no
# environment's root module calls this yet.

resource "aws_s3_bucket" "this" {
  bucket = "fintech-${var.environment}-${var.bucket_purpose}"
}

resource "aws_s3_bucket_versioning" "this" {
  bucket = aws_s3_bucket.this.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "this" {
  bucket = aws_s3_bucket.this.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "aws:kms"
    }
  }
}

resource "aws_s3_bucket_public_access_block" "this" {
  bucket                  = aws_s3_bucket.this.id
  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
