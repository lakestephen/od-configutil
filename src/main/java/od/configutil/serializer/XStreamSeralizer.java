package od.configutil.serializer;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import od.configutil.util.ConfigManagerException;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 22-Nov-2010
 * Time: 14:43:07
 */
public class XStreamSeralizer implements ConfigSerializer {

    private XStream xStream;

    public XStreamSeralizer() {
        this(new XStream(new DomDriver()));
    }

    public XStreamSeralizer(XStream xStream) {
        this.xStream = xStream;
    }

    public String serialize(Object configObject) throws Exception {
        return xStream.toXML(configObject);
    }

    public <V> V deserialize(String serializedConfig, Class<V> clazz) throws Exception {
        Object o = xStream.fromXML(serializedConfig);
        if ( ! clazz.isAssignableFrom(o.getClass())) {
            throw new XStreamSerializerException("The deserialized config was not of the expected type " + clazz + ", instead it was of type " + o.getClass());
        }
        return (V)o;
    }

    public static class XStreamSerializerException extends ConfigManagerException {

        public XStreamSerializerException(String message) {
            super(message);
        }
    }
}
