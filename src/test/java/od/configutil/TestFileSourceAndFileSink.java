package od.configutil;

import junit.framework.TestCase;
import od.configutil.sinkandsource.FileSink;
import od.configutil.sinkandsource.FileSource;

import java.io.File;

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

        ConfigUtilTestConfig config = new ConfigUtilTestConfig();
        FileSink configSink = new FileSink(f);
        c.saveConfig("testWriteWithFileSink", config, configSink);

        FileSource fileSource = new FileSource(f);
        ConfigUtilTestConfig config2 = c.loadConfig("testWriteWithFileSink", ConfigUtilTestConfig.class, fileSource);

        assertEquals("Config 1 and 2", config, config2);
    }

}
