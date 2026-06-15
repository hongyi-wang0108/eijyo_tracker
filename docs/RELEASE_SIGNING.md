# Release 签名

## ⚠️ 必须备份（丢了就无法更新已上架的 App）

两个文件**已 gitignore、不在仓库里**，请离线备份（密码管理器 / 加密网盘）：

- `eijyo-release.jks` — release 签名密钥库（项目根目录）
- `keystore.properties` — 密钥库密码配置（项目根目录）

当前密码（**上架前请改成更强的，并同步改 keystore.properties**）：

| 项 | 值 |
|----|----|
| storeFile | `eijyo-release.jks` |
| storePassword | `eijyo2026release` |
| keyAlias | `eijyo` |
| keyPassword | `eijyo2026release` |
| 证书 DN | CN=Eijyo Tracker, OU=Mobile, O=hongyi-wang0108, L=Tokyo, C=JP |
| 有效期 | 10000 天 |
| 指纹 SHA-256 | `9217694accfcd1cac4783e275c9f331093a6f7b66aff31ee4acfbd56fd85004f` |

## 出包

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleRelease          # → app/build/outputs/apk/release/app-release.apk（已签名）
# 上架 Google Play 用 AAB：
./gradlew :app:bundleRelease            # → app/build/outputs/bundle/release/app-release.aab
```

## 机制

`app/build.gradle.kts` 在配置期读 `keystore.properties`：
- 文件存在 → release buildType 用该 signingConfig 签名。
- 文件缺失（CI / 新 clone 未配置）→ release 不签名，构建不报错。

所以别人 clone 仓库也能编译；只有拥有 keystore 的人能出可上架的签名包。

## 重新生成 keystore（仅首次或丢失时；丢失后无法更新旧版本，只能改包名重新上架）

```bash
"$JAVA_HOME/bin/keytool" -genkeypair -v \
  -keystore eijyo-release.jks -alias eijyo \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass <pwd> -keypass <pwd> \
  -dname "CN=Eijyo Tracker, OU=Mobile, O=hongyi-wang0108, L=Tokyo, C=JP"
```
