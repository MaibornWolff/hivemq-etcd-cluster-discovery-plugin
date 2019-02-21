package com.hivemq.extensions;

import com.hivemq.extension.sdk.api.parameter.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class EtcdDiscoveryExtensionMainTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock
    public ExtensionStartInput extensionStartInput;
    @Mock
    public ExtensionStartOutput extensionStartOutput;
    @Mock
    public ExtensionStopInput extensionStopInput;
    @Mock
    public ExtensionStopOutput extensionStopOutput;
    @Mock
    public ExtensionInformation extensionInformation;

    private EtcdDiscoveryExtensionMain etcdDiscoveryExtensionMain;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(extensionStartInput.getExtensionInformation()).thenReturn(extensionInformation);
        when(extensionInformation.getExtensionHomeFolder()).thenReturn(temporaryFolder.getRoot());
        etcdDiscoveryExtensionMain = new EtcdDiscoveryExtensionMain();
    }

    @Test
    public void test_start_success() {
        etcdDiscoveryExtensionMain.extensionStart(extensionStartInput, extensionStartOutput);
        Assert.assertNotNull(etcdDiscoveryExtensionMain.etcdDiscoveryCallback);
    }

    @Test
    public void test_start_failed() {
        when(extensionInformation.getExtensionHomeFolder()).thenThrow(new NullPointerException());
        etcdDiscoveryExtensionMain.extensionStart(extensionStartInput, extensionStartOutput);
        Assert.assertNull(etcdDiscoveryExtensionMain.etcdDiscoveryCallback);
    }

    @Test(expected = RuntimeException.class)
    public void test_stop_success() {
        etcdDiscoveryExtensionMain.extensionStart(extensionStartInput, extensionStartOutput);
        etcdDiscoveryExtensionMain.extensionStop(extensionStopInput, extensionStopOutput);
    }

    @Test
    public void test_stop_no_start_failed() {
        when(extensionInformation.getExtensionHomeFolder()).thenThrow(new NullPointerException());
        etcdDiscoveryExtensionMain.extensionStart(extensionStartInput, extensionStartOutput);
        etcdDiscoveryExtensionMain.extensionStop(extensionStopInput, extensionStopOutput);
        Assert.assertNull(etcdDiscoveryExtensionMain.etcdDiscoveryCallback);
    }
}