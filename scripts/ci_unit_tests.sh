#!/usr/bin/env bash

./gradlew wallet:jacocoTestReport core:testCoverage coreui:testCoverage buysell:testCoverage buysellui:testCoverage app:testEnvProdMinApi21DebugUnitTestCoverage app:coveralls -Dpre-dex=false -Pkotlin.incremental=false --stacktrace
