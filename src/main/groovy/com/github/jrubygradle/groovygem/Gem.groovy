package com.github.jrubygradle.groovygem

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 * Plain Old Groovy Object for an enumeration of metadata provided by a gem
 */
@TypeChecked
@CompileStatic
class Gem {
    @JsonProperty
    String name

    @JsonProperty
    Version version

    @JsonProperty
    String description

    @JsonProperty
    String platform

    @JsonProperty
    String email

    @JsonProperty
    String homepage

    @JsonProperty
    List<String> authors = []

    @JsonProperty
    List<String> files

    @JsonProperty(value='test_files')
    List<String> testFiles

    @JsonProperty
    List<String> executables

    @JsonProperty(value='require_paths')
    List<String> requirePaths

    @JsonProperty
    List<String> licenses

    @JsonProperty(value='specification_version')
    Integer specificationVersion

    @JsonProperty(value='rubygems_version')
    String rubygemsVersion

    /**
     * Take the given argument and produce a {@code Gem} instance
     *
     * @param metadata a {@code java.lang.String}, a {@code java.io.File} or a {@code java.util.zip.GZIPInputStream}
     * @return
     */
    static Gem fromFile(Object metadata) {
        if (metadata instanceof String) {
            return createGemFromFile(new File(metadata))
        }
        if (metadata instanceof File) {
            return createGemFromFile(metadata as File)
        }
        if (metadata instanceof InputStream) {
            return createGemFromInputStream(metadata as InputStream)
        }

        return null
    }

    private static Gem createGemFromFile(File gemMetadataFile) {
        if (!gemMetadataFile.exists()) {
            return null
        }
        return getYamlMapper().readValue(gemMetadataFile, Gem)
    }

    private static Gem createGemFromInputStream(InputStream gemMetadataStream) {
        return getYamlMapper().readValue(gemMetadataStream, Gem)
    }

    private static ObjectMapper getYamlMapper() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper
    }
}
