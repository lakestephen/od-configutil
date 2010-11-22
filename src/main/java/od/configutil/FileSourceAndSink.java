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
 */
public class FileSourceAndSink implements ConfigSink, ConfigSource {

    private File configDirectory;
    private String extension;
    private String textFileEncoding;
    private FileSource fileSource;
    private FileSink fileSink;

    public FileSourceAndSink(File configDirectory) {
        this(configDirectory, "xml", "UTF-8");
    }

    public FileSourceAndSink(File configDirectory, String extension, String textFileEncoding) {
        this.configDirectory = configDirectory;
        this.extension = extension;
        this.textFileEncoding = textFileEncoding;
        this.fileSource = new FileSource();
        this.fileSink = new FileSink();
    }

    public ConfigData loadConfiguration(String configName, SortedSet<Long> supportedVersions) throws ConfigManagerException {
        return fileSource.loadConfiguration(configName, supportedVersions);
    }

    public URL saveConfiguration(ConfigData configuration) throws ConfigManagerException {
        return fileSink.saveConfiguration(configuration);
    }

    /**
     * Implementation of FileSource
     */
    private class FileSource extends AbstractConfigSource {

        public FileSource() {
            super(textFileEncoding);
        }

        @Override
        protected List<String> getFileNames(String configName, List<Long> supportedVersions) {
            List<String> fileNames = new LinkedList<String>();
            for ( Long version : supportedVersions) {
                fileNames.add(FileSourceAndSink.this.getConfigFileName(configName, version));
            }
            return fileNames;
        }

        protected void loadStarting(String configName) throws ConfigManagerException {
            LogMethods.log.info("Searching for " + configName + " configuration in: " + configDirectory);
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
                LogMethods.log.info("Found configuration file: " + f);
                configInputStream = new FileInputStream(f);
            } else {
                LogMethods.log.info("Could not " + (f.exists() ? "read" : "find") + " configuration file: " + f);
            }
            return configInputStream;
        }
    }

    /**
     * Implementation of FileSink
     */
    private class FileSink extends AbstractConfigSink {

        public FileSink() {
            super(textFileEncoding);
        }

        @Override
        protected String getConfigFileName(String configName, long version) {
            return FileSourceAndSink.this.getConfigFileName(configName, version);
        }

        protected URL writeConfig(ConfigData configuration, String fileName) throws Exception {
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
                FileOutputStream fos = new FileOutputStream(tempConfigFile);
                writeConfigToStream(fos, configuration.getSerializedConfig(), configuration.getVersion());
    
                LogMethods.log.debug("Written: " + tempConfigFile);

                if ( backupFile.exists()) {
                    LogMethods.log.debug("About to delete " + backupFile);
                    backupFile.delete();
                }

                if ( configFile.exists()) {
                    LogMethods.log.debug("About to rename old config: " + configFile + " to " + backupFile);
                    if (!configFile.renameTo(backupFile)) {
                        throw new IOException("Unable to rename old config: " + configFile + " to " + backupFile);
                    }
                }
                
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
            return configFile.toURI().toURL();
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

    protected String getConfigFileName(String configName, long version) {
        return configName + "." + version + "." + extension;
    }

}
