# user-task-management

![Version: 1.9.7](https://img.shields.io/badge/Version-1.9.7-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.9.7](https://img.shields.io/badge/AppVersion-1.9.7-informational?style=flat-square)

Chart for deploying user-task-management

**Homepage:** <EPAM>

## Maintainers

| Name | Email | Url |
| ---- | ------ | --- |
| OSD-DDM |  |  |

## Source Code

* <https://github.com>

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| appConfigMountPath | string | `"/app/config"` |  |
| deployProfile | string | `"prod"` |  |
| global.registry.userTaskManagement.container.envVars | object | `{}` |  |
| global.registry.userTaskManagement.container.resources.limits | object | `{}` |  |
| global.registry.userTaskManagement.container.resources.requests | object | `{}` |  |
| global.registry.userTaskManagement.hpa.enabled | bool | `false` |  |
| global.registry.userTaskManagement.hpa.maxReplicas | int | `3` |  |
| global.registry.userTaskManagement.hpa.minReplicas | int | `1` |  |
| global.registry.userTaskManagement.istio.sidecar.enabled | bool | `true` |  |
| global.registry.userTaskManagement.istio.sidecar.resources.limits | object | `{}` |  |
| global.registry.userTaskManagement.istio.sidecar.resources.requests | object | `{}` |  |
| global.registry.userTaskManagement.replicas | int | `1` |  |
| image.name | string | `"user-task-management"` |  |
| image.version | string | `"latest"` |  |
| ingress.platform | string | `"openshift"` |  |
| keycloak.certificatesEndpoint | string | `"/protocol/openid-connect/certs"` |  |
| keycloak.realms.admin | string | `"admin"` |  |
| keycloak.realms.citizen | string | `"citizen-portal"` |  |
| keycloak.realms.officer | string | `"officer-portal"` |  |
| livenessPath | string | `"/user-task-management/actuator/health/liveness"` |  |
| monitoring.namespace | string | `"openshift-monitoring"` |  |
| platform.security.csrf.enabled | bool | `false` |  |
| podAnnotations | object | `{}` |  |
| port | int | `8080` |  |
| prometheus.endpoints[0].port | string | `"{{ .Values.service.port }}"` |  |
| prometheus.endpoints[0].scrapePath | string | `"/user-task-management/actuator/prometheus"` |  |
| readinessPath | string | `"/user-task-management/actuator/health/readiness"` |  |
| redis.secretName | string | `"redis-auth"` |  |
| redisSecretsMountPath | string | `"/app/secrets/redis"` |  |
| service.port | int | `8080` |  |
| service.type | string | `"ClusterIP"` |  |
| storage.backend.redis.password | string | `"${REDIS_PASSWORD}"` |  |
| storage.backend.redis.sentinel.master | string | `"mymaster"` |  |
| storage.backend.redis.sentinel.nodes | string | `"${redis.endpoint}"` |  |
| storage.form-data.type | string | `"redis"` |  |