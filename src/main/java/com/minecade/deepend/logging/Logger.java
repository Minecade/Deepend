/*
 * Copyright 2016 Minecade
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.minecade.deepend.logging;

import com.minecade.deepend.data.DataObject;
import com.minecade.deepend.resources.DeependBundle;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Deepend logger
 *
 * @author Citymonstret
 */
public class Logger {

    public static Class<? extends LogHandler<Logger, String>> logHandler = Log4jLogHandler.class;

    private static Logger unknownLoggerDefault = null;

    private static Map<String, Logger> loggerMap;

    static {
        loggerMap = new ConcurrentHashMap<>();
    }

    /**
     * Get the logger instance
     * @return Logger instance (there's only one)
     */
    @SneakyThrows
    public static Logger get() {
        Logger logger = unknownLoggerDefault;
        if (logger == null) {
            throw new RuntimeException("Cannot access logger before creation");
        }
        return logger;
    }

    public static Logger get(String name) {
        return loggerMap.get(name);
    }

    /**
     * Setup the logger
     * @param name Logger Name
     * @param resourceBundle Logger translation bundle
     */
    public static void setup(String name, DeependBundle resourceBundle) {
        loggerMap.put(name, new Logger(name, resourceBundle));
        if (unknownLoggerDefault == null) {
            unknownLoggerDefault = loggerMap.get(name);
        }
    }

    private final DeependBundle resourceBundle;
    private final LogHandler<Logger, String> logger;

    private boolean debugMode;

    @SneakyThrows
    protected Logger(@NonNull String name, DeependBundle resourceBundle) {
        try {
            this.logger = logHandler.getConstructor(String.class, Logger.class).newInstance(name, this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw e;
        }
        this.resourceBundle = resourceBundle;
        this.debugMode = true;
    }

    /**
     * Send a info message
     * @see Logger#info(String)
     * @param message Message
     * @return The instance
     */
    public Logger info(String message) {
        logger.info(getMessage(message));
        return this;
    }

    public Logger info(String message, Object ... replacements) {
        String[] transformed = new String[replacements.length];
        for (int i = 0; i < replacements.length; i++) {
            transformed[i] = replacements[i].toString();
        }
        logger.info(String.format(getMessage(message), transformed));
        return this;
    }

    private String getMessage(String message) {
        String m = message;
        if (this.resourceBundle != null && this.resourceBundle.containsKey(message)) {
            m = this.resourceBundle.get(message);
        }
        return m;
    }

    /**
     * Will dump the data object
     * @see #info(String)
     * @param o Object to dump
     */
    public void dump(DataObject o) {
        this.info("\"" + o.getName() + "\":" + "\"" + o.getValue() + "\"");
    }

    /**
     * Will send an error message
     * @param message Error Message
     * @return this
     */
    public Logger error(String message) {
        return error(message, null);
    }

    /**
     * Toggle debug messages
     * @param b True | False
     */
    public void setDebugMode(boolean b) {
        this.debugMode = b;
    }

    /**
     * Send a debug message
     * @param message Message
     * @return this
     */
    public Logger debug(String message) {
        // logger.warning(message);
        logger.debug(getMessage(message));
        return this;
    }

    /**
     * Dump the contents of a map
     * @see #dump(DataObject)
     * @param map Map to dump
     * @return this
     */
    public Logger dump(@NonNull Map<?,?> map) {
        map.forEach(this::dump);
        return this;
    }

    public Logger dump(Object key, Object val) {
        this.dump(new DataObject(key.toString(), val.toString()));
        return this;
    }

    /**
     * Send an error message
     * @param message Error Message
     * @param cause The cause
     * @return this
     */
    public Logger error(String message, Throwable cause) {
        // logger.severe(message);
        logger.error(getMessage(message));
        if (debugMode && cause != null) {
            cause.printStackTrace();
        }
        return this;
    }
}
