# Statistics Permission Plugin

## 简介

Statistics Permission Plugin主要为了统计Android应用的权限。
经常遇到Android应用依赖许多模块(module)、依赖库(.aar)，本插件的目的是分析权限是被哪个模块引入的及其必要性。

[io.github.jingtuo.permission](https://plugins.gradle.org/plugin/io.github.jingtuo.permission)

> 通过这个插件的开发，积累插件开发经验

## 思路

1. 解析AndroidManifest.xml文件中uses-permission，提取出来最终生成文件(.csv)。
2. 针对依赖库的处理，基于build任务将依赖库下载到本地user.home/.gradle/caches/modules-2/files-2.1/，再通过插件解析这个目录下的依赖库


> 由于Gradle官方不建议插件依赖过多三方库，所以就直接写到csv中

## 使用

### 配置

Gradle插件引入-新方式:
```groovy
plugins {
   id "io.github.jingtuo.permission" version "1.2"
}
```

Gradle插件引入-旧方式
```groovy
buildscript {
   repositories {
      maven {
         url "https://plugins.gradle.org/m2/"
      }
   }

   dependencies {
      //发布gradle的maven仓库, artifactId默认使用了当前工程名
      //发布到本地仓库, 插件maven-publish根据publishing.publications.publication.artifactId生成一个发布包
      //插件com.gradle.plugin-publish根据工程名生成一个publishing.publications.permissionPluginMarkerMaven
      classpath "io.github.jingtuo:permission:1.2"
   }
}
```


```groovy
//应用之后，如果是Android Application/Library，会创建一个task: statistics工程名(App)Permission
apply plugin: "io.github.jingtuo.permission"

statisticsPermission {
    //当前工程以及子工程的AndroidManifest.xml相对路径, 默认值: src/main/AndroidManifest.xml。
    // （一般都应该在这里，现在应该没有用Eclipse开发Android的了吧）
    androidManifestRelativePath = "src/main/AndroidManifest.xml"
    //数据导出位置, 默认为当前工程目录
    dataExportRelativePath = ""
}
```

> Gradle maven仓库对插件id有要求, io.github.jingtuo需要有对应的域名https://jingtuo.github.io/

### 执行任务

```shell
# 分析依赖库需要先通过build任务，将依赖库下载本地
./gradlew.bat build
# 执行统计任务
./gradlew.bat statisticsAppPermission
```

### 数据格式

| permission_name | user | user_type |
| :-- | :-- | :-- |
| android.permission.INTERNET | app | project |
| android.permission.INTERNET | com.xxx:xxx:1.1 | library |

- permission_name表示权限名称
- user表示权限的使用者
- user_type表示使用者是工程还是依赖库

## 权限

### CALL_PHONE

[android.permission.CALL_PHONE](https://developer.android.google.cn/reference/android/Manifest.permission#CALL_PHONE)用于直接呼叫号码。

使用代码: [Intent.ACTION_CALL](https://developer.android.google.cn/reference/android/content/Intent?hl=en#ACTION_CALL)
```kotlin
//需要授权, 否则抛异常, 不支持直接呼叫紧急电话
startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:手机号码")))
```
> 课外知识: [ROLE_DIALER](https://developer.android.google.cn/reference/android/app/role/RoleManager#ROLE_DIALER)

替代方案:
```kotlin
//ACTION_DIAL也可以换成ACTION_VIEW
startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:手机号码")))
```


## 版本

### 1.2

1. 解决发布到Gradle Maven仓库的artifactId是plugin(io.github.jingtuo:plugin:1.1)的问题

### 1.1

1. artifactId从statistics-permission修改为permission

### 1.0

1. 分析使用权限的工程、依赖库
2. 将数据生成到csv中

## Publish To Local

```shell
./gradlew.bat publishPermissionPublicationToLocalPlugin
```

## 问题

1. 如果一个rootProject中存在名字相同的project，建议修改project名称。
2. 如果projectA依赖projectB，projectB又依赖projectA，不论是直接依赖还是间接依赖，这都是不允许的，从工程结构上来说是死循环。
3. 如果依赖库A依赖了依赖库B，但dependencies中没有声明依赖库B，则不会分析依赖库B
4. Gradle Version 4.9不支持用接口类、抽象类定义Task、Extension
5. 针对依赖库的分析增加了白名单，减少不必要的分析，依赖库的group以下面字符串开头的不进行分析：
    - androidx.
    - io.reactivex.
    - com.google.
    - com.squareup.
    - org.apache.
    - org.jetbrains.
    - com.github.bumptech.glide
    - com.android.
    - android.arch.
    - org.greenrobot

## 参考资料

- [Maven Publish Plugin](https://docs.gradle.org/current/userguide/publishing_maven.html)
- [Publish Gradle Plugin](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html)
- [Github Pages Quick Start](https://docs.github.com/cn/pages/quickstart)
