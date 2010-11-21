package od.configutil;

import java.io.*;
import java.util.*;

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

    public ConfigData loadConfiguration(String configName, SortedSet<Long> supportedVersions) throws ConfigManagerException {
        loadStarting(configName);

        //get a list of possible file names based on configName and supported versions
        //this may contain a number of possible filenames, where the version number is part of the name
        //alternatively, a single possible file name may be returned
        //we expect each file to contain the version number token, so we can find a version in either case
        //the first file name in list returned will be examined first, and first config file found with a supported version will be used
        //we want a list of filenames with highest version first
        List<Long> versionList = new ArrayList<Long>(supportedVersions);
        Collections.reverse(versionList);
        List<String> fileNameList = getFileNames(configName, versionList);

        //check each file it turn to see if it contains an acceptable config/version and return the first found
        ConfigData configuration = null;
        for ( String fileName : fileNameList ) {
            configuration = readConfig(configName, fileName, supportedVersions);
            if ( configuration != null) {
                break;
            }
        }
        return configuration;
    }

    /**
     * @return a List of possible filenames derived from configName and supportedVersions, first item in list takes priority
     */
    protected abstract List<String> getFileNames(String configName, List<Long> supportedVersions);

    protected abstract void loadStarting(String configName) throws ConfigManagerException;

    private ConfigData readConfig(String configName, String fileName, SortedSet<Long> supportedVersions) {
        ConfigData result = null;
        try {
            InputStream configInputStream = getInputStream(fileName);
            if (configInputStream != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(configInputStream, textFileEncoding));
                long fileVersion = checkVersion(br, supportedVersions, fileName);
                if (fileVersion != -1) {
                    String config = convertStreamToString(br);
                    result = new ConfigData(configName, fileVersion, config);
                }
            }
        } catch (Throwable t) {
            LogMethods.log.error("Error loading " + configName + " configuration version " + supportedVersions + " looking for older configs..", t);
        }
        return result;
    }

    protected abstract InputStream getInputStream(String configFileName) throws Exception;

    //expect to find configVersion=versionId on the first line
    //we strip this off before parsing the rest of the config
    protected long checkVersion(BufferedReader br, SortedSet<Long> requiredVersion, String fileName) throws IOException {
        String firstLine = br.readLine();

        long result = -1;
        if (firstLine.startsWith(CONFIG_VERSION_PREFIX)) {
            long versionNumber = Long.parseLong(firstLine.substring(CONFIG_VERSION_PREFIX.length()));
            if (requiredVersion.contains(versionNumber)) {
                result = versionNumber;
            }
        } else {
            LogMethods.log.warn("Could not find configVersion in file " + fileName + ", will skip this file");
        }
        return result;
    }

    protected String convertStreamToString(BufferedReader br) throws IOException {
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
