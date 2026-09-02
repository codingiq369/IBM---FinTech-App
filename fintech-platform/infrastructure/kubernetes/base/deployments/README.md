# base/deployments

One Deployment per real workload (customer-service, accounts-service, ledger-service, transfers-service, api-gateway, web-banking). Environment-agnostic; overlays/<env>/patches sets replicas, resources, and image tag.
