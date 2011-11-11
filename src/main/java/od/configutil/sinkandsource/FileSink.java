package od.configutil.sinkandsource;

import od.configutil.util.ConfigLogImplementation;
import od.configutil.util.ConfigUtilConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 20/03/11
 * Time: 13:21
 *
 * Save a config to a specified file
 * The configName is not used, since the File already has a name
 * This is useful for loading one off config files, from locations not necessarily in the main config directory
 */
public class FileSink extends AbstractConfigSink {

    private File file;

    public FileSink(File file) {
        this(file, ConfigUtilConstants.DEFAULT_TEXT_ENCODING);
    }

    public FileSink(File file, String textFileEncoding) {
        super(textFileEncoding);
        this.file = file;
    }

    protected String getConfigFileName(String configName, long version) {
        return file.getName();
    }

    protected URL writeConfig(ConfigData configuration, String fileName) throws Exception {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            super.writeConfigToStream(fos, configuration.getSerializedConfig(), configuration.getVersion());
            return file.toURI().toURL();
        } finally {
            if ( fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    ConfigLogImplementation.logMethods.error("Failed to close out file stream to file " + file.getPath(), e);
                }
            }
        }
    }

    public boolean canWrite() {
        return doesNotExistButWritable() || file.canWrite();
    }

    private boolean doesNotExistButWritable() {
        return (! file.exists() &&
               file.getParentFile().exists() &&
               file.getParentFile().canWrite() );
    }
}
