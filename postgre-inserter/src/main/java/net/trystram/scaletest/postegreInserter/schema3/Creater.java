package net.trystram.scaletest.postegreInserter.schema3;

import io.glutamate.lang.Exceptions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.trystram.scaletest.postegreInserter.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Creater extends net.trystram.scaletest.postegreInserter.schema1and2.Creater {

    private final String CREDENTIALS_TABLE = "device_credentials";

    public Creater(final Config config) throws SQLException {
        super(config);
    }

    @Override
    protected void setUpDB(Statement st) throws SQLException {

        st.execute(CreateTables.getDropTablesQuery());
        st.execute(CreateTables.getFullCreateQuery());
    }

    @Override
    protected void createDevice(final String tenantId, final long i, final String version) throws SQLException {

        final String deviceId = this.config.getDeviceIdPrefix() + Long.toString(i);

        final String deviceInfo = Exceptions.wrap(() -> this.mapper.writeValueAsString(deviceJson(i)));
        final List<Map<String, Object>> creds = credentialJson(i);

        final List<String> paramsDevice = new ArrayList<>();
        paramsDevice.add(tenantId);
        paramsDevice.add(deviceId);
        paramsDevice.add(version);
        paramsDevice.add(deviceInfo);

        // forge insert device request
        final String sqldevice = String.format("INSERT INTO %s (tenant_id, device_id, version, data) "
                + "VALUES (?, ?, ?, to_jsonb(?::jsonb))", DEVICE_TABLE);
        PreparedStatement pst = sqlConnection.prepareStatement(sqldevice);

        pst.setString(1, tenantId);
        pst.setString(2, deviceId);
        pst.setString(3, version);
        pst.setString(4, deviceInfo);

//        log.debug("createDevice - sql: {}, params: {}", sqldevice, paramsDevice);
        pst.executeUpdate();

        //forge insert credentials request
        final String sqlcredentials = String.format("INSERT INTO %s (tenant_id, device_id, version, type, auth_id, data) "
                +"VALUES (?, ?, ?, ?, ?, to_jsonb(?::jsonb))", CREDENTIALS_TABLE);

        for(Map<String, Object> cred : creds) {
            final String type = (String)cred.remove("type");
            final String authId = (String)cred.remove("auth-id");

            final String data = Exceptions.wrap(() -> this.mapper.writeValueAsString(cred));

            // TODO multi statements single query
            pst = sqlConnection.prepareStatement(sqlcredentials);

            pst.setString(1, tenantId);
            pst.setString(2, deviceId);
            pst.setString(3, version);
            pst.setString(4, type);
            pst.setString(5, authId);
            pst.setString(6, data);

//            log.debug(String.format("insertCredentials - sql: {}, params: %s, %s %s %s %s",
//                    tenantId, deviceId, type, authId, data), sqlcredentials);

            pst.executeUpdate();
        }
    }
}
