import groovy.json.JsonSlurperClassic

def call(Map params) {
    cleanWs()
    downloadArtifact(params.bbFuelVersion)

    withEnv(["JAVA_HOME=${tool name: "${env.JDK_TOOL_NAME}"}", "PATH+MAVEN=${tool name: "${env.MAVEN_TOOL_NAME}"}/bin:${env.JAVA_HOME}/bin"]) {
        sh "java -Denvironment.name=${params.environmentName} " +
                "${params.additionalArguments} " +
                "-jar ${bbFuelTagName}-boot.jar"
    }
}

def downloadArtifact(version) {
    def bbFuelTagName
    if ("${params.prerelease}".toBoolean() && params.bbFuelVersion == 'latest') {
        bbFuelTagName = getLatestPreReleaseTagName()
    } else {
        bbFuelTagName = getBbFuelTagName(version)
    }

    sh  "curl -X GET -s https://api.github.com/repos/backbase/bb-fuel/releases/tags/${bbFuelTagName} | grep browser_download_url | cut -d '\"' -f 4 | wget -qi -"
}

def getBbFuelTagName(version) {
    def tagName = "bb-fuel-${version}"

    if (version == 'latest') {
        tagName = sh(returnStdout: true, script: '''curl -X GET -s https://api.github.com/repos/backbase/bb-fuel/releases/latest | grep tag_name | cut -d '"' -f 4''').toString().trim()
    }

    return tagName
}

def getLatestPreReleaseTagName() {
    def response = httpRequest url: 'https://api.github.com/repos/backbase/bb-fuel/releases', httpMode: 'GET', contentType: 'APPLICATION_JSON', ignoreSslErrors: true
    def jsonSlurper = new JsonSlurperClassic()
    def jsonArrayResponse = jsonSlurper.parseText("${response.content}")

    for (def jsonObject : jsonArrayResponse) {
        if (jsonObject.prerelease == true) {
            return jsonObject.tag_name
        }
        echo "No pre-releases found"
    }
}
