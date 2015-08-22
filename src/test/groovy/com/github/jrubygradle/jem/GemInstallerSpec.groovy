package com.github.jrubygradle.jem

import spock.lang.*
import java.nio.file.Files
import java.nio.file.Path

import com.github.jrubygradle.jem.internal.GemInstaller as GemInstallerImpl

/**
 * Test the externally facing GemInstaller API
 */
class GemInstallerSpec extends Specification {
    final String FIXTURES_ROOT = new File(['src', 'test', 'resources'].join(File.separator)).absolutePath
    final String GEM_FIXTURE = [FIXTURES_ROOT, 'thor-0.19.1.gem'].join(File.separator)

    GemInstaller installer
    Path installDirPath = Files.createTempDirectory("geminstallerspec")
    String installDir = installDirPath as String

    GemInstallerImpl mockedImpl() {
        return Mock(GemInstallerImpl, constructorArgs: [
                installDir,
                [GEM_FIXTURE]
                ])
    }

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

    def "install() should call our impl with defaults"() {
        given:
        GemInstallerImpl impl = mockedImpl()
        GemInstaller installer = new GemInstaller(impl)
        1 * impl.install()

        expect:
        installer.install()
    }

    def "install(DuplicateBehavior) should pass those attributes on to impl"() {
        given:
        GemInstaller.DuplicateBehavior behavior = GemInstaller.DuplicateBehavior.OVERWRITE
        GemInstallerImpl impl = mockedImpl()
        GemInstaller installer = new GemInstaller(impl)
        1 * impl.install(behavior)

        expect:
        installer.install(behavior)
    }
}
