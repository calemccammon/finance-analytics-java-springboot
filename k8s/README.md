# Kubernetes Deployment Guide

## Prerequisites

- [minikube](https://minikube.sigs.k8s.io/docs/start/) installed and running
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed
- A GCP service account key JSON file with BigQuery read access

---

## Running on minikube

### 1. Start minikube

```bash
minikube start
```

### 2. Point Docker to minikube's daemon (builds the image directly into the cluster)

```bash
# macOS/Linux
eval $(minikube docker-env)

# Windows (PowerShell)
& minikube -p minikube docker-env --shell powershell | Invoke-Expression
```

### 3. Build the image

```bash
docker build -t ghcr.io/calemccammon/finance-analytics-java-springboot:latest .
```

### 4. Create the namespace

```bash
kubectl apply -f k8s/namespace.yaml
```

### 5. Create secrets

```bash
# GCP service account key
kubectl create secret generic gcp-credentials \
  --from-file=credentials.json=/path/to/your/service-account-key.json \
  --namespace=finance-analytics

# BigQuery project ID
kubectl create secret generic finance-analytics-secrets \
  --from-literal=BQ_PROJECT=your-gcp-project-id \
  --namespace=finance-analytics
```

### 6. Apply all manifests

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
```

### 7. Wait for the pod to be ready

```bash
kubectl rollout status deployment/finance-analytics -n finance-analytics
```

### 8. Access the API

```bash
kubectl port-forward service/finance-analytics 8080:8080 -n finance-analytics
```

Then open: http://localhost:8080/swagger-ui.html

---

## Useful commands

```bash
# View pods
kubectl get pods -n finance-analytics

# View logs
kubectl logs -l app=finance-analytics -n finance-analytics

# Describe a pod (useful for debugging startup failures)
kubectl describe pod -l app=finance-analytics -n finance-analytics

# Delete everything
kubectl delete namespace finance-analytics
```

---

## Deploying to GKE (production)

On GKE, replace the `gcp-credentials` secret with **Workload Identity** — no JSON key file required.

1. Create a GCP service account and grant it `roles/bigquery.dataViewer`
2. Bind it to the Kubernetes service account:
   ```bash
   gcloud iam service-accounts add-iam-policy-binding SA_NAME@PROJECT.iam.gserviceaccount.com \
     --role=roles/iam.workloadIdentityUser \
     --member="serviceAccount:PROJECT.svc.id.goog[finance-analytics/default]"
   ```
3. Annotate the Kubernetes service account:
   ```bash
   kubectl annotate serviceaccount default \
     iam.gke.io/gcp-service-account=SA_NAME@PROJECT.iam.gserviceaccount.com \
     -n finance-analytics
   ```
4. Remove the `gcp-credentials` volume and `GOOGLE_APPLICATION_CREDENTIALS` env var from `deployment.yaml` — the GKE metadata server handles auth automatically.
