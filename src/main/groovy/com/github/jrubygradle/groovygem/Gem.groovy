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
        File metadataFile
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        if (metadata instanceof String) {
            metadataFile = new File(metadata)
        }
        else if (metadata instanceof File) {
            metadataFile = metadata as File
        }
        else if (metadata instanceof InputStream) {
            return mapper.readValue(metadata, Gem)
        }

        if (!(metadataFile?.exists())) {
            return null
        }

        return mapper.readValue(metadataFile, Gem)
    }
}
