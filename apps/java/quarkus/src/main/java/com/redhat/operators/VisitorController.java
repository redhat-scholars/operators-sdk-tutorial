package com.redhat.operators;

import javax.inject.Inject;

import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class VisitorController implements ResourceController<Visitor> {

   @Inject
   MySqlResources mySqlResources;

   @Inject
   BackendResources backendResources;

   @Inject
   FrontendResources frontendResources;

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
}
