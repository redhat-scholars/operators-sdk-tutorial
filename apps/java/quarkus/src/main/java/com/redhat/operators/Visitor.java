package com.redhat.operators;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.ShortNames;
import io.fabric8.kubernetes.model.annotation.Version;

@Group("com.redhat.operators")
@Version("v1alpha1")
@ShortNames("vi")
public class Visitor extends CustomResource<VisitorSpec, VisitorStatus> implements Namespaced {
    
}
