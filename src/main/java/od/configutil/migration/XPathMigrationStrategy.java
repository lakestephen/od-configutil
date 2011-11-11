package od.configutil.migration;

import od.configutil.util.ConfigLogImplementation;
import od.configutil.util.ConfigUtilConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * This strategy allows you to find a section of XML using an XPath query and then replace it's
 *
 * @author James Langley
 */
public class XPathMigrationStrategy implements ConfigMigrationStategy {

    private static final String REMOVE_NODE = "REMOVE NODE";
    private long versionTarget;
    private String xpathExpression;
    private String replacementString;

    public XPathMigrationStrategy(long versionTarget, String[] arguments) {
        this.versionTarget = versionTarget;
        this.xpathExpression = arguments[0];
        this.replacementString = arguments[1];
    }

    public String migrate(String configKey, String source) {
        ConfigLogImplementation.logMethods.info("Patching " + configKey + " configuration to version " + versionTarget + " with XPath strategy");
        Node rootNode = null;
        try {
            rootNode = stringToXML(source);
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodeList = (NodeList) xpath.evaluate(xpathExpression, rootNode, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node item = nodeList.item(i);
                System.out.println(item.toString());
                if ( replacementString.trim().equalsIgnoreCase(REMOVE_NODE)) {
                    item.getParentNode().removeChild(item);
                } else {
                    item.setNodeValue(replacementString);
                }
            }
        } catch (XPathExpressionException e) {
            ConfigLogImplementation.logMethods.error("Error in XPath config migration", e);
        }
        return xmlToString(rootNode);
    }

    private Node stringToXML(String source) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(source.getBytes()));
            return document.getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String xmlToString(Node node) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, ConfigUtilConstants.DEFAULT_TEXT_ENCODING);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(node), new StreamResult(bos));
            return bos.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
