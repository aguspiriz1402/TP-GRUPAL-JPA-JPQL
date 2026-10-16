# Lanzador local para Windows: no instala Maven en el sistema.
$ErrorActionPreference = 'Stop'
$mavenVersion = '3.9.11'
$mavenHash = '03e2d65d4483a3396980629f260e25cac0d8b6f7f2791e4dc20bc83f9514db8d0f05b0479e699a5f34679250c49c8e52e961262ded468a20de0be254d8207076'
$toolsPath = Join-Path $PSScriptRoot '.tools'
$mavenPath = Join-Path $toolsPath "apache-maven-$mavenVersion\bin\mvn.cmd"

if (-not $env:JAVA_HOME) {
    $javaRoots = @('C:\Program Files\Java', 'C:\Program Files\Eclipse Adoptium')
    $javaCandidate = foreach ($javaRoot in $javaRoots) {
        if (Test-Path -LiteralPath $javaRoot) {
            Get-ChildItem -LiteralPath $javaRoot -Directory |
                Where-Object { $_.Name -match 'jdk-?(17|21)(\D|$)' } |
                Sort-Object Name -Descending
        }
    }
    $javaCandidate = $javaCandidate | Select-Object -First 1
    if ($javaCandidate) {
        $env:JAVA_HOME = $javaCandidate.FullName
    } else {
        throw 'Instalar JDK 17 o 21 y definir JAVA_HOME con la carpeta del JDK.'
    }
}
if (-not (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
    throw 'JAVA_HOME debe apuntar a un JDK, no a un JRE.'
}

if (-not (Test-Path -LiteralPath $mavenPath)) {
    New-Item -ItemType Directory -Force -Path $toolsPath | Out-Null
    $zipPath = Join-Path $toolsPath 'maven.zip'
    $uri = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$mavenVersion/apache-maven-$mavenVersion-bin.zip"
    Invoke-WebRequest -Uri $uri -OutFile $zipPath -UseBasicParsing
    if ((Get-FileHash -LiteralPath $zipPath -Algorithm SHA512).Hash -ine $mavenHash) {
        throw 'El archivo de Maven no coincide con su SHA-512 oficial.'
    }
    Expand-Archive -LiteralPath $zipPath -DestinationPath $toolsPath -Force
}

Push-Location $PSScriptRoot
try {
    $repositoryPath = Join-Path $toolsPath 'repository'
    & $mavenPath "-Dmaven.repo.local=$repositoryPath" @args
    $mavenExitCode = $LASTEXITCODE
} finally {
    Pop-Location
}
exit $mavenExitCode
