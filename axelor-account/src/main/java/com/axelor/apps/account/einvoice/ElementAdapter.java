package com.axelor.apps.account.einvoice;

import org.w3c.dom.Element;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;

public class ElementAdapter extends XmlAdapter<String, Element> {

    @Override
    public Element unmarshal(String v) throws Exception {
        if (v == null || v.isEmpty()) {
            return null;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(v.getBytes()));
        return document.getDocumentElement();
    }

    @Override
    public String marshal(Element v) throws Exception {
        if (v == null) {
            return null;
        }
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(v), new StreamResult(writer));
        return writer.toString();
    }
}
