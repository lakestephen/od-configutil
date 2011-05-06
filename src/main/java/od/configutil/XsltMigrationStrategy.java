package od.configutil;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Nick Ebbutt
 * Date: 26-Aug-2008
 * Time: 16:33:37
 *
 */
public class XsltMigrationStrategy implements ConfigMigrationStategy {

    private String xslResourceInClassPath;
    private long versionTarget;

    public XsltMigrationStrategy(long versionTarget, String[] arguments) {
        this.versionTarget = versionTarget;
        xslResourceInClassPath = arguments[0];
    }

    public String migrate(String configKey, String source) {
        ConfigLogImplementation.logMethods.info("Migrating " + configKey + " configuration to version " + versionTarget + " using xslt strategy " + xslResourceInClassPath);
        String transformedSource = source;

        TransformerFactory tFactory = new TransformerFactoryImpl();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(source.getBytes());

        InputStream xslInputStream = XsltMigrationStrategy.class.getResourceAsStream(xslResourceInClassPath);
        if ( xslInputStream != null ) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream(1000);
                Transformer transformer = tFactory.newTransformer(new StreamSource(xslInputStream));
                transformer.transform(new StreamSource(byteArrayInputStream), new StreamResult(os));
                transformedSource = os.toString();
            } catch ( Throwable t) {
                ConfigLogImplementation.logMethods.error("Failed to transform config using xsl strategy", t);
            } finally {
                try {
                    xslInputStream.close();
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    ConfigLogImplementation.logMethods.error("Failed close stream after xsl config migration strategy", e);
                }
            }
        } else {
            ConfigLogImplementation.logMethods.error("Resource " + xslResourceInClassPath + " not found. Failed to transform config using xsl strategy");
        }
        //System.out.println("----------------------------------------------->");
        //System.out.println(transformedSource);
        //System.out.println("----------------------------------------------->");
        return transformedSource;
    }
}
