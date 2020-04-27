package net.trystram.scaletest.httpReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import io.glutamate.lang.Environment;
import okhttp3.HttpUrl;

public class Config {

    private String namespace;
    private long maxDevicesCreated = Long.MAX_VALUE;
    private long devicesToRead = Long.MAX_VALUE;
    private List<String> deviceIdPrefixes;

    private String tenantId;
    private HttpUrl registryUrl;
    private boolean insecureTls;
    private boolean onlyRegister;
    private boolean disableConnectionPool;
    private boolean verifyPasswords;
    private boolean readPSKCredentials;

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setMaxDevicesCreated(long maxDevicesCreated) {
        this.maxDevicesCreated = maxDevicesCreated;
    }

    public long getMaxDevicesCreated() {
        return maxDevicesCreated;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setDeviceIdPrefixes(List<String> deviceIdPrefixes) {
        this.deviceIdPrefixes = deviceIdPrefixes;
    }

    public List<String> getDeviceIdPrefixes() {
        return deviceIdPrefixes;
    }

    public long getDevicesToRead() {
        return this.devicesToRead > 0 ? this.devicesToRead : Long.MAX_VALUE;
    }

    public void setDevicesToRead(long devicesToRead) {
        this.devicesToRead = devicesToRead;
    }

    public void setRegistryUrl(HttpUrl registryUrl) {
        this.registryUrl = registryUrl;
    }

    public HttpUrl getRegistryUrl() {
        return registryUrl;
    }

    public void setInsecureTls(boolean disableTls) {
        this.insecureTls = disableTls;
    }

    public boolean isInsecureTls() {
        return insecureTls;
    }

    public void setOnlyRegister(boolean onlyRegister) {
        this.onlyRegister = onlyRegister;
    }

    public boolean isOnlyRegister() {
        return onlyRegister;
    }

    public void setDisableConnectionPool(boolean disableConnectionPool) {
        this.disableConnectionPool = disableConnectionPool;
    }

    public boolean isDisableConnectionPool() {
        return disableConnectionPool;
    }

    public void setVerifyPasswords(boolean verifyPasswords) {
        this.verifyPasswords = verifyPasswords;
    }

    public boolean isVerifyPasswords() {
        return verifyPasswords;
    }

    public boolean isReadPSKCredentials() {
        return readPSKCredentials;
    }

    public void setReadPSKCredentials(boolean readPSKCredentials) {
        this.readPSKCredentials = readPSKCredentials;
    }

    public static Config fromEnv() throws IOException {
        final Config result = new Config();

        final String namespace = Environment.getRequired("NAMESPACE");
        result.setNamespace(namespace);

        final List<String> prefixes = Environment.get("DEVICE_ID_PREFIXES")
                .map(str -> Arrays.asList(str.split("\\s*,\\s*")))
                .orElseGet(() -> Jobs.findJobs(namespace));
        System.out.format("Using prefixes: %s%n", prefixes);
        result.setDeviceIdPrefixes(prefixes);

        if (result.getDeviceIdPrefixes().isEmpty()) {
            throw new IllegalStateException("No prefixes set. Unable to run.");
        }

        result.setTenantId(Environment.get("TENANT_ID")
                .or(() -> Environment.get("IOT_PROJECT")
                        .map(prj -> namespace + "." + prj))
                .orElseThrow(
                        () -> new IllegalStateException("Missing tenant information. Need 'TENANT_ID' or 'IOT_PROJECT' and 'NAMESPACE'")));

        Environment.consumeAs("MAX_DEVICES_CREATED", Long::parseLong, result::setMaxDevicesCreated);
        Environment.consumeAs("DEVICES_TO_READ", Long::parseLong, result::setDevicesToRead);

        result.setRegistryUrl(Environment.get("REGISTRY_URL")
                .map(HttpUrl::parse)
                .orElseGet(() -> {
                    final HttpUrl.Builder builder = new HttpUrl.Builder()
                            .scheme(Environment.get("REGISTRY_SCHEME").orElse("https"))
                            .host(Environment.getRequired("REGISTRY_HOST"));

                    Environment.consumeAs("REGISTRY_PORT", Integer::parseInt, builder::port);
                    Environment.get("REGISTRY_BASE").ifPresent(builder::addPathSegments);

                    return builder.build();
                }));

        Environment.consumeAs("TEST_PSK", Boolean::parseBoolean, result::setReadPSKCredentials);
        Environment.consumeAs("INSECURE_TLS", Boolean::parseBoolean, result::setInsecureTls);
        Environment.consumeAs("ONLY_REGISTER", Boolean::parseBoolean, result::setOnlyRegister);
        Environment.consumeAs("DISABLE_CONNECTION_POOL", Boolean::parseBoolean, result::setDisableConnectionPool);
        Environment.consumeAs("VERIFY_PASSWORDS", Boolean::parseBoolean, result::setVerifyPasswords);

        return result;
    }

}
