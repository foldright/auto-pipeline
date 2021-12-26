logAndRun MVN_CMD clean install -DperformRelease -P'!gen-sign'

(
    cd auto-pipeline-example
    logAndRun ./gradlew clean test
)
