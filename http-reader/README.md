## 1 - Enable debug endpoint in the device registry

Add the correct environment variables then rebuild the device registry image.
Exemple of values to add in `<enmasse-source>/iot/iot-device-registry-jdbc/Dockerfile` :

```
ENV ENMASSE_IOT_REGISTRY_DEBUG_ENABLED=true
ENV ENMASSE_IOT_REGISTRY_DEBUG_PORT=44120
```

## 2 - Deploy the sidecar to expose the device registry debug endpoint

Create the configamp
```
oc apply -f registry-sidecar-deploy/010-Registry-sidecar-configmp.yaml
```

Add the configmap as a volume to the device registry pod 
```
oc set volumes -n enmasse-infra deployment iot-device-registry --add --name=sidecar-nginx-config -t configmap --configmap-name=iot-device-registry-debug-endpoint-sidecar-nginx-config
```

Add the nginx sidecar container in the deployment :
```
oc patch -n enmasse-infra deployment iot-device-registry -p "$(cat registry-sidecar-deploy/020-Registry-sidecar-deployment.yaml)"
```

## 3 - Run the test ! 

See the [other readme](../README.md).