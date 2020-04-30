# Testing with the CrunchyDB Postgres Operator

This README assumes:

* You are using OpenShift 4.3+
* You are running on AWS
* You have the PGO command line tools installed (`pgo` and `evalexp`)

## Installation

    git clone -b v4.2.2 https://github.com/CrunchyData/postgres-operator.git
    cd postgres-operator

Tweak the configuration (`$PGOROOT/conf/postgres-operator`):

* Switch storage size to `100Gi`
* Create/tweak resource configuration `xlarge`

And then continue:

    export PGOROOT=$(pwd)
    cd $PGOROOT/deploy
    $PGOROOT/deploy/gen-api-keys.sh
    $PGOROOT/deploy/gen-sshd-keys.sh
    cd $PGOROOT
    
    oc create namespace pgo
    
    oc -n pgo create secret generic pgo-backrest-repo-config \
      --from-file=config=$PGOROOT/conf/pgo-backrest-repo/config \
      --from-file=sshd_config=$PGOROOT/conf/pgo-backrest-repo/sshd_config \
      --from-file=aws-s3-credentials.yaml=$PGOROOT/conf/pgo-backrest-repo/aws-s3-credentials.yaml \
      --from-file=aws-s3-ca.crt=$PGOROOT/conf/pgo-backrest-repo/aws-s3-ca.crt
    
    oc -n pgo create secret generic pgo-auth-secret \
      --from-file=server.crt=$PGOROOT/conf/postgres-operator/server.crt \
      --from-file=server.key=$PGOROOT/conf/postgres-operator/server.key
    
    oc project pgo
    PGO_CMD=oc $PGOROOT/deploy/install-bootstrap-creds.sh
    
    oc create -f $PGOROOT/deploy/pgo-scc.yaml
    
    oc -n pgo create secret tls pgo.tls \
      --key=$PGOROOT/conf/postgres-operator/server.key \
      --cert=$PGOROOT/conf/postgres-operator/server.crt
    
    oc -n pgo create configmap pgo-config \
      --from-file=$PGOROOT/conf/postgres-operator

## OperatorHub

Install the Operator using the Operator Hub UI into the namespace `pgo`.

## Finishing up

    oc -n pgo expose deployment postgres-operator
    oc -n pgo create route passthrough --service=postgres-operator
    export PGO_APISERVER_URL=https://$(oc get route postgres-operator -ojsonpath="{.status.ingress[*].host})

## Local env

Set the following:

    export PGO_CA_CERT=$PGOROOT/conf/postgres-operator/server.crt
    export PGO_CLIENT_CERT=$PGOROOT/conf/postgres-operator/server.crt
    export PGO_CLIENT_KEY=$PGOROOT/conf/postgres-operator/server.key
    export PGO_NAMESPACE=pgo
    
## Crete database and schema

Set local env var:

    export ENMASSE_BASE=<enmasse base dir>

Perform creation:

    #pgo create cluster device-registry
    pgo create cluster device-registry --metrics --pgbouncer --replica-count 2 --resources-config xlarge
    pgo create user device-registry --username registry --password user12
    
    oc -n pgo rsh deployment/device-registry createdb device-registry
    
    oc -n pgo rsh deployment/device-registry bash -c "PGPASSWORD=user12 psql -h localhost device-registry registry" < "$ENMASSE_BASE/templates/iot/examples/postgresql/create.devcon.sql"
    
    # Table model
    
    oc -n pgo rsh deployment/device-registry bash -c "PGPASSWORD=user12 psql -h localhost device-registry registry" < "$ENMASSE_BASE/templates/iot/examples/postgresql/create.table.sql"

## Enter database

    oc -n pgo rsh deployment/device-registry bash -c "PGPASSWORD=user12 psql -h localhost device-registry registry"
