package od.configutil;

import java.net.URL;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 30-Apr-2010
 * Time: 10:06:47
 */
public class ClasspathMigrationLoader extends UrlMigrationLoader {

    private static final String DEFAULT_MIGRATION_PATH = "/configMigrations.xml";
    private String path;

    public ClasspathMigrationLoader() {
        this(DEFAULT_MIGRATION_PATH);
    }

    public ClasspathMigrationLoader(String path) {
        this.path = path;
    }

    protected List<URL> getURL() {
        List<URL> urls = new ArrayList<URL>();
        URL u = ClasspathMigrationLoader.class.getResource(
            path
        );
        if ( u != null ) {
            urls.add(u);
        } else {
            ConfigLogImplementation.logMethods.error("Failed to find configMigrations resource from " + path);
        }
        return urls;
    }
}
