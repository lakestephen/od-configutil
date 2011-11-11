package od.configutil;

import od.configutil.util.ConfigManagerException;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 21-Nov-2010
 * Time: 11:30:18
 * To change this template use File | Settings | File Templates.
 */
public class NoConfigFoundException extends ConfigManagerException {

    public NoConfigFoundException(String message) {
        super(message);
    }

    public NoConfigFoundException(String message, Throwable t) {
        super(message, t);
    }
}
