/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.extensions.config;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.parameter.ExtensionInformation;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Abdullah Imal
 * @author Alwin Ebermann
 * @since 4.0.0
 */
public class ConfigurationReader {

    public static final String ETCD_CONFIG_FILE = "EtcdDiscovery.properties";

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationReader.class);

    private final File extensionHomeFolder;

    public ConfigurationReader(@NotNull final ExtensionInformation extensionInformation) {
        this.extensionHomeFolder = extensionInformation.getExtensionHomeFolder();
    }

    @Nullable
    public EtcdConfig readConfiguration() {
        final File propertiesFile = new File(extensionHomeFolder, ETCD_CONFIG_FILE);

        if (!propertiesFile.exists()) {
            logger.error("Could not find '{}'. Please verify that the properties file is located under '{}'.", ETCD_CONFIG_FILE, extensionHomeFolder);
            return null;
        }

        if (!propertiesFile.canRead()) {
            logger.error("Could not read '{}'. Please verify that the user running HiveMQ has reading permissions for it.", propertiesFile.getAbsolutePath());
            return null;
        }

        Properties properties = null;
        try (final InputStream inputStream = new FileInputStream(propertiesFile)) {

            logger.debug("Reading properties file '{}'.", propertiesFile.getAbsolutePath());
            properties = new Properties();
            properties.load(inputStream);
        } catch (final IOException ex) {
            logger.error("An error occurred while reading the properties file {}", propertiesFile.getAbsolutePath(), ex);
        }

        EtcdConfig etcdConfig = ConfigFactory.create(EtcdConfig.class, System.getenv(), properties);
        if (!isValid(etcdConfig)) {
            logger.error("Configuration of the Etcd Discovery extension is not valid!");
            return null;
        }
        logger.trace("Read properties file '{}' successfully.", propertiesFile.getAbsolutePath());

        if (! etcdConfig.getKey().endsWith("/")) {
            etcdConfig.setProperty("key", etcdConfig.getKey() + "/");
        }

        return etcdConfig;
    }

    private boolean isValid(@NotNull final EtcdConfig etcdConfig) {
        final String key = etcdConfig.getKey();
        if (key == null || key.isBlank()) {
            logger.error("Etcd Discovery Extension - Key name is empty!");
            return false;
        }

        final Long fileExpirationInSeconds;
        try {
            fileExpirationInSeconds = etcdConfig.getExpirationInSeconds();
        } catch (final UnsupportedOperationException ex) {
            logger.error("Etcd Discovery Extension - File expiration interval is not set!");
            return false;
        }
        if (fileExpirationInSeconds < 0) {
            logger.error("Etcd Discovery Extension - File expiration interval is negative!");
            return false;
        }

        final Long fileUpdateIntervalInSeconds;
        try {
            fileUpdateIntervalInSeconds = etcdConfig.getFileUpdateIntervalInSeconds();
        } catch (final UnsupportedOperationException ex) {
            logger.error("Etcd Discovery Extension - File update interval is not set!");
            return false;
        }
        if (fileUpdateIntervalInSeconds < 0) {
            logger.error("Etcd Discovery Extension - File update interval is negative!");
            return false;
        }

        if (!(fileUpdateIntervalInSeconds == 0 && fileExpirationInSeconds == 0)) {

            if (fileUpdateIntervalInSeconds.equals(fileExpirationInSeconds)) {
                logger.error("Etcd Discovery Extension - File update interval is the same as the expiration interval!");
                return false;
            }

            if (fileUpdateIntervalInSeconds == 0) {
                logger.error("Etcd Discovery Extension - File update interval is deactivated but expiration is set!");
                return false;
            }

            if (fileExpirationInSeconds == 0) {
                logger.error("Etcd Discovery Extension - File expiration is deactivated but update interval is set!");
                return false;
            }

            if (!(fileUpdateIntervalInSeconds < fileExpirationInSeconds)) {
                logger.error("Etcd Discovery Extension - File update interval is larger than expiration interval!");
                return false;
            }
        }

        return true;
    }
}