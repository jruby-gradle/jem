package com.github.jrubygradle.groovygem

import groovy.transform.CompileStatic

/**
 * GemInstaller manages the installation of a .gem file into a given directory
 */
@CompileStatic
class GemInstaller {
    protected File installDirectory
    protected List<File> gems

    GemInstaller(String installDir, String gemPath) {
        this(installDir, new File(gemPath))
    }

    GemInstaller(String installDir, File gemFile) {
        this(installDir, [gemFile])
    }

    GemInstaller(String installDir, List<File> gemPaths) {
        this.installDirectory = new File(installDir)
        this.gems = gemPaths
    }
}
