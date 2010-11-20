package od.configutil;

import java.io.*;

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

    public void saveConfiguration(ConfigData configuration) throws ConfigManagerException {
        String fileName = getConfigFileName(configuration.getConfigName(), configuration.getVersion());
        writeConfig(configuration, fileName);
    }

    protected abstract String getConfigFileName(String configName, long version);

    protected abstract void writeConfig(ConfigData configuration, String fileName) throws ConfigManagerException;

    protected void writeConfigToStream(OutputStream outStream, String text, long version) throws IOException {
        if (text == null) {
            throw new IllegalArgumentException("Text is null");
        }
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream, textFileEncoding));
        try {
            //write configVersion=versionId on the first line
            out.append(ConfigSource.CONFIG_VERSION_PREFIX);
            out.append(String.valueOf(version));
            out.newLine();

            out.write(text);
        } finally {
            out.flush();
            out.close();
        }
    }
}
