package od.configutil;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 20/03/11
 * Time: 17:44
 */
public class TestFileSourceAndFileSink extends TestCase {

    public void testWriteWithFileSink() throws Exception {
        ConfigManager c = new ConfigManager();

        File f = File.createTempFile("temp", "xml");
        f.deleteOnExit();

        TestConfig config = new TestConfig();
        FileSink configSink = new FileSink(f);
        c.saveConfig("testWriteWithFileSink", config, configSink);

        FileSource fileSource = new FileSource(f);
        TestConfig config2 = c.loadConfig("testWriteWithFileSink", TestConfig.class, fileSource);

        assertEquals("Config 1 and 2", config, config2);
    }

}
