package io.github.SSTentacleSS.mindustry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "generate-config", defaultPhase = LifecyclePhase.NONE)
public class GenerateConfigMojo extends AbstractMojo {

    @Parameter(property = "editor", required = true)
    private String editor;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            JarFile thisFile = new JarFile(
                FileUtils.toFile(
                    getClass()
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                )
            );

            thisFile.stream()
                .filter(fileOrDirEntry -> fileOrDirEntry.getName().startsWith("editors") && !fileOrDirEntry.isDirectory())
                .forEach(fileEntry -> {
                    try {
                        FileUtils.writeStringToFile(
                            Path.of(
                                fileEntry.getName().replace("editors/", "")
                            ).toFile(),
                            IOUtils.toString(getClass().getResourceAsStream("/" + fileEntry.getName()), Charset.defaultCharset()),
                            Charset.defaultCharset()
                        );
                    } catch (IOException e) {
                        getLog().error("Error writing " + fileEntry.getName(), e);
                    }
                });
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }
}
