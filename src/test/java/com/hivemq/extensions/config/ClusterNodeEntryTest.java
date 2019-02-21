package com.hivemq.extensions.config;

import com.hivemq.extension.sdk.api.services.cluster.parameter.ClusterNodeAddress;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ClusterNodeEntryTest {

    private final ClusterNodeAddress clusterNodeAddress = new ClusterNodeAddress("127.0.0.1", 7800);
    private final String nodeId = "ABCD12";

    @Test
    public void test_cluster_node_file_successful_create() {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        Assert.assertNotNull(clusterNodeEntry);
    }

    @Test
    public void test_cluster_node_file_successful_get_node_address() {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        Assert.assertNotNull(clusterNodeEntry.getClusterNodeAddress());
    }

    @Test
    public void test_cluster_node_file_successful_get_cluster_id() {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        Assert.assertNotNull(clusterNodeEntry.getClusterId());
    }

    @Test
    public void test_cluster_node_file_equals() {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        final String clusterNodeFileString = clusterNodeEntry.toString();
        final ClusterNodeEntry newClusterNodeEntry = ClusterNodeEntry.parseClusterNodeEntry(clusterNodeFileString);
        Assert.assertTrue(clusterNodeEntry.toString().contentEquals(newClusterNodeEntry.toString()));
    }

    @Test
    public void test_cluster_node_file_not_equal() {
        final ClusterNodeEntry clusterNodeEntry1 = new ClusterNodeEntry(nodeId + 1, clusterNodeAddress);
        final ClusterNodeEntry clusterNodeEntry2 = new ClusterNodeEntry(nodeId + 2, clusterNodeAddress);
        Assert.assertFalse(clusterNodeEntry1.toString().contentEquals(clusterNodeEntry2.toString()));
    }

    @Test(expected = NullPointerException.class)
    public void test_cluster_node_file_nodeId_null() {
        new ClusterNodeEntry(null, clusterNodeAddress);
    }


    @Test(expected = IllegalArgumentException.class)
    public void test_cluster_node_file_nodeId_blank() {
        new ClusterNodeEntry(" ", clusterNodeAddress);
    }

    @Test(expected = NullPointerException.class)
    public void test_cluster_node_file_cluster_node_address_null() {
        new ClusterNodeEntry(nodeId, null);
    }

    @Test
    public void test_cluster_node_file_expiration_deactivated() {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        Assert.assertFalse(clusterNodeEntry.isExpired(0));
    }

    @Test
    public void test_cluster_node_file_expired() throws Exception {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        TimeUnit.SECONDS.sleep(2);
        Assert.assertTrue(clusterNodeEntry.isExpired(1));
    }

    @Test
    public void test_cluster_node_file_not_expired() {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        Assert.assertFalse(clusterNodeEntry.isExpired(1));
    }

    @Test
    public void test_cluster_node_file_not_expired_sleep() throws Exception {
        final ClusterNodeEntry clusterNodeEntry = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        TimeUnit.SECONDS.sleep(1);
        Assert.assertFalse(clusterNodeEntry.isExpired(2));
    }

    @Test
    public void test_parseClusterNodeFile_success() {
        final ClusterNodeEntry clusterNodeEntry1 = new ClusterNodeEntry(nodeId, clusterNodeAddress);
        final String clusterNodeFile1String = clusterNodeEntry1.toString();
        final ClusterNodeEntry clusterNodeEntry2 = ClusterNodeEntry.parseClusterNodeEntry(clusterNodeFile1String);
        Assert.assertTrue(clusterNodeEntry1.toString().contentEquals(clusterNodeEntry2.toString()));
    }

    @Test(expected = NullPointerException.class)
    public void test_parseClusterNodeFile_null() {
        ClusterNodeEntry.parseClusterNodeEntry(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_parseClusterNodeFile_blank() {
        ClusterNodeEntry.parseClusterNodeEntry("  ");
    }

}