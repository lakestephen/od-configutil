package od.configutil.migration;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: GA2EBBU
 * Date: 11/11/11
 * Time: 19:43
 *
 * Combine URLs for two or more separate URLMigrationLoader
 */
public class AggregatedURLMigrationLoader extends UrlMigrationLoader {

    private UrlMigrationLoader[] loaders;

    public AggregatedURLMigrationLoader(UrlMigrationLoader... loaders) {
        this.loaders = loaders;
    }

    protected List<URL> getURL() {
        List<URL> urls = new LinkedList<URL>();
        for ( UrlMigrationLoader l : loaders) {
            urls.addAll(l.getURL());
        }
        return urls;
    }
}
