package net.trystram.scaletest.postegreInserter.schema1and2;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.trystram.scaletest.AbstractInserter;

import io.glutamate.lang.Exceptions;

import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.trystram.scaletest.postegreInserter.Config;
import org.slf4j.LoggerFactory;

public class Creater extends AbstractInserter implements AutoCloseable {

    protected final ObjectMapper mapper = new ObjectMapper();

    protected final Config config;
    protected final Connection sqlConnection;
    protected final String DEVICE_TABLE = "devices";

    public Creater(final Config config) throws SQLException {
        log = LoggerFactory.getLogger(Creater.class);
        System.out.format("Running with config: %s%n", config);

        this.config = config;
        this.plain = config.isPlainPasswords();
        this.dynamic = config.isDynamicPasswords();

        sqlConnection = DriverManager.getConnection(config.getDbUrl(), config.getDbUser(), config.getDbPassword());
        Statement statement = sqlConnection.createStatement();

        setUpDB(statement);
    }

    protected void setUpDB(Statement st) throws SQLException {

        st.execute(CreateTables.getDropTablesQuery());
        st.execute(CreateTables.getFullCreateQuery());
    }

    @Override
    public void close() {
        try {
            sqlConnection.close();
        } catch (SQLException e){
            log.warn("Error while closing the connection to the database", e);
        }
    }

    @Override
    protected void createDevice(final String tenantId, final long i, final String version) throws SQLException {

        final String deviceId = this.config.getDeviceIdPrefix() + Long.toString(i);

        final String deviceInfo = Exceptions.wrap(() -> this.mapper.writeValueAsString(deviceJson(i)));
        final String creds = Exceptions.wrap(() -> this.mapper.writeValueAsString(credentialJson(i)));

        final List<String> params = new ArrayList<>();
        params.add(tenantId);
        params.add(deviceId);
        params.add(version);
        params.add(deviceInfo);
        params.add(creds);

        // forge insert request
        final String sql = String.format("INSERT INTO %s (tenant_id, device_id, version, data, credentials) "
                +"VALUES (?, ?, ?, to_jsonb(?::jsonb), to_jsonb(?::jsonb))", DEVICE_TABLE);
        PreparedStatement pst = sqlConnection.prepareStatement(sql);

        pst.setString(1, tenantId);
        pst.setString(2, deviceId);
        pst.setString(3, version);
        pst.setString(4, deviceInfo);
        pst.setString(5, creds);

        //log.debug("createDevice - sql: {}, params: {}", sql, params);

        pst.executeUpdate();
    }

    protected void handleError(final Exception e) {
        log.warn("Failed to process", e);
    }

    protected List<Map<String, Object>> credentialJson(final long i) {
        if (this.config.isAlternateCredentialFormat()){
            return alternateCredentialJson(i);
        } else {
            return standardCredentialJson(i);
        }
    }


    // Alternate format :

    // {
    //   "hashed-password": {
    //    "prefix-1_auth-1": {
    //      "secrets": [ ... ]
    //    }
    //  }
    //}
    protected List<Map<String, Object>> alternateCredentialJson(long i) {
        final Map<String, Object> credentials = new HashMap<>();

        final Map<String, Object> hashed = new HashMap<>();
        final Map<String, Object> psk = new HashMap<>();

        for (int authId=1; authId <= config.getCredentialsPerDevice(); authId++) {

            final String authIdValue = String.format("%s%d_authId-%d", this.config.getDeviceIdPrefix(), i, authId);

            final Map<String, Object> secret = new HashMap<>();

            if (pskSecret) {
                secret.put("secrets", Collections.singletonList(pskSecret(i)));
                psk.put(authIdValue, secret);
            } else {
                secret.put("secrets", Collections.singletonList(passwordSecret(i)));
                hashed.put(authIdValue, secret);
            }

            pskSecret = !pskSecret;
        }

        if (hashed.size() > 0) {
            credentials.put("hashed-password", hashed);
        }
        if (psk.size() > 0) {
            credentials.put("psk", psk);
        }

        return Collections.singletonList(credentials);
    }


}
