#!/usr/bin/perl

use strict;
use warnings FATAL => 'all';

require "../GradleNpmLink.pm";

# execute npmLink subroutine
GradleNpmLink::execute(
    project_gradlew_root_dir       => "../",
    gradle_task_name_for_js_export => "kmp:lib:jsNodeDevelopment",
    output_from_gradlew_root_dir   => "build/js/packages/kresil-experiments-kmp-lib/kotlin", # where package.json is located
    go_to_root_from_output_dir     => "../../../../../",
    output_package_json_name       => "kresil-experiments-kmp-lib",
    consumer_from_gradlew_root_dir => "kmp/apps/js-app", # where the package.json is located
);

