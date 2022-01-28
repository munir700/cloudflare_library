package yap.utils;

import static java.nio.charset.StandardCharsets.UTF_8;

import static yap.utils.EncodingUtils.base64Decode;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

/**
 * Utility class for loading certificates and keys.
 */
public final class EncryptionUtils {

    private static final String PKCS_1_PEM_HEADER = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PKCS_1_PEM_FOOTER = "-----END RSA PRIVATE KEY-----";
    private static final String PKCS_8_PEM_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PKCS_8_PEM_FOOTER = "-----END PRIVATE KEY-----";

    private static final String PUK_PEM_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PUK_PEM_FOOTER = "-----END PUBLIC KEY-----";
    private static final int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;
    // buffer size used for reading and writing
    private static final int BUFFER_SIZE = 8192;

    private EncryptionUtils() {
    }

    /**
     * Populate a X509 encryption certificate object with the certificate data at the given file path.
     */
    public static Certificate loadEncryptionCertificate(String certificatePath) throws CertificateException, FileNotFoundException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(new FileInputStream(certificatePath));
    }

    /**
     * Populate a X509 encryption certificate object with the certificate data at the given InputStream
     */
    public static Certificate loadEncryptionCertificate(InputStream iss) throws CertificateException, FileNotFoundException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        return factory.generateCertificate(iss);
    }

    /**
     * Load a RSA decryption key from a file (PEM or DER).
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static PrivateKey loadDecryptionKey(String keyFilePath) throws GeneralSecurityException, IOException {
        byte[] keyDataBytes = Files.readAllBytes(Paths.get(keyFilePath));

        String keyDataString = new String(keyDataBytes, UTF_8);

        if (keyDataString.contains(PKCS_1_PEM_HEADER)) {
            // OpenSSL / PKCS#1 Base64 PEM encoded file
            keyDataString = keyDataString.replace(PKCS_1_PEM_HEADER, "");
            keyDataString = keyDataString.replace(PKCS_1_PEM_FOOTER, "");
            return readPkcs1PrivateKey(base64Decode(keyDataString));
        }

        if (keyDataString.contains(PKCS_8_PEM_HEADER)) {
            // PKCS#8 Base64 PEM encoded file
            keyDataString = keyDataString.replace(PKCS_8_PEM_HEADER, "");
            keyDataString = keyDataString.replace(PKCS_8_PEM_FOOTER, "");
            return readPkcs8PrivateKey(base64Decode(keyDataString));
        }
        // We assume it's a PKCS#8 DER encoded binary file
        return readPkcs8PrivateKey(Files.readAllBytes(Paths.get(keyFilePath)));
    }

    /**
     * Load a RSA decryption key from a file (PEM or DER).
     */
    public static PrivateKey loadDecryptionKey(InputStream source) throws GeneralSecurityException, IOException {
        byte[] keyDataBytes = read(source, source.available());

        String keyDataString = new String(keyDataBytes, UTF_8);

        if (keyDataString.contains(PKCS_1_PEM_HEADER)) {
            // OpenSSL / PKCS#1 Base64 PEM encoded file
            keyDataString = keyDataString.replace(PKCS_1_PEM_HEADER, "");
            keyDataString = keyDataString.replace(PKCS_1_PEM_FOOTER, "");
            keyDataString = keyDataString.replace(System.lineSeparator(), "");
            return readPkcs1PrivateKey(base64Decode(keyDataString));
        }

        if (keyDataString.contains(PKCS_8_PEM_HEADER)) {
//            generateTestPrivateKey();
            // PKCS#8 Base64 PEM encoded file
            keyDataString = keyDataString.replace(PKCS_8_PEM_HEADER, "");
            keyDataString = keyDataString.replace(PKCS_8_PEM_FOOTER, "");
            keyDataString = keyDataString.replace(System.lineSeparator(), "");
            return readPkcs8PrivateKey(base64Decode(keyDataString));
        }
        // We assume it's a PKCS#8 DER encoded binary file
        return readPkcs8PrivateKey(read(source, source.available()));
    }

    private static byte[] read(InputStream source, int initialSize) throws IOException {
        int capacity = initialSize;
        byte[] buf = new byte[capacity];
        int nread = 0;
        int n;
        for (; ; ) {
            // read to EOF which may read more or less than initialSize (eg: file
            // is truncated while we are reading)
            while ((n = source.read(buf, nread, capacity - nread)) > 0)
                nread += n;

            // if last call to source.read() returned -1, we are done
            // otherwise, try to read one more byte; if that failed we're done too
            if (n < 0 || (n = source.read()) < 0)
                break;

            // one more byte was read; need to allocate a larger buffer
            if (capacity <= MAX_BUFFER_SIZE - capacity) {
                capacity = Math.max(capacity << 1, BUFFER_SIZE);
            } else {
                if (capacity == MAX_BUFFER_SIZE)
                    throw new OutOfMemoryError("Required array size too large");
                capacity = MAX_BUFFER_SIZE;
            }
            buf = Arrays.copyOf(buf, capacity);
            buf[nread++] = (byte) n;
        }
        return (capacity == nread) ? buf : Arrays.copyOf(buf, nread);
    }

    /**
     * Load a RSA decryption key out of a PKCS#12 container.
     */
    public static PrivateKey loadDecryptionKey(String pkcs12KeyFilePath,
                                               String decryptionKeyAlias,
                                               String decryptionKeyPassword) throws GeneralSecurityException, IOException {
        KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
        pkcs12KeyStore.load(new FileInputStream(pkcs12KeyFilePath), decryptionKeyPassword.toCharArray());
        return (PrivateKey) pkcs12KeyStore.getKey(decryptionKeyAlias, decryptionKeyPassword.toCharArray());
    }

    public static PrivateKey loadDecryptionKey(InputStream is,
                                               String decryptionKeyAlias,
                                               String decryptionKeyPassword) throws GeneralSecurityException, IOException {
        KeyStore pkcs12KeyStore = KeyStore.getInstance("PKCS12");
        pkcs12KeyStore.load(is, decryptionKeyPassword.toCharArray());
        return (PrivateKey) pkcs12KeyStore.getKey(decryptionKeyAlias, decryptionKeyPassword.toCharArray());
    }

    /**
     * Create a PrivateKey instance from raw PKCS#8 bytes.
     */
    private static PrivateKey readPkcs8PrivateKey(byte[] pkcs8Bytes) throws GeneralSecurityException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Bytes);
        try {
            return keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Unexpected key format!", e);
        }
    }

    /**
     * Create a PrivateKey instance from raw PKCS#1 bytes.
     */
    private static PrivateKey readPkcs1PrivateKey(byte[] pkcs1Bytes) throws GeneralSecurityException {
        // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
        int pkcs1Length = pkcs1Bytes.length;
        int totalLength = pkcs1Length + 22;
        byte[] pkcs8Header = new byte[]{
                0x30, (byte) 0x82, (byte) ((totalLength >> 8) & 0xff), (byte) (totalLength & 0xff), // Sequence + total length
                0x2, 0x1, 0x0, // Integer (0)
                0x30, 0xD, 0x6, 0x9, 0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
                0x4, (byte) 0x82, (byte) ((pkcs1Length >> 8) & 0xff), (byte) (pkcs1Length & 0xff) // Octet string + length
        };
        byte[] pkcs8bytes = join(pkcs8Header, pkcs1Bytes);
        return readPkcs8PrivateKey(pkcs8bytes);
    }

    private static byte[] join(byte[] byteArray1, byte[] byteArray2) {
        byte[] bytes = new byte[byteArray1.length + byteArray2.length];
        System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.length);
        System.arraycopy(byteArray2, 0, bytes, byteArray1.length, byteArray2.length);
        return bytes;
    }

    public static PublicKey loadPublicKey(InputStream source)
            throws Exception {

        byte[] keyBytes = read(source, source.available());
        String keyDataString = new String(keyBytes, UTF_8);
        keyDataString = keyDataString.replace(PUK_PEM_HEADER, "");
        keyDataString = keyDataString.replace(PUK_PEM_FOOTER, "");
        X509EncodedKeySpec spec =
                new X509EncodedKeySpec(base64Decode(keyDataString));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
