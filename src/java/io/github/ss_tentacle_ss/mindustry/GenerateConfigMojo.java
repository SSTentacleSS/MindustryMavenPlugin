package io.github.ss_tentacle_ss.mindustry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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

            List<String> files = thisFile.stream()
                .filter(fileOrDirEntry -> fileOrDirEntry.getName().startsWith("editors") && !fileOrDirEntry.isDirectory())
                .map(fileEntry -> fileEntry.toString())
                .toList();
            Supplier<Stream<String>> filesStreamSupplier = () -> files.stream();

            if (
                !filesStreamSupplier
                    .get()
                    .anyMatch(file -> file.startsWith("editors/." + editor))
            ) throw new MojoExecutionException(editor + " editor is not supported. Supported editors: " +
                filesStreamSupplier.get().collect(
                    StringBuilder::new,
                    (x, y) -> {
                        Matcher matcher = Pattern
                            .compile("^editors\\/\\.([^\\/]*)[\\d\\D]+", 0)
                            .matcher(y);
                        matcher.find();

                        if (x.indexOf(matcher.group(1)) == -1)
                            x.append("\"" + matcher.group(1) + "\" ");
                    }, StringBuilder::append
                ).toString()
            );

            filesStreamSupplier.get()
                .forEach(file -> {
                    try {
                        FileUtils.writeStringToFile(
                            Path.of(
                                file.replace("editors/", "")
                            ).toFile(),
                            IOUtils.toString(getClass().getResourceAsStream("/" + file), Charset.defaultCharset()),
                            Charset.defaultCharset()
                        );
                    } catch (IOException e) {
                        getLog().error("Error writing " + file, e);
                    }
                });
        } catch (IOException e) {
            throw new MojoExecutionException(e);
        }
    }
}
