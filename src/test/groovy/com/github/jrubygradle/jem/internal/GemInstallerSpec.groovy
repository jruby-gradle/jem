package com.github.jrubygradle.jem.internal

import com.github.jrubygradle.jem.Gem
import com.github.jrubygradle.jem.Version
import spock.lang.*

import java.nio.file.Files
import java.nio.file.Path
import org.apache.commons.io.FileUtils


class GemInstallerSpec extends Specification {
    static final String FIXTURES_ROOT = new File(['src', 'test', 'resources'].join(File.separator)).absolutePath
    static final String GEM_NAME = 'thor-0.19.1'
    static final String GEM_FILENAME = "${GEM_NAME}.gem"
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

    @Ignore("WIP")
    def "isvalidGem should return true with a given file that is a gem"() {
        given:
        File gem = new File(GEM_FIXTURE)
        installer = new GemInstaller(installDir, [gem])

        expect:
        installer.isValidGem(gem)
    }

    def "gemFullName() should be name-version by default"() {
        given:
        installer = new GemInstaller(installDir, [])
        Gem gem = new Gem()
        gem.name = 'thor'
        gem.version = new Version()
        gem.version.version = '1.0'

        expect:
        installer.gemFullName(gem) == 'thor-1.0'
    }

    def "gemFullName() should be name-version-platform if platform is present"() {
        given:
        installer = new GemInstaller(installDir, [])
        Gem gem = new Gem()
        gem.name = 'thread_safe'
        gem.platform = 'java'
        gem.version = new Version()
        gem.version.version = '1.0'

        expect:
        installer.gemFullName(gem) == 'thread_safe-1.0-java'
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

        then: "the ${installDir}/specifications dir should contain the gemspec"
        File specification = new File(installDir, ['specifications', "${GemInstallerSpec.GEM_NAME}.gemspec"].join(File.separator))
        specification.isFile()
        specification.size() > 0

        and: "the ${installDir}/gems/ directory should contain an extract of data.tar.gz"
        File outputDir = new File(installDir, ['gems', GemInstallerSpec.GEM_NAME].join(File.separator))
        outputDir.isDirectory()
        (new File(outputDir, "thor.gemspec")).isFile()

        and: "the executable should be placed in ${installDir}/bin"
        File binFile = new File(installDir, ['bin', 'thor'].join(File.separator))
        binFile.isFile()
    }
}
