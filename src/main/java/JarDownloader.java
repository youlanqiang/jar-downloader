import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.supplier.SessionBuilderSupplier;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class JarDownloader {


    public static void main(String[] args) throws DependencyResolutionException {
        // 1. 定义 Maven 坐标 (groupId:artifactId:version)
        String mavenCoordinate = "org.apache.commons:commons-lang3:3.12.0";
        // 本地maven仓库地址，建议每次都指定一个新的文件夹，使用后删除
        String localRepoDirPath = "/home/youlanqiang/IdeaProjects/jar-downloader/testRepository";
        // 远程maven仓库地址，使用阿里云代理
        String remoteMavenRepository = "https://maven.aliyun.com/repository/public";

        // 3. 初始化 RepositorySystem
        RepositorySystem system = newRepositorySystem();

        // 4. 创建 RepositorySystemSession，并指定一个本地仓库路径（用于缓存）
        // 这里使用一个临时目录作为本地仓库，避免干扰系统的 ~/.m2/repository
        File localRepoDir = new File(localRepoDirPath);
        localRepoDir.mkdirs();
        RepositorySystemSession session = newSession(system, localRepoDir.toPath());

        // 5. 定义远程仓库 (Maven Central)
        RemoteRepository centralRepo = new RemoteRepository.Builder(
                "central", "default", remoteMavenRepository).build();

        // 6. 创建要解析的构件
        Artifact artifact = new DefaultArtifact(mavenCoordinate);

        // 7. 构建收集请求，指定根依赖和远程仓库
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(new Dependency(artifact, "compile"));
        collectRequest.setRepositories(Arrays.asList(centralRepo));

        // 8. 构建依赖请求（自动处理传递性依赖）
        DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);

        // 9. 执行解析
        DependencyResult dependencyResult = system.resolveDependencies(session, dependencyRequest);

        // 10. 遍历解析结果，将 JAR 文件复制到目标文件夹
        List<ArtifactResult> artifactResults =
                dependencyResult.getArtifactResults();
        for (org.eclipse.aether.resolution.ArtifactResult result : artifactResults) {
            Artifact resolvedArtifact = result.getArtifact();
            File sourceFile = resolvedArtifact.getFile();
            if (sourceFile != null && sourceFile.exists()) {
                String fileName = sourceFile.getName();
                System.out.println("已下载: " + fileName);
            }
        }
        System.out.println("所有依赖已下载成功 ");
    }

    /**
     * 初始化 RepositorySystem。这是核心入口点。
     * 这里手动构建，实际项目中可以考虑使用 maven-resolver-supplier 库简化。
     */
    private static RepositorySystem newRepositorySystem() {
        return new RepositorySystemSupplier().get();
    }

    /**
     * 创建 RepositorySystemSession，主要用于设置本地仓库路径。
     */
    private static RepositorySystemSession newSession(RepositorySystem system, Path localRepoDir) {
        return new SessionBuilderSupplier(system).get().withLocalRepositoryBaseDirectories(localRepoDir).build();
    }
}
