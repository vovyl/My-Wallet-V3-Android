#!/usr/bin/env bash

./gradlew wallet:jacocoTestReport core:testDebug app:testEnvProdMinApi21DebugUnitTestCoverage app:coveralls -Dpre-dex=false -Pkotlin.incremental=false --stacktrace
