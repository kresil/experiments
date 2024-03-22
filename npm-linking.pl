# using npm-link to link the kmp module to the consuming module
# Example:
# - instead of: import {Adapter} from '../../../../build/js/packages/kresil-experiments-kmp/kotlin/kresil-experiments-kmp.mjs'
# - we use: import {Adapter} from 'kresil-experiments-kmp'
# Useful Commands:
# $ npm ls --global
# $ npm uninstall --global my-package
# Inside the directory of the package that contains a package.json file:
# $ npm link
# Inside the directory of the package that will consume the package:
# $ npm link my-package

#!/usr/bin/perl

use strict;
use warnings;

# Run Gradle to generate the JS files from KMP module
system("./gradlew kmp:jsNodeDevelopment");

# Go to the output directory and link the package
chdir("build/js/packages/kresil-experiments-kmp/kotlin");
system("npm link");

# Go back to the root directory
chdir("../../../../../..");

# Go to the consuming module
chdir("js-app/src/main/js");

# Link the package to the consuming module
system("npm link kresil-experiments-kmp");


# more

# ../../../../kotlin-app/build/js/packages/k2j-kotlin-app/kotlin/k2j-kotlin-app.mjs

