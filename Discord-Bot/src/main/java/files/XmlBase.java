package main.java.files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XmlBase {
	
	public static Document load(File xmlFile, String rootName) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//dbFactory.setValidating(true);
		//dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc;
        try {
        	doc = dBuilder.parse(xmlFile);
        } catch(FileNotFoundException | SAXParseException e) {
        	xmlFile.delete();
        	xmlFile.createNewFile();
        	doc = dBuilder.newDocument();
        	Element rootElement = doc.createElement(rootName);
            doc.appendChild(rootElement);
            
            System.out.println("New File created. Fatal Error can be ignored.");
        }
        doc.getDocumentElement().normalize();
        return doc;
	}
	
	public static void save(Document doc, File xmlFile) throws TransformerException {
		XPath xp = XPathFactory.newInstance().newXPath();
		NodeList nl;
		try {
			nl = (NodeList) xp.evaluate("//text()[normalize-space(.)='']", doc, XPathConstants.NODESET);
			for (int i=0; i < nl.getLength(); ++i) {
			    Node node = nl.item(i);
			    node.getParentNode().removeChild(node);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}

		
		
		TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(xmlFile);
        transformer.transform(source, streamResult);
	}
}
