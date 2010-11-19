package od.configutil;

import java.net.URL;
import java.util.*;
import java.io.File;

/**
 *
 */
public class ConfigManager {

    private ConfigSource configSource;
    private ConfigSink configSink;
    private MigrationSource migrationSource;

    public ConfigManager() {
        this(new ClasspathMigrationLoader());
        createDefaultSourceAndSink();
    }

    public ConfigManager(URL... migrationsResources) {
        migrationSource = new UrlMigrationLoader(Arrays.asList(migrationsResources));
        createDefaultSourceAndSink();
    }

    public ConfigManager(MigrationSource migrationSource) {
        this.migrationSource = migrationSource;
        createDefaultSourceAndSink();
    }

    public ConfigManager(File configDirectory) {
        this();
        createDefaultSourceAndSink(configDirectory);
    }

    public void setConfigSource(ConfigSource configSource) {
        this.configSource = configSource;
    }

    public void setConfigSink(ConfigSink configSink) {
        this.configSink = configSink;
    }

    public void setMigrationSource(MigrationSource migrationSource) {
        this.migrationSource = migrationSource;
    }

    public void setConfigDirectory(File directory) {
        createDefaultSourceAndSink(directory);
    }

    private void createDefaultSourceAndSink() {
        createDefaultSourceAndSink(new File(System.getProperty("user.home")));
    }

    private void createDefaultSourceAndSink(File configDirectory) {
        FileSourceAndSink defaultSourceAndSink = new FileSourceAndSink(configDirectory);
        configSource = defaultSourceAndSink;
        configSink = defaultSourceAndSink;
    }

    /**
     * Load the config with the name provided and migrate it to the latest patch level, using BeanPersistenceSerializer to
     * create the returned config object after patching is complete
     * @return a config at the latest patch level, or null if no config file could be found
     */
    public <V> V loadConfig(String configName) throws ConfigManagerException {
        BeanPersistenceSerializer<V> serializer = getDefaultSerializer();
        return loadConfig(configName, serializer);
    }

    /**
     * Load the config with the name provided and migrate it to the latest patch level, using BeanPersistenceSerializer to
     * create the returned config object after patching is complete
     * @return a config at the latest patch level, or null if no config file could be found
     */
    public <V> V loadConfig(String configName, ConfigSerializer<V> serializer) throws ConfigManagerException {
        try {
            return doLoad(configName, serializer);
        } catch (ConfigManagerException t) {
            throw t;
        } catch (Throwable t) {
            throw new ConfigManagerException("Failed during ConfigManger.loadConfig", t);
        }
    }

    public <V> void saveConfig(String configName, V config) throws ConfigManagerException {
        BeanPersistenceSerializer<V> serializer = getDefaultSerializer();
        saveConfig(configName, config, serializer);
    }

    public <V> void saveConfig(String configName, V config, ConfigSerializer<V> serializer) throws ConfigManagerException {
        try {
            doSave(configName, config, serializer);
        } catch (ConfigManagerException t) {
            throw t;
        } catch (Throwable t) {
            throw new ConfigManagerException("Failed during ConfigManger.saveConfig", t);
        }
    }

    protected <V> BeanPersistenceSerializer<V> getDefaultSerializer() {
        return new BeanPersistenceSerializer<V>();
    }

    private <V> V doLoad(String configName, ConfigSerializer<V> serializer) throws Exception {
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations = readConfigMigrations();
        ConfigData d = configSource.loadConfiguration(configName, new ArrayList<Long>(configMigrations.keySet()));

        V result = null;
        if ( d != null ) {
            d = patchConfig(configMigrations, d);
            String serializedConfig = d.getSerializedConfig();
            result = serializer.deserialize(serializedConfig);
        }
        return result;
    }

    private <V> void doSave(String configName, V config, ConfigSerializer<V> serializer) throws Exception {
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations = readConfigMigrations();
        String serializedConfig = serializer.serialize(config);
        ConfigData configData = new ConfigData(configName, configMigrations.lastKey(), serializedConfig);
        configSink.saveConfiguration(configData);
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
}
