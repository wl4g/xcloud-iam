### 一个基于CAS协议的SSO登录认证企业级增强实现(PC/Android/iOS/WechatMp统一接口)，还支持QQ/Facebook等社交SNS授权认证，提供Opensaml开放API授权，内置接口级AOP二次认证实现等.

#### 一、快速开始
- 编译安装

```
cd xcloud-iam
# 打包为单个可执行jar
mvn -U clean install -DskipTests -T 2C -P springExecJar
# 打包为通用软件发布包(目录结构)
# mvn -U clean install -DskipTests -T 2C -P mvnAssTar
```


##### 1，服务端集成：
- 1.1，独立运行模式，使用iam的数据库表，适用于新系统集成，
- 1.2，依赖嵌入模式，使用外部自定义数据库表，适用于旧系统改造集成，
- 1.3，所有支持的yml配置，


##### 2，客户端集成：
- 2.1，PC集成(前后端分离)
- 2.2，[安卓端接入（全局认证拦截器）](xcloud-iam-client-example/src/main/java/com/wl4g/iam/example/android/AndroidIamUserCoordinator.java)
- 2.3，微信公众号集成，
- 2.4，服务端所有支持的yml配置(以及默认值):

```
<dependency>
    <groupId>com.wl4g</groupId>
    <artifactId>xcloud-iam-client</artifactId>
    <version>${latest}</version>
</dependency>
```


#### 二、认证机制原理



#### 三、二次开发指南
#####	- 3.1、客户端二次开发
#####	- 3.2、服务端二次开发



<p align="center">
    <img src="https://github.com/wl4g/xcloud-iam/tree/master/shots/iam-logo.png" width="150">
    <h3 align="center">XCloud IAM</h3>
    <p align="center">
        A Distributed IAM(CAS/SSO) SpringCloud Application.
        <br>
        <a href="https://github.com/wl4g/xcloud-iam/"><strong>-- Home Page --</strong></a>
        <br>
        <br>
        <a href="https://maven-badges.herokuapp.com/maven-central/com.xuxueli/xxl-sso/">
            <img src="https://img.shields.io/badge/Maven-3.5+-green.svg" >
        </a>
         <a href="https://github.com/wl4g/xcloud-iam/releases">
             <img src="https://img.shields.io/badge/release-v2.0.0-green.svg" >
         </a>
        <a href="http://www.apache.org/licenses/LICENSE-2.0">
            <img src="https://img.shields.io/badge/license-Apache2.0+-green.svg" >
        </a>
    </p>    
</p>


## Introduction

XCloud IAM 是一个分布式单点登录框架。只需要登录一次就可以访问所有相互信任的应用系统。
拥有"轻量级、分布式、跨站/域、Cookie+Token均支持、Web+APP均支持"等特性。现已开放源代码，开箱即用。


## Documentation
- English version goes [here](README.md)


## Features
1. 简洁：API直观简洁，可快速上手
2. 轻量级：环境依赖小，部署与接入成本较低
3. 单点登录：只需要登录一次就可以访问所有相互信任的应用系统
4. 分布式：接入 IAM/SSO 认证中心的应用，支持分布式部署
5. HA：Server端与Client端，均支持集群部署，提高系统可用性
6. 跨域：支持跨域应用接入 IAM/SSO 认证中心
7. Cookie+Token均支持：支持基于Cookie和基于Token两种接入方式，并均提供Sample项目
8. Web+APP均支持：支持Web和APP接入
9. 实时性：系统登陆、注销状态，全部有IAM Server统一控制，与Client端准同步
10. CS结构：基于CS结构，包括Server"认证中心"与Client"受保护应用"
11. 路径排除：支持自定义多个排除路径，支持Ant表达式。用于排除 IAM/SSO 客户端不需要过滤的路径
12. 支持多种模式部署运行（local模式）：传统单体应用，没有认证客户端一说，即，IAM server与BizApp在同一JVM进程，好处是部署、运维简单，适合小型管理类项目。
13. 支持多种模式部署运行（cluster模式）：将认证中心与业务应用和认证客户端分离，即，IAM client与BizApp在同一JVM进程，IAM server在一个进程，适合微服务或跨站跨域的多应用需要统一认证的情况。
14. 支持多种模式部署运行（gateway模式）：与cluster类似，区别是将认证客户端放到了网关，使BizApp专注于提供业务服务，实现了网关、业务应用、认证中心的完全分离，
    即，gateway+IAM client、BizApp、IAM server，非常适合完全微服务架构的认证中心部署（量身定制）


## Development
于2018年初，我在github上创建 XCloud IAM 项目仓库并提交第一个commit，随之进行系统结构设计，UI选型，交互设计……

至今，IAM/SSO 已接入某物联网平台的生产环境，稳定运行1year+，接入场景如电商业务，O2O业务和核心中间件配置动态化等。

欢迎大家的关注和使用，IAM/SSO也将拥抱变化，持续发展。


## Contributing

欢迎参与项目贡献！比如提交PR修复一个bug，或者新建 [Issue](https://github.com/wl4g/xcloud-iam/issues/) 讨论新特性或者变更。


## Copyright and License
This product is open source and free, and will continue to provide free community technical support. Individual or enterprise users are free to access and use.

- Licensed under the Apache License v2.
- Copyright (c) 2018-present, wanglsir.

产品开源免费，并且将持续提供免费的社区技术支持。个人或企业内部可自由的接入和使用。


## Donate

无论金额多少都足够表达您这份心意，非常感谢 ：）      [前往捐赠]()
