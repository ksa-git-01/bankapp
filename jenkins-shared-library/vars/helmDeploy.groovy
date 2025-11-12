def deploy(String serviceName, String workspacePath, String namespace, String imageTag, String registry) {
    echo "Deploying ${serviceName} to ${namespace}"
    sh """
        helm upgrade --install ${serviceName} ${workspacePath}/helm/${serviceName} \
            --namespace ${namespace} \
            --create-namespace \
            --set image.tag=${imageTag} \
            --set image.repository=${registry}/${serviceName} \
            --wait
    """
}