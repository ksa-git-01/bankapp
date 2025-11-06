def call(Map config = [:]) {
    pipeline {
        agent any

        parameters {
            choice(name: 'ENVIRONMENT', choices: ['test', 'prod'], description: 'Target environment')
            booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Skip unit tests')
        }

        environment {
            SERVICE_NAME = config.serviceName
            WORKSPACE_PATH = config.workspacePath ?: '/workspace/bankapp'
            DOCKER_REGISTRY = config.dockerRegistry ?: 'localhost:5000'
            IMAGE_TAG = "${env.BUILD_NUMBER}"
            MAVEN_OPTS = '-Dmaven.repo.local=.m2/repository'
        }

        stages {
            stage('Validate') {
                steps {
                    script {
                        mavenBuild.validate(env.WORKSPACE_PATH, env.SERVICE_NAME)
                    }
                }
            }

            stage('Build') {
                steps {
                    script {
                        mavenBuild.build(env.WORKSPACE_PATH, env.SERVICE_NAME)
                    }
                }
            }

            stage('Test') {
                when {
                    expression { !params.SKIP_TESTS }
                }
                steps {
                    script {
                        mavenTest.run(env.WORKSPACE_PATH, env.SERVICE_NAME)
                    }
                }
                post {
                    always {
                        junit "${env.WORKSPACE_PATH}/${env.SERVICE_NAME}/target/surefire-reports/*.xml"
                    }
                }
            }

            stage('Build Docker Image') {
                steps {
                    script {
                        dockerBuild.buildAndTag(
                                env.WORKSPACE_PATH,
                                env.SERVICE_NAME,
                                env.DOCKER_REGISTRY,
                                env.IMAGE_TAG
                        )
                    }
                }
            }

            stage('Push to Registry') {
                steps {
                    script {
                        dockerPush.push(
                                env.DOCKER_REGISTRY,
                                env.SERVICE_NAME,
                                env.IMAGE_TAG
                        )
                    }
                }
            }

            stage('Deploy to Test') {
                when {
                    expression { params.ENVIRONMENT == 'test' }
                }
                steps {
                    script {
                        helmDeploy.deploy(
                                env.SERVICE_NAME,
                                env.WORKSPACE_PATH,
                                'test',
                                env.IMAGE_TAG,
                                env.DOCKER_REGISTRY
                        )
                    }
                }
            }

            stage('Deploy to Prod') {
                when {
                    expression { params.ENVIRONMENT == 'prod' }
                }
                steps {
                    input message: 'Deploy to Production?', ok: 'Deploy'
                    script {
                        helmDeploy.deploy(
                                env.SERVICE_NAME,
                                env.WORKSPACE_PATH,
                                'prod',
                                env.IMAGE_TAG,
                                env.DOCKER_REGISTRY
                        )
                    }
                }
            }
        }

        post {
            success {
                echo "Pipeline completed successfully for ${env.SERVICE_NAME}"
            }
            failure {
                echo "Pipeline failed for ${env.SERVICE_NAME}"
            }
            cleanup {
                cleanWs()
            }
        }
    }
}