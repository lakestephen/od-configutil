package od.configutil;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 *
 */
public class ConfigManager {

    private ConfigSource configSource;
    private ConfigSink configSink;
    private MigrationSource migrationSource;
    private ConfigSerializer configSerializer;

    public ConfigManager() {
        this.migrationSource = new ClasspathMigrationLoader();
        createDefaultSourceAndSink();
        createDefaultSerializer();
    }

    public ConfigManager(File configDirectory) {
        this.migrationSource = new ClasspathMigrationLoader();
        createDirectorySourceAndSink(configDirectory);
        createDefaultSerializer();
    }

    private void createDefaultSourceAndSink() {
        createDirectorySourceAndSink(new File(System.getProperty("user.home")));
    }

    private void createDirectorySourceAndSink(File configDirectory) {
        FileSourceAndSink defaultSourceAndSink = new FileSourceAndSink(configDirectory);
        configSource = defaultSourceAndSink;
        configSink = defaultSourceAndSink;
    }

    protected void createDefaultSerializer() {
        configSerializer = new XStreamSeralizer();
    }

   /**
     * Load the config with the name provided using the configSource registered with this ConfigManager,
     * and migrate it to the latest patch level using the migrations defined by config manager's MigrationSource.
    *
     * @return a config at the latest patch level
     * @throws ConfigManagerException, if config could not be loaded
     */
    public <V> V loadConfig(String configName, Class<V> configClass) throws ConfigManagerException {
        return loadConfig(configName, configSerializer, configClass);
    }

    /**
     * Load the config with the name provided using the configSource registered with this ConfigManager,
     * and migrate it to the latest patch level using the migrations defined by config manager's MigrationSource.
     * Use the serializer provided to deserialize the migrated config into an instance of the required config type 
     * @return a config at the latest patch level
     * @throws ConfigManagerException, if config could not be loaded
     */
    public <V> V loadConfig(String configName, ConfigSerializer serializer, Class<V> configClass) throws ConfigManagerException {
        try {
            return doLoad(configName, serializer, configClass);
        } catch (ConfigManagerException t) {
            throw t;
        } catch (Throwable t) {
            throw new ConfigManagerException("Failed during ConfigManger.loadConfig", t);
        }
    }

    /**
     * Save the config using the name provided and configSink registered with configManager
     * @return a URL to the saved config file
     * @throws ConfigManagerException, if the save failed
     */
    public URL saveConfig(String configName, Object config) throws ConfigManagerException {
        return saveConfig(configName, config, configSerializer);
    }

    /**
     * Save the config using the name and serializer provided, and the configSink registered with configManager
     * @return a URL to the saved config file
     * @throws ConfigManagerException, if the save failed
     */
    public URL saveConfig(String configName, Object config, ConfigSerializer serializer) throws ConfigManagerException {
        try {
            return doSave(configName, config, serializer);
        } catch (ConfigManagerException t) {
            throw t;
        } catch (Throwable t) {
            throw new ConfigManagerException("Failed during ConfigManger.saveConfig", t);
        }
    }

    private <V> V doLoad(String configName, ConfigSerializer serializer, Class<V> configClass) throws Exception {
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

    private URL doSave(String configName, Object config, ConfigSerializer serializer) throws Exception {
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
        LogMethods.log.info("config " + configName + " at version " + fromVersion + ", required version " + toVersion);

        migrationsToRun.remove(fromVersion); //we are already at this patch version, don't need to run migration        
        for (Map.Entry<Long, List<ConfigMigrationStategy>> entry : migrationsToRun.entrySet() ) {
            for (ConfigMigrationStategy s : entry.getValue()) {
                LogMethods.log.info("Migrating config " + configName + " to version " + entry.getKey() + " using strategy " + s);
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
