# CloudFront CDN — only created when domain_name is set and enable_cloudfront=true
#
# Architecture: Route53 → CloudFront → ALB → ECS
# ACM cert MUST be in us-east-1 (enforced because our region var defaults to us-east-1)
# Static assets (_next/static) cached for 1 year; API and pages bypass cache.

resource "aws_cloudfront_distribution" "main" {
  count = var.domain_name != "" && var.enable_cloudfront ? 1 : 0

  origin {
    domain_name = aws_lb.main.dns_name
    origin_id   = "alb"

    custom_origin_config {
      http_port              = 80
      https_port             = 443
      origin_protocol_policy = local.certificate_arn != "" ? "https-only" : "http-only"
      origin_ssl_protocols   = ["TLSv1.2"]
    }
  }

  enabled         = true
  is_ipv6_enabled = true
  aliases         = [var.domain_name, "www.${var.domain_name}"]

  # ── API paths — no cache, all headers/cookies forwarded ──────────────────
  ordered_cache_behavior {
    path_pattern     = "/v1/*"
    allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "alb"

    forwarded_values {
      query_string = true
      headers      = ["*"]
      cookies { forward = "all" }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 0
    max_ttl                = 0
  }

  # ── Next.js static assets — long cache, no cookies ────────────────────────
  ordered_cache_behavior {
    path_pattern     = "/_next/static/*"
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "alb"
    compress         = true

    forwarded_values {
      query_string = false
      cookies { forward = "none" }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 86400    # 1 day default
    max_ttl                = 31536000 # 1 year max
  }

  # ── Default — SSR pages, no cache, cookies forwarded ─────────────────────
  default_cache_behavior {
    allowed_methods  = ["DELETE", "GET", "HEAD", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "alb"

    forwarded_values {
      query_string = true
      headers      = ["Host", "Authorization", "CloudFront-Forwarded-Proto"]
      cookies { forward = "all" }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 0
    max_ttl                = 0
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn      = local.certificate_arn
    ssl_support_method       = "sni-only"
    minimum_protocol_version = "TLSv1.2_2021"
  }

  price_class = "PriceClass_All"
  tags        = { Name = "${var.app_name}-cdn" }
}
