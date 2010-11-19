package od.configutil;

import java.io.*;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 16:30:13
 */
public class FileSourceAndSink implements ConfigSink, ConfigSource {

    private File configDirectory;
    private String extension;

    public FileSourceAndSink(File configDirectory) {
        this(configDirectory, "xml");
    }

    public FileSourceAndSink(File configDirectory, String extension) {
        this.configDirectory = configDirectory;
        this.extension = extension;
    }

    public void saveConfiguration(ConfigData configuration) throws ConfigManagerException {
        String fileName = getConfigFileName(configuration.getConfigName(), configuration.getVersion());
        File configFile = new File(configDirectory, fileName);
        File backupFile = new File(configDirectory, fileName + ".bak");
        LogMethods.log.info("Writing configuration file at " + configFile);
        checkConfigDirectoryWritable();
        checkConfigFileWritableIfExists(configFile);
        checkConfigFileWritableIfExists(backupFile);

        File tempConfigFile = null;
        try {
            LogMethods.log.debug("About to create temp file");
            tempConfigFile = File.createTempFile("tempConfig", "." + extension, configDirectory);
            tempConfigFile.deleteOnExit();

            LogMethods.log.debug("About to write: " + tempConfigFile);
            writeStringToFile(tempConfigFile, configuration.getSerializedConfig());
            LogMethods.log.debug("Written: " + tempConfigFile);

            LogMethods.log.debug("About to delete " + backupFile);
            backupFile.delete();

            LogMethods.log.debug("About to rename old config: " + configFile + " to " + backupFile);
            configFile.renameTo(backupFile);

            LogMethods.log.debug("About to rename temp config: " + tempConfigFile + " to " + configFile);
            if (!tempConfigFile.renameTo(configFile)) {
                throw new IOException("Unable to rename temp config: " + tempConfigFile + " to new config: " + configFile);
            }
        } catch (IOException e) {
            LogMethods.log.error("Unable to save config: " + configFile, e);
            throw new ConfigManagerException("Unable to save config", e);
        } finally {
            if (tempConfigFile != null && tempConfigFile.exists()) {
                /*Yes this looks strange, but if there's been a problem closing
                the file, then the underlying outputstream is not closed
                which means that the file cannot be deleted because the stream
                still has a lock on it.  I've submitted a bug report to Sun about
                this.  One easy way to hit the bug if to have a full hard drive
                 - saving the configuration then results in the temporary file not
                being cleaned up.*/
                System.gc();
                tempConfigFile.delete();
             }
        }
    }

    public ConfigData loadConfiguration(String configName, List<Long> supportedVersions) throws ConfigManagerException {
        LogMethods.log.info("Searching for " + configName + " configuration in: " + configDirectory);

        checkConfigDirectoryReadable();
        Stack<Long> versions = new Stack<Long>();
        versions.addAll(supportedVersions);

        ConfigData configuration = null;
        while (! versions.isEmpty() && configuration == null) {
            long v = versions.pop();
            configuration = readConfig(configName, v);
         }
        return configuration;
    }

    private ConfigData readConfig(String configName, long v) {
        ConfigData result = null;
        try {
            InputStream configInputStream = getInputStream(configName, v);
            BufferedReader br = new BufferedReader(new InputStreamReader(configInputStream));
            if ( configInputStream != null) {
                if ( checkVersion(br, v) ) {
                    String config = convertStreamToString(br);
                    result = new ConfigData(configName, v, config);
                }
            }
        } catch (Exception e) {
            LogMethods.log.error("Error loading " + configName + " configuration version " + v + " looking for older configs..", e);
        }
        return result;
    }

    //expect to find configVersion=versionId on the first line
    //we strip this off before parsing the rest of the config
    private boolean checkVersion(BufferedReader br, long v) throws IOException {
        String version = br.readLine();

        boolean result = false;
        if ( version.startsWith("configVersion=")) {
            long versionNumber = Long.parseLong(version.substring(14));
            if ( versionNumber == v) {
                result = true;
            }
        }
        return result;
    }

    private InputStream getInputStream(String configName, long version) throws FileNotFoundException {
        InputStream configInputStream = null;
        File f = new File(configDirectory, getConfigFileName(configName, version));
        if (f.canRead()) {
            LogMethods.log.info("Found configuration file: " + f);
            configInputStream = new FileInputStream(f);
        } else {
            LogMethods.log.info("Could not " + (f.exists() ? "read" : "find") + " configuration file: " + f);
        }
        return configInputStream;
    }

    protected String getConfigFileName(String configName, long version) {
        return configName + "." + version + "." + extension;
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
            if ( br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return returnBuffer.toString();
    }

    private void writeStringToFile(File file, String text) throws IOException {
        System.out.println("Writing: " + file);
        if (text == null) {
            throw new IllegalArgumentException("Text is null");
        }
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            out.write(text);
        } finally {
            out.close();
        }
    }

    private void checkConfigDirectoryReadable() throws ConfigManagerException {
        if ( ! configDirectory.canRead() ) {
            throw new ConfigManagerException("Cannot read from config directory " + configDirectory);
        }
    }

    private void checkConfigDirectoryWritable() throws ConfigManagerException {
        if ( ! configDirectory.canWrite() ) {
            throw new ConfigManagerException("Cannot write to config directory " + configDirectory);
        }
    }

    private void checkConfigFileWritableIfExists(File configToWrite) throws ConfigManagerException {
        if ( configToWrite.exists() && ! configToWrite.canWrite()) {
            throw new ConfigManagerException("Cannot write to config file " + configToWrite);
        }
    }
}
