package com.github.jrubygradle.groovygem

import groovy.transform.CompileStatic

/**
 * GemInstaller manages the installation of a .gem file into a given directory
 */
@CompileStatic
class GemInstaller {
    GemInstaller(String installDir, String gemPath) {
    }

    GemInstaller(String installDir, File gemFile) {
    }

    GemInstaller(String installDir, List<Object> gemPaths) {
    }
}
