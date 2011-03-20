package od.configutil;

import com.thoughtworks.xstream.XStream;

import java.io.*;
import java.util.*;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 14:41:09
 */
public class UrlMigrationLoader implements MigrationSource {

    private List<URL> urls;

    public UrlMigrationLoader(URL... url) {
        this(Arrays.asList(url));
    }

    public UrlMigrationLoader(List<URL> urls) {
        this.urls = urls;
    }

    public SortedMap<Long, List<ConfigMigrationStategy>> loadConfigMigrations() throws Exception {
        SortedMap<Long, List<ConfigMigrationStategy>> configMigrations = new TreeMap<Long, List<ConfigMigrationStategy>>();
        try {
            if ( urls.size() == 0) {
                throw new Exception("No configMigration resources found");
            }

            for( URL url : urls ) {
                readMigrations(configMigrations, url);
            }
        } catch (Exception e) {
            LogMethods.log.error("Failed to load config migrations", e);
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
                LogMethods.log.error("Failed to close ConfigMigration InputStream from URL " + url, e);
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
        x.alias("NullMigrationStrategy", NullMigrationStrategy.class);
        x.alias("XPathMigrationStrategy", XPathMigrationStrategy.class);
        x.alias("XsltMigrationStrategy", XsltMigrationStrategy.class);
        x.alias("RegexMigrationStrategy", RegexMigrationStrategy.class);
        return x;
    }

    //util to write the first config migrations file, solve the chicken an egg problem. After that we can do it manually
    public static void main(String[] args) {
        Migration c = new Migration(201011181800l, "NullMigrationStrategy", new String[] {});
        ConfigManagerMigrations m = new ConfigManagerMigrations();
        m.addMigration(c);
        writeMigrationsFile(m, new File(args[0]));
    }

}
