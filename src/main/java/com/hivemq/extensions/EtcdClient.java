package com.hivemq.extensions;

import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.config.ConfigurationReader;
import com.hivemq.extensions.config.EtcdConfig;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.api.RangeResponse;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.ibm.etcd.client.KeyUtils.bs;

public class EtcdClient {
    private ConfigurationReader configurationReader;
    private EtcdConfig etcdConfig;
    // REVIEW: you should separate member i-vars and c-vars into separate sections - generally it's preferred to have the statics first, then the i-vars.
    private static Logger logger = LoggerFactory.getLogger(EtcdClient.class);
    private KvClient client;

    public EtcdClient(@NotNull final ConfigurationReader configurationReader) {
        this.configurationReader = configurationReader;
    }

    public void createOrUpdate() {
        final EtcdConfig newEtcdConfig = configurationReader.readConfiguration();
        if (newEtcdConfig == null) {
            throw new IllegalStateException("Configuration of the Etcd discovery extension couldn't be loaded.");
        }
        this.etcdConfig = newEtcdConfig;
        logger.trace("Loaded configuration successfully.");
        if (client == null) {
            KvStoreClient client;
            if (etcdConfig.getTls()) {
                if (etcdConfig.getCAPath().equals("")) {
                    logger.debug("Using system certificates for certificate verification");
                    client = com.ibm.etcd.client.EtcdClient.forEndpoint(etcdConfig.getEndpoint(), etcdConfig.getPort()).build();
                } else {
                    logger.debug("Using custom CA for certificate verification");
                    try {
                        File file = new File(etcdConfig.getCAPath());
                        ByteSource cert = Files.asByteSource(file);
                        client = com.ibm.etcd.client.EtcdClient.forEndpoint(etcdConfig.getEndpoint(), etcdConfig.getPort()).withCaCert(cert).build();
                    } catch (IOException e) {
                        logger.error("Could not read etcd CA file: "+e);
                        client = null;
                    }
                }
            } else {
                client = com.ibm.etcd.client.EtcdClient.forEndpoint(etcdConfig.getEndpoint(), etcdConfig.getPort()).withPlainText().build();
            }
            this.client = client.getKvClient();
        }
    }

    @NotNull
    public EtcdConfig getEtcdConfig() {
        return etcdConfig;
    }

    public void saveObject(@NotNull final String objectKey, @NotNull final String content) {
        client.put(bs(objectKey), bs(content)).sync();
    }

    public void deleteObject(@NotNull final String objectKey) {
        client.delete(bs(objectKey)).sync();
    }

    public List<String> getObjects(@NotNull final String objectKey) {
        RangeResponse rr = client.get(bs(objectKey)).asPrefix().sync();
        List<String> nodeStrings = new ArrayList<>();
        // REVIEW: you might also want to format the code ;)
        for(int i=0;i<rr.getKvsCount();i++) { // TODO: write this as lambda
            KeyValue kv=rr.getKvs(i);
            nodeStrings.add(kv.getValue().toStringUtf8());
        }
        return nodeStrings;
    }

}
