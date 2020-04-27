package net.trystram.scaletest.postegreInserter;

import com.google.common.base.MoreObjects;
import io.glutamate.lang.Environment;
import java.io.IOException;

public class Config extends net.trystram.scaletest.BaseConfig{

    private boolean alternateCredentialFormat;

    private String dbHost;
    private int dbPort = 5432;
    private String dbUser;
    private String dbPassword;
    private String dbName;

    public boolean isAlternateCredentialFormat() {
        return alternateCredentialFormat;
    }

    public void setAlternateCredentialFormat(boolean alternateCredentialFormat) {
        this.alternateCredentialFormat = alternateCredentialFormat;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return dbPort > 0 ? dbPort : 5432;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUrl(){
        return String.format("jdbc:postgresql://%s:%d/%s",
                this.getDbHost(),
                this.getDbPort(),
                this.getDbName());
    }

    public static Config fromEnv() throws IOException {
        final Config result = new Config();

        result.setDbHost(Environment.get("DB_HOST").orElseThrow(
                () -> new IllegalStateException("Missing database host address. Need 'DB_HOST'")));
        Environment.consumeAs("DB_PORT", Integer::parseInt, result::setDbPort);
        result.setDbUser(Environment.get("DB_USER").orElseThrow(
                () -> new IllegalStateException("Missing database username. Need 'DB_USER'")));
        result.setDbPassword(Environment.get("DB_PASSWORD").orElseThrow(
                () -> new IllegalStateException("Missing database password. Need 'DB_PASSWORD'")));

        result.setDbName(Environment.get("DB_NAME").orElse("device-registry"));

        result.setDeviceIdPrefix(Environment.get("DEVICE_ID_PREFIX").orElse(""));
        result.setTenantId(Environment.get("TENANT_ID_PREFIX").orElse("tenant"));

        Environment.consumeAs("DEVICES_PER_TENANT", Long::parseLong, result::setDevicesToCreate);

        Environment.consumeAs("NUMBER_OF_TENANTS", Long::parseLong, result::setTenantsToCreate);

        Environment.consumeAs("PLAIN_PASSWORDS", Boolean::parseBoolean, result::setPlainPasswords);
        Environment.consumeAs("DYNAMIC_PASSWORDS", Boolean::parseBoolean, result::setDynamicPasswords);

        Environment.consumeAs("ALTERNATE_CREDENTIAL_FORMAT", Boolean::parseBoolean, result::setAlternateCredentialFormat);

        Environment.consumeAs("CREDENTIALS_PER_DEVICE", Integer::parseInt, result::setCredentialsPerDevice);
        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dbHost", this.dbHost)
                .add("dbPort", this.dbPort)
                .add("dbUser", this.dbUser)
                .add("dbPassword", this.dbPassword)
                .add("dbName", this.dbName)
                .add("dbUrl", this.getDbUrl())
                .add("tenantID", this.tenantId)
                .add("deviceIdPrefix", this.deviceIdPrefix)
                .add("devicesPerTenant", this.devicesToCreate)
                .add("tenantsToCreate", this.tenantsToCreate)
                .add("credentialsPerDevice", this.credentialsPerDevice)
                .add("alternateCredentialFormat", this.alternateCredentialFormat)
                .add("plainPasswords", this.plainPasswords)
                .add("dynamicPasswords", this.dynamicPasswords)
                .toString();
    }

}
