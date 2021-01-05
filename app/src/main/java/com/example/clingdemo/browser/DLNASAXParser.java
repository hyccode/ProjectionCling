package com.example.clingdemo.browser;

import org.seamless.xml.SAXParser;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;

public class DLNASAXParser extends SAXParser {

    protected XMLReader create() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            //fix bug .see https://stackoverflow.com/questions/10837706/solve-security-issue-parsing-xml-using-sax-parser
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // Configure factory to prevent XXE attacks
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);

            factory.setNamespaceAware(true);

            if (getSchemaSources() != null) {
                factory.setSchema(createSchema(getSchemaSources()));
            }

            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            xmlReader.setErrorHandler(getErrorHandler());
            return xmlReader;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}