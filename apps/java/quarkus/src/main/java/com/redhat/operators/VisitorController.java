package com.redhat.operators;

import javax.inject.Inject;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.EventSourceManager;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class VisitorController implements ResourceController<Visitor> {

   @Inject
   MySqlResources mySqlResources;

   @Inject
   BackendResources backendResources;

   @Inject
   FrontendResources frontendResources;

   @Inject
   KubernetesClient client;

    @Override
    public UpdateControl<Visitor> createOrUpdateResource(Visitor resource, Context<Visitor> context) {
   
        // mysql
        mySqlResources.createResources();
        // Backend
        backendResources.createResources(resource);
        //frontend
        frontendResources.createResources(resource);

        resource.setStatus(new VisitorStatus("jdob/visitors-service:1.0.0", "jdob/visitors-webui:1.0.0"));

        return UpdateControl.updateStatusSubResource(resource);
    }

    @Override
    public DeleteControl deleteResource(Visitor resource, Context<Visitor> context) {
        mySqlResources.deleteResources();
        backendResources.deleteResources();
        frontendResources.deleteResources();
        return ResourceController.super.deleteResource(resource, context);
    }

    @Override
    public void init(EventSourceManager eventSourceManager) {
        eventSourceManager.registerEventSource("visitors-service-watcher", ServiceEventSource.create(client));
    }
}
