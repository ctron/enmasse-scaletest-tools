package net.trystram.scaletest;

public abstract class BaseConfig {

    protected long devicesToCreate = Long.MAX_VALUE;
    protected long tenantsToCreate = 1;

    protected String deviceIdPrefix;
    protected String tenantId;
    protected boolean plainPasswords;
    protected boolean dynamicPasswords;
    protected int credentialsPerDevice;

    protected int registrationExtPayloadSize;
    protected int credentialExtPayloadSize;

    public void setDevicesToCreate(long devicesToCreate) {
        this.devicesToCreate = devicesToCreate;
    }

    public long getDevicesToCreate() {
        return devicesToCreate > 0 ? devicesToCreate : Long.MAX_VALUE;
    }

    public long getTenantsToCreate() {
        return tenantsToCreate > 0 ? tenantsToCreate : 1;
    }

    public void setTenantsToCreate(long tenantsToCreate) {
        this.tenantsToCreate = tenantsToCreate;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setDeviceIdPrefix(String deviceIdPrefix) {
        this.deviceIdPrefix = deviceIdPrefix;
    }

    public String getDeviceIdPrefix() {
        return deviceIdPrefix;
    }

    public void setPlainPasswords(boolean plainPasswords) {
        this.plainPasswords = plainPasswords;
    }

    public boolean isPlainPasswords() {
        return plainPasswords;
    }

    public void setDynamicPasswords(boolean dynamicPasswords) {
        this.dynamicPasswords = dynamicPasswords;
    }

    public boolean isDynamicPasswords() {
        return dynamicPasswords;
    }

    public int getCredentialsPerDevice() {
        return credentialsPerDevice > 0 ? credentialsPerDevice : 1;
    }

    public void setCredentialsPerDevice(int credentialsPerDevice) {
        this.credentialsPerDevice = credentialsPerDevice;
    }

    public int getRegistrationExtPayloadSize() {
        return registrationExtPayloadSize;
    }

    public void setRegistrationExtPayloadSize(int registrationExtPayloadSize) {
        this.registrationExtPayloadSize = registrationExtPayloadSize;
    }

    public int getCredentialExtPayloadSize() {
        return credentialExtPayloadSize;
    }

    public void setCredentialExtPayloadSize(int credentialExtPayloadSize) {
        this.credentialExtPayloadSize = credentialExtPayloadSize;
    }

    public abstract String toString();

}
