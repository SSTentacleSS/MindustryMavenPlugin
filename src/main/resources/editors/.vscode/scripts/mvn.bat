set MvnExists=0
FOR %%x IN (mvn.exe) DO IF not [%%~$PATH:x]==[] set MvnExists=1

IF %MvnExists%==1 (
    mvn %*
) ELSE (
    IF exist ".\\mvnw.cmd" (
        .\\mvnw.cmd %*
    ) ELSE (
        echo Maven is not installed locally or globally!
        EXIT 127
    )
)