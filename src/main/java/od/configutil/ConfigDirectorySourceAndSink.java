package od.configutil;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 16:30:13
 *
 * ConfigDirectorySourceAndSink is the default source and sink for ConfigManager
 * It saves and loads config files to/from a config directory.
 *
 * File names are calculated from the configName and the version number
 * The version number forms part of the fileName, so that previous config versions can be kept
 *
 * When an attempt is made to load a config using a configName, ConfigDirectorySourceAndSink attempts to
 * find possible config files, starting with the most recent version number, and returns ConfigData from the
 * first valid config found.
 */
public class ConfigDirectorySourceAndSink implements ConfigSink, ConfigSource {

    private File configDirectory;
    private String extension;
    private String textFileEncoding;
    private DirectorySource dirSource;
    private DirectorySink dirSink;

    public ConfigDirectorySourceAndSink(File configDirectory) {
        this(configDirectory, "xml", ConfigUtilConstants.DEFAULT_TEXT_ENCODING);
    }

    public ConfigDirectorySourceAndSink(File configDirectory, String extension, String textFileEncoding) {
        this.configDirectory = configDirectory;
        this.extension = extension;
        this.textFileEncoding = textFileEncoding;
        this.dirSource = new DirectorySource();
        this.dirSink = new DirectorySink();
    }

    public ConfigData loadConfiguration(String configName, SortedSet<Long> supportedVersions) throws ConfigManagerException {
        return dirSource.loadConfiguration(configName, supportedVersions);
    }

    public URL saveConfiguration(ConfigData configuration) throws ConfigManagerException {
        return dirSink.saveConfiguration(configuration);
    }

    public boolean canWrite() {
        return dirSink.canWrite();
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    /**
     * Implementation of FileSource
     */
    private class DirectorySource extends AbstractConfigSource {

        public DirectorySource() {
            super(textFileEncoding);
        }

        @Override
        protected List<String> getFileNames(String configName, List<Long> supportedVersions) {
            List<String> fileNames = new LinkedList<String>();
            for ( Long version : supportedVersions) {
                fileNames.add(ConfigDirectorySourceAndSink.this.getConfigFileName(configName, version));
            }
            return fileNames;
        }

        protected void loadStarting(String configName) throws ConfigManagerException {
            ConfigLogImplementation.logMethods.info("Searching for " + configName + " configuration in: " + configDirectory);
            checkConfigDirectoryReadable();
        }

        private void checkConfigDirectoryReadable() throws ConfigManagerException {
            if ( ! configDirectory.canRead() ) {
                throw new ConfigManagerException("Cannot read from config directory " + configDirectory);
            }
        }

        protected InputStream getInputStream(String configFileName) throws FileNotFoundException {
            InputStream configInputStream = null;
            File f = new File(configDirectory, configFileName);
            if (f.canRead()) {
                ConfigLogImplementation.logMethods.info("Found configuration file: " + f);
                configInputStream = new FileInputStream(f);
            } else {
                ConfigLogImplementation.logMethods.info("Could not " + (f.exists() ? "read" : "find") + " configuration file: " + f);
            }
            return configInputStream;
        }
    }

    /**
     * Implementation of FileSink
     */
    private class DirectorySink extends AbstractConfigSink {

        public DirectorySink() {
            super(textFileEncoding);
        }

        @Override
        protected String getConfigFileName(String configName, long version) {
            return ConfigDirectorySourceAndSink.this.getConfigFileName(configName, version);
        }

        protected URL writeConfig(ConfigData configuration, String fileName) throws Exception {
            File configFile = new File(configDirectory, fileName);
            File backupFile = new File(configDirectory, fileName + ".bak");
            ConfigLogImplementation.logMethods.info("Writing configuration file at " + configFile);
            checkConfigDirectoryWritable();
            checkConfigFileWritableIfExists(configFile);
            checkConfigFileWritableIfExists(backupFile);

            File tempConfigFile = null;
            FileOutputStream fos = null;
            try {
                ConfigLogImplementation.logMethods.debug("About to create temp file");
                tempConfigFile = File.createTempFile("tempConfig", "." + extension, configDirectory);
                tempConfigFile.deleteOnExit();

                ConfigLogImplementation.logMethods.debug("About to write: " + tempConfigFile);

                try {
                    fos = new FileOutputStream(tempConfigFile);
                    writeConfigToStream(fos, configuration.getSerializedConfig(), configuration.getVersion());
                } finally {
                    if ( fos != null) {
                        try {
                            fos.close();
                        } catch (Exception e) {
                            ConfigLogImplementation.logMethods.error("Failed to close out file stream to temp file " + tempConfigFile.getPath(), e);
                        }
                    }
                }
                ConfigLogImplementation.logMethods.debug("Written: " + tempConfigFile);

                if ( backupFile.exists()) {
                    ConfigLogImplementation.logMethods.debug("About to delete " + backupFile);
                    boolean deleted = backupFile.delete();
                    if (deleted) {
                        ConfigLogImplementation.logMethods.debug("Deleted " + backupFile);
                    } else {
                        ConfigLogImplementation.logMethods.warn("Failed to delete " + backupFile);
                    }
                }

                if ( configFile.exists()) {
                    ConfigLogImplementation.logMethods.debug("About to rename old config: " + configFile + " to " + backupFile);
                    if (!configFile.renameTo(backupFile)) {
                        throw new IOException("Unable to rename old config: " + configFile + " to " + backupFile);
                    }
                }
                
                ConfigLogImplementation.logMethods.debug("About to rename temp config: " + tempConfigFile + " to " + configFile);
                if (!tempConfigFile.renameTo(configFile)) {
                    throw new IOException("Unable to rename temp config: " + tempConfigFile + " to new config: " + configFile);
                }
            } catch (IOException e) {
                ConfigLogImplementation.logMethods.error("Unable to save config: " + configFile, e);
                throw new ConfigManagerException("Unable to save config", e);
            }
            return configFile.toURI().toURL();
        }

        private void checkConfigDirectoryWritable() throws ConfigManagerException {
            if ( ! configDirectory.canWrite() ) {
                //Windows sometimes returns false for canWrite although in fact we
                //can write a file to the directory. My Documents folder seems to be like this
                //we can only really tell by trying to write a test file
                boolean testFileWrite = tryToWriteTestFile();
                if ( ! testFileWrite ) {    
                    throw new ConfigManagerException("Cannot write to config directory " + configDirectory);
                }
            }
        }

        private boolean tryToWriteTestFile() {
            boolean testFileWrite = false;
            File testFile = new File(configDirectory, "tmp" + System.currentTimeMillis());
            try {
                boolean success = testFile.createNewFile();
                if ( success ) {
                    boolean deleted = testFile.delete();
                    if ( ! deleted ) {
                        testFile.deleteOnExit();
                    } else {
                        testFileWrite = true;
                    }
                }
            } catch (IOException e) {
                ConfigLogImplementation.logMethods.warn("Test file write to config directory failed at " + configDirectory);
            }
            return testFileWrite;
        }

        private void checkConfigFileWritableIfExists(File configToWrite) throws ConfigManagerException {
            if ( configToWrite.exists() && ! configToWrite.canWrite()) {
                throw new ConfigManagerException("Cannot write to config file " + configToWrite);
            }
        }

        public boolean canWrite() {
            boolean result = true;
            try {
                checkConfigDirectoryWritable();
            } catch (ConfigManagerException e) {
                result = false;
            }
            return result;
        }
    }

    protected String getConfigFileName(String configName, long version) {
        return configName + "." + version + "." + extension;
    }

}
