package io.github.SSTentacleSS.mindustry;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "debug", defaultPhase = LifecyclePhase.NONE)
public class DebugMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "mindustryVersion", required = true)
    private String mindustryVersion;
    
    @Parameter(property = "suspend", defaultValue = "false")
    private boolean suspend;
    
    @Parameter(property = "debugPort", defaultValue = "8000")
    private int debugPort;
    
    @Parameter(property = "pluginJars", required = true, defaultValue = "")
    private String[] pluginJars;

    @Parameter(property = "args", defaultValue = "")
    private String[] args;

    public void execute() throws MojoExecutionException, MojoFailureException {
        String tmpdir = System.getProperty("java.io.tmpdir");
        String fileName = String.format("%040x", new BigInteger(1, mindustryVersion.getBytes())).replaceAll("^0+", "");

        debugMessage("tmp dir set to \"" + tmpdir + "\"");

        File distFile = Path.of(tmpdir, "servers/" + fileName + ".jar").toFile();

        debugMessage("Set server path to " + distFile.toString());

        if (!distFile.exists()) {
            try {
                URL sourceUrl = new URL("https://github.com/Anuken/Mindustry/releases/download/" + mindustryVersion + "/server-release.jar");

                logMessage("Start downloading \"" + sourceUrl.toExternalForm() + "\"");

                FileUtils.copyURLToFile(
                    sourceUrl,
                    distFile
                );

                logMessage("Download completed successfully");
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }

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
        modsDirectory.delete();
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

    private void debugMessage(String message) {
        getLog()
            .debug(message);
    }

    private void logMessage(String message) {
        getLog()
            .info(message);
    }
}