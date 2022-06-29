mvn -version >nul 2>&1 && (
    mvn %*
) || (
    .\\mvnw -version >nul 2>&1 && (
        .\\mvnw %*
    ) || (
        echo Maven is not installed locally or globally!
    )
)