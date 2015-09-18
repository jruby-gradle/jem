package com.github.jrubygradle.jem;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * GemInstaller manages the installation of a .gem file into a given directory
 */
public class GemInstaller {
    public static enum DuplicateBehavior {
        OVERWRITE,
        SKIP,
        FAIL
    };

    protected com.github.jrubygradle.jem.internal.GemInstaller impl;

    /**
     * Create an installer with the given installation directory and a single gem
     *
     * @param installDir relative or absoluate path to the installation destination directory
     * @param gemPath relative or absolute path to the gem to be installed
     */
    public GemInstaller(String installDir, String gemPath) {
        this(installDir, new File(gemPath));
    }

    /**
     * Create an installer with the given installation directory and a single gem
     *
     * @param installDir relative or absoluate path to the installation destination directory
     * @param gemFile File object for the gem to be installed
     */
    public GemInstaller(String installDir, File gemFile) {
        this(installDir, Arrays.asList(gemFile));
    }

    /**
     * Create an installer with the given installation directory and a list of gems
     *
     * @param installDir relative or absoluate path to the installation destination directory
     * @param gemPaths List of File objects for gems to be installed
     */
    public GemInstaller(String installDir, List<File> gemPaths) {
        impl = new com.github.jrubygradle.jem.internal.GemInstaller(installDir, gemPaths);
    }

    /**
     * Inject a custom GemInstaller implementation that adheres to the private internal API.
     *
     * Chances are you do not want this constructor, which is largely used for unit testing!
     *
     * @param implementation An internal GemInstaller implementation to use for the public API
     */
    public GemInstaller(com.github.jrubygradle.jem.internal.GemInstaller implementation) {
        impl = implementation;
    }

    /**
     * Install the gems in the configured installation directory with all
     * the default settings
     */
    public void install() {
        impl.install();
    }

    /**
     * Install the gems in the configuration installation directory with the given
     * overwrite setting
     *
     * @param overwriteBehavior Flag to determine what behavior should be taken on overwriting existing gems
     */
    public void install(DuplicateBehavior overwriteBehavior) {
        impl.install(overwriteBehavior);
    }
}
