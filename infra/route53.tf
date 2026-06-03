# Route53 + ACM — only created when var.domain_name is set
#
# If var.acm_certificate_arn is provided (BYO cert), ACM resources are skipped.
# If empty, a new certificate is created and validated via DNS automatically.
# After apply, copy the Route53 nameservers (output.route53_nameservers) to your registrar.

# ─── Hosted Zone ──────────────────────────────────────────────────────────────

resource "aws_route53_zone" "main" {
  count = var.domain_name != "" ? 1 : 0
  name  = var.domain_name
  tags  = { Name = "${var.app_name}-zone" }
}

# ─── ACM Certificate (skipped when acm_certificate_arn is supplied) ───────────

resource "aws_acm_certificate" "main" {
  count             = var.domain_name != "" && var.acm_certificate_arn == "" ? 1 : 0
  domain_name       = var.domain_name
  validation_method = "DNS"

  subject_alternative_names = ["www.${var.domain_name}"]

  lifecycle {
    create_before_destroy = true
  }

  tags = { Name = "${var.app_name}-cert" }
}

resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in(
      var.domain_name != "" && var.acm_certificate_arn == ""
      ? aws_acm_certificate.main[0].domain_validation_options
      : []
    ) : dvo.domain_name => dvo
  }

  allow_overwrite = true
  name            = each.value.resource_record_name
  records         = [each.value.resource_record_value]
  ttl             = 60
  type            = each.value.resource_record_type
  zone_id         = aws_route53_zone.main[0].zone_id
}

resource "aws_acm_certificate_validation" "main" {
  count                   = var.domain_name != "" && var.acm_certificate_arn == "" ? 1 : 0
  certificate_arn         = aws_acm_certificate.main[0].arn
  validation_record_fqdns = [for r in aws_route53_record.cert_validation : r.fqdn]
}

# ─── A Records → ALB (or CloudFront when spec-010 is applied) ────────────────

resource "aws_route53_record" "apex" {
  count   = var.domain_name != "" ? 1 : 0
  zone_id = aws_route53_zone.main[0].zone_id
  name    = var.domain_name
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = true
  }
}

resource "aws_route53_record" "www" {
  count   = var.domain_name != "" ? 1 : 0
  zone_id = aws_route53_zone.main[0].zone_id
  name    = "www.${var.domain_name}"
  type    = "A"

  alias {
    name                   = aws_lb.main.dns_name
    zone_id                = aws_lb.main.zone_id
    evaluate_target_health = true
  }
}
