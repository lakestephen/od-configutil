package od.configutil.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 *
 * A mechanism to store a Properties object using the Preferences mechanism, which should be portable
 * Can be used as a way of obtaining a path to a config directory, so we know where to look to load the main config file
 */
public class PreferenceSettings {

    private String rootNodePath;
    private String configPropertiesNodeName;

    private Properties properties = new Properties();
    private static final String MAIN_CONFIG_DIRECTORY_PROPERTY = "mainConfigDirectoryPath";


    public PreferenceSettings(String rootNodePath, String propertiesNodeName) {
        this.rootNodePath = rootNodePath;
        this.configPropertiesNodeName = propertiesNodeName;
    }

    /**
     * @return a File if the main config directory is set in user preferences, or null.
     * The main config directory may or may not exist, and may or may not be readable
     */
    public File getMainConfigDirectory() {
        File result = null;
        if (isConfigDirectorySet()) {
            result = new File(properties.getProperty(MAIN_CONFIG_DIRECTORY_PROPERTY));
        }
        return result;
    }

    public void setMainConfigDirectory(File f) {
        if ( f.isDirectory()) {
            properties.put(MAIN_CONFIG_DIRECTORY_PROPERTY, f.getAbsolutePath());
        } else {
            throw new UnsupportedOperationException("Cannot set a non-directory file as the main config directory");
        }
    }

    /**
     * @return true, if getMainConfigFile can return a File instance (although this doesn't mean the File necessarily exists)
     */
    public boolean isConfigDirectorySet() {
        return properties.containsKey(MAIN_CONFIG_DIRECTORY_PROPERTY);
    }

    public void load()  {
        try {
            Preferences prefs = getRootNode();
            byte[] configMap = prefs.getByteArray(configPropertiesNodeName, null);
            if ( configMap != null ) {
                ByteArrayInputStream bis = new ByteArrayInputStream(configMap);
                properties.loadFromXML(bis);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void store() {
        Preferences prefs = getRootNode();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            properties.storeToXML(bos, "PreferenceSettings");
            if ( bos.size() > (Preferences.MAX_VALUE_LENGTH * 0.75) ) {
                throw new IOException("Saved config preferences too large to save, please remove some config files");
            }
            prefs.putByteArray(configPropertiesNodeName, bos.toByteArray());
            prefs.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private Preferences getRootNode() {
        return Preferences.userRoot().node(rootNodePath);
    }

}
