@Library('test-library') _

pipeline {
    agent any

    parameters {
        string(name: 'BB_FUEL_VERSION', defaultValue: 'latest', description: 'Use latest to get the latest version or add an existing version, and only the version number. E.g: 2.6.9. \n You can see the existing versions at: https://github.com/Backbase/bb-fuel/releases')
        booleanParam(name: 'PRERELEASE', defaultValue: false, description: 'Only applicable if BB_FUEL_VERSION = latest')
        choice(name: 'SPRING_PROFILES_ACTIVE', choices: 'k8s\nk8s-beta\nk8s-3ang\naws\npcf', description: 'Select the profile to operate.\nK8s (default):\nKubernetes requires environment name\n\nk8s-beta: \nUsed for DBS with lean beta services \n\nk8s-3ang: \nUsed for DBS with triangular services only\n\nAWS: \n Requires environment name\n\nPCF: \nRequires space name (environment name will be ignored)')
        string(name: 'ENVIRONMENT_NAME', defaultValue: '00.dbs', description: 'K8S environment-creator example: 42.dbs\nK8S Beck example: friendly-feyman\n\nAutoconfig example: frosty-snow-99\nRead before running: https://github.com/Backbase/bb-fuel/blob/master/README.md')
        choice(name: 'INFRA_BASE_URI', choices: 'infra\neditorial\n', description: 'Select the base INFRA URI.(Autoconfig-only)\n\n\nAWS dbs-microservices: infra\n\nAWS dbs-cx6.1-editorial: editorial')
        choice(name: 'ENVIRONMENT_DOMAIN', choices: 'backbase.eu\nbackbase.test\nbackbase.test:8080\n', description: 'Select the base URI.\nK8S environment-creator: backbase.test\nK8S Beck: backbase.eu\n\nAWS: backbase.test:8080\n\n')
        string(name: 'PCF_SPACE', defaultValue: 'your-space', description: 'Required only for PCF. Space name, example: approvals\nRead before running: https://github.com/Backbase/bb-fuel/blob/master/README.md')
        booleanParam(name: 'US_PRODUCTIZED_DATASET', defaultValue: false, description: 'Use data seed specific for US market')
        booleanParam(name: 'UNIVERSAL_PRODUCTIZED_DATASET', defaultValue: false, description: 'Use data seed specific for UNIVERSAL market')
        booleanParam(name: 'RETAIL_DATASET', defaultValue: false, description: 'Use data seed specific for RETAIL banking')
        booleanParam(name: 'INGEST_ACCESS_CONTROL', defaultValue: true, description: 'Ingest access control setup')
        booleanParam(name: 'INGEST_CUSTOM_SERVICE_AGREEMENTS', defaultValue: false, description: 'Ingest custom service agreements')
        booleanParam(name: 'INGEST_BALANCE_HISTORY', defaultValue: false, description: 'Ingest balance history per arrangement (only applicable on the first run when INGEST_ACCESS_CONTROL = true)\n' +
                'Only enable when strictly necessary (long running job)')
        booleanParam(name: 'INGEST_TRANSACTIONS', defaultValue: false, description: 'Ingest transactions per arrangement (only applicable on the first run when INGEST_ACCESS_CONTROL = true)')
        booleanParam(name: 'INGEST_APPROVALS_FOR_PAYMENTS', defaultValue: false, description: 'Ingest approvals for payments')
        booleanParam(name: 'INGEST_APPROVALS_FOR_CONTACTS', defaultValue: false, description: 'Ingest approvals for contacts')
        booleanParam(name: 'INGEST_APPROVALS_FOR_NOTIFICATIONS', defaultValue: false, description: 'Ingest approvals for notifications')
        booleanParam(name: 'INGEST_APPROVALS_FOR_BATCHES', defaultValue: false, description: 'Ingest approvals for batches')
        booleanParam(name: 'INGEST_LIMITS', defaultValue: false, description: 'Ingest limits')
        booleanParam(name: 'INGEST_CONTACTS', defaultValue: false, description: 'Ingest contacts per user')
        booleanParam(name: 'INGEST_NOTIFICATIONS', defaultValue: false, description: 'Ingest notifications on global target group')
        booleanParam(name: 'INGEST_PAYMENTS', defaultValue: false, description: 'Ingest payments per user')
        booleanParam(name: 'INGEST_MESSAGES', defaultValue: false, description: 'Ingest messages per user')
        booleanParam(name: 'INGEST_ACTIONS', defaultValue: false, description: 'Ingest actions per user')
        booleanParam(name: 'INGEST_BILLPAY', defaultValue: false, description: 'Enrol users into Bill Pay')
        booleanParam(name: 'INGEST_ACCOUNT_STATEMENTS', defaultValue: false, description: 'Ingest Account Statements per user')
        booleanParam(name: 'USE_PERFORMANCE_TEST_DATA_SETUP', defaultValue: false, description: 'Use performance test data setup\n' +
                'Only enable when strictly necessary (long running job)')
        choice(name: 'PERFORMANCE_TEST_DATA', choices: 'retail\nbusiness', description: 'Retail or business performance test data setup')
        booleanParam(name: 'IDENTITY_FEATURE_TOGGLE', defaultValue: true, description: 'Use identity')
        string(name: 'IDENTITY_REALM', defaultValue: 'backbase', description: 'Identity realm')
        string(name: 'IDENTITY_CLIENT', defaultValue: 'bb-tooling-client', description: 'AWS: hybrid-flow\nK8S: bb-tooling-client')
        booleanParam(name: 'HEALTHCHECK_USE_ACTUATOR', defaultValue: true, description: 'Should be false if BOM version <= 2.16.4')
        string(name: 'ADDITIONAL_ARGUMENTS', defaultValue: '', description: 'Additional command line arguments')
        booleanParam(name: 'MULTI_TENANCY_ENVIRONMENT', defaultValue: false, description: 'Enable multi tenancy')
    }

    stages {
        stage('load data') {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER} - env: ${params.ENVIRONMENT_NAME}"
                    currentBuild.description = "env: ${params.ENVIRONMENT_NAME}\ntype: ${params.SPRING_PROFILES_ACTIVE}\nbb-fuel-version: ${params.BB_FUEL_VERSION}"

                    def customLegalEntitiesWithUsersJson = params.USE_PERFORMANCE_TEST_DATA_SETUP ?
                            " -Dlegal.entities.with.users.json=data/performance/performance-test-legal-entities-with-users-${params.PERFORMANCE_TEST_DATA}.json " :
                            "";

                    def usProductizedPropertiesPath = params.US_PRODUCTIZED_DATASET ?
                            " -Dcustom.properties.path=data/us-productized-dataset/us-productized-data.properties " :
                            "";

                    def universalProductizedPropertiesPath = params.UNIVERSAL_PRODUCTIZED_DATASET ?
                            " -Dcustom.properties.path=data/universal-productised-dataset/universal-productised-data.properties " :
                            "";

                    def retailDatasetPropertiesPath = params.RETAIL_DATASET ? " -Dcustom.properties.path=data/retail/data.properties "

                    loadData(
                            environmentName: params.ENVIRONMENT_NAME,
                            bbFuelVersion: params.BB_FUEL_VERSION,
                            prerelease: params.PRERELEASE,
                            additionalArguments: "-Dbb-fuel.platform.infra=http://${params.ENVIRONMENT_NAME}-${params.INFRA_BASE_URI}.${params.ENVIRONMENT_DOMAIN} " +
                                    "-Denvironment.domain=${params.ENVIRONMENT_DOMAIN} " +
                                    "-Dingest.access.control=${params.INGEST_ACCESS_CONTROL} " +
                                    "-Dingest.custom.service.agreements=${params.INGEST_CUSTOM_SERVICE_AGREEMENTS} " +
                                    "-Dingest.balance.history=${params.INGEST_BALANCE_HISTORY} " +
                                    "-Dingest.transactions=${params.INGEST_TRANSACTIONS} " +
                                    "-Dingest.approvals.for.payments=${params.INGEST_APPROVALS_FOR_PAYMENTS} " +
                                    "-Dingest.approvals.for.contacts=${params.INGEST_APPROVALS_FOR_CONTACTS} " +
                                    "-Dingest.approvals.for.notifications=${params.INGEST_APPROVALS_FOR_NOTIFICATIONS} " +
                                    "-Dingest.approvals.for.batches=${params.INGEST_APPROVALS_FOR_BATCHES} " +
                                    "-Dingest.limits=${params.INGEST_LIMITS} " +
                                    "-Dingest.contacts=${params.INGEST_CONTACTS} " +
                                    "-Dingest.notifications=${params.INGEST_NOTIFICATIONS} " +
                                    "-Dingest.payments=${params.INGEST_PAYMENTS} " +
                                    "-Dingest.messages=${params.INGEST_MESSAGES} " +
                                    "-Dingest.actions=${params.INGEST_ACTIONS} " +
                                    "-Dingest.billpay=${params.INGEST_BILLPAY} " +
                                    "-Dingest.accountStatements=${params.INGEST_ACCOUNT_STATEMENTS} " +
                                    "-Didentity.feature.toggle=${params.IDENTITY_FEATURE_TOGGLE} " +
                                    "-Didentity.realm=${params.IDENTITY_REALM} " +
                                    "-Didentity.client=${params.IDENTITY_CLIENT} " +
                                    "-Dmulti.tenancy.environment=${params.MULTI_TENANCY_ENVIRONMENT} " +
                                    "-Dhealthcheck.use.actuator=${params.HEALTHCHECK_USE_ACTUATOR} " +
                                    customLegalEntitiesWithUsersJson +
                                    usProductizedPropertiesPath +
                                    universalProductizedPropertiesPath +
                                    retailDatasetPropertiesPath +
                                    "${params.ADDITIONAL_ARGUMENTS}"
                    )
                }
            }
        }
    }
}
