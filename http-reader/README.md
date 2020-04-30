## 1 - Enable debug endpoint in the device registry

Add the correct environment variables then rebuild the device registry image. Execute
the following command from this directory:

    oc -n enmasse-infra apply -f http-reader/enmasse-infra
    oc -n enmasse-infra start-build iot-device-registry-jdbc-debug
    #oc -n enmasse-infra patch iotconfig default -p "$(cat patches/patch-iotconfig.yaml)"

## 2 - Deploy the sidecar to expose the device registry debug endpoint

Add the configmap as a volume to the device registry pod and add the nginx sidecar:

    oc -n enmasse-infra set volumes deployment iot-device-registry --add --name=sidecar-nginx-config -t configmap --configmap-name=iot-device-registry-debug-endpoint-sidecar-nginx-config
    oc -n enmasse-infra patch deployment iot-device-registry -p "$(cat http-reader/patches/patch-registry-sidecar.yaml)"

When using the split service registry:

    oc -n enmasse-infra set volumes deployment iot-device-registry-management --add --name=sidecar-nginx-config -t configmap --configmap-name=iot-device-registry-debug-endpoint-sidecar-nginx-config
    oc -n enmasse-infra patch deployment iot-device-registry-management -p "$(cat http-reader/patches/patch-registry-sidecar.yaml)"

## 3 - Run the test!

See the [other readme](../README.md).
