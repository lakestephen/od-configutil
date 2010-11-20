package od.configutil;

import junit.framework.TestCase;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: nick
 * Date: 20-Nov-2010
 * Time: 09:57:19
 * To change this template use File | Settings | File Templates.
 */
public class TestURLConfigSource extends TestCase {

    public void testLoadFromFileUrl() throws ConfigManagerException {

        String tmpDirPath = System.getProperty("java.io.tmpdir");
        File tmpDir = new File(tmpDirPath);

        FileSourceAndSink fs = new FileSourceAndSink(tmpDir);
        ConfigManager cm = new ConfigManager(tmpDir);
        cm.setMigrationSource(new ClasspathMigrationLoader("/configMigrations.xml"));

        cm.saveConfig("testConfig", new TestConfig());




    }

    public static class TestConfig {

        private String testConfig = "TestConfig";

        public String getTestConfig() {
            return testConfig;
        }

        public void setTestConfig(String testConfig) {
            this.testConfig = testConfig;
        }
    }
}
