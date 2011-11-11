package od.configutil.sinkandsource;

import od.configutil.util.ConfigManagerException;

import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
* User: Nick Ebbutt
* Date: 29-Apr-2010
* Time: 14:35:19
* 
* Interface to implement for classes which are responsible for loading configurations
*/
public interface ConfigSource {

    String CONFIG_VERSION_PREFIX = "configVersion=";

    /**
     * Return the most up to date ConfigData available
     * If this is not the most recent version, it may subsequently be migrated
     *
     * @param configName, name of the config to load
     * @param supportedVersions, sorted set of supported versions ids, highest ids representing most recent supported version
     * @return the ConfigData pre-migration with the highest available version id, or null if no config could be found
     * @throws od.configutil.util.ConfigManagerException if an error occurs which prevents config load
     */
    public ConfigData loadConfiguration(String configName, SortedSet<Long> supportedVersions) throws ConfigManagerException;
}
