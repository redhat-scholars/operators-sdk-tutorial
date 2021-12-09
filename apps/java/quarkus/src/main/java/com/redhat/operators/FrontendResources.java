package com.redhat.operators;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

@ApplicationScoped
public class FrontendResources extends AbstractResources{


    public void createResources(Visitor visitor) {
        createFrontendDeployment(visitor);
        createFrontendService();
    }

    private void createFrontendDeployment(Visitor visitor) {
        Optional<Deployment> potentialDeployment = checkDeploymentExists("visitors-frontend");
        if(potentialDeployment.isEmpty()){
        Deployment deployment1 = new DeploymentBuilder()
                .withNewMetadata()
                    .withName("visitors-frontend")
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1) 
                    .withNewSelector()
                        .withMatchLabels(Map.of("tier", "frontend","app", "visitors"))
                    .endSelector()
                    .withNewTemplate()
                        .withNewMetadata()
                            .addToLabels("app", "visitors")
                            .addToLabels("tier", "frontend")
                        .endMetadata()
                    .withNewSpec()
                        .addNewContainer()
                            .withName("visitors-frontend")
                            .withImage("jdob/visitors-webui:1.0.0")
                            .addNewPort().withContainerPort(3000).withName("visitors").endPort()
                            .addNewEnv().withName("REACT_APP_TITLE").withValue(visitor.getSpec().getTitle()).endEnv()
                        .endContainer()
                    .endSpec()
                .endTemplate()
                .endSpec()
                .build();
                client.apps().deployments().inNamespace(client.getNamespace()).create(deployment1);
        }
        else { //We are maybe dealing with an update
          //EnvVar envar = potentialDeployment.get().getSpec().getTemplate().getSpec().getContainers().get(0).
          client.apps().deployments().inNamespace(client.getNamespace())
                .withName("visitors-frontend").edit(
                         d -> new DeploymentBuilder(d).editSpec().editTemplate().editSpec().editFirstContainer().editFirstEnv().withValue(visitor.getSpec().getTitle()).endEnv().endContainer().endSpec().endTemplate().endSpec().build()
                 );
        }
    }


    private void createFrontendService() {
        if(checkServiceExists("visitors-frontend-service").isEmpty()){
            Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName("visitors-frontend-service")
                    .withLabels(Map.of("tier", "frontend","app", "visitors"))
                .endMetadata()
                .withNewSpec()
                    .withType("NodePort")
                    .addNewPort()
                        .withPort(3000)
                        .withTargetPort(new IntOrString(3000))
                        .withNodePort(30686)
                        .withProtocol("TCP")
                    .endPort()
                    .withSelector(Map.of("tier", "frontend","app", "visitors"))
                .endSpec()
                .build();
            client.services().inNamespace(client.getNamespace()).create(service);        
        }
    }

    @Override
    public void deleteResources() {
       deleteDeployment("visitors-frontend");
       deleteService("visitors-frontend-service");
    }
    
}
