package od.configutil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: GA2EBBU
 * Date: 11/11/11
 * Time: 16:34
 *
 * Add a URL migration loader for a URL specified as a system property
 */
public class SysPropertyURLMigrationLoader extends UrlMigrationLoader {

    private static final String MIGRATION_PROPERTY_NAME = "configMigrationURL";
    private String systemPropertyName;

    public SysPropertyURLMigrationLoader() {
        this(MIGRATION_PROPERTY_NAME);
    }

    public SysPropertyURLMigrationLoader(String systemPropertyName) {
        this.systemPropertyName = systemPropertyName;
    }

    protected List<URL> getURL() {
        List<URL> urls = new LinkedList<URL>();
        String url = System.getProperty(systemPropertyName);
        if ( url != null) {
            try {
                URL u = new URL(url);
                urls.add(u);
            } catch (MalformedURLException e) {
                ConfigLogImplementation.logMethods.error("Could not create a valid URL from migrations system property " + systemPropertyName + " with value " + url, e);
            }
        }
        return urls;
    }
}
