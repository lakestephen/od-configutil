package od.configutil;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 22-Nov-2010
 * Time: 14:43:07
 */
public class XStreamSeralizer<V> implements ConfigSerializer<V> {

    private XStream xStream;

    public XStreamSeralizer() {
        this(new XStream(new DomDriver()));
    }

    public XStreamSeralizer(XStream xStream) {
        this.xStream = xStream;
    }

    public String serialize(V configObject) throws Exception {
        return xStream.toXML(configObject);
    }

    public V deserialize(String serializedConfig) throws Exception {
        return (V)xStream.fromXML(serializedConfig);
    }
}
