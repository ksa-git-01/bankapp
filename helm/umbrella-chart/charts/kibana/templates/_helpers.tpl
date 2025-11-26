{{- define "kibana.name" -}}
kibana
{{- end -}}

{{- define "kibana.fullname" -}}
{{ include "kibana.name" . }}
{{- end -}}
