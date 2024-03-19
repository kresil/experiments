# using npm link to link the kmp module to the consuming module
# Example:
# - instead of: import {Adapter} from '../../../../build/js/packages/kresil-experiments-kmp/kotlin/kresil-experiments-kmp.mjs'
# - we use: import {Adapter} from 'kresil-experiments-kmp'

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
chdir("js-app/src/main/js/server.mjs");

# Link the package to the consuming module
system("npm link kresil-experiments-kmp");
