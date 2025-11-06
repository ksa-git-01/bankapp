def buildAndTag(String workspacePath, String serviceName, String registry, String tag) {
    echo "Building Docker image for ${serviceName}"
    dir("${workspacePath}/${serviceName}") {
        sh """
            docker build -t ${registry}/${serviceName}:${tag} .
            docker tag ${registry}/${serviceName}:${tag} ${registry}/${serviceName}:latest
        """
    }
}