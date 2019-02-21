package com.hivemq.extensions;

import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.extensions.config.ConfigurationReader;
import com.hivemq.extensions.config.EtcdConfig;
import com.ibm.etcd.api.KeyValue;
import com.ibm.etcd.api.RangeResponse;
import com.ibm.etcd.client.KvStoreClient;
import com.ibm.etcd.client.kv.KvClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.ibm.etcd.client.KeyUtils.bs;

public class EtcdClient {
    private ConfigurationReader configurationReader;
    private EtcdConfig etcdConfig;
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
        // TODO:read and use more stuff from the config instead of hardcoding here

        if (client == null) {
            KvStoreClient client = com.ibm.etcd.client.EtcdClient.forEndpoint(etcdConfig.getEndpoint(), etcdConfig.getPort()).withPlainText().build();
            this.client = client.getKvClient();
        }
    }

    @NotNull
    public EtcdConfig getEtcdConfig() {
        return etcdConfig;
    }

    public void saveObject(@NotNull final String objectKey, @NotNull final String content) {
        logger.info("Saving "+objectKey);
        client.put(bs(objectKey), bs(content)).sync();
    }

    public void deleteObject(@NotNull final String objectKey) {
        logger.info("Deleting "+objectKey);
        client.delete(bs(objectKey)).sync();
    }

    public List<String> getObjects(@NotNull final String objectKey) {
        RangeResponse rr = client.get(bs(objectKey)).asPrefix().sync();
        List<String> nodeStrings = new ArrayList<>();
        for(int i=0;i<rr.getKvsCount();i++) { // TODO: write this as lambda
            KeyValue kv=rr.getKvs(i);
            nodeStrings.add(kv.getValue().toStringUtf8());
        }
        return nodeStrings;
    }

}
