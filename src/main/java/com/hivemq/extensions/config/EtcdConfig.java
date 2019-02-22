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
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Mutable;

/**
 * @author Abdullah Imal
 * @author Alwin Ebermann
 * @since 4.0.0
 */
public interface EtcdConfig extends Config, Mutable {

    @Key("HIVEMQ_ETCD_KEY")
    @DefaultValue("/hivemq/discovery")
    String keyEnv();

    @Key("HIVEMQ_ETCD_PORT")
    @DefaultValue("2379")
    int portEnv();

    @Key("HIVEMQ_ETCD_UPDATE_INTERVAL")
    @DefaultValue("180")
    int updateEnv();

    @Key("HIVEMQ_ETCD_EXPIRATION")
    @DefaultValue("360")
    int expEnv();

    @Key("HIVEMQ_ETCD_USE_TLS")
    @DefaultValue("false")
    String tlsEnv();

    @Key("HIVEMQ_ETCD_CA_PATH")
    @DefaultValue("")
    String caPathEnv();

    @Key("key")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_KEY}")
    String getKey();

    @Key("expiration")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_EXPIRATION}")
    Long getExpirationInSeconds();

    @Key("update-interval")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_UPDATE_INTERVAL}")
    Long getFileUpdateIntervalInSeconds();

    @Key("etcd-endpoint")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_ENDPOINT}")
    String getEndpoint();

    @Key("etcd-port")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_PORT}")
    int getPort();

    @Key("use-tls")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_USE_TLS}")
    boolean getTls();

    @Key("ca-path")
    @NotNull
    @DefaultValue("${HIVEMQ_ETCD_CA_PATH}")
    String getCAPath();

}
