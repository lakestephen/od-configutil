package od.configutil.serializer;

import od.configutil.util.ConfigUtilConstants;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 16:55:29
 */
public class BeanPersistenceSerializer implements ConfigSerializer {

    public String serialize(Object configObject) throws UnsupportedEncodingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        XMLEncoder encoder = new XMLEncoder(bos);
        encoder.writeObject(configObject);
        encoder.flush();
        encoder.close();
        return bos.toString(ConfigUtilConstants.DEFAULT_TEXT_ENCODING);
    }

    public <V> V deserialize(String serializedConfig, Class<V> clazz) throws UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedConfig.getBytes(ConfigUtilConstants.DEFAULT_TEXT_ENCODING));
        XMLDecoder d = new XMLDecoder(bis);
        return (V)d.readObject();
    }
}
