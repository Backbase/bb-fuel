def call(Map params) {
    def dataLoaderVersion = getDataLoaderVersion(params.dataLoaderVersion)

    cleanWs()
    downloadArtifact(dataLoaderVersion)

    withEnv(["JAVA_HOME=${tool name: "${env.JDK_TOOL_NAME}"}", "PATH+MAVEN=${tool name: "${env.MAVEN_TOOL_NAME}"}/bin:${env.JAVA_HOME}/bin"]) {
        sh "java -Denvironment.name=${params.environmentName} " +
                "${params.additionalArguments} " +
                "-jar bbfuel-${dataLoaderVersion}-boot.jar"
    }
}

def downloadArtifact(version) {
    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: "${env.ARTIFACTS_CREDENTIALS_ID}",
                      usernameVariable: 'AR_USERNAME', passwordVariable: 'AR_PASSWORD']]) {
        sh "curl -X GET -k -u ${AR_USERNAME}:${AR_PASSWORD} https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bbfuel/${version}/bbfuel-${version}-boot.jar -O -J -L"
    }
}

def getDataLoaderVersion(version) {
    if (version == 'latest') {
        withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: "${env.ARTIFACTS_CREDENTIALS_ID}",
                          usernameVariable: 'AR_USERNAME', passwordVariable: 'AR_PASSWORD']]) {
            version = sh(returnStdout: true, script: '''curl -X GET -s -k -u ${AR_USERNAME}:${AR_PASSWORD} https://artifacts.backbase.com/backbase-development-builds/com/backbase/ct/bbfuel/ | grep href | grep -v maven | cut -d'"' -f2 | cut -d'/' -f1 | sort --version-sort | tail -n 1''').toString().trim()
        }
    }

    return version
}
