package com.github.jrubygradle.groovygem

import com.fasterxml.jackson.annotation.JsonProperty
import groovy.transform.TypeChecked
import sun.org.mozilla.javascript.commonjs.module.Require

/**
 */
@TypeChecked
class Dependency {
    @JsonProperty
    String name

    @JsonProperty
    List<Requirement> requirements
}
