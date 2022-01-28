package encryption;

import static java.nio.charset.StandardCharsets.UTF_8;

import static encryption.FieldLevelEncryptionConfigBuilder.sha256digestBytes;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;

import yap.utils.EncodingUtils;


/**
 * RSA-SHA256 signature method. The RSA-SHA256 signature method uses the RSASSA-PKCS1-v1_5 signature algorithm as defined in RFC3447
 * section 8.2 (more simply known as PKCS#1), using SHA-1 as the hash function for EMSA-PKCS1-v1_5.
 *
 * <p>
 * The OAuth 1.0 Protocol <a href="https://tools.ietf.org/html/rfc3447">RFC 3447</a> is obsoleted by the OAuth 2.0 Authorization Framework <a href="https://tools.ietf.org/html/rfc6749">RFC 6749</a>.
 * <p>
 * <p>
 * digital signature algorithm as specified by PKCS#1 v2.1/RFC3447 (specifically
 * the method described in Section 8.2 RSASSA-PKCS1-v1_5) <a href="https://tools.ietf.org/html/rfc5849">RFC 3447</a>
 * 3. The RSA signature value shall be base64 encoded and constitute the value of
 * the digital signature within the TAV (the signature value should just be a
 * base64-encoded representation of the raw signature bytes).
 * <p>
 * The characters concatenated should be UTF-8 (also often referred to as ASCII or
 * ISO-8559-1) encoded and not EBCDIC or any other encoding.
 * <p>
 * The input to the digital signature algorithm consists of the UTF-8 encoded strings
 * concatenated together. There is no additional conversion required after the strings have
 * been concatenated.
 *
 * @author Irfan Arshad
 */

public class TAVSignatureMethod {

    /**
     * The name of this RSA-SHA1 signature method ("RSA-SHA256").
     */
    public static final String SIGNATURE_NAME = "RSA-SHA256";
    // Signature Algorithm
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Construct a RSA-SHA1 signature method with the given RSA-SHA1 public/private key pair.
     *
     * @param privateKey The private key.
     * @param publicKey  The public key.
     */
    public TAVSignatureMethod(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    /**
     * Construct a RSA-SHA1 signature method with the given RSA-SHA1 private key.  This constructor is to be
     * used by the consumer (who has access to its own private key).
     *
     * @param key The key.
     */
    public TAVSignatureMethod(PrivateKey key) {
        this(key, null);
    }

    /**
     * Construct a RSA-SHA1 signature method with the given RSA-SHA1 public key.  This constructor is to be
     * used by the provider (who has access to the public key of the consumer).
     *
     * @param key The key.
     */
    public TAVSignatureMethod(PublicKey key) {
        this(null, key);
    }

    /**
     * The name of this RSA-SHA1 signature method ("RSA-SHA1").
     *
     * @return The name of this RSA-SHA1 signature method.
     */
    public String getName() {
        return SIGNATURE_NAME;
    }

    /**
     * The Signature Base String is signed using the Consumer’s RSA private key per RFC3447 section 8.2.1, where K is the Consumer’s RSA private key,
     * M the Signature Base String, and S is the result signature octet string:
     * <p>
     * {@code S = RSASSA-PKCS1-V1_5-SIGN (K, M) }
     * <p>
     * oauth_signature is set to S, first base64-encoded per RFC2045 section 6.8, then URL-encoded per Parameter Encoding.
     *
     * @param signatureBaseString The signature base string.
     * @return The signature.
     * @throws InvalidSignatureException If there is no private key.
     */
    private String sign(String signatureBaseString) throws InvalidSignatureException {
        if (privateKey == null) {
            throw new InvalidSignatureException("Cannot sign the base string: no private key supplied.");
        }

        try {
//            generateTestPrivateKey();
            Signature signer = Signature.getInstance(SIGNATURE_ALGORITHM);
            signer.initSign(privateKey);
//            signatureBaseString = "helloWOrld";
            signer.update(signatureBaseString.getBytes(UTF_8));
            byte[] signatureBytes = signer.sign();
            String hashData = EncodingUtils.hexEncode(sha256digestBytes(signatureBaseString.getBytes(UTF_8)));
            System.out.println("TAVSignatureMethod HASH_DATA>>" + hashData);
            System.out.println("TAVSignatureMethod Signed_Hashed_Data>>" + EncodingUtils.hexEncode(signatureBytes));
            return EncodingUtils.base64Encode(signatureBytes);//new String(signatureBytes, UTF_8);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new InvalidSignatureException("Invalid signature for signature method:" + e.getLocalizedMessage(), e);
        }
    }

    /**
     * Verify the signature of the given signature base string. The signature is verified by generating a new request signature octet string, and comparing it
     * to the signature provided by the Consumer, first URL-decoded per Parameter Encoding, then base64-decoded per RFC2045 section 6.8. The signature is
     * generated using the request parameters as provided by the Consumer, and the Consumer Secret and Token Secret as stored by the Service Provider.
     *
     * @param signatureBaseString The signature base string.
     * @param signature           The signature.
     * @throws InvalidSignatureException     If the signature is invalid for the specified base string.
     * @throws UnsupportedOperationException If there is no public key.
     */
    public boolean verify(String signatureBaseString, byte[] signature) throws InvalidSignatureException {
        if (publicKey == null) {
            throw new UnsupportedOperationException("A public key must be provided to verify signatures.");
        }
        try {
            byte[] signatureBytes = signature;//EncodingUtils.base64Decode(signature);//Base64.decode(signature.getBytes(UTF_8), Base64.DEFAULT);
            Signature verifier = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifier.initVerify(publicKey);
            verifier.update(signatureBaseString.getBytes(UTF_8));
            if (!verifier.verify(signatureBytes)) {
                throw new InvalidSignatureException("Invalid signature for signature method " + getName());
            }
            return true;
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            throw new IllegalStateException(e);
        }
    }

    @Deprecated
    public static String createDigitalSignature(TAVSignatureConfig config) throws InvalidSignatureException {
        TAVSignatureMethod signatureMethod = new TAVSignatureMethod(config.privateKey);
        return signatureMethod.sign(config.concatenatedData.toString());
    }

    public static String createBase64DigitalSignature(TAVSignatureConfig config) throws InvalidSignatureException {
        TAVSignatureMethod signatureMethod = new TAVSignatureMethod(config.privateKey, config.publicKey);
        String signature = signatureMethod.sign(config.concatenatedData.toString());
        System.out.println("TAVSignatureMethod Bas64 Encoded Data>>" + signature);
        String toJson = null;
        if (config.tavFormat == TAVSignatureConfig.TAVFormat.TAV_FORMAT_2) {
            TAVStructure2 tavStructur2 = new TAVStructure2(config.tavFormat.version, SIGNATURE_NAME, true, false, signature);
            toJson = new GsonBuilder().disableHtmlEscaping().create().toJson(tavStructur2);
        } else if (config.tavFormat == TAVSignatureConfig.TAVFormat.TAV_FORMAT_3) {
            TAVStructure3 tavStructure = new TAVStructure3(config.tavFormat.version, SIGNATURE_NAME, config.dataValidUntilTimestamp, config.includedFieldsInOrder.toString(), signature);
            toJson = new GsonBuilder().disableHtmlEscaping().create().toJson(tavStructure);
        }
        if (toJson == null)
            throw new InvalidSignatureException(" TAV signature Json can not be null");
        System.out.println("TAVSignatureMethod JSON Payload>>" + toJson);
        byte[] data = toJson.getBytes(UTF_8);
        System.out.println("TAVSignatureMethod Base64 Encoded JSON Payload>>" + EncodingUtils.base64Encode(data));
        return EncodingUtils.base64Encode(data);
    }

    /**
     * The private key.
     *
     * @return The private key.
     */
    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    /**
     * The private key.
     *
     * @return The private key.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    private static abstract class TAVStructure {

        @SerializedName("version")
        public String version;
        @SerializedName("signatureAlgorithm")
        public String signatureAlgorithm;
        @SerializedName("signature")
        public String signature;

        public TAVStructure(String version, String signatureAlgorithm, String signature) {
            this.version = version;
            this.signatureAlgorithm = signatureAlgorithm;
            this.signature = signature;
        }
    }

    private static class TAVStructure3 extends TAVStructure {

        @SerializedName("dataValidUntilTimestamp")
        public String dataValidUntilTimestamp;
        @SerializedName("includedFieldsInOrder")
        public String includedFieldsInOrder;

        public TAVStructure3(String version, String signatureAlgorithm, String dataValidUntilTimestamp, String includedFieldsInOrder, String signature) {
            super(version, signatureAlgorithm, signature);
            this.dataValidUntilTimestamp = dataValidUntilTimestamp;
            this.includedFieldsInOrder = includedFieldsInOrder;
        }
    }

    private static class TAVStructure2 extends TAVStructure {

        @SerializedName("expirationDateIncluded")
        public boolean expirationDateIncluded;
        @SerializedName("tokenUniqueReferenceIncluded")
        public boolean tokenUniqueReferenceIncluded;

        public TAVStructure2(String version, String signatureAlgorithm, boolean expirationDateIncluded, boolean tokenUniqueReferenceIncluded, String signature) {
            super(version, signatureAlgorithm, signature);
            this.expirationDateIncluded = expirationDateIncluded;
            this.tokenUniqueReferenceIncluded = tokenUniqueReferenceIncluded;
        }
    }

    // Generating the asymmetric key pair for Testing purpose only
    // using SecureRandom class
    // functions and RSA algorithm.
    public PrivateKey generateTestPrivateKey() {
//            throws Exception {

        SecureRandom secureRandom
                = new SecureRandom();
        KeyPairGenerator keyPairGenerator
                = null;
        try {
            keyPairGenerator = KeyPairGenerator
                    .getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
        }
        keyPairGenerator
                .initialize(
                        2048, secureRandom);
        privateKey = keyPairGenerator
                .generateKeyPair().getPrivate();
        publicKey = keyPairGenerator
                .generateKeyPair().getPublic();
        return keyPairGenerator
                .generateKeyPair().getPrivate();
    }
}