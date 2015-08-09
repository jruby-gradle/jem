package com.github.jrubygradle.groovygem.internal

import spock.lang.*

import java.nio.file.Files
import java.nio.file.Path
import org.apache.commons.io.FileUtils

class GemInstallerSpec extends Specification {
    static final String FIXTURES_ROOT = new File(['src', 'test', 'resources'].join(File.separator)).absolutePath
    static final String GEM_FILENAME = 'thor-0.19.1.gem'
    static final String GEM_FIXTURE = [FIXTURES_ROOT, GEM_FILENAME].join(File.separator)

    GemInstaller installer
    Path installDirPath = Files.createTempDirectory("geminstallerspec")
    String installDir = installDirPath as String

    /** Return true if the given rootDir looks like a Gem home dir */
    boolean isGemHome(String rootDir) {
        ['bin', 'build_info', 'cache', 'doc', 'extensions',
         'gems', 'specifications'].each { String madeDir ->

            File fullPath = new File(installDir, madeDir)

            if ( (!fullPath.exists()) || (!fullPath.isDirectory()) ) {
                return false
            }
        }
        return true
    }

    def "mkdirs() should create the necessary directories for a GEM_HOME"() {
        given:
        installer = new GemInstaller(installDir, [new File(GEM_FIXTURE)])

        when: "mkdirs() is invoked"
        installer.mkdirs()

        then: "the dir should exist as a directory"
        isGemHome(installDir)
    }

    def "isValidGem() should return false if the given file doesn't end in .gem"() {
        given:
        Path fakeGemPath = Files.createTempFile('geminstallerspec', null)
        File fakeGem = new File(fakeGemPath as String)
        installer = new GemInstaller(installDir, [fakeGem])

        expect:
        !installer.isValidGem(fakeGem)
    }

    def "isValidGem() should return false if the given file isn't valid"() {
        given:
        Path fakeGemPath = Files.createTempFile('geminstallerspec', '.gem')
        File fakeGem = new File(fakeGemPath as String)
        installer = new GemInstaller(installDir, [fakeGem])

        expect:
        !installer.isValidGem(fakeGem)
    }

    def "isvalidGem should return true with a given file that is a gem"() {
        given:
        File gem = new File(GEM_FIXTURE)
        installer = new GemInstaller(installDir, [gem])

        expect:
        installer.isValidGem(gem)
    }
}

class GemInstallerIntegrationSpec extends Specification {
    GemInstaller installer

    Path installDirPath = Files.createTempDirectory("geminstallerintegrationspec")
    String installDir = installDirPath as String

    def setup() {
        installer = new GemInstaller(installDir, [new File(GemInstallerSpec.GEM_FIXTURE)])
    }

    def cleanup() {
        File dir = new File(installDir)

        if (dir.exists() && dir.absolutePath.startsWith('/tmp')) {
            FileUtils.deleteDirectory(dir)
        }
    }

    def "install() should actually install my gem in the happy path"() {
        when:
        installer.install()

        then: "the gem should be cached in ${installDir}/cache"
        (new File(installDir, ['cache', GemInstallerSpec.GEM_FILENAME].join(File.separator))).exists()

        and: "the ${installDir}/specifications dir should contain the gemspec"
        (new File(installDir, ['specifications', "${GemInstallerSpec.GEM_FILENAME}spec"].join(File.separator))).exists()
    }
}
