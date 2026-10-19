package com.spandan.framework.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

/** Reads JSON test data from src/test/resources/testdata into POJOs. */
public final class TestDataReader {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TestDataReader() {
    }

    public static <T> T read(String fileName, Class<T> type) {
        String path = "testdata/" + fileName;
        try (InputStream in = TestDataReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("Test data not found: " + path);
            }
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse " + path, e);
        }
    }
}
