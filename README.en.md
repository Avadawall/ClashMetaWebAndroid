## Clash Meta Web Android

A Graphical user interface of [Clash.Meta](https://github.com/MetaCubeX/Clash.Meta) for Android


### Feature

1. Feature of [Clash.Meta](https://github.com/MetaCubeX/Clash.Meta)
> \[!IMPORTANT]
> 2. Add PC Remote External Controller feature to Clash of Android, use WebUI to Add and Modify configuration
> - enable Web UI in setting to monitor clash state
> - add or change profile by select Web UI

> \[!IMPORTANT]
> ### How to use WebUI feature
> 1. first, need to connect local network with WIFI. 
> 2. enable remote controller in network setting. default passcode is "clash", after start Clash Android, the phone local IP address will displayed. connect to Android of Clash IP address with browser. then, you check clash status from PC browser.
> 3. add profile, select "Web UI", the local IP will displayed, connect to Clash of Android with the IP, you need to select URL or FILE and input URL and upload the configure file to add profile.

> ### How to use WebUI feature
> 1. first, need to connect local network with WIFI. 
> 2. enable remote controller in network setting. default passcode is "clash", after start Clash Android, the phone local IP address will displayed. connect to Android of Clash IP address with browser. then, you check clash status from PC browser.
> 3. add profile, select "Web UI", the local IP will displayed, connect to Clash of Android with the IP, you need to select URL or FILE and input URL and upload the configure file to add profile.

> ### How to use WebUI features
> 1. First, you’ll need your phone to connect to your local Wi-Fi network

> 2. Navigate to Settings → Network and enable Remote Controller. The default password is ‘clash.’ After starting the Clash instance, your phone’s local IP address will be displayed. Connect to the Clash Android’s IP:Port address using a PC web browser, and you’ll be able to view Clash’s status in the PC browser。
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_013936.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_013956.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_014008.jpg" width="300">

> 3. To add configuration information, click the “+” sign and select “Web UI”. Your phone’s IP address and port information will be displayed. Use that IP:port to connect to the Clash instance

<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_170014.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_162640.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_162648.jpg" width="300">

> 4. Proceed on your PC, based on the displayed IP:Port, by logging in to the Clash Meta Web Android service using a web browser. You’ll need to choose either URL or file configuration input, and then upload the configuration file or input the server URL to add the configuration. (After each successful addition, you’ll need to go back and re-enter to add the next configuration)
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-login.png" width="500">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-input.png" width="500">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-inputdone.png" width="500">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-url-input.png" width="500">
### Requirement

- Android 5.0+ (minimum)
- Android 7.0+ (recommend)
- `armeabi-v7a` , `arm64-v8a`, `x86` or `x86_64` Architecture

### License

See also [LICENSE](./LICENSE) and [NOTICE](./NOTICE)

### Privacy Policy

See also [PRIVACY_POLICY.md](./PRIVACY_POLICY.md)

### Build

1. Update submodules

   ```bash
   git submodule update --init --recursive
   ```

2. Install **OpenJDK 11**, **Android SDK**, **CMake** and **Golang**

3. Create `local.properties` in project root with

   ```properties
   sdk.dir=/path/to/android-sdk
   ```

4. Create `signing.properties` in project root with

   ```properties
   keystore.path=/path/to/keystore/file
   keystore.password=<key store password>
   key.alias=<key alias>
   key.password=<key password>
   ```

5. Build

   ```bash
   ./gradlew app:assembleMetaRelease
   ```

6. Pick `app-<version>-meta-<arch>-release.apk` in `app/build/outputs/apk/meta/release/`

