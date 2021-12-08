package com.redhat.operators;

import java.util.Optional;

import javax.inject.Inject;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;

public abstract class AbstractResources {
    
    @Inject
    KubernetesClient client;

    protected Optional<Deployment> checkDeploymentExists(String name){
     return Optional.ofNullable(client.apps().deployments().inNamespace(client.getNamespace()).withName(name).get());
    }

    protected Optional<Service> checkServiceExists(String name) {
        return Optional.ofNullable(client.services().inNamespace(client.getNamespace()).withName(name).get());
    }

    protected void deleteDeployment(String name){
        client.apps().deployments().inNamespace(client.getNamespace()).withName(name).delete();
    }

    protected void deleteService(String name){
        client.services().inNamespace(client.getNamespace()).withName(name).delete();
    }

    abstract public void deleteResources();
}
