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

package com.hivemq.extensions.callbacks;

import com.google.gson.Gson;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.services.cluster.ClusterDiscoveryCallback;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterDiscoveryInput;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterDiscoveryOutput;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterNodeAddress;
import com.hivemq.extensions.EtcdClient;
import com.hivemq.extensions.config.ClusterNodeEntry;
import com.hivemq.extensions.config.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Limpöck
 * @author Abdullah Imal
 * @author Alwin Ebermann
 * @since 4.0.0
 */
public class EtcdDiscoveryCallback implements ClusterDiscoveryCallback {

    private static final Logger logger = LoggerFactory.getLogger(EtcdDiscoveryCallback.class);

    @NotNull
    EtcdClient etcdClient;

    private ClusterNodeEntry ownNodeEntry;

    public EtcdDiscoveryCallback(@NotNull final ConfigurationReader configurationReader) {
        this.etcdClient = new EtcdClient(configurationReader);
    }

    @Override
    public void init(@NotNull final ClusterDiscoveryInput clusterDiscoveryInput, @NotNull final ClusterDiscoveryOutput clusterDiscoveryOutput) {
        try {
            try {
                etcdClient.createOrUpdate();
            } catch (final Exception ex) {
                logger.error("Configuration of the Etcd discovery extension couldn't be loaded. Skipping initial discovery.");
                return;
            }
            saveOwnInstance(clusterDiscoveryInput.getOwnClusterId(), clusterDiscoveryInput.getOwnAddress());
            clusterDiscoveryOutput.provideCurrentNodes(getNodeAddresses());
        } catch (final Exception ex) {
            logger.error("Initialization of the Etcd discovery callback failed.", ex);
        }
    }

    @Override
    public void reload(@NotNull final ClusterDiscoveryInput clusterDiscoveryInput, @NotNull final ClusterDiscoveryOutput clusterDiscoveryOutput) {
        try {
            try {
                etcdClient.createOrUpdate();
            } catch (final Exception ex) {
                logger.error("Configuration of the Etcd discovery extension couldn't be reloaded. Skipping reload callback.");
                return;
            }
            if (ownNodeEntry == null || ownNodeEntry.isExpired(etcdClient.getEtcdConfig().getFileUpdateIntervalInSeconds())) {
                saveOwnInstance(clusterDiscoveryInput.getOwnClusterId(), clusterDiscoveryInput.getOwnAddress());
            }
            clusterDiscoveryOutput.provideCurrentNodes(getNodeAddresses());
        } catch (final Exception ex) {
            logger.error("Reload of the Etcd discovery callback failed.", ex);
        }
    }

    @Override
    public void destroy(@NotNull final ClusterDiscoveryInput clusterDiscoveryInput) {
        try {
            if (ownNodeEntry != null) {
                removeOwnFile(clusterDiscoveryInput.getOwnClusterId());
            }
        } catch (final Exception ex) {
            logger.error("Destroy of the Etcd discovery callback failed.", ex);
        }
    }

    private void saveOwnInstance(@NotNull final String ownClusterId, @NotNull final ClusterNodeAddress ownAddress) throws Exception {
        ClusterNodeEntry newNodeFile = new ClusterNodeEntry(ownClusterId, ownAddress);

        etcdClient.saveObject(etcdClient.getEtcdConfig().getKey() + ownClusterId, newNodeFile.toString());
        ownNodeEntry = newNodeFile;

        logger.debug("Updated own Etcd entry '{}'.", ownClusterId);
    }

    private void removeOwnFile(@NotNull final String ownClusterId) {
        final String objectKey = etcdClient.getEtcdConfig().getKey() + ownClusterId;

        etcdClient.deleteObject(objectKey);
        ownNodeEntry = null;

        logger.debug("Removed own Etcd entry '{}'.", objectKey);
    }

    @NotNull
    private List<ClusterNodeAddress> getNodeAddresses() {
        final List<ClusterNodeAddress> nodeAddresses = new ArrayList<>();

        final List<ClusterNodeEntry> nodeFiles;
        try {
            nodeFiles = getNodeEntries();
        } catch (final Exception ex) {
            logger.error("Unknown error while reading all node entries.", ex);
            return nodeAddresses;
        }

        for (final ClusterNodeEntry nodeFile : nodeFiles) {

            if (nodeFile.isExpired(etcdClient.getEtcdConfig().getExpirationInSeconds())) {

                logger.debug("Etcd entry of node with clusterId {} is expired. Entry will be deleted.", nodeFile.getClusterId());

                final String objectKey = etcdClient.getEtcdConfig().getKey() + nodeFile.getClusterId();
                etcdClient.deleteObject(objectKey);
            } else {
                nodeAddresses.add(nodeFile.getClusterNodeAddress());
            }
        }

        logger.debug("Found following node addresses with the Etcd extension: {}", nodeAddresses);

        return nodeAddresses;
    }

    @NotNull
    private List<ClusterNodeEntry> getNodeEntries() {
        List<String> objectListing = etcdClient.getObjects(etcdClient.getEtcdConfig().getKey());
        List<ClusterNodeEntry> clusterNodeEntries = new ArrayList<>();
        Gson g = new Gson();
        for (final String nodeString : objectListing) {
            clusterNodeEntries.add(g.fromJson(nodeString, ClusterNodeEntry.class));
        }
        return clusterNodeEntries;

    }
}
