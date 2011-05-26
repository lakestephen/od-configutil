package od.configutil;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 20-Nov-2010
 * Time: 09:57:19
 * To change this template use File | Settings | File Templates.
 */
public class TestURLConfigSource extends TestCase {

    public void testLoadFromFileUrl() throws Exception {

        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath);

        ConfigDirectorySourceAndSink fs = new ConfigDirectorySourceAndSink(tmpDir);
        ConfigManager cm = new ConfigManager(tmpDir);
        cm.setMigrationSource(new ClasspathMigrationLoader("/configMigrations.xml"));

        URL savedFileUrl = cm.saveConfig("testConfig", new ConfigUtilTestConfig());
        URLConfigSource urlConfigSource = new URLConfigSource();
        urlConfigSource.setTimeout(100);

        //now try loading the config using the URLConfigSource
        cm.setConfigSource(urlConfigSource);
        ConfigUtilTestConfig t = cm.loadConfig(savedFileUrl.toString(), ConfigUtilTestConfig.class);
        assertEquals("TestConfig", t.getStringField());

        long timeMillis = System.currentTimeMillis();
        urlConfigSource.setTimeout(10);
        try {
            t = cm.loadConfig("http://wibble.com/wibble.txt", ConfigUtilTestConfig.class);
            fail("Should throw NoConfigFoundException");
        } catch ( NoConfigFoundException nfe) {
            assertTrue((System.currentTimeMillis() - timeMillis) < 1000);
        } catch ( Throwable th) {
            fail("Should have thrown NoConfigFoundException, instead " + th);
        }
    }

}
