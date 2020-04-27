# Tests setup

## Requirements

 - a OCP 4 cluster with latest enmasse installed
 - 
 
## Write tests

Each test is conducted in it's own namespace.

Create a new namespace.
```
oc new-project test-1
```

Make sure to reset the database between each test:
```
ENMASSE_BASE=/path/to/enmasse
IOT_EXAMPLE=$ENMASSE_BASE/templates/build/enmasse-latest/install/components/iot/examples

# Drop the existing data. Make sure you adjust to tree model if needed.
oc -n device-registry-storage rsh deployment/postgresql bash -c "PGPASSWORD=user12 psql -h postgresql device-registry registry" < $IOT_EXAMPLE/postgresql/drop.sql
oc -n device-registry-storage rsh deployment/postgresql bash -c "PGPASSWORD=user12 psql -h postgresql device-registry registry" < $IOT_EXAMPLE/postgresql/drop.table.sql

#Re-create the tables.
oc -n device-registry-storage rsh deployment/postgresql bash -c "PGPASSWORD=user12 psql -h postgresql device-registry registry" < $IOT_EXAMPLE/postgresql/create.sql
oc -n device-registry-storage rsh deployment/postgresql bash -c "PGPASSWORD=user12 psql -h postgresql device-registry registry" < $IOT_EXAMPLE/postgresql/create.table.sql
```

Adjusting the number of device-registry pods :
```
oc patch -n enmasse-infra iotconfig default --type=json -p '{"spec": {"services": {"deviceRegistry": {"replicas":"5"}}}}'
```

Change `parralelism` in `http-inserter/deploy/070-Job.yaml` to the number of writing instances you want.

Deploy the iot project and the monitoring components : 
*Note* : the operatorGroup resource needs to be updated with the current namespace (todo: automate it).
The default value is `test-1`. Change it in `deploy/005-OperatorGroup.yaml`.
```
oc apply -f deploy/
```
If some components are not ready you might need to re-run the command.

Then you can navigate to https://https://grafana-route-test-1.apps.<yourClusterName> 
and upload `grafana-overview.json` as a new dashboard. 

Launch the test : 
```
oc apply -f http-inserter/deploy/
```
First the http-inserter image will be built then the job will start. \

When the test is over (the Job is completed or it as ran for as long as you whish) 
the logs can be collected with : 
```
HEADLINE="Pod;Time;Write;Total;Rate;ErrorsR;ErrorsC;AvgR;AvgC"
SINCE_TIME=20m
JOB=INSERTER
OUTPUT_FILE=/tmp/test1

for i in $(oc get pods -l job-name=http-$INSERTER --no-headers | awk '{ print $1}' ); 
do echo $HEADLINE; echo -n "\"${i}\";"; oc logs --since $SINCE_TIME $i; echo; echo; echo; done > $OUTPUT_FILE
```

## Read tests

The steps from the write tests procedure can be followed, replacing `http-inserter`
with `http-reader` when applicable.

The debug endpoint must be deployed on the device registry. See [this guide](http-reader/README.md).

## Tips

Delete the prometheus metrics to reset the data in the grafana dahsboard:
```
oc -n test-1 delete pod prometheus-metrics-0
```

