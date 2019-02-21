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

import com.google.gson.Gson;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extension.sdk.api.annotations.Nullable;
import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterNodeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Abdullah Imal
 * @author Alwin Ebermann
 * @since 4.0.0
 */
public class ClusterNodeEntry {

    private final String clusterId;
    private final String clusterNodeIP;
    private final int clusterNodePort;
    private final long creationTimeInMillis;

    public ClusterNodeEntry(@NotNull final String clusterId, @NotNull final ClusterNodeAddress clusterNodeAddress) {
        if (clusterId == null) {
            throw new NullPointerException("ClusterId must not be null!");
        }
        if (clusterId.isBlank()) {
            throw new IllegalArgumentException("ClusterId must not empty!");
        }
        if (clusterNodeAddress == null) {
            throw new NullPointerException("ClusterNodeAddress must not be null!");
        }

        this.clusterId = clusterId;
        this.clusterNodeIP = clusterNodeAddress.getHost();
        this.clusterNodePort = clusterNodeAddress.getPort();
        this.creationTimeInMillis = System.currentTimeMillis();
    }

    @Nullable
    public static ClusterNodeEntry parseClusterNodeEntry(@NotNull final String entryContent) {
        if (entryContent == null) {
            throw new NullPointerException("EntryContent must not be null!");
        }
        if (entryContent.isBlank()) {
            throw new IllegalArgumentException("EntryContent must not be empty!");
        }

        Gson g = new Gson();
        return g.fromJson(entryContent, ClusterNodeEntry.class);
    }

    @NotNull
    public String getClusterId() {
        return clusterId;
    }

    @NotNull
    public ClusterNodeAddress getClusterNodeAddress() {
        return new ClusterNodeAddress(clusterNodeIP, clusterNodePort);
    }

    public boolean isExpired(final long expirationInSeconds) {
        // 0 = deactivated
        if (expirationInSeconds == 0) {
            return false;
        }
        final long creationPlusExpirationInMillis = creationTimeInMillis + (expirationInSeconds * 1_000);
        return creationPlusExpirationInMillis < System.currentTimeMillis();
    }

    @Override
    public String toString() {
        Gson g = new Gson();
        return g.toJson(this);
    }

}
