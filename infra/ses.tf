# SES domain verification — only when domain_name is set.
# Requires Route53 hosted zone (spec-001). After apply, SES sandbox still
# restricts sending to verified addresses; request production access via AWS console.

# ─── SES Domain Identity + DKIM ──────────────────────────────────────────────

resource "aws_ses_domain_identity" "main" {
  count  = var.domain_name != "" ? 1 : 0
  domain = var.domain_name
}

resource "aws_ses_domain_dkim" "main" {
  count  = var.domain_name != "" ? 1 : 0
  domain = aws_ses_domain_identity.main[0].domain
}

resource "aws_route53_record" "ses_dkim" {
  count   = var.domain_name != "" ? 3 : 0
  zone_id = aws_route53_zone.main[0].zone_id
  name    = "${aws_ses_domain_dkim.main[0].dkim_tokens[count.index]}._domainkey"
  type    = "CNAME"
  ttl     = 600
  records = ["${aws_ses_domain_dkim.main[0].dkim_tokens[count.index]}.dkim.amazonses.com"]
}

# ─── MAIL FROM domain + SPF / MX ─────────────────────────────────────────────

resource "aws_ses_domain_mail_from" "main" {
  count            = var.domain_name != "" ? 1 : 0
  domain           = aws_ses_domain_identity.main[0].domain
  mail_from_domain = "mail.${var.domain_name}"
}

resource "aws_route53_record" "ses_mail_from_mx" {
  count   = var.domain_name != "" ? 1 : 0
  zone_id = aws_route53_zone.main[0].zone_id
  name    = "mail.${var.domain_name}"
  type    = "MX"
  ttl     = 600
  records = ["10 feedback-smtp.${var.aws_region}.amazonses.com"]
}

resource "aws_route53_record" "ses_mail_from_spf" {
  count   = var.domain_name != "" ? 1 : 0
  zone_id = aws_route53_zone.main[0].zone_id
  name    = "mail.${var.domain_name}"
  type    = "TXT"
  ttl     = 600
  records = ["v=spf1 include:amazonses.com -all"]
}

# ─── IAM: allow ECS task to send email ───────────────────────────────────────

resource "aws_iam_role_policy" "ecs_task_ses" {
  name = "${var.app_name}-ecs-task-ses"
  role = aws_iam_role.ecs_task.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["ses:SendEmail", "ses:SendRawEmail"]
      Resource = "*"
    }]
  })
}

# ─── SSM: from-email address ──────────────────────────────────────────────────

resource "aws_ssm_parameter" "ses_from_email" {
  name  = "${local.ssm_prefix}/ses-from-email"
  type  = "String"
  value = var.domain_name != "" ? "noreply@${var.domain_name}" : "noreply@example.com"
  tags  = { Name = "ses-from-email" }
}
