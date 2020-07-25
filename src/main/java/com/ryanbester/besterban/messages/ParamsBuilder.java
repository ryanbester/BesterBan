package com.ryanbester.besterban.messages;

import java.util.HashMap;

/**
 * Assists in passing parameters to a message.
 */
public class ParamsBuilder {

    public HashMap<String, String> params = new HashMap<>();

    /**
     * Creates a new parameters builder object.
     */
    public ParamsBuilder() {

    }

    /**
     * Add a parameter to the list.
     *
     * @param key   The placeholder name.
     * @param value The value of the parameter.
     * @return The parameters builder object so calls can be chained.
     */
    public ParamsBuilder add(String key, String value) {
        params.put(key, value);
        return this;
    }

}
