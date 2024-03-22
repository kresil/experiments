# Access the Kotlin module
chdir("./kotlin-app");

# Run Gradle task to generate the JS files
system("./gradlew :assemble");

# Go to the output directory
chdir("build/js/packages/j2k-kotlin-app/kotlin");
system("npm link");

# Go to consuming module and link the package to it
chdir("../../../../../../js-app/src/main/js");
system("npm link j2k-kotlin-app");