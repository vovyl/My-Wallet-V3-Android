#!/usr/bin/env bash

./gradlew generateTestCoverage coveralls -Dpre-dex=false -Pkotlin.incremental=false --stacktrace
