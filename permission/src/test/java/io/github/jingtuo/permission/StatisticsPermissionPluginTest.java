/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package io.github.jingtuo.permission;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A simple unit test for the 'io.github.jingtuo.permission' plugin.
 */
class StatisticsPermissionPluginTest {
    @Test void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("io.github.jingtuo.permission");

        // Verify the result
        assertNotNull(project.getTasks().findByName("greeting"));
    }
}
