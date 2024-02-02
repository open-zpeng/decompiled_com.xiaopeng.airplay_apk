package org.fourthline.cling;

import java.io.PrintStream;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.LocalDevice;
import org.fourthline.cling.model.meta.RemoteDevice;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryListener;
/* loaded from: classes.dex */
public class Main {
    public static void main(String[] args) throws Exception {
        RegistryListener listener = new RegistryListener() { // from class: org.fourthline.cling.Main.1
            @Override // org.fourthline.cling.registry.RegistryListener
            public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
                PrintStream printStream = System.out;
                printStream.println("Discovery started: " + device.getDisplayString());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void remoteDeviceDiscoveryFailed(Registry registry, RemoteDevice device, Exception ex) {
                PrintStream printStream = System.out;
                printStream.println("Discovery failed: " + device.getDisplayString() + " => " + ex);
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
                PrintStream printStream = System.out;
                printStream.println("Remote device available: " + device.getDisplayString());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void remoteDeviceUpdated(Registry registry, RemoteDevice device) {
                PrintStream printStream = System.out;
                printStream.println("Remote device updated: " + device.getDisplayString());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
                PrintStream printStream = System.out;
                printStream.println("Remote device removed: " + device.getDisplayString());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void localDeviceAdded(Registry registry, LocalDevice device) {
                PrintStream printStream = System.out;
                printStream.println("Local device added: " + device.getDisplayString());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void localDeviceRemoved(Registry registry, LocalDevice device) {
                PrintStream printStream = System.out;
                printStream.println("Local device removed: " + device.getDisplayString());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void beforeShutdown(Registry registry) {
                PrintStream printStream = System.out;
                printStream.println("Before shutdown, the registry has devices: " + registry.getDevices().size());
            }

            @Override // org.fourthline.cling.registry.RegistryListener
            public void afterShutdown() {
                System.out.println("Shutdown of registry complete!");
            }
        };
        System.out.println("Starting Cling...");
        UpnpService upnpService = new UpnpServiceImpl(listener);
        System.out.println("Sending SEARCH message to all devices...");
        upnpService.getControlPoint().search(new STAllHeader());
        System.out.println("Waiting 10 seconds before shutting down...");
        Thread.sleep(10000L);
        System.out.println("Stopping Cling...");
        upnpService.shutdown();
    }
}
