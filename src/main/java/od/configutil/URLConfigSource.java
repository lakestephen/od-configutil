package od.configutil;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 20-Nov-2010
 * Time: 09:43:06
 *
 * Load a config from a URL which is determined from the context URL provided
 * and the file name
 */
public class URLConfigSource extends AbstractConfigSource {

    private URL context;

    public URLConfigSource(URL context) {
        this(context, "UTF-8");
    }
    
    public URLConfigSource(URL context, String encoding) {
        super(encoding);
        this.context = context;
    }

    @Override
    protected void loadStarting(String configName) throws ConfigManagerException {
        LogMethods.log.info("Loading config " + configName + " from context");
    }

    @Override
    protected InputStream getInputStream(String configFileName) throws Exception {
        URL fileUrl = new URL(context, configFileName);
        return fileUrl.openStream();
    }

    @Override
    protected String getConfigFileName(String configName, long version) {
        return getUrlSpec(configName);
    }

    protected String getUrlSpec(String configName) {
        return configName;
    }
}
