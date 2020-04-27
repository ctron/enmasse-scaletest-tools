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
    public Long tableEntries() {

        long count = -1;
        for(Row row : tableSizeQuery("devices")){
            logger.info("Row: {}", row);
            count = row.getLong("count");
        }
        return count;
    }

    private RowSet<Row> tableSizeQuery(final String table) {
        return client.query("select count(*) from " + table)
                .await()
                .indefinitely();
    }
}
