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

package com.hivemq.extensions;

import com.hivemq.extension.sdk.api.ExtensionMain;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStartOutput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopInput;
import com.hivemq.extension.sdk.api.parameter.ExtensionStopOutput;
import com.hivemq.extension.sdk.api.services.Services;
import com.hivemq.extensions.callbacks.EtcdDiscoveryCallback;
import com.hivemq.extensions.config.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Florian Limpöck
 * @author Abdullah Imal
 * @author Alwin Ebermann
 * @since 4.0.0
 */
public class EtcdDiscoveryExtensionMain implements ExtensionMain {

    private static final Logger logger = LoggerFactory.getLogger(EtcdDiscoveryExtensionMain.class);

    // REVIEW: why package scoped? Could this be private?
    EtcdDiscoveryCallback etcdDiscoveryCallback;

    @Override
    public void extensionStart(@NotNull final ExtensionStartInput extensionStartInput, @NotNull final ExtensionStartOutput extensionStartOutput) {
        try {
            final ConfigurationReader configurationReader = new ConfigurationReader(extensionStartInput.getExtensionInformation());

            etcdDiscoveryCallback = new EtcdDiscoveryCallback(configurationReader);

            Services.clusterService().addDiscoveryCallback(etcdDiscoveryCallback);

            logger.debug("Registered Etcd discovery callback successfully.");
        } catch (final Exception ex) {
            logger.error("Not able to start Etcd Discovery Extension.", ex);
            extensionStartOutput.preventExtensionStartup("Exception caught at extension start.");
        }
    }

    @Override
    public void extensionStop(@NotNull final ExtensionStopInput extensionStopInput, @NotNull final ExtensionStopOutput extensionStopOutput) {

        if (etcdDiscoveryCallback != null) {
            Services.clusterService().removeDiscoveryCallback(etcdDiscoveryCallback);
        }
    }
}
