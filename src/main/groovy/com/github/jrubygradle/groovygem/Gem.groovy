package com.github.jrubygradle.groovygem

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import groovy.transform.TypeChecked

/**
 * Plain Old Groovy Object for an enumeration of metadata provided by a gem
 */
@TypeChecked
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

    static Gem fromFile(Object metadata) {
        File metadataFile
        ObjectMapper mapper

        if (metadata instanceof String) {
            metadataFile = new File(metadata)
        }
        else if (metadata instanceof File) {
            metadataFile = metadata as File
        }

        if (!(metadataFile?.exists())) {
            return null
        }

        mapper = new ObjectMapper(new YAMLFactory())
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper.readValue(metadataFile, Gem.class)
    }
}
