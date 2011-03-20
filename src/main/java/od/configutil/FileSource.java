package od.configutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 20/03/11
 * Time: 09:36
 *
 * Load a config from a specified file
 * The configName is not used, since the File already has a name
 * This is useful for saving config files to locations outside the main config directory
 */
public class FileSource extends AbstractConfigSource {

    private File file;

    public FileSource(File file) {
        this(file, ConfigUtilConstants.DEFAULT_TEXT_ENCODING);
    }

    public FileSource(File file, String textFileEncoding) {
        super(textFileEncoding);
        this.file = file;
    }

    protected void loadStarting(String configName) throws ConfigManagerException {
        LogMethods.log.info("About to load config from file " + file);
        if ( file == null ) {
            throw new ConfigManagerException("Cannot load config from a null file");
        } else if ( ! file.canRead()) {
            throw new ConfigManagerException("Cannot read config from file " + file + ", file is not readable");
        }
    }

    protected List<String> getFileNames(String configName, List<Long> supportedVersions) {
        //only one possible file name, since just one input file
        return Collections.singletonList(file.getName());
    }

    protected InputStream getInputStream(String configFileName) throws Exception {
        return new FileInputStream(file);
    }
}
