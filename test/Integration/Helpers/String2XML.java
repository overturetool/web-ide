package Integration.Helpers;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class String2XML {
    public static synchronized List<Document> Convert(List<String> xmlStringList) {
        List<Document> xmlList = new ArrayList<>();

        for (String xmlString : xmlStringList) {
            xmlList.add(Convert(xmlString));
        }

        return xmlList;
    }

    public static synchronized Document Convert(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document document = null;

        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return document;
    }
}
