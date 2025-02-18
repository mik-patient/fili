// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.data.dimension;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Dimension field.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonPropertyOrder({"name", "longName", "description"})
public interface DimensionField {

    /**
     * The name of the dimension field.
     *
     * @return field name
     */
    String getName();

    /**
     * The disply of the dimension field (if different from the name).
     *
     * @return field name
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    default String getLongName() {
        return null;
    }

    /**
     * The description for the dimension field.
     *
     * @return the description
     */
    String getDescription();
}
