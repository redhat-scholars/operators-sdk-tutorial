package com.redhat.operators;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.javaoperatorsdk.operator.processing.event.AbstractEventSource;

public class ServiceEventSource extends AbstractEventSource implements Watcher<Service> {
    private final KubernetesClient client;

    private ServiceEventSource(KubernetesClient client) {
        this.client = client;
    }

    public static ServiceEventSource create(KubernetesClient client) {
        final var eventSource = new ServiceEventSource(client);
        client.services().withLabel("app","visitors").watch(eventSource);
        return eventSource;
    }

    @Override
    public void eventReceived(Action action, Service service) {
       if(action.name().equals("DELETED")){
           //recreate only if deleted from outside of the operator
       }
    }

    @Override
    public void onClose(WatcherException e) {

    }
}
