// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.serialization) apply false
}

subprojects {
  tasks.withType<JavaCompile>().configureEach {
    options.isFork = true

    // System properties go in jvmArgs
    options.forkOptions.jvmArgs = (
      options.forkOptions.jvmArgs.orEmpty() +
      listOf("-Duser.language=ja", "-Duser.country=JP")
    )

    // Compiler flags go in compilerArgs
    options.compilerArgs.add("-Xdiags:verbose")
  }
}

tasks.register<Exec>("gitupload") {
    group = "git"
    description = "Clean, commit and push"
    dependsOn(":app:clean")
    commandLine("bash","-c",
        "git add . && git commit -m `date -Is` && git push -u origin main")
}
