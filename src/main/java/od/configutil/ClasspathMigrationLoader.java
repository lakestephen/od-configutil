package od.configutil;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 30-Apr-2010
 * Time: 10:06:47
 */
public class ClasspathMigrationLoader extends UrlMigrationLoader {

    private static final String DEFAULT_MIGRATION_PATH = "/configMigrations.xml";

    public ClasspathMigrationLoader() {
        super(getDefaultUrl(DEFAULT_MIGRATION_PATH));
    }

    public ClasspathMigrationLoader(String path) {
        super(getDefaultUrl(path));
    }

    private static List<URL> getDefaultUrl(String path) {
        List<URL> urls = new ArrayList<URL>();
        URL u = ClasspathMigrationLoader.class.getResource(
            path
        );
        if ( u != null ) {
            urls.add(u);
        } else {
            LogMethods.log.error("Failed to find default configMigrations resource from " + path);
        }
        return urls;
    }
}
