package com.github.jrubygradle.jem

import spock.lang.*
import java.nio.file.Files
import java.nio.file.Path

/**
 */
class GemInstallerSpec extends Specification {
    final String FIXTURES_ROOT = new File(['src', 'test', 'resources'].join(File.separator)).absolutePath
    final String GEM_FIXTURE = [FIXTURES_ROOT, 'thor-0.19.1.gem'].join(File.separator)

    GemInstaller installer
    Path installDirPath = Files.createTempDirectory("geminstallerspec")
    String installDir = installDirPath as String

    def "ctor should take a dir and single path"() {
        when:
        installer = new GemInstaller(installDir, GEM_FIXTURE)

        then:
        installer instanceof GemInstaller
    }

    def "ctor should take a dir and single File"() {
        when:
        installer = new GemInstaller(installDir, new File(GEM_FIXTURE))

        then:
        installer instanceof GemInstaller
    }

    def "ctor should take a dir and a list of paths"() {
        when:
        installer = new GemInstaller(installDir, [GEM_FIXTURE])

        then:
        installer instanceof GemInstaller
    }

    def "ctor should take a dir and a list of Files"() {
        when:
        installer = new GemInstaller(installDir, [new File(GEM_FIXTURE)])

        then:
        installer instanceof GemInstaller
    }
}
