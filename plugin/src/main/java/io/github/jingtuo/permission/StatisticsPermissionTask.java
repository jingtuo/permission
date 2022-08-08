package io.github.jingtuo.permission;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

import java.util.*;

/**
 * configuration分类:
 *  1. _internal_aapt2_binary, 依赖: com.android.tools.build
 *  2. androidApis, 依赖:
 *  3. androidTestAnnotationProcessor, 依赖:
 *  4. androidTestApi, 依赖:
 *  5. androidTestApk
 *  6. androidTestCompile
 *  7. androidTestCompileOnly
 *  8. androidTestDebugAnnotationProcessor
 *  9. androidTestDebugApi
 *  10. androidTestDebugApk
 *  11. androidTestDebugCompile
 *  12. androidTestDebugCompileOnly
 *  13. androidTestDebugImplementation
 *  14. androidTestDebugProvided
 *  15. androidTestDebugRuntimeOnly
 *  16. androidTestDebugWearApp
 *  17. androidTestImplementation
 *  18. androidTestProvided
 *  19. androidTestRuntimeOnly
 *  20. androidTestUtil
 *  21. androidTestWearApp
 *  22. annotationProcessor
 *  23. api
 *  24. apk
 *  25. archives
 *  26. compile
 *  27. compileOnly
 *  28. coreLibraryDesugaring
 *  29. debugAabPublication
 *  30. debugAndroidTestAnnotationProcessorClasspath
 *  31. debugAndroidTestCompileClasspath
 *  32. debugAndroidTestRuntimeClasspath
 *  33. debugAnnotationProcessor
 *  34. debugAnnotationProcessorClasspath
 *  35. debugApi
 *  36. debugApiElements
 *  37. debugApk
 *  38. debugApkPublication
 *  39. debugCompile
 *  40. debugCompileClasspath
 *  41. debugCompileOnly
 *  42. debugImplementation
 *  43. debugProvided
 *  44. debugReverseMetadataValues
 *  45. debugRuntimeClasspath
 *  46. debugRuntimeElements
 *  47. debugRuntimeOnly
 *  48. debugUnitTestAnnotationProcessorClasspath
 *  49. debugUnitTestCompileClasspath
 *  50. debugUnitTestRuntimeClasspath
 *  51. debugWearApp
 *  52. debugWearBundling
 *  53. default
 *  54. implementation
 *  55. lintChecks
 *  56. lintClassPath
 *  57. lintPublish
 *  58. provided
 *  59. releaseAabPublication
 *  60. releaseAnnotationProcessor
 *  61. releaseAnnotationProcessorClasspath
 *  62. releaseApi
 *  63. releaseApiElements
 *  64. releaseApk
 *  65. releaseApkPublication
 *  66. releaseCompile
 *  67. releaseCompileClasspath
 *  68. releaseCompileOnly
 *  69. releaseImplementation
 *  70. releaseProvided
 *  71. releaseReverseMetadataValues
 *  72. releaseRuntimeClasspath
 *  73. releaseRuntimeElements
 *  74. releaseRuntimeOnly
 *  75. releaseUnitTestAnnotationProcessorClasspath
 *  76. releaseUnitTestCompileClasspath
 *  77. releaseUnitTestRuntimeClasspath
 *  78. releaseWearApp
 *  79. releaseWearBundling
 *  80. runtimeOnly
 *  81. testAnnotationProcessor
 *  82. testApi
 *  83. testApk
 *  84. testCompile
 *  85. testCompileOnly
 *  86. testDebugAnnotationProcessor
 *  87. testDebugApi
 *  88. testDebugApk
 *  89. testDebugCompile
 *  90. testDebugCompileOnly
 *  91. testDebugImplementation
 *  92. testDebugProvided
 *  93. testDebugRuntimeOnly
 *  94. testDebugWearApp
 *  95. testImplementation
 *  96. testProvided
 *  97. testReleaseAnnotationProcessor
 *  98. testReleaseApi
 *  99. testReleaseApk
 *  100. testReleaseCompile
 *  101. testReleaseCompileOnly
 *  102. testReleaseImplementation
 *  103. testReleaseProvided
 *  104. testReleaseRuntimeOnly
 *  105. testReleaseWearApp
 *  107. testRuntimeOnly
 *  108. testWearApp
 *  109. wearApp
 *
 */
public abstract class StatisticsPermissionTask extends DefaultTask {

    @Optional
    @Input
    public abstract Property<String> getAndroidManifestRelativePath();

    @Optional
    @Input
    public abstract Property<String> getDataExportRelativePath();

    @TaskAction
    public void statisticsPermission() {
        Project project = getProject();
        Project rootProject = project.getRootProject();
        //统计所有的工程
        List<Project> allProjects = new ArrayList<>();
        StatisticsPermissionUtils.collectProject(rootProject, allProjects);

        //使用缓存记录已经处理过的工程, 避免一个工程重复解析, A依赖B和C, B和C依赖D
        Map<String, Set<UsePermissionInfo>> cache = new HashMap<>();

        String androidManifestRelativePath;
        if (getAndroidManifestRelativePath().isPresent()) {
            androidManifestRelativePath = getAndroidManifestRelativePath().get();
        } else {
            androidManifestRelativePath = Constants.DEFAULT_ANDROID_MANIFEST_RELATIVE_PATH;
        }

        String dataExportRelativePath;
        if (getDataExportRelativePath().isPresent()) {
            dataExportRelativePath = getDataExportRelativePath().get();
        } else {
            dataExportRelativePath = "";
        }

        Set<UsePermissionInfo> permissions = StatisticsPermissionUtils.extractPermission(project,
                cache, allProjects, androidManifestRelativePath);
        StatisticsPermissionUtils.export(project, permissions, dataExportRelativePath);
    }

}
