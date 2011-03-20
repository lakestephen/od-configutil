package od.configutil;

/**
* Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 20/03/11
* Time: 17:45
*/
public class TestConfig {

    private String stringField = "TestConfig";

    public String getStringField() {
        return stringField;
    }

    public void setStringField(String stringField) {
        this.stringField = stringField;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestConfig that = (TestConfig) o;

        if (stringField != null ? !stringField.equals(that.stringField) : that.stringField != null) return false;

        return true;
    }

    public int hashCode() {
        return stringField != null ? stringField.hashCode() : 0;
    }
}
