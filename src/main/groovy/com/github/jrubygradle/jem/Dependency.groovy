package com.github.jrubygradle.jem

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

/**
 */
@TypeChecked
@CompileStatic
class Dependency {
    @JsonProperty
    String name

    @JsonProperty
    List<Requirement> requirements
}
