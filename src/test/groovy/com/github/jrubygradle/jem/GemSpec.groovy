package com.github.jrubygradle.jem

import spock.lang.*

import java.util.zip.GZIPInputStream

class GemSpec extends Specification {
    final String FIXTURES_ROOT = new File(['src', 'test', 'resources'].join(File.separator)).absolutePath
    final String METADATA_FIXTURE = [FIXTURES_ROOT, 'thor-0.19.1', 'metadata'].join(File.separator)

    def "fromFile with an invalid/empty file should return null"() {
        given:
        Gem gem

        when:
        gem = Gem.fromFile(f)

        then:
        !(gem instanceof Gem)

        where:
        f                      | _
        null                   | _
        ''                     | _
        new File('/tmp/spock') | _
    }

    def "fromFile with a valid file path should return a Gem"() {
        given:
        final String path = METADATA_FIXTURE

        expect:
        Gem.fromFile(path) instanceof Gem
    }

    def "fromFile with a valid File object should return a Gem"() {
        given:
        final File f = new File(METADATA_FIXTURE)

        expect:
        Gem.fromFile(f) instanceof Gem
    }

    def "fromFile with a GZIPInputStream should return a Gem"() {
        given:
        Gem gem
        final GZIPInputStream f = new GZIPInputStream(new FileInputStream("${METADATA_FIXTURE}.gz"))

        when:
        gem = Gem.fromFile(f)

        then:
        gem instanceof Gem

        and:
        gem.name == 'thor'
    }

    def "a metadata file should parse into a Gem"() {
        given:
        Gem gem

        when: "a metadata file is deserialized"
        gem = Gem.fromFile(METADATA_FIXTURE)

        then: "it should create an instance"
        gem instanceof Gem

        and: "its version should be correct"
        gem.version.version == '0.19.1'

        and: "its properties should equal what's in the YAML"
        gem."${property}" == value

        where:
        property               | value
        'name'                 | 'thor'
        'platform'             | 'ruby'
        'email'                | 'ruby-thor@googlegroups.com'
        'authors'              | ['Yehuda Katz', 'José Valim']
        'executables'          | ['thor']
        'licenses'             | ['MIT']
        'homepage'             | 'http://whatisthor.com/'
        'rubygemsVersion'      | '2.2.2'
        'specificationVersion' | 4
        'requirePaths'         | ['lib']
    }
}
