def validate(String workspacePath, String serviceName) {
    echo "Validating ${serviceName}"
    dir("${workspacePath}/${serviceName}") {
        sh 'mvn validate'
    }
}

def build(String workspacePath, String serviceName) {
    echo "Building ${serviceName}"
    dir("${workspacePath}/${serviceName}") {
        sh 'mvn clean package -DskipTests'
    }
}