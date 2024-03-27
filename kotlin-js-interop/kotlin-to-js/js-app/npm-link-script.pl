#!/usr/bin/perl

use strict;
use warnings FATAL => 'all';
require "./GradleNpmLink.pm";

# execute npmLink subroutine
GradleNpmLink::execute(
    project_gradlew_root_dir           => "kotlin-js-interop/kotlin-to-js/kotlin-app/",
    gradle_task_name_for_js_export     => "assemble",
    output_from_gradlew_root_dir       => "build/js/packages/k2j-kotlin-app/kotlin", # where package.json is located
    go_to_project_root_from_output_dir => "../../../../../",
    output_package_json_name           => "k2j-kotlin-app",
    consumer_from_gradlew_root_dir     => "../js-app", # where the package.json is located
);
