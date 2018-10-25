def call(Map params) {
    def bbFuelTagName = getBbFuelTagName(params.bbFuelVersion)

    cleanWs()
    downloadArtifact(bbFuelTagName)

    withEnv(["JAVA_HOME=${tool name: "${env.JDK_TOOL_NAME}"}", "PATH+MAVEN=${tool name: "${env.MAVEN_TOOL_NAME}"}/bin:${env.JAVA_HOME}/bin"]) {
        sh "java -Denvironment.name=${params.environmentName} " +
                "${params.additionalArguments} " +
                "-jar ${bbFuelTagName}-boot.jar"
    }
}

def downloadArtifact(tagName) {
    sh  "curl -X GET -s https://api.github.com/repos/backbase/bb-fuel/releases/tags/${tagName} | grep browser_download_url | cut -d '\"' -f 4 | wget -qi -"
}

def getBbFuelTagName(version) {
    def tagName = "bb-fuel-${version}"

    if (version == 'latest') {
        tagName = sh(returnStdout: true, script: '''curl -X GET -s https://api.github.com/repos/backbase/bb-fuel/releases/latest | grep tag_name | cut -d '"' -f 4''').toString().trim()
    }

    return tagName
}
