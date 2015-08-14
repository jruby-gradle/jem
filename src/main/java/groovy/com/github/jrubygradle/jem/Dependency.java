package com.github.jrubygradle.jem;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 */
public class Dependency {
    @JsonProperty
    public String name;

    @JsonProperty
    public List<Requirement> requirements;
}
