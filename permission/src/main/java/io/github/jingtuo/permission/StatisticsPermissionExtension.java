package io.github.jingtuo.permission;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;

/**
 * gradle 4.9版本, 不支持private和abstract修饰的Extension
 */
public abstract class StatisticsPermissionExtension {

    @Optional
    @Input
    public abstract Property<String> getAndroidManifestRelativePath();

    @Optional
    @OutputDirectory
    public abstract Property<String> getDataExportRelativePath();

}
