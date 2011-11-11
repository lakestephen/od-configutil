package od.configutil.serializer;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 29-Apr-2010
 * Time: 16:24:56
 */
public interface ConfigSerializer {

    String serialize(Object configObject) throws Exception;

    <V> V deserialize(String serializedConfig, Class<V> clazz) throws Exception;
}
