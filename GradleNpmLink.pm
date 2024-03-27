package GradleNpmLink;
use strict;
use warnings FATAL => 'all';
use Cwd;

# Defines a script for using npm-link in order to link a local output package to
# a consuming module without publishing it to npm.
# Inside the directory of the package that contains a package.json file:
# $ npm link
# Inside the directory of the package where the package.json file is that will consume the package:
# $ npm link my-package
# Useful Commands:
# $ npm ls --global
# $ npm uninstall --global my-package
# Use case in ES6>
# - instead of: import {Adapter} from '../../../../build/js/packages/kresil-experiments-kmp/kotlin/kresil-experiments-kmp.mjs'
# - we use: import {Adapter} from 'kresil-experiments-kmp'

# subroutines
sub println {
    my @parms = @_;
    my $msg = $parms[0];
    print "$msg\n";
}

sub currDirPrint {
    my @parms = @_;
    my $dir_name = $parms[0];
    my $dir = cwd();
    println("Current working directory ($dir_name): $dir");
}

sub chdirAndPrintCurrent {
    my @parms = @_;
    my $dir = $parms[0];
    my $dir_name = $parms[1];
    chdir($dir) or die "Can't change to $dir_name directory: $!";
    currDirPrint($dir_name);
}

sub execute {
    my (%args) = @_;

    # variables
    my $project_gradlew_root_dir = $args{project_gradlew_root_dir};
    my $gradle_task_name_for_js_export = $args{gradle_task_name_for_js_export};
    my $output_from_gradlew_root_dir = $args{output_from_gradlew_root_dir};
    my $go_to_project_root_from_output_dir = $args{go_to_project_root_from_output_dir};
    my $output_package_json_name = $args{output_package_json_name};
    my $consumer_from_gradlew_root_dir = $args{consumer_from_gradlew_root_dir};
    my $project_root = "project_root";

    println("Starting npm linking process...");
    currDirPrint("script");

    # Go to the root directory to access gradle wrapper
    chdirAndPrintCurrent($project_gradlew_root_dir, $project_root);

    # Run Gradle task to generate the JS files
    system("gradlew $gradle_task_name_for_js_export");

    # Go to the output directory package.json location
    chdirAndPrintCurrent($output_from_gradlew_root_dir, "output");
    system("npm link");
    println("Producer npm link executed successfully...");

    # Go back to root
    chdirAndPrintCurrent($go_to_project_root_from_output_dir, $project_root);

    # Go to the consumer directory
    chdirAndPrintCurrent($consumer_from_gradlew_root_dir, "consumer");
    system("npm link $output_package_json_name");
    println("Consumer npm link executed successfully...");

    # confirm npm link addition with : $ npm ls --global
    println("Current npm link status:");
    system("npm ls --global");
    println("To uninstall malformed link use: \$ npm uninstall --global $output_package_json_name");
}

1; # because the require or use expects a true return value, this one is
# just a perl idiom but any true value would do
