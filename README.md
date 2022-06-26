# **MindustryDebugPlugin**

Maven plugin for Mindustry

# RoadMap

- [x] Add debug template for editors (Ready for Visual Studio Code)

# Installing

add this to `pom.xml` file
```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.SSTentacleSS.mindustry</groupId>
            <artifactId>mindustry-maven-plugin</artifactId>
            <version>1.2.1</version>
            <configuration>
                <mindustryVersion>${mindustryVersion}</mindustryVersion> <!-- Required, debug mindustry version -->
                <pluginJars>
                    <jar>target/${finalName}.jar</jar>
                </pluginJars> <!-- Required, path to debug plugin jar -->

                <suspend>false</suspend> <!-- Will the debugger wait for your connection? default false -->
                <debugPort>8000</debugPort> <!-- Debug port, default 8000 -->
                <force>false</force> <!-- Force the plugin to download the server assembly again, default false -->

                <args> <!-- Args for mindustry server -->
                    <arg>args1</arg>
                    <arg>args2</arg>
                    <arg>args3</arg>
                </args>
            </configuration>
        </plugin>
    </plugins>
</build>
```

# Integrating with editors:

* Visual Studio Code
    1. run `mvn mindustry:generate-config -Deditor=vscode`
    2. run `mvn wrapper:wrapper` Or change all ./mvnw to mvn in .vscode/tasks.json
    2. Use `Run and Debug` -> `Build and Debug`

# Debugging

run `mvn mindustry:debug`
