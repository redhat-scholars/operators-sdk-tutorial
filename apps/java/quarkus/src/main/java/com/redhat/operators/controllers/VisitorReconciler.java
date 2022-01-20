package com.redhat.operators.controllers;

import static io.javaoperatorsdk.operator.api.reconciler.Constants.WATCH_CURRENT_NAMESPACE;

import java.util.List;

import javax.inject.Inject;
import com.redhat.operators.Visitor;
import com.redhat.operators.VisitorStatus;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceContext;
import io.javaoperatorsdk.operator.api.reconciler.EventSourceInitializer;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.InformerEventSource;
import io.javaoperatorsdk.operator.processing.event.source.informer.Mappers;


@ControllerConfiguration(namespaces = WATCH_CURRENT_NAMESPACE,eventFilters = {CustomFilter.class})
public class VisitorReconciler implements Reconciler<Visitor>, EventSourceInitializer<Visitor> {

   @Inject
   MySqlResources mySqlResources;

   @Inject
   BackendResources backendResources;

   @Inject
   FrontendResources frontendResources;

   @Inject
   KubernetesClient client;

   @Override
    public List<EventSource> prepareEventSources(EventSourceContext<Visitor> context) {
      SharedIndexInformer<Deployment> deploymentInformer =
          client.apps().deployments().inAnyNamespace()
              .withLabel("app.kubernetes.io/managed-by", "visitors-operator")
              .runnableInformer(0);
      SharedIndexInformer<Service> serviceInformer =
      client.services().inAnyNamespace()
              .withLabel("app.kubernetes.io/managed-by", "visitors-operator")
              .runnableInformer(0);
  
      return List.of(new InformerEventSource<>(
          deploymentInformer, Mappers.fromOwnerReference()),
          new InformerEventSource<>(serviceInformer, Mappers.fromOwnerReference()));
    }

    @Override
    public UpdateControl<Visitor> reconcile(Visitor resource, Context context) {
   
        // mysql
        mySqlResources.createResources(resource);
        // Backend 
       backendResources.createResources(resource);
        //frontend
        frontendResources.createResources(resource);

        resource.setStatus(new VisitorStatus("jdob/visitors-service:1.0.0", "jdob/visitors-webui:1.0.0"));

        return context.getSecondaryResource(Deployment.class)
                .map(Deployment -> {
                    System.out.println("In the deployment update");
                    backendResources.createResources(resource);
                    return UpdateControl.updateResource(resource);
                })
                .orElse(UpdateControl.updateResource(resource));
    }

    
}
