import { apiRequest } from './httpClient'

export interface HealthStatus {
  status: string
}

export function checkGatewayHealth(): Promise<HealthStatus> {
  return apiRequest<HealthStatus>('/actuator/health')
}
