#!/usr/bin/env bash

./gradlew wallet:test app:testEnvProdMinApi21DebugUnitTestCoverage app:coveralls -Dpre-dex=false -Pkotlin.incremental=false --stacktrace
