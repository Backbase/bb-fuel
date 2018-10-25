def call(Map params) {
    def bbFuelVersion = getBbFuelVersion(params.bbFuelVersion)

    cleanWs()
    downloadArtifact(bbFuelVersion)

    withEnv(["JAVA_HOME=${tool name: "${env.JDK_TOOL_NAME}"}", "PATH+MAVEN=${tool name: "${env.MAVEN_TOOL_NAME}"}/bin:${env.JAVA_HOME}/bin"]) {
        sh "java -Denvironment.name=${params.environmentName} " +
                "${params.additionalArguments} " +
                "-jar bb-fuel-${bbFuelVersion}-boot.jar"
    }
}

def downloadArtifact(version) {
    sh "curl -X GET -s https://github.com/backbase/bb-fuel/releases/download/bb-fuel-${version}/bb-fuel-${version}-boot.jar -O -J -L"
}

def getBbFuelVersion(version) {
    if (version == 'latest') {
        version = sh(returnStdout: true, script: '''curl -X GET -s https://github.com/backbase/bb-fuel/releases/latest | grep browser_download_url | cut -d '"' -f 4 | wget -qi -''').toString().trim()
    }

    return version
}
