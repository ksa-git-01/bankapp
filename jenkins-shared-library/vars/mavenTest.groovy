def run(String workspacePath, String serviceName) {
    echo "Testing ${serviceName}"
    dir("${workspacePath}/${serviceName}") {
        sh 'mvn test'
    }
}