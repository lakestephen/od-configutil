package od.configutil;

import od.configutil.migration.*;
import od.configutil.serializer.ConfigSerializer;
import od.configutil.serializer.XStreamSeralizer;
import od.configutil.sinkandsource.ConfigData;
import od.configutil.sinkandsource.ConfigDirectorySourceAndSink;
import od.configutil.sinkandsource.ConfigSink;
import od.configutil.sinkandsource.ConfigSource;
import od.configutil.util.ConfigLogImplementation;
import od.configutil.util.ConfigManagerException;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * ConfigManager is a utility for loading and saving config files, which also supports config file versioning and
 * migrations between versions in a highly customisable manner.
 *
 * Key Concepts:
 * Loading of configs takes place from a ConfigSource
 * Saving of configs takes place to a ConfigSink
 *
 * When saving a config, the ConfigManager expects to receive a Java class instance representing config data
 * The bean will then be serialized using the configured ConfigSerializer (this may perform serialization
 * to XML using XStream, for example). The serialized String data output will then be written to the ConfigSink
 *
 * Versioning of configs is also handled by ConfigManager. Associated with ConfigManager is a migrationSource.
 * This defines a list of Config Migrations each of which map to a configuration id. When saving a config, in
 * general the ConfigSink will persist the config data along with the most recent configuration id as supplied by
 * the configured migrationSource.
 *
 * When loading a config, both the saved config data and the saved configuration id are loaded by the ConfigSource.
 * Before deserialization takes place, any config migrations from the migrationSource which represent more recent
 * versions of the configuration are used to transform the loaded configuration into a version consistent with the
 * most recent configuration id. For example, if the persisted config contained XML which referenced a renamed class,
 * a regular expression migration might be used to migrate the class name for the more recent config version. Once any
 * migrations have been applied, the config data is then deserialized back into a Java object by the configSerializer.
 * A ClasspathMigrationLoader is supplied, which loads config migrations from an xml file on the classpath, typically
 * from /configMigrations.xml
 *
 * All the above elements, the ConfigSource, ConfigSink, MigrationSource and ConfigSerialzier are customisable.
 * ConfigManger may be configured with a default in each case, but alternative instances can also be passed into
 * overloaded save and load method implementations.
 *
 * One further concept is the configName.
 * When saving and loading, a configName is supplied as a parameter to the load and save method.
 * The semantics of this varies depending on the actual config source and sink implementations involved. For some sinks, e.g. FileSink
 * the configName is not used (other than for logging) since the File with which the sink is created determines file names absolutely.
 * For other sink types (e.g. DirectorySink) the configName is relevant. Here the configName is used to identify the config
 * as one of several which may co-exist in the same directory. In this case, the configName provides the file name prefix,
 * while the suffix is comprised of the latest version id and a file extension. DirectorySink may therefore end up writing a
 * directory containing newer and older config files under several different config names. When configs are loaded from a
 * DirectorySouce, typically only the most recent config file for the given configName is loaded.
 */
public class ConfigManager {

    private ConfigSource configSource;
    private ConfigSink configSink;
    private MigrationSource migrationSource;
    private ConfigSerializer configSerializer;

    public ConfigManager() {
        setDefaultMigrationSource();
        createDefaultSourceAndSink();
        createDefaultSerializer();
    }

    public ConfigManager(File configDirectory) {
        setDefaultMigrationSource();
        createDirectorySourceAndSink(configDirectory);
        createDefaultSerializer();
    }

    protected void setDefaultMigrationSource() {
        this.migrationSource = new AggregatedURLMigrationLoader(
            new ClasspathMigrationLoader(),
            new SysPropertyURLMigrationLoader()
        );
    }

    private void createDefaultSourceAndSink() {
        createDirectorySourceAndSink(new File(System.getProperty("user.home")));
    }

    private void createDirectorySourceAndSink(File configDirectory) {
        ConfigDirectorySourceAndSink defaultSourceAndSink = new ConfigDirectorySourceAndSink(configDirectory);
        configSource = defaultSourceAndSink;
        configSink = defaultSourceAndSink;
    }

    protected void createDefaultSerializer() {
        configSerializer = new XStreamSeralizer();
    }

   /**
     * @return a config at the latest patch level
     * @throws ConfigManagerException, if config could not be loaded
     */
    public <V> V loadConfig(String configName, Class<V> configClass) throws ConfigManagerException {
        return loadConfig(configName, configClass, this.configSource, this.configSerializer);
    }

    /**
     * @return a config at the latest patch level
     * @throws ConfigManagerException, if config could not be loaded
     */
    public <V> V loadConfig(String configName, Class<V> configClass, ConfigSerializer serializer) throws ConfigManagerException {
        return loadConfig(configName, configClass, this.configSource, serializer);

    }

    /**
     * @return a config at the latest patch level
     * @throws ConfigManagerException, if config could not be loaded
     */
    public <V> V loadConfig(String configName, Class<V> configClass, ConfigSource configSource) throws ConfigManagerException {
        return loadConfig(configName, configClass, configSource, this.configSerializer);
    }

    /**
     * Load the config with configName using the configSource provided
     * Migrate it to the latest patch level using the migrations defined by config manager's MigrationSource.
     * Use the serializer provided to deserialize the migrated config into an instance of the required config type
     *
     * @return a config at the latest patch level
     * @throws ConfigManagerException, if config could not be loaded
     */
    public <V> V loadConfig(String configName, Class<V> configClass, ConfigSource configSource, ConfigSerializer serializer) throws ConfigManagerException {
        try {
            return doLoad(configName, serializer, configClass, configSource);
        } catch (ConfigManagerException t) {
            throw t;
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(null, "TimeSerious could not load your config. A default config will be used", "Failed to load config", JOptionPane.WARNING_MESSAGE);
            throw new ConfigManagerException("Failed during ConfigManger.loadConfig", t);
        }
    }


    /**
     * Save the config using the name provided and configSink registered with configManager
     * @return a URL to the saved config file
     * @throws ConfigManagerException, if the save failed
     */
    public URL saveConfig(String configName, Object config) throws ConfigManagerException {
        return saveConfig(configName, config, this.configSink, this.configSerializer);
    }

     /**
     * Save the config using the name and serializer provided, and the configSink registered with configManager
     * @return a URL to the saved config file
     * @throws ConfigManagerException, if the save failed
     */
    public URL saveConfig(String configName, Object config, ConfigSerializer serializer) throws ConfigManagerException {
        return saveConfig(configName, config, this.configSink, serializer);
    }

     /**
     * Save the config using the name and serializer provided, and the configSink registered with configManager
     * @return a URL to the saved config file
     * @throws ConfigManagerException, if the save failed
     */
    public URL saveConfig(String configName, Object config, ConfigSink configSink) throws ConfigManagerException {
        return saveConfig(configName, config, configSink, this.configSerializer);
    }


    /**
     * Save the config using the name and serializer provided, and the configSink registered with configManager
     * @return a URL to the saved config file
     * @throws ConfigManagerException, if the save failed
     */
    public URL saveConfig(String configName, Object config, ConfigSink configSink, ConfigSerializer serializer) throws ConfigManagerException {
        try {
            return doSave(configName, config, serializer, configSink);
        } catch (ConfigManagerException t) {
            throw t;
        } catch (Throwable t) {
            throw new ConfigManagerException("Failed during ConfigManger.saveConfig", t);
        }
    }

    private <V> V doLoad(String configName, ConfigSerializer serializer, Class<V> configClass, ConfigSource configSource) throws Exception {
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations = readConfigMigrations();
        SortedSet<Long> migrationVersions = new TreeSet<Long>(configMigrations.keySet());

        //this should throw an exception if an error occurs, null indicates config not found
        ConfigData d = configSource.loadConfiguration(configName, migrationVersions);
        if ( d == null ) {
            throw new NoConfigFoundException("Could not find a config to load");
        } else {
            d = patchConfig(configMigrations, d);
            String serializedConfig = d.getSerializedConfig();
            return serializer.deserialize(serializedConfig, configClass);
        }
    }

    private URL doSave(String configName, Object config, ConfigSerializer serializer, ConfigSink configSink) throws Exception {
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations = readConfigMigrations();
        String serializedConfig = serializer.serialize(config);
        ConfigData configData = new ConfigData(configName, configMigrations.lastKey(), serializedConfig);
        return configSink.saveConfiguration(configData);
    }

    private SortedMap<Long, List<ConfigMigrationStategy>> readConfigMigrations() throws Exception {
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations;
        configMigrations = migrationSource.loadConfigMigrations();
        if ( configMigrations.size() == 0) {
            throw new ConfigManagerException("No config migrations defined, we cannot determine an expected version");
        }
        return configMigrations;
    }


    private ConfigData patchConfig(SortedMap<Long,List<ConfigMigrationStategy>> migrations, ConfigData oldConfig) {
        SortedMap<Long,List<ConfigMigrationStategy>> migrationsToRun =
                migrations.tailMap(oldConfig.getVersion()); // this is inclusive of the fromVersion

        String configName = oldConfig.getConfigName();
        long fromVersion = oldConfig.getVersion();
        long toVersion = migrations.lastKey();
        String configString = oldConfig.getSerializedConfig();
        ConfigLogImplementation.logMethods.info("config " + configName + " at version " + fromVersion + ", required version " + toVersion);

        migrationsToRun.remove(fromVersion); //we are already at this patch version, don't need to run migration        
        for (Map.Entry<Long, List<ConfigMigrationStategy>> entry : migrationsToRun.entrySet() ) {
            for (ConfigMigrationStategy s : entry.getValue()) {
                ConfigLogImplementation.logMethods.info("Migrating config " + configName + " to version " + entry.getKey() + " using strategy " + s);
                configString = s.migrate(configName, configString);
            }
        }
        return new ConfigData(configName, toVersion, configString);
    }

    /**
     * This method should make all feasible attempts to work out whether a config save
     * is likely to be successful, although it can't guarantee that in the event, this will be the case
     * @return true if ConfigManager should be able to write a config
     */
    public boolean canWrite() {
        return configSink.canWrite();
    }

    public ConfigSource getConfigSource() {
        return configSource;
    }

    public void setConfigSource(ConfigSource configSource) {
        this.configSource = configSource;
    }

    public ConfigSink getConfigSink() {
        return configSink;
    }

    public void setConfigSink(ConfigSink configSink) {
        this.configSink = configSink;
    }

    public MigrationSource getMigrationSource() {
        return migrationSource;
    }

    public void setMigrationSource(MigrationSource migrationSource) {
        this.migrationSource = migrationSource;
    }

    public ConfigSerializer getConfigSerializer() {
        return configSerializer;
    }

    public void setConfigSerializer(ConfigSerializer configSerializer) {
        this.configSerializer = configSerializer;
    }

    /**
     * Create and set a FileConfigSourceAndSink to read and write configs to the directory supplied
     */
    public void setConfigDirectory(File directory) {
        createDirectorySourceAndSink(directory);
    }

}
