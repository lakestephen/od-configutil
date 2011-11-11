package od.configutil.sinkandsource;

import od.configutil.util.ConfigManagerException;

import java.io.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 19-Nov-2010
 * Time: 23:56:31
 * 
 */
public abstract class AbstractConfigSink implements ConfigSink {

    private String textFileEncoding;

    public AbstractConfigSink(String textFileEncoding) {
        this.textFileEncoding = textFileEncoding;
    }

    public URL saveConfiguration(ConfigData configuration) throws ConfigManagerException {
        String fileName = getConfigFileName(configuration.getConfigName(), configuration.getVersion());
        try {
            return writeConfig(configuration, fileName);
        } catch ( Throwable t ) {
            if ( t instanceof ConfigManagerException ) {
                throw (ConfigManagerException)t; //re-throw to preserve stack
            } else {
                throw new ConfigManagerException("Error saving config", t);
            }
        }
    }

    protected abstract String getConfigFileName(String configName, long version);

    protected abstract URL writeConfig(ConfigData configuration, String fileName) throws Exception;

    protected void writeConfigToStream(OutputStream outStream, String text, long version) throws IOException {
        if (text == null) {
            throw new IllegalArgumentException("Text is null");
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream, textFileEncoding));
        //write configVersion=versionId on the first line
        out.append("<!-- "); //use a comment format which works well with sgml and derivatives
        out.append(ConfigSource.CONFIG_VERSION_PREFIX);
        out.append(String.valueOf(version));
        out.append(" -->");
        out.newLine();

        out.write(text);
        out.flush();
    }
}
