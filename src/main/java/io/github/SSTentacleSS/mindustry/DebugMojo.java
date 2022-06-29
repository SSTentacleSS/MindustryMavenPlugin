package io.github.SSTentacleSS.mindustry;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "debug", defaultPhase = LifecyclePhase.NONE)
public class DebugMojo extends AbstractMojo {

    @Parameter(property = "mindustryVersion", required = true)
    private String mindustryVersion;
    
    @Parameter(property = "suspend", defaultValue = "false")
    private boolean suspend;

    @Parameter(property = "force", defaultValue = "false")
    private boolean force;
    
    @Parameter(property = "debugPort", defaultValue = "8000")
    private int debugPort;
    
    @Parameter(property = "pluginJars", required = true)
    private String[] pluginJars;

    @Parameter(property = "args", defaultValue = "")
    private String[] args;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        String fileName = String.format("%040x", new BigInteger(1, mindustryVersion.getBytes())).replaceAll("^0+", "");

        debugMessage("tmp dir set to \"" + tmpdir + "\"");

        File distFile = Path.of(tmpdir, "servers/" + fileName + ".jar").toFile();

        debugMessage("Set server path to " + distFile.toString());

        if (!distFile.exists() && !force) {
            try {
                URL sourceUrl = new URL("https://github.com/Anuken/Mindustry/releases/download/" + mindustryVersion + "/server-release.jar");

                logMessage("Start downloading \"" + sourceUrl.toExternalForm() + "\"");

                FileUtils.copyURLToFile(
                    sourceUrl,
                    distFile,
                    10000,
                    10000
                );

                logMessage("Download completed successfully");
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

        checkServerJar(distFile);

        File debugPath = Path.of(tmpdir, "servers/" + fileName + "-debug").toFile();

        if (!debugPath.exists())
            debugPath.mkdirs();

        debugMessage("Debug directory set to \"" + debugPath.getAbsolutePath() + "\"");

        List<String> command = new ArrayList<>(List.of(
            "java",
            "-jar",
            "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (suspend ? 'y' : 'n') + ",address=" + debugPort,
            distFile.getAbsolutePath()
        ));

        command.addAll(Arrays.asList(args));

        ProcessBuilder server = new ProcessBuilder()
            .inheritIO()
            .command(command)
            .directory(debugPath);
        File modsDirectory = Path.of(debugPath.getAbsolutePath(), "config/mods").toFile();

        debugMessage("Deleting \"" + modsDirectory.getAbsolutePath() + "\"");
        deleteDirectory(modsDirectory);
        debugMessage("Deleted \"" + modsDirectory.getAbsolutePath() + "\"");

        for (String pluginJar : pluginJars) {
            debugMessage("Copying \"" + pluginJar + "\" to debug directory");
            
            try {
                FileUtils.copyFileToDirectory(
                    Path.of(pluginJar).toFile(),
                    modsDirectory,
                    false
                );
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }

            debugMessage("Copied \"" + pluginJar + "\" to debug directory");
        }

        try {
            logMessage("Executing " + server.command().toString());

            Process childProcess = server.start();

            logMessage("Server exited with code " + childProcess.waitFor());
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private boolean checkServerJar(File jarFile) throws MojoExecutionException {
        debugMessage("Integrity check started!");

        try (JarFile serverJar = new JarFile(jarFile)) {
            String serverMainClass = serverJar.getManifest()
                .getMainAttributes()
                .getValue("Main-Class");

            if (serverJar.getEntry(serverMainClass.replace('.', '/') + ".class") == null)
                throw new Error();

            debugMessage("Integrity check finished successfully!");
            return true;
        } catch (Throwable e) {
            jarFile.delete();
            throw new MojoExecutionException("Downloaded Jar corrupted! Try again later (Downloaded jar deleted)");
        }
    }

    private boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();

        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }

        return directoryToBeDeleted.delete();
    }

    private void debugMessage(String message) {
        getLog()
            .debug(message);
    }

    private void logMessage(String message) {
        getLog()
            .info(message);
    }
}