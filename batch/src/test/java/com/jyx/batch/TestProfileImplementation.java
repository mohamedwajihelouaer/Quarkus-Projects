package com.jyx.batch;

import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Map;


public class TestProfileImplementation implements QuarkusTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of(
                "batch.toprocess.dir", "src/test/resources/data/toprocess",
                "batch.processing.dir", "src/test/resources/data/processing"
        );
    }
}