package org.goddag4j.io;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

@SuppressWarnings("serial")
public class NamespaceMap extends HashMap<URI, String> {
    public static final String XML_DTD_NS_PREFIX = "dtd";
    public static final String W3C_XML_SCHEMA_NS_PREFIX = "xsd";
    public static final String W3C_XML_SCHEMA_INSTANCE_NS_PREFIX = "xsi";
    public static final String RELAXNG_NS_PREFIX = "rng";

    private static final String TEI_NS_PREFIX = "tei";
    private static final String TEI_NS_URI = "http://www.tei-c.org/ns/1.0";
    
    public static final String GODDAG_NS_URI = "http://juxtasoftware.org/goddag/ns";
    public static final String GODDAG_NS_PREFIX = "goddag";

    public NamespaceMap() {
        put(URI.create(XMLConstants.XML_NS_URI), XMLConstants.XML_NS_PREFIX);
        put(URI.create(XMLConstants.XMLNS_ATTRIBUTE_NS_URI), XMLConstants.XMLNS_ATTRIBUTE);
        put(URI.create(XMLConstants.XML_DTD_NS_URI), XML_DTD_NS_PREFIX);
        put(URI.create(XMLConstants.W3C_XML_SCHEMA_NS_URI), W3C_XML_SCHEMA_NS_PREFIX);
        put(URI.create(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI), W3C_XML_SCHEMA_INSTANCE_NS_PREFIX);
        put(URI.create(XMLConstants.RELAXNG_NS_URI), RELAXNG_NS_PREFIX);
        put(URI.create(GODDAG_NS_URI), GODDAG_NS_PREFIX);
    }

    public static final Map<URI, String> EMPTY_MAP = new HashMap<URI, String>();
    
    public static final Map<URI, String> TEI_MAP = new NamespaceMap();

    static {
        TEI_MAP.put(URI.create(TEI_NS_URI), TEI_NS_PREFIX);
    }

}
