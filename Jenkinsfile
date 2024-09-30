pipeline {
    agent {
        label 'node-agent'
    }
    environment {
        def isPendingUpstreamDependenciesExists = false
        def triggeredViaPush = false
        SCANNER_HOME = tool 'sonar-scanner'
        JAVA_HOME = '/usr/lib/jvm/java-11-openjdk'
        PATH = "${JAVA_HOME}/bin:${env.PATH}" 
    }
    stages {
        stage('Initialize Variables') {
            steps {
                script {
                    // Define swaggerEndPoint as a global variable
                    swaggerEndPoint = {
                        def matcher = (env.CHANGE_URL =~ /^(?<host>https?:\/\/(?:www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b)(?<path>[-a-zA-Z0-9()@:%_\+.~#?&\/=]*)$/)
                        matcher.find()
                        return matcher.group('host') + '/api/v1/repos' + matcher.group('path')
                    }

                    echo "Swagger Endpoint: ${swaggerEndPoint.call()}"
                }
            }
        }

        stage('Tool Versioning') {
            steps {
                script {
                    sh 'java -version'
                    sh 'node --version'
                    sh 'npm --version'
                    sh 'jq --version'
                }
            }
        }

        stage('Check Environment Variables') {
            steps {
                script {
                    echo "CHANGE_ID: ${env.CHANGE_ID}"
                    echo "CHANGE_URL: ${env.CHANGE_URL}"
                    echo "CHANGE_AUTHOR: ${env.CHANGE_AUTHOR}"
                    echo "CHANGE_BRANCH: ${env.CHANGE_BRANCH}"
                    echo "JAVA_HOME: ${JAVA_HOME}"
        	    echo "PATH: ${PATH}"
                }
            }
        }
        
       stage('Check SonarQube Installation') {
	steps {
        script {
            echo "Initial JAVA_HOME: ${env.JAVA_HOME}"
            echo "Initial PATH: ${env.PATH}"
            
            withEnv(["JAVA_HOME=${env.JAVA_HOME}", "PATH=${env.JAVA_HOME}/bin:${env.PATH}"]) {
                sh """
                java -version
                ${SCANNER_HOME}/bin/sonar-scanner --version
                """
            }
        }
    }
}

        stage('Fetch Pending Upstream Dependencies') {
            steps {
                script {
                    if (env.CHANGE_ID) {
                        def url = swaggerEndPoint.call()
                        echo "Fetching from URL: ${url}"
                        withCredentials([usernamePassword(credentialsId: '4093a0bf-073a-4874-b929-be5b69273f4a', passwordVariable: 'password', usernameVariable: 'username')]) {
                            def response = sh(script: """curl -X GET "${url}" -H 'accept: application/json' -u \$username:\$password""", returnStdout: true).trim()
                            echo "API Response: ${response}"
                            isPendingUpstreamDependenciesExists = sh(script: "echo '${response}' | jq 'contains({\"labels\": [{ \"name\": \"pending upstream\"}]})'", returnStdout: true).trim().toBoolean()
                        }
                    } else {
                        echo '[Jenkinsfile] Triggered via a push request.'
                        echo '[Jenkinsfile] Skipping dependency checking.'
                        triggeredViaPush = true
                    }
                }
            }
        }

        stage('Execute Test Suites') {
            steps {
                script {
                    if (!isPendingUpstreamDependenciesExists) {
                        echo '[Jenkinsfile] Pending upstream dependencies do not exist.'
                        echo '[Jenkinsfile] Entering testing phase.'
                        try {
                            checkout scm
                            withCredentials([usernamePassword(credentialsId: 'builder2-deployer', passwordVariable: 'password', usernameVariable: 'username')]) {
                                sh """/opt/scripts/run-test.sh -u \$username -p \$password"""
                            }
                            currentBuild.result = 'SUCCESS'
                            message = 'Tests approved'
                        } catch (error) {
                            currentBuild.result = 'FAILURE'
                            message = 'Tests cannot be approved'
                        }
                    } else {
                        echo '[Jenkinsfile] Pending upstream dependencies exist.'
                        echo '[Jenkinsfile] Entering waiting phase.'
                        currentBuild.result = 'NOT_BUILT'
                        message = 'PR waiting due to pending upstream dependencies'
                    }
                }
            }
        }
        
       stage('Code Quality Check') {
    steps {
        script {
            def projectName = "device-mgt-core-${env.CHANGE_ID}"
            def projectKey = "device-mgt-core-${env.CHANGE_ID}"
            
            withSonarQubeEnv('sonar') {
                sh """
                    $SCANNER_HOME/bin/sonar-scanner \
                    -Dsonar.projectName=${projectName} \
                    -Dsonar.projectKey=${projectKey} \
                    -Dsonar.java.binaries=target
                """
            }
        }
    }
}

        stage('Report Job Status') {
            steps {
                script {
                    if (true) {
                        withCredentials([usernamePassword(credentialsId: '4093a0bf-073a-4874-b929-be5b69273f4a', passwordVariable: 'password', usernameVariable: 'username')]) {
                            def url = swaggerEndPoint.call() + '/reviews'
                            echo "[Jenkinsfile] Notifying pull request build status to ${url}"
                            def response = sh(script: """curl -X POST "${url}" -H 'accept: application/json' -H 'Content-Type: application/json' -u \$username:\$password -d '{ "body": "${message}" }'""", returnStdout: true).trim()
                            echo "API Response: ${response}"
                        }
                    }

                    def committerEmail = sh(
                        script: 'git --no-pager show -s --format=\'%ae\'',
                        returnStdout: true
                    ).trim()

                    if (currentBuild.result == 'FAILURE') {
                        emailext(
                            subject: "${currentBuild.result}: Job ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
                            body: 'Hi, Please find below.\n<pre>${BUILD_LOG_REGEX, regex="BUILD FAILURE", linesAfter=30, showTruncatedLines=false, escapeHtml=true}</pre>' + "Find more at : ${env.BUILD_URL}",
                            to: triggeredViaPush ? '$DEFAULT_RECIPIENTS' : committerEmail
                        )
                    }
                }
            }
        }
    }
}

