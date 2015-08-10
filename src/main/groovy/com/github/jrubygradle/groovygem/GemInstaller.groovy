package com.github.jrubygradle.groovygem

import groovy.transform.CompileStatic

import com.github.jrubygradle.groovygem.internal.GemInstaller as GemInstallerImpl

/**
 * GemInstaller manages the installation of a .gem file into a given directory
 */
@CompileStatic
class GemInstaller {
    static enum DuplicateBehavior {
        OVERWRITE,
        SKIP,
        FAIL
    }


    protected GemInstallerImpl impl

    GemInstaller(String installDir, String gemPath) {
        this(installDir, new File(gemPath))
    }

    GemInstaller(String installDir, File gemFile) {
        this(installDir, [gemFile])
    }

    GemInstaller(String installDir, List<File> gemPaths) {
        impl = new GemInstallerImpl(installDir, gemPaths)
    }

    void install() {
        impl.install()
    }
}
