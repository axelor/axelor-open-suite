package com.axelor.apps.account.einvoice;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import org.w3c.dom.Element;
import java.io.StringWriter;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
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
