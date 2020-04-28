package net.trystram.scaletest.postgre;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;

@ApplicationScoped
@RegisterForReflection
public class PostgreMetrics {

    private static final Logger logger = LoggerFactory.getLogger(PostgreMetrics.class);

    @Inject
    io.vertx.mutiny.pgclient.PgPool client;

    void onStart(@Observes StartupEvent ev) {
        logger.info("The application is starting...");
        logger.info("Client: {}", this.client);
    }

    @Gauge(name = "table_entries", tags = {"table=devices"}, unit = MetricUnits.NONE, description = "Total number of entries in the 'devices' table")
    public Long tableDevicesEntries() {
        return tableSizeQuery("devices");
    }

    @Gauge(name = "table_entries", tags = {"table=device_registrations"}, unit = MetricUnits.NONE, description = "Total number of entries in the 'device_registrations' table")
    public Long tableDeviceRegistrationsEntries() {
        return tableSizeQuery("device_registrations");
    }

    @Gauge(name = "table_entries", tags = {"table=device_credentials"}, unit = MetricUnits.NONE, description = "Total number of entries in the 'device_credentials' table")
    public Long tableDeviceCredentialsEntries() {
        return tableSizeQuery("device_credentials");
    }

    private long tableSizeQuery(final String table) {
         final RowSet<Row> rows = client.query("select count(*) from " + table)
                .await()
                .indefinitely();

        for(Row row : rows){
            logger.info("Row: {}", row);
            return row.getLong("count");
        }

        return -1;
    }
}
