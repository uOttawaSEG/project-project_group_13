import java.time.YearMonth;
import java.util.Objects;

public enum PaymentMethodType {
    CREDIT_CARD,
    DEBIT_CARD,
    APPLE_PAY,
}

public final class Cards {
    private final PaymentMethodType type;
    private final String cardnumber;
    private final YearMonth expirydate;
    private final String cvv;
    private final String applePayToken;

    public Cards(PaymentMethodType type,String cardnumber, YearMonth expirydate, String cvv, String applePayToken) {
        this.type = Objects.requireNonNull("type is required");
        this.cardnumber = cardnumber;
        this.expirydate = expirydate;
        this.cvv = cvv;
        this.applePayToken = applePayToken
    }

    public static Cards createCreditCard(String cardnumber, YearMonth expirydate, String cvv) {
        return new Cards(
            PaymentMethodType.CREDIT_CARD,
            valideateCardNumber(cardnumber),
            validateCvv(cvv),
            valideExpiryDate(expirydate),
            null
        );
    }

    public static Cards createDebitCard(String cardnumber, YearMonth expirydate, String cvv) {
        return new Cards(
            PaymentMethodType.DEBIT_CARD,
            valideateCardNumber(cardnumber),
            validateCvv(cvv),
            valideExpiryDate(expirydate),
            null
        );
    }

    public static Cards createDebitCard(String cardnumber, YearMonth expirydate, String cvv) {
        return new Cards(
            PaymentMethodType.APPLE_PAY,
            null,
            null,
            null,
            validateToken(token)
        );
    }    

    public String getMaskedNumber() {
        return "**** **** **** " + cardnumber.substring(cardnumber.length() - 4);
    }

    public String getCvv() {
        return cvv;
    }

    public YearMonth getExpiry() {
        return expirydate;
    }

}

