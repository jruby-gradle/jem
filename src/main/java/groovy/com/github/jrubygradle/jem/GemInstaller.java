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

    public GemInstaller(String installDir, String gemPath) {
        this(installDir, new File(gemPath));
    }

    public GemInstaller(String installDir, File gemFile) {
        this(installDir, Arrays.asList(gemFile));
    }

    public GemInstaller(String installDir, List<File> gemPaths) {
        impl = new com.github.jrubygradle.jem.internal.GemInstaller(installDir, gemPaths);
    }

    public void install() {
        impl.install();
    }
}
