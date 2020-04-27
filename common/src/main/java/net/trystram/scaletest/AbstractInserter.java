package net.trystram.scaletest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.glutamate.lang.Exceptions;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractInserter implements AutoCloseable {

    protected Logger log = LoggerFactory.getLogger(AbstractInserter.class);
    /**
     * The RNG for the salt. As this is just for testing, we don't need to use {@link SecureRandom}.
     */
    private static final Random R = new Random();

    private final ObjectMapper mapper = new ObjectMapper();

    protected BaseConfig config;
    protected boolean plain;
    protected boolean dynamic;
    protected boolean pskSecret = false;
    protected int credentialsPerDevice;
    protected String deviceIdPrefix;
    protected int credentialExtSize;

    public void run() {

        final long maxDevices = this.config.getDevicesToCreate();
        final long maxTenants = this.config.getTenantsToCreate();

        for (long i = 0; i < maxTenants; i++) {
            String tenantId = String.format("%s-%d", this.config.getTenantId(), i);
            for (long j = 0; j < maxDevices; j++) {
                try {
                    createDevice(
                            tenantId,
                            j,
                            UUID.randomUUID().toString());
                } catch (final Exception e) {
                    handleError(e);
                }
            }
            log.info(String.format("Inserted %d devices for tenant %d", maxDevices, i));
        }

        log.info(String.format("Finished creating %d devices total", maxDevices*maxTenants));
    }

    protected abstract void createDevice(final String tenantId, final long deviceIndex, final String version) throws Exception;



    protected void handleError(final Exception e) {
        log.warn("Failed to process", e);
    }


    // format :

    // [{
    //   "type": "hashed-password"
    //   "auth-id": "prefix-1_auth-1"
    //   "secrets": [ ... ]
    //    }
    //  }
    //}]
    protected List<Map<String, Object>> standardCredentialJson(long i) {
        List<Map<String, Object>> credentials = new ArrayList();

        for (int authId=1; authId <= credentialsPerDevice; authId++) {

            final Map<String, Object> result = new HashMap<>();
            if (credentialExtSize > 0) {
                result.put("ext", getCredentialExt(credentialExtSize));
            }
            if (pskSecret) {
                result.put("type", "psk");
                result.put("secrets", Collections.singletonList(pskSecret(i)));
            } else {
                result.put("type", "hashed-password");
                result.put("secrets", Collections.singletonList(passwordSecret(i)));
            }
            result.put("auth-id", String.format("%s%d_authId-%d", deviceIdPrefix, i, authId));

            if (credentialsPerDevice > 1) {
                pskSecret = !pskSecret;
            }
            credentials.add(result);
        }
        return credentials;
    }



    protected Map<String, String> passwordSecret(long i){
        final Map<String, String> secret = new HashMap<>();
        final String password = "longerThanUsualPassword-" + i;
        if (this.plain) {
            secret.put("pwd-plain", password);
        } else if (!this.dynamic) {
            secret.put("pwd-hash", "GO/C0ZqOnMFs2QnCUxFR92pu1uPe4fdMgVCXGjnH7uk=");
            secret.put("salt", "YvajSViCAW8=");
            secret.put("hash-function", "sha-256");
        } else {
            final byte[] salt = new byte[8];
            R.nextBytes(salt);
            secret.put("pwd-hash", encodePassword(salt, password));
            secret.put("hash-function", "sha-256");
            secret.put("salt", Base64.getEncoder().encodeToString(salt));
        }
        secret.put("enabled", "true");
        return secret;
    }

    protected Map<String, String> pskSecret(long i){
        final String key = "somePresharedKey-" + i;
        final String encodedKey = Base64.getEncoder().encodeToString(key.getBytes(StandardCharsets.UTF_8));

        final Map<String, String> secret = new HashMap<>();
            secret.put("key", encodedKey);
            secret.put("enabled", "true");
        return secret;
    }

    protected Map<String, Object> deviceJson(long i) {
        final Map<String, Object> result = new HashMap<>(3);
        result.put("enabled", true);

        final Map<String, String> ext = new HashMap<>(2);
        ext.put("externalField", "This is device "+ String.valueOf(i));
        ext.put("someComment", "about device"+ String.valueOf(i));

        return result;
    }

    protected static String encodePassword(byte[] salt, final String password) {
        final MessageDigest digest = Exceptions.wrap(() -> MessageDigest.getInstance("SHA-256"));
        if (salt != null) {
            digest.update(salt);
        }
        return Base64.getEncoder().encodeToString(digest.digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    protected String getRegistrationBody(final int size){

        if (size > 0) {
            Map<String, Object> root = new HashMap<>();
            root.put("enabled", "true");

            Map<String, Object> ext = new HashMap<>();

            ext.put("additionalProp1", getRandomString(size));
            root.put("ext", ext);

            return Exceptions.wrap(() -> mapper.writeValueAsString(root));
        } else {
            return "{}";
        }
    }


    private static Map<String, Object> getCredentialExt(final int credentialExtSize){

        Map<String, Object> root = new HashMap<>();
         root.put("additionalProp1", getRandomString(credentialExtSize));
            return root;
    }

    private static String getRandomString(final int size){
        Random random = ThreadLocalRandom.current();
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
                .toCharArray();

        StringBuffer out = new StringBuffer(size);
        for (int i = 0; i < size; i++)
        {
            out.append(alphabet[random.nextInt(alphabet.length)]);
        }

        return out.toString();
    }
}
