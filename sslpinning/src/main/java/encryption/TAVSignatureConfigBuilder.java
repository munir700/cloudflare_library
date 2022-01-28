package encryption;

import static encryption.FieldLevelEncryption.isNullOrEmpty;
import static encryption.TAVSignatureConfig.TAVFormat.TAV_FORMAT_2;
import static encryption.TAVSignatureConfig.TAVFormat.TAV_FORMAT_3;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class TAVSignatureConfigBuilder {
    private final static String ACCOUNT_NUMBER_FIELD = "accountNumber";
    private final static String ACCOUNT_EXPIRY_FIELD = "accountExpiry";
    private final static String DATE_VALID_UNTIL_TIMESTAMP_FIELD = "dataValidUntilTimestamp";

    private Map<String, String> signaturePaths = new HashMap<>();

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private TAVSignatureConfig.TAVFormat tavFormat = TAVSignatureConfig.TAVFormat.NAN;


    private TAVSignatureConfigBuilder() {
    }

    public static TAVSignatureConfigBuilder aTAVSignatureConfig() {
        return new TAVSignatureConfigBuilder();
    }

    public TAVSignatureConfigBuilder withPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
        return this;
    }

    public TAVSignatureConfigBuilder withTavFormat(TAVSignatureConfig.TAVFormat tavFormat) {
        this.tavFormat = tavFormat;
        return this;
    }

    public TAVSignatureConfigBuilder withPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    /**
     * accountNumber. Funding Primary Account Number (FPAN) of the payment
     * card being tokenized.
     * Primary Account Number of the card to be tokenized
     */
    public TAVSignatureConfigBuilder withAccountNumber(String accountNumber) {
        if (isNullOrEmpty(ACCOUNT_NUMBER_FIELD)) {
            throw new NullPointerException("Can't create digital signature  without accountNumber key!");
        }
        if (isNullOrEmpty(accountNumber)) {
            throw new NullPointerException("Can't create digital signature  without accountNumber value!");
        }
        if (signaturePaths.containsKey(ACCOUNT_NUMBER_FIELD)) {
            throw new IllegalArgumentException("accountNumber already added. check accountNumber should not be added more then one time");
        }
        signaturePaths.put(ACCOUNT_NUMBER_FIELD, accountNumber);
        return this;
    }

    /**
     * accountExpiry. Expiration date of the payment card being tokenized in MMYY
     * format
     * Expiration Date of the card to be tokenized
     */
    public TAVSignatureConfigBuilder withAccountExpiry(String accountExpiry) {
        if (isNullOrEmpty(ACCOUNT_EXPIRY_FIELD)) {
            throw new NullPointerException("Can't create digital signature  without accountExpiry key!");
        }
        if (isNullOrEmpty(accountExpiry)) {
            throw new NullPointerException("Can't create digital signature  without accountExpiry value!");
        }
        if (signaturePaths.containsKey(ACCOUNT_EXPIRY_FIELD)) {
            throw new IllegalArgumentException("accountExpiry already added. check accountExpiry should not be added more then one time");
        }
        signaturePaths.put(ACCOUNT_EXPIRY_FIELD, accountExpiry);
        return this;
    }

    /**
     * This time stamp field
     * must be included, otherwise the TAV validation fails
     * <p>
     * ISO 8601 format of the date and time (with Time Zone) the TAV expires
     * and will no longer be honored
     * <p>
     * Must be expressed in ISO 8601 extended format as
     * one of the following, where [.sss] is optional and can
     * be 1–3 digits:
     * • YYYY-MM-DDThh:mm:ss[.sss]Z
     * • YYYY-MM-DDThh:mm:ss[.sss]±hh:mm
     * Must be a value no more than 30 days in the future.
     * Mastercard recommends using a value of Current
     * Time + 30 minutes.
     */
    public TAVSignatureConfigBuilder withDataValidUntilTimestamp(String accountExpiryFieldValue) {
        if (isNullOrEmpty(DATE_VALID_UNTIL_TIMESTAMP_FIELD)) {
            throw new NullPointerException("Can't create digital signature  without dataValidUntilTimestamp key!");
        }
        if (isNullOrEmpty(accountExpiryFieldValue)) {
            throw new NullPointerException("Can't create digital signature  without dataValidUntilTimestamp value!");
        }
        if (signaturePaths.containsKey(DATE_VALID_UNTIL_TIMESTAMP_FIELD)) {
            throw new IllegalArgumentException("dataValidUntilTimestamp already added. check dataValidUntilTimestamp should not be added more then one time");
        }
        signaturePaths.put(DATE_VALID_UNTIL_TIMESTAMP_FIELD, accountExpiryFieldValue);
        return this;
    }

    public TAVSignatureConfig build() {
        TAVSignatureConfig config = new TAVSignatureConfig();
        config.privateKey = this.privateKey;
        config.publicKey = this.publicKey;
        config.tavFormat = this.tavFormat;
        checkParameterConsistency();
        if (config.tavFormat == TAV_FORMAT_2) {
            config.concatenatedData.append(signaturePaths.get(ACCOUNT_NUMBER_FIELD))
                    .append(config.expirationDateIncluded)
                    .append(signaturePaths.get(ACCOUNT_EXPIRY_FIELD))
                    .append(config.tokenUniqueReferenceIncluded);
            return config;
        } else if (config.tavFormat == TAV_FORMAT_3) {
            config.concatenatedData.append(signaturePaths.get(ACCOUNT_NUMBER_FIELD))
                    .append("|")
                    .append(signaturePaths.get(ACCOUNT_EXPIRY_FIELD))
                    .append("|")
                    .append(signaturePaths.get(DATE_VALID_UNTIL_TIMESTAMP_FIELD));

            config.includedFieldsInOrder.append(ACCOUNT_NUMBER_FIELD)
                    .append("|")
                    .append(ACCOUNT_EXPIRY_FIELD)
                    .append("|")
                    .append(DATE_VALID_UNTIL_TIMESTAMP_FIELD);
            config.dataValidUntilTimestamp = signaturePaths.get(DATE_VALID_UNTIL_TIMESTAMP_FIELD);
            return config;
        }

        return null;
    }

    private void checkParameterConsistency() {
        if(tavFormat ==  TAV_FORMAT_3 && !signaturePaths.containsKey(DATE_VALID_UNTIL_TIMESTAMP_FIELD) ){
            throw new NullPointerException("Can't create digital signature  without dataValidUntilTimestamp key! use withDataValidUntilTimestamp to add dataValidUntilTimestamp");
        }
        if (privateKey == null) {
            throw new IllegalArgumentException("Can't create digital signature  without privateKey key!");
        }
        if (tavFormat == TAVSignatureConfig.TAVFormat.NAN) {
            throw new IllegalArgumentException("Can't create digital signature! tavFormat should be TAV_FORMAT_2 or TAV_FORMAT_3 ");
        }
    }


}
