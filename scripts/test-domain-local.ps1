# Runs domain tests using the locally cached Kotlin compiler when Gradle cannot start.
# Requires existing debug classes for unchanged Android-dependent model types.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Push-Location $projectRoot
try {
    $cache = Join-Path $env:USERPROFILE '.gradle/caches/modules-2/files-2.1'
    $jars = @(rg --files $cache -g '*.jar' -g '!*-sources.jar' -g '!*-javadoc.jar')
    $compilerJars = @($jars | Where-Object { $_ -match '(kotlin-compiler-embeddable|kotlin-stdlib|kotlin-reflect|kotlin-script-runtime|kotlin-daemon-embeddable)\\2.2.10\\|kotlinx-coroutines-core-jvm\\1.9.0\\|org.jetbrains\\annotations\\23.0.0\\' })
    $testJars = @($jars | Where-Object { $_ -match 'junit\\junit\\4.13.2\\|hamcrest-core\\1.3\\' })
    if ($compilerJars.Count -lt 4 -or $testJars.Count -ne 2) { throw 'Missing cached Kotlin/JUnit dependencies. Use Gradle instead.' }
    $classes = (Resolve-Path 'app/build/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes').Path
    $classpath = ($compilerJars + $testJars + $classes) -join ';'
    $output = Join-Path $projectRoot 'build/domain-review'
    New-Item -ItemType Directory -Force $output | Out-Null
    $main = 'app/src/main/java/com/example/fitnessrun'
    $sources = @('ProfileIdentity.kt','FoodCatalog.kt','PersonalizationEngine.kt','ProgressSnapshot.kt','DemoProgress.kt','VideoUrlPolicy.kt','RegistrationDraft.kt','ProfileFields.kt') | ForEach-Object { Join-Path $main $_ }
    # CloudErrorsTest depends on Firebase/Android resources; run it with Gradle.
    $tests = @(rg --files app/src/test -g '*.kt' -g '!CloudErrorsTest.kt')
    & java -cp ($compilerJars -join ';') org.jetbrains.kotlin.cli.jvm.K2JVMCompiler -no-stdlib -no-reflect -jvm-target 11 -classpath $classpath -d $output @sources @tests
    if ($LASTEXITCODE -ne 0) { throw 'Domain compilation failed.' }
    $testClasses = @($tests | ForEach-Object { 'com.example.fitnessrun.' + [IO.Path]::GetFileNameWithoutExtension($_) })
    & java -cp ($output + ';' + $classpath) org.junit.runner.JUnitCore @testClasses
    if ($LASTEXITCODE -ne 0) { throw 'Domain tests failed.' }
} finally { Pop-Location }

