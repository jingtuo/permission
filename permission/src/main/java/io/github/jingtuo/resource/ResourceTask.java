package io.github.jingtuo.resource;

import org.gradle.api.DefaultTask;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResourceTask extends DefaultTask {


    public static void main(String[] args) {
        String projectPath = "D:/Projects/ghzq/release/TZYJ_Android";
        File folder = new File(projectPath);
        List<File> javaOrXmlFiles = new ArrayList<>();
        List<File> drawableFiles = new ArrayList<>();
        addFolderTo(folder, javaOrXmlFiles, drawableFiles);
        Set<String> drawableSet = new HashSet<>();
        File exportFile = new File("D:/Documents/test.csv");
        Set<String> nonUsage = new HashSet<>();
        for (File drawableFile : drawableFiles) {
            String drawableName = getName(drawableFile.getName());
            if (drawableSet.contains(drawableName)) {
                continue;
            }
            drawableSet.add(drawableName);
            System.out.println("find drawable: " + drawableName);
            String prefix = "";
            int index = drawableName.lastIndexOf("_");
            if (index != -1) {
                String temp = drawableName.substring(index + 1);
                if ("day".equals(temp) || "day.9".equals(temp)
                        || "night".equals(temp) || "night.9".equals(temp)
                        || "light".equals(temp) || "dark".equals(temp)) {
                    prefix = drawableName.substring(0, index);
                } else {
                    try {
                        //监测是不是以数字结尾
                        Integer.parseInt(temp);
                        prefix = drawableName.substring(0, index);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            boolean exits = false;
            for (File javaOrXmlFile : javaOrXmlFiles) {
                //查找R.drawable.xxx
                //@drawable/xxx
                //查找getIdentifier("xxx", "drawable", "");
                if (findUsage(drawableName, prefix, javaOrXmlFile)) {
                    exits = true;
                }
            }
            if (!exits) {
                nonUsage.add(drawableName);
            }
        }
        try (FileWriter writer = new FileWriter(exportFile)) {
            //写入标题
            writer.write("名称, 路径, 大小\n");
            for (File drawableFile : drawableFiles) {
                String name = getName(drawableFile.getName());
                if (nonUsage.contains(name)) {
                    writer.write(name + ", " + drawableFile.getAbsolutePath() + ", " + drawableFile.length() + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addFolderTo(File folder, List<File> javaOrXmlFiles, List<File> drawableFiles) {
        if (folder.getAbsolutePath().contains("\\build\\")) {
            System.out.println("skip build: " + folder.getAbsolutePath());
            return;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                addFolderTo(file, javaOrXmlFiles, drawableFiles);
            } else {
                addFileTo(file, javaOrXmlFiles, drawableFiles);
            }
        }
    }


    private static void addFileTo(File file, List<File> javaOrXmlFiles, List<File> drawableFiles) {
        String fileName = file.getName();
        String ext = getExt(fileName);
        if ("java".equals(ext) || "kt".equals(ext)) {
            //java代码或kotlin代码
            javaOrXmlFiles.add(file);
        } else {
            File parent = file.getParentFile();
            if (parent.getName().startsWith("drawable")) {
                //drawable资源, 存在xml, png, jpg等
                drawableFiles.add(file);
            } else {
                //其他目录下的xml文件
                if ("xml".equals(ext)) {
                    //xml文件
                    javaOrXmlFiles.add(file);
                }
            }
        }
    }


    private static String getExt(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            //没有后缀
            return "";
        }
        return fileName.substring(index + 1);
    }


    private static String getName(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            //没有后缀
            return fileName;
        }
        return fileName.substring(0, index);
    }

    /**
     * @param drawableName 静态引用, 或者全名动态引用
     * @param prefix       根据前缀动态引用: 后缀是黑白版皮肤、编号
     * @param file         文件
     * @return 是否存在
     */
    private static boolean findUsage(String drawableName, String prefix, File file) {
        boolean exits = false;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNo = 0;
            while (true) {
                line = reader.readLine();
                lineNo++;
                if (line == null) {
                    break;
                }
                if (line.contains("R.drawable." + drawableName)
                        || line.contains("@drawable/" + drawableName)
                        || line.contains("\"" + drawableName + "\"")
                        || !"".equals(prefix) && line.contains(prefix)) {
                    //包含静态引用或者动态引用
                    System.out.println("drawableName: " + drawableName + ", " +
                            "exits: " + file.getName() + "-" + lineNo + ", " + line);
                    exits = true;
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return exits;
    }

}
