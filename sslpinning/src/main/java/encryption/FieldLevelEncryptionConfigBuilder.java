package encryption;

import static java.security.spec.MGF1ParameterSpec.SHA256;
import static java.security.spec.MGF1ParameterSpec.SHA512;
import static encryption.FieldLevelEncryption.isNullOrEmpty;
import static yap.utils.EncodingUtils.encodeBytes;

import com.jayway.jsonpath.JsonPath;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A builder class for {@link FieldLevelEncryptionConfig}.
 *
 * @author Irfan Arshad
 */
public final class FieldLevelEncryptionConfigBuilder {

    private Certificate encryptionCertificate;
    private String encryptionCertificateFingerprint;
    private String encryptionKeyFingerprint;
    private PrivateKey decryptionKey;
    private Map<String, String> encryptionPaths = new HashMap<>();
    private Map<String, String> decryptionPaths = new HashMap<>();
    private String oaepPaddingDigestAlgorithm;
    private String ivFieldName;
    private String ivHeaderName;
    private String oaepPaddingDigestAlgorithmFieldName;
    private String oaepPaddingDigestAlgorithmHeaderName;
    private String encryptedKeyFieldName;
    private String tokenizationAuthenticationValueFieldName = "tokenizationAuthenticationValue";
    private String encryptedKeyHeaderName;
    private String encryptedValueFieldName;
    private String encryptionCertificateFingerprintFieldName;
    private String encryptionCertificateFingerprintHeaderName;
    private String encryptionKeyFingerprintFieldName;
    private FieldLevelEncryptionConfig.FieldValueEncoding fieldValueEncoding;

    private FieldLevelEncryptionConfigBuilder() {
    }

    /**
     * Get an instance of the builder.
     */
    public static FieldLevelEncryptionConfigBuilder aFieldLevelEncryptionConfig() {
        return new FieldLevelEncryptionConfigBuilder();
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionCertificate}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionCertificate(Certificate encryptionCertificate) {
        this.encryptionCertificate = encryptionCertificate;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionCertificateFingerprint}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionCertificateFingerprint(String encryptionCertificateFingerprint) {
        this.encryptionCertificateFingerprint = encryptionCertificateFingerprint;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionKeyFingerprint}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionKeyFingerprint(String encryptionKeyFingerprint) {
        this.encryptionKeyFingerprint = encryptionKeyFingerprint;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#decryptionKey}.
     */
    public FieldLevelEncryptionConfigBuilder withDecryptionKey(PrivateKey decryptionKey) {
        this.decryptionKey = decryptionKey;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionPaths}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionPath(String jsonPathIn, String jsonPathOut) {
        this.encryptionPaths.put(jsonPathIn, jsonPathOut);
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#decryptionPaths}.
     */
    public FieldLevelEncryptionConfigBuilder withDecryptionPath(String jsonPathIn, String jsonPathOut) {
        this.decryptionPaths.put(jsonPathIn, jsonPathOut);
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#oaepPaddingDigestAlgorithm}.
     */
    public FieldLevelEncryptionConfigBuilder withOaepPaddingDigestAlgorithm(String oaepPaddingDigestAlgorithm) {
        this.oaepPaddingDigestAlgorithm = oaepPaddingDigestAlgorithm;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#ivFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withIvFieldName(String ivFieldName) {
            this.ivFieldName = ivFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#tokenizationAuthenticationValueFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withTokenizationAuthenticationValueFieldName(String tokenizationAuthenticationValueFieldName) {
        this.tokenizationAuthenticationValueFieldName = tokenizationAuthenticationValueFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#oaepPaddingDigestAlgorithmFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withOaepPaddingDigestAlgorithmFieldName(String oaepPaddingDigestAlgorithmFieldName) {
        this.oaepPaddingDigestAlgorithmFieldName = oaepPaddingDigestAlgorithmFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptedKeyFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptedKeyFieldName(String encryptedKeyFieldName) {
        this.encryptedKeyFieldName = encryptedKeyFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptedValueFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptedValueFieldName(String encryptedValueFieldName) {
        this.encryptedValueFieldName = encryptedValueFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionCertificateFingerprintFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionCertificateFingerprintFieldName(String encryptionCertificateFingerprintFieldName) {
        this.encryptionCertificateFingerprintFieldName = encryptionCertificateFingerprintFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionKeyFingerprintFieldName}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionKeyFingerprintFieldName(String encryptionKeyFingerprintFieldName) {
        this.encryptionKeyFingerprintFieldName = encryptionKeyFingerprintFieldName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#fieldValueEncoding}.
     */
    public FieldLevelEncryptionConfigBuilder withFieldValueEncoding(FieldLevelEncryptionConfig.FieldValueEncoding fieldValueEncoding) {
        this.fieldValueEncoding = fieldValueEncoding;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#ivHeaderName}.
     */
    public FieldLevelEncryptionConfigBuilder withIvHeaderName(String ivHeaderName) {
        this.ivHeaderName = ivHeaderName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#oaepPaddingDigestAlgorithmHeaderName}.
     */
    public FieldLevelEncryptionConfigBuilder withOaepPaddingDigestAlgorithmHeaderName(String oaepPaddingDigestAlgorithmHeaderName) {
        this.oaepPaddingDigestAlgorithmHeaderName = oaepPaddingDigestAlgorithmHeaderName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptedKeyHeaderName}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptedKeyHeaderName(String encryptedKeyHeaderName) {
        this.encryptedKeyHeaderName = encryptedKeyHeaderName;
        return this;
    }

    /**
     * See: {@link FieldLevelEncryptionConfig#encryptionCertificateFingerprintHeaderName}.
     */
    public FieldLevelEncryptionConfigBuilder withEncryptionCertificateFingerprintHeaderName(String encryptionCertificateFingerprintHeaderName) {
        this.encryptionCertificateFingerprintHeaderName = encryptionCertificateFingerprintHeaderName;
        return this;
    }

    /**
     * Build a {@link FieldLevelEncryptionConfig}.
     *
     * @throws EncryptionException
     */
    public FieldLevelEncryptionConfig build() throws EncryptionException {

        checkJsonPathParameterValues();
        checkParameterValues();
        checkParameterConsistency();

        computeEncryptionCertificateFingerprintWhenNeeded();
        computeEncryptionKeyFingerprintWhenNeeded();

        FieldLevelEncryptionConfig config = new FieldLevelEncryptionConfig();
        config.encryptionCertificateFingerprintFieldName = this.encryptionCertificateFingerprintFieldName;
        config.encryptionKeyFingerprintFieldName = this.encryptionKeyFingerprintFieldName;
        config.encryptionCertificateFingerprint = this.encryptionCertificateFingerprint;
        config.encryptionKeyFingerprint = this.encryptionKeyFingerprint;
        config.decryptionKey = this.decryptionKey;
        config.encryptionPaths = this.encryptionPaths;
        config.encryptionCertificate = this.encryptionCertificate;
        config.oaepPaddingDigestAlgorithm = this.oaepPaddingDigestAlgorithm;
        config.ivFieldName = this.ivFieldName;
        config.oaepPaddingDigestAlgorithmFieldName = this.oaepPaddingDigestAlgorithmFieldName;
        config.decryptionPaths = this.decryptionPaths;
        config.encryptedKeyFieldName = this.encryptedKeyFieldName;
        config.fieldValueEncoding = this.fieldValueEncoding;
        config.encryptedValueFieldName = this.encryptedValueFieldName;
        config.ivHeaderName = this.ivHeaderName;
        config.oaepPaddingDigestAlgorithmHeaderName = this.oaepPaddingDigestAlgorithmHeaderName;
        config.encryptedKeyHeaderName = this.encryptedKeyHeaderName;
        config.encryptionCertificateFingerprintHeaderName = this.encryptionCertificateFingerprintHeaderName;
        config.tokenizationAuthenticationValueFieldName = this.tokenizationAuthenticationValueFieldName;
        return config;
    }

    private void checkJsonPathParameterValues() {
        for (Entry<String, String> entry : decryptionPaths.entrySet()) {
            if (!JsonPath.isPathDefinite(entry.getKey()) || !JsonPath.isPathDefinite(entry.getValue())) {
                throw new IllegalArgumentException("JSON paths for decryption must point to a single item!");
            }
        }

        for (Entry<String, String> entry : encryptionPaths.entrySet()) {
            if (!JsonPath.isPathDefinite(entry.getKey()) || !JsonPath.isPathDefinite(entry.getValue())) {
                throw new IllegalArgumentException("JSON paths for encryption must point to a single item!");
            }
        }
    }

    private void checkParameterValues() {
        if (oaepPaddingDigestAlgorithm == null) {
            throw new IllegalArgumentException("The digest algorithm for OAEP cannot be null!");
        }

        if (!SHA256.getDigestAlgorithm().equals(oaepPaddingDigestAlgorithm)
                && !SHA512.getDigestAlgorithm().equals(oaepPaddingDigestAlgorithm)) {
            throw new IllegalArgumentException(String.format("Unsupported OAEP digest algorithm: %s!", oaepPaddingDigestAlgorithm));
        }

        if (fieldValueEncoding == null) {
            throw new IllegalArgumentException("Value encoding for fields and headers cannot be null!");
        }

        if (ivFieldName == null && ivHeaderName == null) {
            throw new IllegalArgumentException("At least one of IV field name or IV header name must be set!");
        }

        if (encryptedKeyFieldName == null && encryptedKeyHeaderName == null) {
            throw new IllegalArgumentException("At least one of encrypted key field name or encrypted key header name must be set!");
        }

        if (encryptedValueFieldName == null) {
            throw new IllegalArgumentException("Encrypted value field name cannot be null!");
        }
        if (tokenizationAuthenticationValueFieldName == null) {
            throw new IllegalArgumentException("At least one of tokenizationAuthenticationValue field name name must be set!. Field name must be tokenizationAuthenticationValue");
        }
    }

    private void checkParameterConsistency() {
        if (!decryptionPaths.isEmpty() && decryptionKey == null) {
            throw new IllegalArgumentException("Can't decrypt without decryption key!");
        }

        if (!encryptionPaths.isEmpty() && encryptionCertificate == null) {
            throw new IllegalArgumentException("Can't encrypt without encryption key!");
        }

        if (ivHeaderName != null && encryptedKeyHeaderName == null
                || ivHeaderName == null && encryptedKeyHeaderName != null) {
            throw new IllegalArgumentException("IV header name and encrypted key header name must be both set or both unset!");
        }

        if (ivFieldName != null && encryptedKeyFieldName == null
                || ivFieldName == null && encryptedKeyFieldName != null) {
            throw new IllegalArgumentException("IV field name and encrypted key field name must be both set or both unset!");
        }
    }

    private void computeEncryptionCertificateFingerprintWhenNeeded() throws EncryptionException {
        try {
            if (encryptionCertificate == null || !isNullOrEmpty(encryptionCertificateFingerprint)) {
                // No encryption certificate set or certificate fingerprint already provided
                return;
            }
            // you can change finger print hash calculation to sha256digestBytes
            byte[] certificateFingerprintBytes = sha1digestBytes(encryptionCertificate.getEncoded());
            encryptionCertificateFingerprint = encodeBytes(certificateFingerprintBytes, FieldLevelEncryptionConfig.FieldValueEncoding.HEX);
        } catch (Exception e) {
            throw new EncryptionException("Failed to compute encryption certificate fingerprint!", e);
        }
    }

    private void computeEncryptionKeyFingerprintWhenNeeded() throws EncryptionException {
        try {
            if (encryptionCertificate == null || !isNullOrEmpty(encryptionKeyFingerprint)) {
                // No encryption certificate set or key fingerprint already provided
                return;
            }
            byte[] keyFingerprintBytes = sha256digestBytes(encryptionCertificate.getPublicKey().getEncoded());
            encryptionKeyFingerprint = encodeBytes(keyFingerprintBytes, FieldLevelEncryptionConfig.FieldValueEncoding.HEX);
        } catch (Exception e) {
            throw new EncryptionException("Failed to compute encryption key fingerprint!", e);
        }
    }

    protected static byte[] sha256digestBytes(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

    protected static byte[] sha1digestBytes(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(bytes);
        return messageDigest.digest();
    }
}
