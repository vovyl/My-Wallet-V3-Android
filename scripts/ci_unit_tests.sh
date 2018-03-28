#!/usr/bin/env bash

./gradlew wallet:jacocoTestReport core:testCoverage app:testEnvProdMinApi21DebugUnitTestCoverage app:coveralls -Dpre-dex=false -Pkotlin.incremental=false --stacktrace
