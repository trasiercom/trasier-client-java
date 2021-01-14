package com.trasier.client.configuration;

import lombok.Data;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Data
public class TrasierFilterConfiguration {

    private Pattern url;
    private Pattern operation;

    public void setUrl(String urlPattern) {
        this.url = toPattern(urlPattern);
    }

    public void setOperation(String operationPattern) {
        this.operation = toPattern(operationPattern);
    }

    private Pattern toPattern(String pattern) {
        if (pattern == null) {
            return null;
        }
        try {
            return Pattern.compile(pattern);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Invalid filter pattern " + pattern, e);
        }
    }

}

