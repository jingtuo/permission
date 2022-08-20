package io.github.jingtuo.permission;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class StatisticsPermissionUtils {

    public static void export(Project project, Set<UsePermissionInfo> permissions, String exportDataRelativePath) {
        String path = project.getProjectDir().getAbsolutePath() + File.separator + exportDataRelativePath;
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        File folder = new File(path);
        System.out.println("create path(" + path + ") " + folder.mkdirs());
        File file = new File(folder, project.getName() + ".csv");
        exportToFile(project, permissions, file);
    }

    public static void collectProject(Project project, List<Project> target) {
        target.add(project);
        Map<String, Project> childProjects = project.getChildProjects();
        Set<String> keys = childProjects.keySet();
        for (String key : keys) {
            collectProject(childProjects.get(key), target);
        }
    }

    public static Set<UsePermissionInfo> extractPermission(Project project,
                                                           Map<String, Set<UsePermissionInfo>> cache,
                                                           List<Project> allProjects, String androidManifestRelativePath) {
        if (cache.containsKey(project.getName())) {
            return cache.get(project.getName());
        }
        System.out.println("start extract project: " + project.getName());
        //放入缓存
        Set<UsePermissionInfo> result = extractPermissionFromManifest(project, androidManifestRelativePath);
        cache.put(project.getName(), result);

        //基于Gradle默认存储位置$user_home/.gradle/caches/modules-2/files-2.1解析本地文件
        String path = System.getProperty("user.home");
        String dependencyFolderPath = path + Constants.DEFAULT_GRADLE_CACHE_DEPENDENCIES_PATH;
        Configuration configuration = getReleaseRuntimeClasspathConfiguration(project);
        if (configuration == null) {
            return result;
        }
        configuration.getAllDependencies().forEach(dependency -> {
            String group = dependency.getGroup();
            String name = dependency.getName();
            String version = dependency.getVersion();
            if (group == null || version == null) {
                return;
            }
            if (Constants.VERSION_UNSPECIFIED.equals(version)) {
                //是内部工程
                Project subProject = getProject(allProjects, group, name);
                if (subProject == null) {
                    //其他模块
                    System.out.println("skip project: " + group + ", " + name + "; because it not exits");
                    return;
                }
                //extractPermission内部本身会处理缓存
                Set<UsePermissionInfo> subPermissions = extractPermission(subProject, cache,
                        allProjects, androidManifestRelativePath);
                //合并
                cache.put(project.getName(), subPermissions);
                result.addAll(subPermissions);
                return;
            }

            for (String item : Constants.GROUP_PREFIX_WHITE_LIST) {
                if (group.startsWith(item)) {
                    return;
                }
            }

            String key = group + Constants.SPLIT_COLON + name + Constants.SPLIT_COLON + version;
            if (cache.containsKey(key)) {
                Set<UsePermissionInfo> subPermissions = cache.get(key);
                result.addAll(subPermissions);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("start extract dependency(").append(key).append("): ");
                File folder = new File(dependencyFolderPath + group + "/" + name + "/" + version);
                File aarFile = getAar(folder, name, version);
                if (aarFile == null) {
                    builder.append("not arr or not downloaded");
                    System.err.println(builder);
                    return;
                }
                Set<UsePermissionInfo> subPermissions = extractPermissionFromAar(group, name, version, aarFile);
                if (subPermissions.isEmpty()) {
                    builder.append("no permission");
                } else {
                    for (UsePermissionInfo item : subPermissions) {
                        builder.append(item.getName()).append(Constants.SPLIT_SEMICOLON).append(" ");
                    }
                }
                System.out.println(builder);
                cache.put(key, subPermissions);
                result.addAll(subPermissions);
            }
        });
        return result;
    }

    public static Project getProject(List<Project> allProjects, String group, String name) {
        for (Project project : allProjects) {
            if (project.getName().equals(name) && project.getGroup().equals(group)) {
                return project;
            }
        }
        return null;
    }

    /**
     * 导出到文件
     *
     * @param project 工程名称
     * @param permissions 工程权限信息
     * @param file        保存数据的文件
     */
    public static void exportToFile(Project project, Set<UsePermissionInfo> permissions, File file) {
        if (permissions == null || permissions.isEmpty()) {
            //没有权限, 则不生成文件
            System.out.println(project.getName() + " no permission");
            return;
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write("permission_name;user;user_type" + Constants.ENTER);
            StringBuilder builder;
            for (UsePermissionInfo item : permissions) {
                builder = new StringBuilder();
                builder.append(item.getName())
                        .append(Constants.SPLIT_SEMICOLON)
                        .append(item.getUser())
                        .append(Constants.SPLIT_SEMICOLON)
                        .append(item.getUserType())
                        .append(Constants.ENTER);
                fileWriter.write(builder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param folder  $user_home/.gradle/caches/modules-2/files-2.1/group/name/version
     * @param name    名字 artifactId
     * @param version 版本
     * @return 依赖库aar文件
     */
    public static File getAar(File folder, String name, String version) {
        //SNAPSHOT可能存在多个, 按修改时间降序排, 取第一个
        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }
        String filename = name + "-" + version + ".aar";
        List<File> list = new ArrayList<>();
        for (File item : files) {
            File[] subFiles = item.listFiles(pathname -> pathname.isFile()
                    && pathname.getName().equals(filename));
            if (subFiles != null) {
                list.addAll(Arrays.asList(subFiles));
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        list.sort((o1, o2) -> {
            if (o1.lastModified() > o2.lastModified()) {
                return -1;
            } else if (o1.lastModified() < o2.lastModified()) {
                return 1;
            }
            return 0;
        });
        return list.get(0);
    }

    /**
     * 从AndroidManifest.xml中提取权限
     *
     * @param project                     工程信息
     * @param androidManifestRelativePath AndroidManifest.xml文件相对目录{@link Project#getProjectDir()}的路径
     * @return 权限名称
     */
    public static Set<UsePermissionInfo> extractPermissionFromManifest(Project project, String androidManifestRelativePath) {
        String path = project.getProjectDir().getAbsolutePath() + "/" + androidManifestRelativePath;
        File file = new File(path);
        if (!file.exists()) {
            return Collections.emptySet();
        }
        Set<UsePermissionInfo> result = new HashSet<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = builder.parse(fis);
            NodeList nodeList = document.getElementsByTagName(Constants.TAG_NAME_USES_PERMISSION);
            int size = nodeList.getLength();
            //使用Set去重
            for (int i = 0; i < size; i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrs = node.getAttributes();
                Node attrNode = attrs.getNamedItem("android:name");
                String attrValue = attrNode.getNodeValue();
                result.add(new UsePermissionInfo(attrValue, project.getName(), "project"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从Aar中提取权限
     * @param group 依赖库所属组
     * @param name 依赖库的artifactId
     * @param version 依赖库版本
     * @param file aar文件
     * @return 权限名称
     */
    public static Set<UsePermissionInfo> extractPermissionFromAar(String group, String name,
                                                                  String version, File file) {
        String key = group + Constants.SPLIT_COLON + name + Constants.SPLIT_COLON + version;
        Set<UsePermissionInfo> result = new HashSet<>();
        try (ZipFile zipFile = new ZipFile(file)) {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputStream is = zipFile.getInputStream(new ZipEntry("AndroidManifest.xml"));
            Document document = builder.parse(is);
            NodeList nodeList = document.getElementsByTagName(Constants.TAG_NAME_USES_PERMISSION);
            int size = nodeList.getLength();
            for (int i = 0; i < size; i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrs = node.getAttributes();
                Node attrNode = attrs.getNamedItem("android:name");
                String attrValue = attrNode.getNodeValue();
                result.add(new UsePermissionInfo(attrValue, key, "library"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取releaseRuntimeClasspath配置
     *
     * @param project 工程
     * @return 配置
     */
    public static Configuration getReleaseRuntimeClasspathConfiguration(Project project) {
        ConfigurationContainer container = project.getConfigurations();
        for (Configuration configuration : container) {
            if (configuration.getName().equals(Constants.CONFIG_RELEASE_RUNTIME_CLASS_PATH)
                    || configuration.getName().endsWith(Constants.CONFIG_SUFFIX_RELEASE_RUNTIME_CLASS_PATH)) {
                return configuration;
            }
        }
        return null;
    }

}
