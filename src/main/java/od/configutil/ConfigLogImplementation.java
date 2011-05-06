package od.configutil;

/**
* Created by IntelliJ IDEA.
* User: Nick
* Date: 06/05/11
* Time: 11:30
* To change this template use File | Settings | File Templates.
*/
public class ConfigLogImplementation implements LogMethods {

    public static volatile LogMethods logMethods = new ConfigLogImplementation();

    public static void setLogMethods(LogMethods m) {
        ConfigLogImplementation.logMethods = m;
    }

    public void info(String s) {
        System.out.println("Configuration --> INFO " + s);
    }

    public void error(String description, Throwable cause) {
        System.err.println("Configuration --> ERROR " + description + " " + cause);
        cause.printStackTrace();
    }

    public void error(String description) {
        System.err.println("Configuration --> ERROR " + description);
    }

    public void debug(String s) {
        System.out.println("Configuration --> DEBUG " + s);
    }

    public void warn(String s) {
        System.out.println("Configuration --> WARN " + s);
    }
}
