variable "environment" {
  type = string
}

variable "bucket_purpose" {
  description = "Suffix identifying what the bucket is for, e.g. \"statements\" or \"exports\"."
  type        = string
}
