{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "topical.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "topical.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "topical.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "topical.labels" -}}
app.kubernetes.io/name: {{ include "topical.name" . }}
helm.sh/chart: {{ include "topical.chart" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Create the name of the service account to use
*/}}
{{- define "topical.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
    {{ default (include "topical.fullname" .) .Values.serviceAccount.name }}
{{- else -}}
    {{ default "default" .Values.serviceAccount.name }}
{{- end -}}
{{- end -}}

{{/*
Annotations updated with "traefik.ingress.kubernetes.io/router.middlewares"
if needed.
*/}}
{{- define "topical.annotations.api" -}}
{{- $annotations := .Values.api.ingress.annotations }}
{{- if .Values.ipWhiteList }}
{{- $additionalMiddleware := printf "%s-%s-ipwhitelist@kubernetescrd" .Release.Namespace (include "topical.fullname" .)  }}
{{- $userSuppliedMiddlewares := index $annotations "traefik.ingress.kubernetes.io/router.middlewares" | default "" }}
{{- $_ := set $annotations "traefik.ingress.kubernetes.io/router.middlewares" (trimAll "," (printf "%s,%s" $userSuppliedMiddlewares $additionalMiddleware)) -}}
{{ toYaml $annotations }}
{{- end }}
{{- end }}

{{- define "topical.annotations.ui" -}}
{{- $annotations := .Values.ui.ingress.annotations }}
{{- if .Values.ipWhiteList }}
{{- $additionalMiddleware := printf "%s-%s-ipwhitelist@kubernetescrd" .Release.Namespace (include "topical.fullname" .)  }}
{{- $userSuppliedMiddlewares := index $annotations "traefik.ingress.kubernetes.io/router.middlewares" | default "" }}
{{- $_ := set $annotations "traefik.ingress.kubernetes.io/router.middlewares" (trimAll "," (printf "%s,%s" $userSuppliedMiddlewares $additionalMiddleware)) -}}
{{ toYaml $annotations }}
{{- end }}
{{- end }}
