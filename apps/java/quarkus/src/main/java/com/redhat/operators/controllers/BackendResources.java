package com.redhat.operators.controllers;

import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.redhat.operators.Visitor;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

@ApplicationScoped
public class BackendResources extends AbstractResources {
    

    public void createResources(Visitor visitor) {
        this.setVisitor(visitor);
        createBackendDeployment(visitor);
        createBackendService();
    }

    private void createBackendDeployment(Visitor visitor) {
        Optional<Deployment> potentialDeployment = checkDeploymentExists("visitors-backend"); 
        if(potentialDeployment.isEmpty()){
            Deployment deployment1 = new DeploymentBuilder()
                    .withNewMetadata()
                        .withName("visitors-backend")
                        .withOwnerReferences(this.createOwnerReference(this.getVisitor()))
                    .endMetadata()
                    .withNewSpec()
                        .withReplicas(visitor.getSpec().getSize()) //Here we make use of the CR
                        .withNewSelector()
                            .withMatchLabels(Map.of("tier", "backend","app", "visitors"))
                        .endSelector()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", "visitors")
                                .addToLabels("tier", "backend")
                            .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("visitors-backend")
                                .withImage("jdob/visitors-service:1.0.0")
                                .addNewPort().withContainerPort(8000).withName("visitors").endPort()
                                .addNewEnv().withName("MYSQL_SERVICE_HOST").withValue("mysql-service").endEnv()
                                .addNewEnv().withName("MYSQL_DATABASE").withValue("visitors").endEnv()
                                .addNewEnv().withName("MYSQL_USERNAME").withNewValueFrom().withNewSecretKeyRef("username", "mysql-auth", false).endValueFrom().endEnv()
                                .addNewEnv().withName("MYSQL_PASSWORD").withNewValueFrom().withNewSecretKeyRef("password", "mysql-auth", false).endValueFrom().endEnv()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();
            client.apps().deployments().inNamespace(client.getNamespace()).create(deployment1);
        }
        else { //we are maybe dealing with an update here
            if(potentialDeployment.get().getSpec().getReplicas() != visitor.getSpec().getSize()){
                client.apps().deployments().inNamespace(client.getNamespace())
                .withName("visitors-backend").edit(
                         d -> new DeploymentBuilder(d).editSpec().withReplicas(visitor.getSpec().getSize()).endSpec().build()
                 );
            }
        }
    }

    private void createBackendService() {
        if(checkServiceExists("visitors-backend-service").isEmpty()){
            Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withOwnerReferences(this.createOwnerReference(this.getVisitor()))
                    .withName("visitors-backend-service")
                    .withLabels(Map.of("tier", "backend","app", "visitors"))
                .endMetadata()
                .withNewSpec()
                    .withType("NodePort")
                    .addNewPort()
                        .withPort(8000)
                        .withTargetPort(new IntOrString(8000))
                        .withNodePort(30685)
                        .withProtocol("TCP")
                    .endPort()
                    .withSelector(Map.of("tier", "backend","app", "visitors"))
                .endSpec()
                .build();
            client.services().inNamespace(client.getNamespace()).create(service);        
        }
    }
}
