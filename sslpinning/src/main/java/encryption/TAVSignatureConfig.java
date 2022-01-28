package encryption;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * A POJO for storing the TAV Signature configuration.
 */
public class TAVSignatureConfig {
    public StringBuilder includedFieldsInOrder = new StringBuilder();
    public StringBuilder concatenatedData = new StringBuilder();
    public String dataValidUntilTimestamp;
    public String expirationDateIncluded = "Y";
    public String tokenUniqueReferenceIncluded = "N";

    PrivateKey privateKey;
    PublicKey publicKey;
    TAVFormat tavFormat;

    protected TAVSignatureConfig() {

    }

    public enum TAVFormat {
        TAV_FORMAT_2("2"), TAV_FORMAT_3("3"), NAN("0");
        public String version;

        TAVFormat(String version) {
            this.version = version;
        }
    }
}
