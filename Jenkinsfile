properties([
        parameters([
                string(name: 'ENVIRONMENT_NAME', defaultValue: 'env-name-00', description: 'Autoconfig environment name, example: frosty-snow-99\nRead before running: https://stash.backbase.com/projects/CT/repos/dataloader/browse/README.md'),
                booleanParam(name: 'INGEST_ACCESS_CONTROL', defaultValue: true, description: 'Ingest access control setup'),
                booleanParam(name: 'INGEST_CUSTOM_SERVICE_AGREEMENTS', defaultValue: false, description: 'Ingest custom service agreements'),
                booleanParam(name: 'INGEST_BALANCE_HISTORY', defaultValue: false, description: 'Ingest balance history per arrangement (only applicable when INGEST_ACCESS_CONTROL = true)\n' +
                        'Only enable when strictly necessary (long running job)'),
                booleanParam(name: 'INGEST_TRANSACTIONS', defaultValue: false, description: 'Ingest transactions per arrangement (only applicable when INGEST_ACCESS_CONTROL = true)'),
                booleanParam(name: 'USE_PFM_CATEGORIES_FOR_TRANSACTIONS', defaultValue: false, description: 'Use PFM categories for transactions (only applicable when INGEST_TRANSACTIONS = true)'),
                booleanParam(name: 'INGEST_APPROVALS_FOR_PAYMENTS', defaultValue: false, description: 'Ingest approvals for payments'),
                booleanParam(name: 'INGEST_APPROVALS_FOR_CONTACTS', defaultValue: false, description: 'Ingest approvals for contacts'),
                booleanParam(name: 'INGEST_LIMITS', defaultValue: false, description: 'Ingest limits'),
                booleanParam(name: 'INGEST_CONTACTS', defaultValue: false, description: 'Ingest contacts per user'),
                booleanParam(name: 'INGEST_NOTIFICATIONS', defaultValue: false, description: 'Ingest notifications on global target group'),
                booleanParam(name: 'INGEST_PAYMENTS', defaultValue: false, description: 'Ingest payments per user'),
                booleanParam(name: 'INGEST_MESSAGES', defaultValue: false, description: 'Ingest messages per user'),
                booleanParam(name: 'INGEST_ACTIONS', defaultValue: false, description: 'Ingest actions per user'),
                booleanParam(name: 'MULTI_TENANCY_ENVIRONMENT', defaultValue: false, description: 'Enable for multi-tenancy environments'),
                string(name: 'TENANT_ID', defaultValue: 'tenant_a', description: ''),
                string(name: 'ROOT_ENTITLEMENTS_ADMIN', defaultValue: 'admin', description: ''),
                booleanParam(name: 'USE_PERFORMANCE_TEST_DATA_SETUP', defaultValue: false, description: 'Use performance test data setup\n' +
                        'Only enable when strictly necessary (long running job)'),
                choice(name: 'PERFORMANCE_TEST_DATA', choices: 'retail\nbusiness', description: 'Retail or business performance test data setup'),
                choice(name: 'INFRA_BASE_URI', choices: 'infra.backbase.test:8080\neditorial.backbase.test:8080', description: ''),
                string(name: 'DATALOADER_VERSION', defaultValue: 'LATEST', description: ''),
                string(name: 'HEALTHCHECK_TIMEOUT_IN_MINUTES', defaultValue: '1', description: '')
        ])
])

def downloadArtifact = { version ->
    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: "${env.ARTIFACTS_CREDENTIALS_ID}",
                      usernameVariable: 'AR_USERNAME', passwordVariable: 'AR_PASSWORD']]) {
        sh "curl -X GET -k -u ${AR_USERNAME}:${AR_PASSWORD} https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/dataloader/${version}/dataloader-${version}-boot.jar -O -J -L"
    }
}

def getDataloaderVersion() {
    def version = "${params.DATALOADER_VERSION}"

    if (version == 'LATEST') {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: "${env.ARTIFACTS_CREDENTIALS_ID}",
                          usernameVariable: 'AR_USERNAME', passwordVariable: 'AR_PASSWORD']]) {
            version = sh(returnStdout: true, script: '''curl -X GET -s -k -u ${AR_USERNAME}:${AR_PASSWORD} https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/dataloader/ | grep href | grep -v maven | cut -d'"' -f2 | cut -d'/' -f1 | sort --version-sort | tail -n 1''').toString().trim()
        }
    }

    return version
}

node {
    stage('Load data') {
        def dataloaderVersion = getDataloaderVersion()

        cleanWs()
        downloadArtifact(dataloaderVersion)

        withEnv(["JAVA_HOME=${tool name: "${env.JDK_TOOL_NAME}"}", "PATH+MAVEN=${tool name: "${env.MAVEN_TOOL_NAME}"}/bin:${env.JAVA_HOME}/bin"]) {

            def customLegalEntitiesWithUserJson = ""

            if ("${params.MULTI_TENANCY_ENVIRONMENT}".toBoolean()) {
                customLegalEntitiesWithUserJson = "-Dlegal.entities.with.users.json=data/multitenancy/${params.TENANT_ID}.json "
            } else if ("${params.USE_PERFORMANCE_TEST_DATA_SETUP}".toBoolean()) {
                customLegalEntitiesWithUserJson = "-Dlegal.entities.with.users.json=data/performance-test-legal-entities-with-users-${params.PERFORMANCE_TEST_DATA}.json "
            }

            sh "java -Denvironment.name=${params.ENVIRONMENT_NAME} " +
                    "-Dinfra.base.uri=http://${params.ENVIRONMENT_NAME}-${params.INFRA_BASE_URI} " +
                    "-Dhealthcheck.timeout.in.minutes=${params.HEALTHCHECK_TIMEOUT_IN_MINUTES} " +
                    "-Dmulti.tenancy.environment=${params.MULTI_TENANCY_ENVIRONMENT} " +
                    "-Dtenant.id=${params.TENANT_ID} " +
                    "-Droot.entitlements.admin=${params.ROOT_ENTITLEMENTS_ADMIN} " +
                    "-Dingest.access.control=${params.INGEST_ACCESS_CONTROL} " +
                    "-Dingest.custom.service.agreements=${params.INGEST_CUSTOM_SERVICE_AGREEMENTS} " +
                    "-Dingest.balance.history=${params.INGEST_BALANCE_HISTORY} " +
                    "-Dingest.transactions=${params.INGEST_TRANSACTIONS} " +
                    "-Duse.pfm.categories.for.transactions=${params.USE_PFM_CATEGORIES_FOR_TRANSACTIONS} " +
                    "-Dingest.approvals.for.payments=${params.INGEST_APPROVALS_FOR_PAYMENTS} " +
                    "-Dingest.approvals.for.contacts=${params.INGEST_APPROVALS_FOR_CONTACTS} " +
                    "-Dingest.limits=${params.INGEST_LIMITS} " +
                    "-Dingest.contacts=${params.INGEST_CONTACTS} " +
                    "-Dingest.notifications=${params.INGEST_NOTIFICATIONS} " +
                    "-Dingest.payments=${params.INGEST_PAYMENTS} " +
                    "-Dingest.messages=${params.INGEST_MESSAGES} " +
                    "-Dingest.actions=${params.INGEST_ACTIONS} " +
                    customLegalEntitiesWithUserJson +
                    "-jar dataloader-${dataloaderVersion}-boot.jar"
        }
    }
}
