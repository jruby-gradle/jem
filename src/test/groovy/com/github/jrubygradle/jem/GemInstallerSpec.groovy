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
    final String RACK_FIXTURE = [FIXTURES_ROOT, 'rack-1.6.4.gem'].join(File.separator)

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
        installer = new GemInstaller(impl)
        1 * impl.install(behavior)

        expect:
        installer.install(behavior)
    }

    @Issue("https://github.com/jruby-gradle/jem/issues/6")
    def "install() with a non-existent gem should call the callback with a failure"() {
        given:
        boolean calledBack = false
        installer = new GemInstaller(installDir, [new File('foo.gem')])

        when:
        installer.install(new GemInstallEvent() {
            @Override
            public boolean onInstall(GemInstallResult result) {
                calledBack = (result.type == GemInstallResult.Type.FAILURE)
                return true;
            }
        })

        then:
        calledBack
    }

    @Issue("https://github.com/jruby-gradle/jem/issues/6")
    def "install() with a valid gem should call the callback with a success"() {
        given:
        boolean calledBack = false
        installer = new GemInstaller(installDir, new File(GEM_FIXTURE));

        when:
        installer.install(new GemInstallEvent() {
            @Override
            public boolean onInstall(GemInstallResult result) {
                calledBack = (result.type == GemInstallResult.Type.SUCCESS)
                return true;
            }
        })

        then:
        calledBack
    }

    @Issue("https://github.com/jruby-gradle/jem/issues/6")
    def "install() with should stop with a false return"() {
        given:
        int calledBack = 0
        installer = new GemInstaller(installDir, [new File(GEM_FIXTURE),
                                                  new File(RACK_FIXTURE)])

        when:
        installer.install(new GemInstallEvent() {
            @Override
            public boolean onInstall(GemInstallResult result) {
                calledBack += 1
                return false;
            }
        })

        then: 'the first gem should be installed'
        (new File(installDir, 'specifications/thor-0.19.1.gemspec')).exists()

        and: 'we should have been called back once'
        calledBack == 1

        and: 'the second gem should not be installed'
        !(new File(installDir, 'specifications/rack-1.6.4.gemspec')).exists()

    }
}
