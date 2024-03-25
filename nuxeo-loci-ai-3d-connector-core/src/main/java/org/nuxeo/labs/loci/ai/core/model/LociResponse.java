package org.nuxeo.labs.loci.ai.core.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LociResponse {

    public String filename;

    public List<String> tags;

    public LociResponse() {};

    public LociResponse(String filename, List<String> tags) {
        this.filename = filename;
        this.tags = tags;
    }

}
