# base/pod-disruption-budgets

One PDB per workload. minAvailable is 0 in base (a no-op) -- overlays/staging, /uat, /production raise it once replicas > 1.
