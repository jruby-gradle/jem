package com.github.jrubygradle.groovygem

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.CompileStatic

/**
 */
@CompileStatic
class Version {
    @JsonProperty
    String version
}
