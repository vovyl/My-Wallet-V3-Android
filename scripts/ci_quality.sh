#!/usr/bin/env bash

./gradlew lintEnvProdMinApi16Debug ktlint -Dpre-dex=false -Pkotlin.incremental=false --no-daemon --stacktrace