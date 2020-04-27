package net.trystram.scaletest.httpInserter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.common.base.MoreObjects;

import io.glutamate.lang.Environment;
import io.glutamate.lang.Exceptions;
import net.trystram.scaletest.BaseConfig;
import okhttp3.HttpUrl;

public class Config extends BaseConfig {

    private String authToken;
    private HttpUrl registryUrl;
    private boolean insecureTls;
    private boolean onlyRegister;
    private boolean disableConnectionPool;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
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

    public static Config fromEnv() throws IOException {
        final Config result = new Config();

        result.setAuthToken(Environment.get("AUTH_TOKEN")
                .orElseGet(() -> Exceptions.wrap(
                        () -> Files.readString(
                                Paths.get("/run/secrets/kubernetes.io/serviceaccount/token"),
                                StandardCharsets.UTF_8))));

        System.out.format("Using authToken: '%s'%n", result.getAuthToken());

        result.setTenantId(Environment.get("TENANT_ID")
                .or(() -> {
                    return Environment.get("NAMESPACE")
                            .flatMap(ns -> Environment.get("IOT_PROJECT")
                                    .map(prj -> ns + "." + prj));
                })
                .orElseThrow(
                        () -> new IllegalStateException("Missing tenant information. Need 'TENANT_ID' or 'IOT_PROJECT' and 'NAMESPACE'")));

        Environment.consumeAs("MAX_DEVICES", Long::parseLong, result::setDevicesToCreate);

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

        result.setDeviceIdPrefix(Environment.get("DEVICE_ID_PREFIX").orElse(""));
        Environment.consumeAs("CREDENTIALS_PER_DEVICE", Integer::parseInt, result::setCredentialsPerDevice);

        Environment.consumeAs("INSECURE_TLS", Boolean::parseBoolean, result::setInsecureTls);
        Environment.consumeAs("ONLY_REGISTER", Boolean::parseBoolean, result::setOnlyRegister);
        Environment.consumeAs("PLAIN_PASSWORDS", Boolean::parseBoolean, result::setPlainPasswords);
        Environment.consumeAs("DYNAMIC_PASSWORDS", Boolean::parseBoolean, result::setDynamicPasswords);
        Environment.consumeAs("DISABLE_CONNECTION_POOL", Boolean::parseBoolean, result::setDisableConnectionPool);
        Environment.consumeAs("REGISTRATION_EXT_PAYLOAD_SIZE", Integer::parseInt, result::setRegistrationExtPayloadSize);
        Environment.consumeAs("CREDENTIAL_EXT_PAYLOAD_SIZE", Integer::parseInt, result::setCredentialExtPayloadSize);

        return result;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("deviceIdPrefix", this.deviceIdPrefix)
                .add("devicesToCreate", this.devicesToCreate)
                .add("insecureTls", this.insecureTls)
                .add("onlyRegister", this.onlyRegister)
                .add("plainPasswords", this.plainPasswords)
                .add("dynamicPasswords", this.dynamicPasswords)
                .add("registryUrl", this.registryUrl)
                .add("tenantId", this.tenantId)
                .add("disableConnectionPool", this.disableConnectionPool)
                .add("registrationExtPayloadSize", this.registrationExtPayloadSize)
                .add("credentialExtPayloadSize", this.credentialExtPayloadSize)
                .add("credentialsPerDevice", this.credentialsPerDevice)
                .toString();
    }

}
