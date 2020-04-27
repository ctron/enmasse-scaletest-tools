package net.trystram.scaletest.postegreInserter.schema1and2;

public abstract class CreateTables {

    public static String getFullCreateQuery(){
        return String.format("%s %s %s %s", devices, device_states, devicesIndex, credentialIndex);
    }

    public static String getDropTablesQuery(){
        return "DROP TABLE IF EXISTS devices;"
                +" DROP TABLE IF EXISTS device_states;";
    }

    public final static String devices =
        "CREATE TABLE IF NOT EXISTS devices ("
            +"tenant_id varchar(256) NOT NULL, "
            +"device_id varchar(256) NOT NULL, "
            +"version varchar(36) NOT NULL, "
            +"data jsonb, "
            +"credentials jsonb, "

            +"PRIMARY KEY (tenant_id, device_id)"
        +");";

    public final static String device_states =
        "CREATE TABLE IF NOT EXISTS device_states ("
                +"tenant_id varchar(256) NOT NULL, "
                +"device_id varchar(256) NOT NULL, "
                +"last_known_gateway varchar(256), "

                +"PRIMARY KEY (tenant_id, device_id)"
        +");";

    public final static String devicesIndex =
            "CREATE INDEX idx_devices_tenant ON devices (tenant_id);";

    public final static String credentialIndex =
            "CREATE INDEX idx_credentials ON devices USING gin (credentials jsonb_path_ops);";

}
