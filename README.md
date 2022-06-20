# **MindustryDebugPlugin**

Maven plugin for Mindustry

# Installing

add this to `pom.xml` file
```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.SSTentacleSS.mindustry</groupId>
            <artifactId>mindustry-maven-plugin</artifactId>
            <version>1.1.3</version>
            <configuration>
                <mindustryVersion>${mindustryVersion}</mindustryVersion> <!-- Required, debug mindustry version -->
                <pluginJars>
                    <jar>target/${finalName}.jar</jar>
                </pluginJars> <!-- Required, path to debug plugin jar -->

                <suspend>false</suspend> <!-- Will the debugger wait for your connection? default false -->
                <debugPort>8000</debugPort> <!-- Debug port, default 8000 -->

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

# Debugging

run `mvn mindustry:debug`