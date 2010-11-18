package od.configutil;

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
public class BeanPersistenceSerializer<V> implements ConfigSerializer<V> {

    public String serialize(V configObject) throws UnsupportedEncodingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        XMLEncoder encoder = new XMLEncoder(bos);
        encoder.writeObject(configObject);
        encoder.flush();
        encoder.close();
        return bos.toString("UTF-8");
    }

    public V deserialize(String serializedConfig) throws UnsupportedEncodingException {
        ByteArrayInputStream bis = new ByteArrayInputStream(serializedConfig.getBytes("UTF-8"));
        XMLDecoder d = new XMLDecoder(bis);
        return (V)d.readObject();
    }
}
