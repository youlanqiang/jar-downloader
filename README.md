# jar-downloader

一个基于 **Maven Resolver (Eclipse Aether)** 的轻量级工具，用于将指定的 Maven 构件（artifact）及其**全部传递性依赖**下载到本地指定目录。

适合在没有完整 Maven 工程的情况下，快速拉取某个 jar 及其依赖树，例如离线部署、依赖分析或打包分发场景。

## 功能特性

- 按 Maven 坐标 `groupId:artifactId:version` 解析构件
- 自动解析并下载全部传递性依赖
- 可自定义本地仓库目录（默认写入独立临时目录，不污染 `~/.m2/repository`）
- 可自定义远程仓库地址（默认使用阿里云公共代理，国内下载更快）
- 下载完成后在控制台打印每个已下载的 jar 文件名

## 技术栈

| 组件 | 版本 |
| --- | --- |
| Java | 8+ |
| maven-resolver-api / impl / util | 2.0.24 |
| maven-resolver-connector-basic | 2.0.24 |
| maven-resolver-transport-http | 1.9.27 |
| maven-resolver-supplier-mvn3 | 2.0.24 |

## 项目结构

```
jar-downloader/
├── pom.xml
└── src/main/java/
    └── JarDownloader.java   # 核心入口，全部逻辑集中在此
```

## 使用方式

所有配置项都集中在 `JarDownloader.java` 的 `main` 方法中，运行前按需修改：

```java
// 1. 定义 Maven 坐标 (groupId:artifactId:version)
String mavenCoordinate = "org.apache.commons:commons-lang3:3.12.0";

// 2. 本地仓库地址，建议每次指定一个新文件夹，用完后删除
String localRepoDirPath = "/home/youlanqiang/IdeaProjects/jar-downloader/testRepository";

// 3. 远程仓库地址，默认使用阿里云代理
String remoteMavenRepository = "https://maven.aliyun.com/repository/public";
```

然后直接运行 `JarDownloader#main`（IDEA 中右键 Run 即可，或先 `mvn compile` 后再手动执行）。

### 输出示例

```
已下载: commons-lang3-3.12.0.jar
已下载: ...（其余传递性依赖）
所有依赖已下载成功
```

## 工作原理

1. 通过 `RepositorySystemSupplier` 构建 `RepositorySystem` 实例
2. 使用 `SessionBuilderSupplier` 创建会话，并指定本地仓库目录
3. 将坐标封装为 `DefaultArtifact`，构建 `CollectRequest` 与 `DependencyRequest`
4. 调用 `resolveDependencies` 解析依赖树并下载
5. 遍历 `DependencyResult`，打印已落地的 jar 文件

## 注意事项

- 本地仓库目录建议每次都换一个新的，下载完成后手动删除，避免历史缓存影响结果。
- 如需下载到系统默认仓库，可将路径指向 `~/.m2/repository`。
- 若目标构件不在阿里云代理中，可替换 `remoteMavenRepository` 为其他仓库地址（如 Maven Central `https://repo.maven.apache.org/maven2`）。
- 当前为单次运行的硬编码示例；如需批量下载，可将坐标改为命令行参数或配置文件读取。
