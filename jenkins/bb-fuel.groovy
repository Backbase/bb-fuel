@Library('test-library') _

pipeline {
    agent any

    parameters {
        choice(name: 'SPRING_PROFILES_ACTIVE', choices: 'aws\nk8s\npcf', description: 'Select the profile to operate.\nAWS (default):\nRequires environment name\n\nK8s: \nKubernetes requires environment name\n\nPCF: \nRequires space name (environment name will be ignored)')
        string(name: 'ENVIRONMENT_NAME', defaultValue: 'env-name-00', description: 'Autoconfig environment name, example: frosty-snow-99. Or Kubernetes environment name , e.g. 42-master\nRead before running: https://github.com/Backbase/bb-fuel/blob/master/README.md')
        string(name: 'PCF_SPACE', defaultValue: 'your-space', description: 'Required only for PCF. Space name, example: approvals\nRead before running: https://github.com/Backbase/bb-fuel/blob/master/README.md')
        booleanParam(name: 'INGEST_ACCESS_CONTROL', defaultValue: true, description: 'Ingest access control setup')
        booleanParam(name: 'INGEST_CUSTOM_SERVICE_AGREEMENTS', defaultValue: false, description: 'Ingest custom service agreements')
        booleanParam(name: 'INGEST_BALANCE_HISTORY', defaultValue: false, description: 'Ingest balance history per arrangement (only applicable when INGEST_ACCESS_CONTROL = true)\n' +
                'Only enable when strictly necessary (long running job)')
        booleanParam(name: 'INGEST_TRANSACTIONS', defaultValue: false, description: 'Ingest transactions per arrangement (only applicable when INGEST_ACCESS_CONTROL = true)')
        booleanParam(name: 'USE_PFM_CATEGORIES_FOR_TRANSACTIONS', defaultValue: false, description: 'Use PFM categories for transactions (only applicable when INGEST_TRANSACTIONS = true)')
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
        booleanParam(name: 'USE_PERFORMANCE_TEST_DATA_SETUP', defaultValue: false, description: 'Use performance test data setup\n' +
                'Only enable when strictly necessary (long running job)')
        choice(name: 'PERFORMANCE_TEST_DATA', choices: 'retail\nbusiness', description: 'Retail or business performance test data setup')
        choice(name: 'INFRA_BASE_URI', choices: 'infra.backbase.test:8080\neditorial.backbase.test:8080\nbackbase.test', description: '')
        booleanParam(name: 'IDENTITY_FEATURE_TOGGLE', defaultValue: false, description: 'Use identity')
        string(name: 'IDENTITY_REALM', defaultValue: 'backbase', description: 'Identity realm')
        string(name: 'IDENTITY_CLIENT', defaultValue: 'hybrid-flow', description: 'Identity client')
        string(name: 'BB_FUEL_VERSION', defaultValue: 'latest', description: '')
        booleanParam(name: 'PRERELEASE', defaultValue: false, description: 'Only applicable if BB_FUEL_VERSION = latest')
        string(name: 'ADDITIONAL_ARGUMENTS', defaultValue: '', description: 'Additional command line arguments')
        booleanParam(name: 'MULTI_TENANCY_ENVIRONMENT', defaultValue: true, description: 'Enable multi tenancy')
    }

    stages {
        stage('load data') {
            steps {
                script {
                    currentBuild.displayName = "#${BUILD_NUMBER} - env: ${params.ENVIRONMENT_NAME}"
                    currentBuild.description = "env: ${params.ENVIRONMENT_NAME}\ntype: ${params.SPRING_PROFILES_ACTIVE}\nbb-fuel-version: ${params.BB_FUEL_VERSION}"
                    def customLegalEntitiesWithUsersJson = ""

                    if ("${params.USE_PERFORMANCE_TEST_DATA_SETUP}".toBoolean()) {
                        customLegalEntitiesWithUsersJson = "-Dlegal.entities.with.users.json=data/performance/performance-test-legal-entities-with-users-${params.PERFORMANCE_TEST_DATA}.json "
                    }

                    loadData(
                            environmentName: params.ENVIRONMENT_NAME,
                            bbFuelVersion: params.BB_FUEL_VERSION,
                            prerelease: params.PRERELEASE,
                            additionalArguments: "-Dbb-fuel.platform.infra=http://${params.ENVIRONMENT_NAME}-${params.INFRA_BASE_URI} " +
                                    "-Dingest.access.control=${params.INGEST_ACCESS_CONTROL} " +
                                    "-Dingest.custom.service.agreements=${params.INGEST_CUSTOM_SERVICE_AGREEMENTS} " +
                                    "-Dingest.balance.history=${params.INGEST_BALANCE_HISTORY} " +
                                    "-Dingest.transactions=${params.INGEST_TRANSACTIONS} " +
                                    "-Duse.pfm.categories.for.transactions=${params.USE_PFM_CATEGORIES_FOR_TRANSACTIONS} " +
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
                                    "-Didentity.feature.toggle=${params.IDENTITY_FEATURE_TOGGLE} " +
                                    "-Didentity.realm=${params.IDENTITY_REALM} " +
                                    "-Didentity.client=${params.IDENTITY_CLIENT} " +
                                    "-Dmulti.tenancy.environment=${params.ENVIRONMENT_MULTI_TENANCY} " +
                                    customLegalEntitiesWithUsersJson +
                                    "${params.ADDITIONAL_ARGUMENTS}"
                    )
                }
            }
        }
    }
}
