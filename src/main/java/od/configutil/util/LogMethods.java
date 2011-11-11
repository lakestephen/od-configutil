package od.configutil.util;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 14:36:21
 */
public interface LogMethods {

    void info(String s);

    void error(String description, Throwable cause);

    void error(String description);

    void debug(String s);

    void warn(String s);

}