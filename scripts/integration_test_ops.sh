logAndRun MVN_CMD clean install -DperformRelease -P'!gen-sign'

(
    cd auto-pipeline-examples
    logAndRun ./gradlew clean test
)
