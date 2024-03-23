# Using npm-link to link a local package to a consuming module
# Example:
# - instead of: import {Adapter} from '../../../../build/js/packages/kresil-experiments-kmp/kotlin/kresil-experiments-kmp.mjs'
# - we use: import {Adapter} from 'kresil-experiments-kmp'
# Useful Commands:
# $ npm ls --global
# $ npm uninstall --global my-package
# Inside the directory of the package that contains a package.json file:
# $ npm link
# Inside the directory of the package where the package.json file is that will consume the package:
# $ npm link my-package

#!/usr/bin/perl

use strict;
use warnings FATAL => 'all';

# Run Gradle task to generate the JS files
system("./gradlew kmp:shared:jsNodeDevelopment");

# Go to the output directory
chdir("build/js/packages/kresil-experiments-kmp-library/kotlin");
system("npm link");

# Go to consuming module and link the package to it
chdir("../../../../../kmp/js-app/src/main/js");
system("npm link kresil-experiments-kmp-library");