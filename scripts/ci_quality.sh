#!/usr/bin/env bash

./gradlew lintEnvProdMinApi16Debug -Dpre-dex=false -Pkotlin.incremental=false --no-daemon --stacktrace

# ktlint not enabled for now - waiting for some fixes in the next version