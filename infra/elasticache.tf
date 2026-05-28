# ─── ElastiCache Redis ────────────────────────────────────────────────────────
# Enabled only when var.enable_elasticache = true

resource "aws_elasticache_subnet_group" "main" {
  count      = var.enable_elasticache ? 1 : 0
  name       = "${var.app_name}-redis-subnet"
  subnet_ids = aws_subnet.private[*].id
  tags       = { Name = "${var.app_name}-redis-subnet-group" }
}

resource "aws_security_group" "redis" {
  count       = var.enable_elasticache ? 1 : 0
  name        = "${var.app_name}-redis-sg"
  description = "Allow Redis from API ECS only"
  vpc_id      = aws_vpc.main.id

  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.api_ecs.id]
  }

  tags = { Name = "${var.app_name}-redis-sg" }
}

resource "aws_elasticache_cluster" "main" {
  count                = var.enable_elasticache ? 1 : 0
  cluster_id           = "${var.app_name}-redis"
  engine               = "redis"
  node_type            = var.redis_node_type
  num_cache_nodes      = 1
  parameter_group_name = "default.redis7"
  engine_version       = "7.1"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.main[0].name
  security_group_ids   = [aws_security_group.redis[0].id]

  tags = { Name = "${var.app_name}-redis" }
}

locals {
  redis_host = var.enable_elasticache ? aws_elasticache_cluster.main[0].cache_nodes[0].address : "localhost"
  redis_port = var.enable_elasticache ? tostring(aws_elasticache_cluster.main[0].cache_nodes[0].port) : "6379"
}
