package od.configutil.migration;

import com.thoughtworks.xstream.XStream;
import od.configutil.util.ConfigLogImplementation;
import od.configutil.util.ConfigUtilConstants;

import java.io.*;
import java.util.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 14:41:09
 */
public abstract class UrlMigrationLoader implements MigrationSource {

    public UrlMigrationLoader() {}

    private List<URL> cachedURL;

    protected abstract List<URL> getURL();

    public SortedMap<Long, List<ConfigMigrationStategy>> loadConfigMigrations() throws Exception {
        List<URL> urls = cachedGetURL();
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations = new TreeMap<Long, List<ConfigMigrationStategy>>();
        try {
            if ( urls.size() == 0) {
                throw new Exception("No configMigration resources found");
            }

            for( URL url : urls ) {
                readMigrations(configMigrations, url);
            }
        } catch (Exception e) {
            ConfigLogImplementation.logMethods.error("Failed to load config migrations", e);
            throw e;
        }
        return configMigrations;
    }

    private void readMigrations(SortedMap<Long, List<ConfigMigrationStategy>> configMigrations, URL url) throws IOException {
        InputStreamReader r = null;
        try {
            r = new InputStreamReader(url.openStream(), ConfigUtilConstants.DEFAULT_TEXT_ENCODING);
            XStream x = createXStream();
            ConfigManagerMigrations migrations = (ConfigManagerMigrations)x.fromXML(r);

            for (Migration m : migrations.getMigrationList()) {
                long versionTarget = m.getTargetVersion();
                String className = m.getMigrationClass();
                String[] constructorArguments = m.getArguments();

                List<ConfigMigrationStategy> configMigrationForVersionTarget = configMigrations.get(versionTarget);
                if (configMigrationForVersionTarget == null) {
                    configMigrationForVersionTarget = new ArrayList();
                    configMigrations.put(versionTarget, configMigrationForVersionTarget);
                }

                ConfigMigrationStategy configMigration = createMigrationStrategy(versionTarget, className, constructorArguments);
                configMigrationForVersionTarget.add(configMigration);
            }
        } finally {
            try {
                if (r != null) r.close();
            } catch (IOException e) {
                ConfigLogImplementation.logMethods.error("Failed to close ConfigMigration InputStream from URL " + url, e);
            }
        }
    }


    private ConfigMigrationStategy createMigrationStrategy(Long versionTarget, String className, String[] constructorArguments) {
        try {
            Object[] arguments = new Object[]{versionTarget, constructorArguments};
            Class migrationClass = Class.forName(className);
            return (ConfigMigrationStategy) migrationClass.getConstructor(new Class[]{long.class, String[].class}).newInstance(arguments);
        } catch (Exception e) {
            throw new RuntimeException("Error loading strategy delegates for config migration", e);
        }
    }

    /**
     *  Utility method to write a migrations file
     */
    public static void writeMigrationsFile(ConfigManagerMigrations m, File f) {
        OutputStreamWriter r = null;
        try {
            r = new OutputStreamWriter(new FileOutputStream(f));
            XStream x = createXStream();
            String config = x.toXML(m);
            r.write(config);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if ( r != null ) {
                try {
                    r.flush();
                    r.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static XStream createXStream() {
        XStream x = new XStream();
        x.alias("configManager", ConfigManagerMigrations.class);
        x.alias("migration", Migration.class);
        return x;
    }

    private List<URL> cachedGetURL() {
        if (cachedURL == null) {
            cachedURL = getURL();
        }
        return cachedURL;
    }

    //util to write the first config migrations file, solve the chicken an egg problem. After that we can do it manually
    public static void main(String[] args) {
        Migration c = new Migration(201011181800L, NullMigrationStrategy.class.getName(), new String[] {});
        Migration c1 = new Migration(201103201834L, RegexMigrationStrategy.class.getName(), new String[] {"Last Refresh\\w", "Last Refresh Time"});
        ConfigManagerMigrations m = new ConfigManagerMigrations();
        m.addMigration(c);
        m.addMigration(c1);
        writeMigrationsFile(m, new File(args[0]));
    }

}
