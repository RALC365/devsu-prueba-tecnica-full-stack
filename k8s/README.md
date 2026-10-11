# Kubernetes

Primero hay que construir las imágenes y crear el ConfigMap con el script de la base:

```bash
docker compose build
kubectl create namespace banco
kubectl create configmap banco-db-init --from-file=BaseDatos.sql -n banco
kubectl apply -k k8s/
kubectl port-forward -n banco svc/frontend 4200:8080
```

En kind o minikube hay que cargar antes las imágenes locales, por ejemplo `kind load docker-image banco-backend:1.0.0 banco-frontend:1.0.0`.
