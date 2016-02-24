package com.minecade.deepend.resources;

import com.minecade.deepend.object.ObjectGetter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DeependBundle implements ObjectGetter<String, String> {

    @Getter(AccessLevel.PRIVATE)
    private final Map<String, String> properties;

    public DeependBundle(String propertyFile) {
        this(propertyFile, false, DefaultBuilder.create());
    }

    final static File folder;
    static {
        folder = new File("./configs");
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new RuntimeException("Couldn't create ./configs, please do it manually!");
            }
        }
    }

    public DeependBundle(@NonNull String propertyFile, boolean hasToExist, DefaultBuilder builder) {
        File file = new File("./configs/" + propertyFile + ".deepend");
        this.properties = new HashMap<>();
        this.properties.putAll(builder.compiled);
        if (!file.exists()) {
            if (hasToExist) {
                throw new RuntimeException("Missing property file: " + file.getName());
            }
            try {
                if (file.createNewFile()) {
                    try (FileWriter writer = new FileWriter(file)) {
                        try (BufferedWriter bWriter = new BufferedWriter(writer)) {
                            this.properties.forEach((k, v) -> {
                                try {
                                    bWriter.write(k + ": " + v + "\n");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch(Exception ee) {
                        ee.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(file)) {
                try (BufferedReader bReader = new BufferedReader(reader)) {
                    String line;
                    while ((line = bReader.readLine()) != null) {
                        String[] parts = line.split(": ");
                        if (parts.length < 2) {
                            continue;
                        }
                        parts[0] = parts[0].replaceAll("\\s+", "");
                        properties.put(parts[0], parts[1]);
                    }
                } catch(final Exception ee) {
                    ee.printStackTrace();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public DeependBundle(String clientStrings, boolean b) {
        this(clientStrings, b, DefaultBuilder.create());
    }

    @Override
    public String get(@NonNull String key) {
        if (properties.containsKey(key)) {
            return properties.get(key);
        }
        return "";
    }

    @Override
    public boolean containsKey(String s) {
        return properties.containsKey(s);
    }

    public static class DefaultBuilder {

        private final Map<String, String> compiled = new HashMap<>();
        private final Map<String, String> defaults = new HashMap<>();

        public DefaultBuilder add(String key, Object value) {
            this.defaults.put(key, value.toString());
            return this;
        }

        public DefaultBuilder build() {
            compiled.putAll(defaults);
            defaults.clear();
            return this;
        }

        public static DefaultBuilder create() {
            return new DefaultBuilder();
        }
    }
}