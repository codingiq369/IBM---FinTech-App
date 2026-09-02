# Not consumed today -- every inter-service call in the implemented vertical
# slice is synchronous HTTP (see "Simplifications made on purpose" in
# docs/architecture/vertical-slice.md and
# docs/architecture/architecture-decisions/ADR-0003-event-bus.md). Built so
# it's ready for step 3 of that doc's "What to build next" -- publishing a
# TransferCompleted event -- but no environment's root module calls this yet.

resource "aws_msk_cluster" "this" {
  cluster_name           = "fintech-${var.environment}"
  kafka_version           = var.kafka_version
  number_of_broker_nodes = var.number_of_broker_nodes

  broker_node_group_info {
    instance_type   = var.broker_instance_type
    client_subnets  = var.subnet_ids
    security_groups = var.vpc_security_group_ids

    storage_info {
      ebs_storage_info {
        volume_size = 100
      }
    }
  }

  encryption_info {
    encryption_in_transit {
      client_broker = "TLS"
      in_cluster    = true
    }
  }
}
