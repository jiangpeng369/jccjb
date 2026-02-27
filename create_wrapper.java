import java.io.*;
import java.net.*;
import java.util.zip.*;

public class create_wrapper {
    public static void main(String[] args) throws Exception {
        String jarPath = "d:/VSCodeProjects/jccjb/TFT-Helper/gradle/wrapper/gradle-wrapper.jar";
        
        // Create minimal jar with GradleWrapperMain
        String classContent = "package org.gradle.wrapper;\n" +
            "public class GradleWrapperMain {\n" +
            "    public static void main(String[] args) throws Exception {\n" +
            "        String gradleVersion = \"8.5\";\n" +
            "        String home = System.getProperty(\"user.home\");\n" +
            "        File gradleDir = new File(home, \".gradle/wrapper/dists/gradle-\" + gradleVersion);\n" +
            "        gradleDir.mkdirs();\n" +
            "        \n" +
            "        // Download gradle if not exists\n" +
            "        File gradleZip = new File(gradleDir, \"gradle-\" + gradleVersion + \"-bin.zip\");\n" +
            "        if (!gradleZip.exists()) {\n" +
            "            System.out.println(\"Downloading Gradle \" + gradleVersion + \"...\");\n" +
            "            downloadFile(\"https://services.gradle.org/distributions/gradle-\" + gradleVersion + \"-bin.zip\", gradleZip);\n" +
            "        }\n" +
            "        \n" +
            "        // Extract and run\n" +
            "        File gradleHome = new File(gradleDir, \"gradle-\" + gradleVersion);\n" +
            "        if (!gradleHome.exists()) {\n" +
            "            unzip(gradleZip, gradleHome.getParentFile());\n" +
            "        }\n" +
            "        \n" +
            "        // Execute gradle\n" +
            "        String os = System.getProperty(\"os.name\").toLowerCase();\n" +
            "        File gradleExe = new File(gradleHome, \"bin/gradle\" + (os.contains(\"win\") ? \".bat\" : \"\"));\n" +
            "        ProcessBuilder pb = new ProcessBuilder(gradleExe.getAbsolutePath());\n" +
            "        pb.inheritIO();\n" +
            "        pb.start().waitFor();\n" +
            "    }\n" +
            "    \n" +
            "    static void downloadFile(String url, File file) throws Exception {\n" +
            "        try (InputStream in = new URL(url).openStream();\n" +
            "             FileOutputStream out = new FileOutputStream(file)) {\n" +
            "            byte[] buf = new byte[8192];\n" +
            "            int n;\n" +
            "            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    static void unzip(File zip, File dest) throws Exception {\n" +
            "        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {\n" +
            "            ZipEntry entry;\n" +
            "            while ((entry = zis.getNextEntry()) != null) {\n" +
            "                File f = new File(dest, entry.getName());\n" +
            "                if (entry.isDirectory()) f.mkdirs();\n" +
            "                else {\n" +
            "                    f.getParentFile().mkdirs();\n" +
            "                    try (FileOutputStream fos = new FileOutputStream(f)) {\n" +
            "                        byte[] buf = new byte[8192];\n" +
            "                        int n;\n" +
            "                        while ((n = zis.read(buf)) > 0) fos.write(buf, 0, n);\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        
        // Compile the class
        File srcDir = new File("d:/VSCodeProjects/jccjb/temp_src/org/gradle/wrapper");
        srcDir.mkdirs();
        File srcFile = new File(srcDir, "GradleWrapperMain.java");
        try (FileWriter fw = new FileWriter(srcFile)) {
            fw.write(classContent);
        }
        
        // Compile
        Process compile = new ProcessBuilder("javac", "-d", "d:/VSCodeProjects/jccjb/temp_classes", srcFile.getAbsolutePath())
            .inheritIO().start();
        compile.waitFor();
        
        // Create jar
        File jarFile = new File(jarPath);
        try (FileOutputStream fos = new FileOutputStream(jarFile);
             JarOutputStream jos = new JarOutputStream(fos)) {
            addClassToJar(jos, new File("d:/VSCodeProjects/jccjb/temp_classes"), "");
        }
        
        // Cleanup
        deleteDir(new File("d:/VSCodeProjects/jccjb/temp_src"));
        deleteDir(new File("d:/VSCodeProjects/jccjb/temp_classes"));
        
        System.out.println("Created: " + jarPath);
    }
    
    static void addClassToJar(JarOutputStream jos, File dir, String path) throws IOException {
        for (File f : dir.listFiles()) {
            String entryPath = path + f.getName();
            if (f.isDirectory()) {
                addClassToJar(jos, f, entryPath + "/");
            } else {
                JarEntry entry = new JarEntry(entryPath);
                jos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = fis.read(buf)) > 0) jos.write(buf, 0, n);
                }
                jos.closeEntry();
            }
        }
    }
    
    static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) deleteDir(f);
        }
        dir.delete();
    }
}
