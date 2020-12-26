package com.github.starnowski.posmulten.configuration.yaml.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Getter
@Setter
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RLSPolicy {
    //    createRLSPolicyForTable(String table, Map<String, String> primaryKeyColumnsList, String tenantColumnName, String rlsPolicyName)
    @JsonProperty(value = "name", required = true)
    private String name;
}