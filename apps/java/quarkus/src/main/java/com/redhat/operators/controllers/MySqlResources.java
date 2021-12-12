package com.redhat.operators.controllers;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.redhat.operators.Visitor;

import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;

@ApplicationScoped
public class MySqlResources extends AbstractResources {

    public void createResources(Visitor visitor) {
        this.setVisitor(visitor);
        createMsqlSecret();
        createMsqlDeployment();
        createMsqlService();
    }
    
    private void createMsqlSecret() {
        Secret secret = client.secrets().inNamespace(client.getNamespace()).withName("mysql-auth").get();
        if(secret==null){
            secret = new SecretBuilder()
            .withType("opaque")
            .withNewMetadata().withName("mysql-auth").endMetadata()
            .addToStringData("username", "visitors-user")
            .addToStringData("password", "visitors-pass")
            .build();
            client.secrets().inNamespace(client.getNamespace()).create(secret);
        }
       
    }

    private void createMsqlDeployment() {
        if(checkDeploymentExists("mysql").isEmpty()) {
            Deployment deployment = new DeploymentBuilder()
                    .withNewMetadata()
                        .withName("mysql")
                        .withOwnerReferences(this.createOwnerReference(this.getVisitor()))
                    .endMetadata()
                    .withNewSpec()
                        .withReplicas(1)
                        .withNewSelector()
                            .withMatchLabels(Map.of("tier", "mysql","app", "visitors"))
                        .endSelector()
                        .withNewTemplate()
                            .withNewMetadata()
                                .addToLabels("app", "visitors")
                                .addToLabels("tier", "mysql")
                            .endMetadata()
                        .withNewSpec()
                            .addNewContainer()
                                .withName("visitors-mysql")
                                .withImage("mysql:5.7")
                                .addNewPort().withContainerPort(3306).withProtocol("TCP").endPort()
                                .addNewEnv().withName("MYSQL_ROOT_PASSWORD").withValue("password").endEnv()
                                .addNewEnv().withName("MYSQL_DATABASE").withValue("visitors").endEnv()
                                .addNewEnv().withName("MYSQL_USER").withNewValueFrom().withNewSecretKeyRef("username", "mysql-auth", false).endValueFrom().endEnv()
                                .addNewEnv().withName("MYSQL_PASSWORD").withNewValueFrom().withNewSecretKeyRef("password", "mysql-auth", false).endValueFrom().endEnv()
                            .endContainer()
                        .endSpec()
                    .endTemplate()
                    .endSpec()
                    .build();
                client.apps().deployments().inNamespace(client.getNamespace()).create(deployment);
        }
    }

    private void createMsqlService() {
       
        if(checkServiceExists("mysql-service").isEmpty()) {
            Service service = new ServiceBuilder()
                .withNewMetadata()
                    .withName("mysql-service")
                    .withLabels(Map.of("tier", "mysql","app", "visitors"))
                    .withOwnerReferences(this.createOwnerReference(this.getVisitor()))
                .endMetadata()
                .withNewSpec()
                    .withClusterIP("None")
                    .addNewPort()
                    .withPort(3306)
                    .endPort()
                    .withSelector(Map.of("tier", "mysql","app", "visitors"))
                .endSpec()
                .build();
            client.services().inNamespace(client.getNamespace()).create(service);        
        }
    }
}
