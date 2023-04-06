{{- define "keycloak.url" -}}
{{- printf "%s%s" "https://" .Values.keycloak.host }}
{{- end -}}

{{- define "keycloak.customUrl" -}}
{{- printf "%s%s" "https://" .Values.keycloak.customHost }}
{{- end -}}

{{- define "keycloak.urlPrefix" -}}
{{- printf "%s%s%s" (include "keycloak.url" .) "/auth/realms/" .Release.Namespace -}}
{{- end -}}

{{- define "keycloak.customUrlPrefix" -}}
{{- printf "%s%s%s" (include "keycloak.customUrl" .) "/auth/realms/" .Release.Namespace -}}
{{- end -}}

{{- define "issuer.officer" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.officer -}}
{{- end -}}

{{- define "issuer.citizen" -}}
{{- printf "%s-%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.citizen -}}
{{- end -}}

{{- define "custom-issuer.officer" -}}
{{- printf "%s-%s" (include "keycloak.customUrlPrefix" .) .Values.keycloak.realms.officer -}}
{{- end -}}

{{- define "custom-issuer.citizen" -}}
{{- printf "%s-%s" (include "keycloak.customUrlPrefix" .) .Values.keycloak.realms.citizen -}}
{{- end -}}

{{- define "jwksUri.officer" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.officer .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "jwksUri.citizen" -}}
{{- printf "%s-%s%s" (include "keycloak.urlPrefix" .) .Values.keycloak.realms.citizen .Values.keycloak.certificatesEndpoint -}}
{{- end -}}

{{- define "userTaskManagement.istioResources" -}}
{{- if .Values.global.registry.userTaskManagement.istio.sidecar.resources.limits.cpu }}
sidecar.istio.io/proxyCPULimit: {{ .Values.global.registry.userTaskManagement.istio.sidecar.resources.limits.cpu | quote }}
{{- end }}
{{- if .Values.global.registry.userTaskManagement.istio.sidecar.resources.limits.memory }}
sidecar.istio.io/proxyMemoryLimit: {{ .Values.global.registry.userTaskManagement.istio.sidecar.resources.limits.memory | quote }}
{{- end }}
{{- if .Values.global.registry.userTaskManagement.istio.sidecar.resources.requests.cpu }}
sidecar.istio.io/proxyCPU: {{ .Values.global.registry.userTaskManagement.istio.sidecar.resources.requests.cpu | quote }}
{{- end }}
{{- if .Values.global.registry.userTaskManagement.istio.sidecar.resources.requests.memory }}
sidecar.istio.io/proxyMemory: {{ .Values.global.registry.userTaskManagement.istio.sidecar.resources.requests.memory | quote }}
{{- end }}
{{- end -}}
