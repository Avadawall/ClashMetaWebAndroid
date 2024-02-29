<p align="right">
   <strong>中文</strong> | <a href="./README.en.md">English</a>
</p>


## Clash Meta Web Android

这是 Clash.Meta 在 Android 上的图形用户界面。

### 特点
1. Clash.Meta 的特点：

> \[!IMPORTANT] 
> 2. 添加 PC 远程外部控制器功能到 Clash Android 中，使用 WebUI 来添加和修改配置。
> - 在设置中启用 Web UI 以监控 Clash 状态。
> - 通过选择 Web UI 来添加或更改配置文件。

> \[!IMPORTANT]
>
> ### 如何使用 WebUI 功能
> 1. 首先，需要手机连接到本地 WIFI 网络。
>
> 2. 在设置->网络 启动打开远端控制。默认密码是 “clash”。启动 Clash实例后，手机的本地 IP 地址将显示出来。通过PC浏览器连接到 Clash Android 的 IP:Port 地址，然后你可以在 PC 浏览器中查看 Clash 的状态。
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_013936.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_013956.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_014008.jpg" width="300">
>
> 3. 添加配置信息，点击 + , 选择 “网页 UI”，手机的IP:Port 信息将显示出来。通过该 IP:Port 地址连接到 Clash 实例。

<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_170014.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_162640.jpg" width="300">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/Screenshot_162648.jpg" width="300">
>
> 4. 转到PC电脑上进行操作，根据显示的IP:port, 使用浏览器登录到Clash Meta Web Android 服务上进行操作，你需要选择 URL 或文件配置输入，然后上传配置文件或者输入服务端URL以添加配置(每次添加完成后，你需要返回后，再进入才能添加下一个配置)
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-login.png" width="500">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-input.png" width="500">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-inputdone.png" width="500">
<img src="https://raw.githubusercontent.com/Avadawall/ClashMetaWebAndroid/master/.github/images/cmwa-web-url-input.png" width="500">

### 要求

- Android 5.0+（最低要求）
- Android 7.0+（推荐）
- armeabi-v7a、arm64-v8a、x86 或 x86_64 架构

### 许可证
请参阅 [LICENSE](./LICENSE) 和 [NOTICE](./NOTICE) 

### 隐私政策
请参阅 [PRIVACY_POLICY.md](./PRIVACY_POLICY.md)

### 构建
1. 更新子模块
    ```bash
    git submodule update --init --recursive
    ```
2. 安装 OpenJDK 11、Android SDK、CMake 和 Golang
3. 在项目根目录创建 local.properties 文件，内容如下：
    ```properties
    sdk.dir=/path/to/android-sdk
    ```

4. 在项目根目录创建 signing.properties 文件，内容如下：
    ```properties
    keystore.path=/path/to/keystore/file
    keystore.password=<密钥库密码>
    key.alias=<密钥别名>
    key.password=<密钥密码>
    ```
5. 构建
    ```bash
    ./gradlew app:assembleMetaRelease
    ```
6. 在 app/build/outputs/apk/meta/release/ 目录中选择 app-<version>-meta-<arch>-release.apk
