# Open open radio

An open source clone of open radio. 
Browse & play your favorite webradios on your android devices.

## Features

- web radio sources:
  - https://github.com/jcorporation/webradiodb
- web radio browser
  - using media3 MediaLibrarySession
- web radio player
  - using media3 MediaSession
- Android Auto support


## Install

The app is distributed as open source builds, outside Google Play.

### Android Auto

Android Auto only lists media apps installed from Google Play by default. To use the app in your car:

1. Open the Android Auto settings on your phone.
2. Tap the version number repeatedly until developer mode is enabled.
3. In the developer settings (top-right menu), enable "Unknown sources".

### Background playback

If the app's battery usage is set to "Restricted", Android stops playback shortly after the screen turns off.
Allow background usage in the app settings (Battery: Unrestricted or Optimized).

## Releasing

GitHub Actions builds and publishes an APK when a stable version tag such as `v1.2.0` is pushed.
The tag must match `versionName` in the APK. Increment `versionCode` and add
`fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` before creating the tag.

Configure these repository Actions secrets once:

- `ANDROID_KEYSTORE_BASE64`: the developer keystore encoded with `base64 -w 0`.
- `ANDROID_KEYSTORE_PASSWORD`: the keystore password, without the blank lines from a password file.

If the private key uses a different password, also set `ANDROID_KEY_PASSWORD`. If the keystore
contains several keys, set the repository variable `ANDROID_KEY_ALIAS` to the developer key's alias.
The workflow checks the signing certificate against the existing developer certificate.

Install Java 21 for local builds. `gradle/gradle-daemon-jvm.properties` also requires the Gradle
daemon to use Java 21, which keeps R8 output consistent with F-Droid. The release workflow compares
two clean unsigned builds, signs using build-tools 34 with APK signature schemes v2/v3, and verifies
signature copying before publishing. See [F-Droid's reproducibility guidance](https://f-droid.org/docs/Reproducible_Builds/).

Each release contains `openopenradio-<versionName>.apk` and its `.apk.sha256` checksum. This keeps
the existing F-Droid binary URL compatible. F-Droid metadata lives in fdroiddata and uses tag-based
updates; this repository's workflow does not modify it.

To retry a failed release, run **Release APK** from the Actions tab and enter the existing tag.
Retries resume an unfinished draft or accept an identical published APK; a different published APK
is never overwritten. Merge the workflow branch before creating the next version tag.

## Privacy

- This app does not collect any information
- This app contact external services that may monitor your devices
  - Github, for web radio sourced from https://github.com/jcorporation/webradiodb
  - Web radio broacasters, as you need to connect to their stream
  - Your android OS
