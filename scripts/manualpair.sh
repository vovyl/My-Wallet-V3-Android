#!/usr/bin/env bash

# Usage, go to manual paring and run this with walletid/password as argument

echo $1

split=(${1//// })

wallet_id=${split[0]}
password=${split[1]}

adb shell input text ${wallet_id} && adb shell input keyevent KEYCODE_TAB && adb shell input text ${password} && adb shell input keyevent KEYCODE_TAB && adb shell input keyevent KEYCODE_TAB && adb shell input keyevent KEYCODE_ENTER
