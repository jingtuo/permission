package io.github.jingtuo.permission;

public class Constants {

    public static final char SPLIT_SEMICOLON = ';';

    public static final char SPLIT_COLON = ':';

    public static final String ENTER = "\r\n";

    public static final String DEFAULT_ANDROID_MANIFEST_RELATIVE_PATH = "src/main/AndroidManifest.xml";


    public static final String TAG_NAME_USES_PERMISSION = "uses-permission";

    public static final String[] GROUP_PREFIX_WHITE_LIST = {
            "androidx.", "io.reactivex.", "com.google.", "com.squareup.",
            "org.apache.", "org.jetbrains.", "com.github.bumptech.glide",
            "com.android.", "android.arch.", "org.greenrobot"};

    public static final String CONFIG_RELEASE_RUNTIME_CLASS_PATH = "releaseRuntimeClasspath";

    public static final String CONFIG_SUFFIX_RELEASE_RUNTIME_CLASS_PATH = "ReleaseRuntimeClasspath";

    public static final String VERSION_UNSPECIFIED = "unspecified";

    public static final String DEFAULT_GRADLE_CACHE_DEPENDENCIES_PATH = "/.gradle/caches/modules-2/files-2.1/";
}
