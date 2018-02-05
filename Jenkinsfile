properties([
        parameters([
                string(name: 'ENVIRONMENT_NAME', defaultValue: 'env-name-00', description: 'Autoconfig environment name, example: frosty-snow-99\nRead before running: https://stash.backbase.com/users/kwo/repos/dataloader/browse/README.md'),
                booleanParam(name: 'INGEST_ENTITLEMENTS', defaultValue: true, description: 'Only works on clean environment'),
                booleanParam(name: 'INGEST_CUSTOM_SERVICE_AGREEMENTS', defaultValue: false, description: 'Ingest custom service agreements'),
                booleanParam(name: 'INGEST_TRANSACTIONS', defaultValue: false, description: 'Only works on when INGEST_ENTITLEMENTS = true'),
                booleanParam(name: 'INGEST_CONTACTS', defaultValue: false, description: 'Ingest contacts per user'),
                booleanParam(name: 'INGEST_NOTIFICATIONS', defaultValue: false, description: 'Ingest notifications on global target group'),
                booleanParam(name: 'INGEST_PAYMENTS', defaultValue: false, description: 'Ingest payments per user'),
                booleanParam(name: 'INGEST_CONVERSATIONS', defaultValue: false, description: 'Ingest conversations per user'),
                choice(name: 'INFRA_BASE_URI', choices: 'infra.backbase.dev:8080\neditorial.backbase.dev:8080', description: '')
        ])
])

node {
    stage('Load data') {
        cleanWs()
        git([credentialsId: 'e7e47e6e-8b7e-41f3-a854-b223f2985c96', url: 'ssh://git@stash.backbase.com:7999/~kwo/dataloader.git', branch: 'master'])

        withEnv(["JAVA_HOME=${tool name: 'jdk-8u152'}", "PATH+MAVEN=${tool name: 'maven-352'}/bin:${env.JAVA_HOME}/bin"]) {

            withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'f8ae341b-af40-4178-844d-b90dd3a977f4',
                              usernameVariable: 'AR_USERNAME', passwordVariable: 'AR_PASSWORD']]) {
                sh "mvn -s settings.xml -Dinternal.repo.username=${AR_USERNAME} -Dinternal.repo.password=${AR_PASSWORD} clean package"
            }

            sh "java -Denvironment.name=${params.ENVIRONMENT_NAME} " +
                    "-Dinfra.base.uri=http://${params.ENVIRONMENT_NAME}-${params.INFRA_BASE_URI} " +
                    "-Dingest.entitlements=${params.INGEST_ENTITLEMENTS} " +
                    "-Dingest.custom.service.agreements=${params.INGEST_CUSTOM_SERVICE_AGREEMENTS} " +
                    "-Dingest.transactions=${params.INGEST_TRANSACTIONS} " +
                    "-Dingest.contacts=${params.INGEST_CONTACTS} " +
                    "-Dingest.notifications=${params.INGEST_NOTIFICATIONS} " +
                    "-Dingest.payments=${params.INGEST_PAYMENTS} " +
                    "-Dingest.conversations=${params.INGEST_CONVERSATIONS} " +
                    "-jar target/dataloader-jar-with-dependencies.jar"
        }
    }
}