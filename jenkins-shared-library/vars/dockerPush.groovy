def push(String registry, String serviceName, String tag) {
    echo "Pushing ${serviceName} to registry"
    sh """
        docker push ${registry}/${serviceName}:${tag}
        docker push ${registry}/${serviceName}:latest
    """
}