package od.configutil;

import java.io.*;
import java.util.List;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 19-Nov-2010
 * Time: 23:40:46
 *
 * An abstract superclass for classes implementing ConfigSource
 */
public abstract class AbstractConfigSource implements ConfigSource {

    private String textFileEncoding;

    public AbstractConfigSource(String textFileEncoding) {
        this.textFileEncoding = textFileEncoding;
    }

    public ConfigData loadConfiguration(String configName, List<Long> supportedVersions) throws ConfigManagerException {
        loadStarting(configName);

        Stack<Long> versions = new Stack<Long>();
        versions.addAll(supportedVersions);

        ConfigData configuration = null;
        while (!versions.isEmpty() && configuration == null) {
            long v = versions.pop();
            configuration = readConfig(configName, v);
        }
        return configuration;
    }

    protected abstract void loadStarting(String configName) throws ConfigManagerException;

    private ConfigData readConfig(String configName, long requiredVersion) {
        ConfigData result = null;
        try {
            String fileName = getConfigFileName(configName, requiredVersion);
            InputStream configInputStream = getInputStream(fileName);
            if (configInputStream != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(configInputStream, textFileEncoding));
                if (checkVersion(br, requiredVersion, fileName)) {
                    String config = convertStreamToString(br);
                    result = new ConfigData(configName, requiredVersion, config);
                }
            }
        } catch (Exception e) {
            LogMethods.log.error("Error loading " + configName + " configuration version " + requiredVersion + " looking for older configs..", e);
        }
        return result;
    }

    protected abstract InputStream getInputStream(String configFileName) throws Exception;

    protected abstract String getConfigFileName(String configName, long version);

    //expect to find configVersion=versionId on the first line
    //we strip this off before parsing the rest of the config
    private boolean checkVersion(BufferedReader br, long requiredVersion, String fileName) throws IOException {
        String firstLine = br.readLine();

        boolean result = false;
        if (firstLine.startsWith(CONFIG_VERSION_PREFIX)) {
            long versionNumber = Long.parseLong(firstLine.substring(CONFIG_VERSION_PREFIX.length()));
            if (versionNumber == requiredVersion) {
                result = true;
            }
        } else {
            LogMethods.log.warn("Could not find configVersion in file " + fileName + ", will skip this file");
        }
        return result;
    }

    private String convertStreamToString(BufferedReader br) throws IOException {
        final int BUF_SIZE = 4096;
        final char[] BUFFER = new char[BUF_SIZE];

        StringBuffer returnBuffer = new StringBuffer();
        try {
            int bytesRead;
            while ((bytesRead = br.read(BUFFER)) != -1) {
                returnBuffer.append(new String(BUFFER, 0, bytesRead));
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return returnBuffer.toString();
    }
}
