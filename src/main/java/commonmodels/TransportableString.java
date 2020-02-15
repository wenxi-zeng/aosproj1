package commonmodels;

public class TransportableString extends Transportable {

    private final String value;

    public TransportableString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

}
