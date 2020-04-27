package net.trystram.scaletest.httpInserter;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import net.trystram.scaletest.AbstractInserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.glutamate.lang.Exceptions;
import net.trystram.scaletest.Tls;
import okhttp3.ConnectionPool;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Creater extends AbstractInserter implements AutoCloseable {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private Logger log = LoggerFactory.getLogger(Creater.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Config config;

    private final Statistics stats;

    private OkHttpClient client;
    private HttpUrl registerUrl;
    private HttpUrl credentialsUrl;

    private final String registrationBody;

    public Creater(final Config config) throws Exception {
        System.out.format("Running with config: %s%n", config);

        this.config = config;
        this.plain = config.isPlainPasswords();
        this.dynamic = config.isDynamicPasswords();
        this.credentialsPerDevice = config.getCredentialsPerDevice();
        this.deviceIdPrefix = config.getDeviceIdPrefix();

        this.registrationBody = getRegistrationBody(this.config.getRegistrationExtPayloadSize());
        this.credentialExtSize = this.config.getCredentialExtPayloadSize();

        var builder = new OkHttpClient.Builder();

        if ( config.isDisableConnectionPool() ) {
            builder.connectionPool(new ConnectionPool(0, 1, TimeUnit.MILLISECONDS));
        }

        if (config.isInsecureTls()) {
            Tls.makeOkHttpInsecure(builder);
        }

        this.client = builder.build();

        final HttpUrl base = config.getRegistryUrl();

        this.registerUrl = base.newBuilder()
                .addPathSegment("devices")
                .addPathSegment(config.getTenantId())
                .build();

        this.credentialsUrl = base.newBuilder()
                .addPathSegment("credentials")
                .addPathSegment(config.getTenantId())
                .build();

        System.out.println("Register URL: " + this.registerUrl);
        System.out.println("Credentials URL: " + this.credentialsUrl);
        System.out.println("Device ID example value:" + this.config.getDeviceIdPrefix() + 0);
        System.out.println("Credential Example JSON: " + credentialJson(0));

        this.stats = new Statistics(System.out, Duration.ofSeconds(10));
    }

    @Override
    public void close() {
        this.stats.close();
    }

    private Request.Builder newRequest() {
        return new Request.Builder()
                .header("Authorization", "Bearer " + this.config.getAuthToken());
    }

    public void run() {

        final long max = this.config.getDevicesToCreate();
        for (long i = 0; i < max; i++) {
            try {
                createDevice(i);
            } catch (final Exception e) {
                handleError(e);
            }
        }

        System.out.format("Finished creating %s devices.%n", max);

    }

    private void createDevice(final long i) throws Exception {

        final String deviceId = this.config.getDeviceIdPrefix() + Long.toString(i);

        final Instant start = Instant.now();
        final Request register = newRequest()
                .url(this.registerUrl
                        .newBuilder()
                        .addPathSegment(deviceId)
                        .build())
                .post(RequestBody.create(getRegistrationBody(i), JSON))
                .build();

        try (Response response = this.client.newCall(register).execute()) {
            if (!response.isSuccessful()) {
                handleRegistrationFailure(response);
                return;
            }
        }

        final Instant endReg = Instant.now();
        if (!config.isOnlyRegister()) {
            final Request credentials = newRequest()
                    .url(this.credentialsUrl
                            .newBuilder()
                            .addPathSegment(deviceId)
                            .build())
                    .put(RequestBody.create(credentialJson(i), JSON))
                    .build();

            try (Response response = client.newCall(credentials).execute()) {
                if (!response.isSuccessful()) {
                    handleCredentialsFailure(response);
                    return;
                }
            }
        }
        final Instant end = Instant.now();

        handleSuccess(
                Duration.between(start, endReg),
                this.config.isOnlyRegister() ? Optional.empty() : Optional.of(Duration.between(endReg, end)));

    }

    private void handleCredentialsFailure(final Response response) {
        this.stats.errorCredentials();
    }

    private void handleRegistrationFailure(final Response response) {
        this.stats.errorRegister();
    }

    private void handleSuccess(final Duration r, final Optional<Duration> c) {
        this.stats.success(r, c);
    }

    private String credentialJson(long i) {
        return Exceptions.wrap(() ->
                this.mapper.writeValueAsString(standardCredentialJson(i)));
    }

    private String getRegistrationBody(long i){
        return Exceptions.wrap(() ->
                this.mapper.writeValueAsString(deviceJson(i)));
    }

    @Override protected void createDevice(String tenantId, long deviceIndex, String version)
            throws Exception {
        // we use a custom run method here.
    }
}
