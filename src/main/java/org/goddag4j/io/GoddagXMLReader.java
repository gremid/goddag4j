/**
 * GODDAG for Java (goddag4j):
 * Java implementation of the GODDAG data model to express document
 * structures including overlapping markup
 *
 * Copyright (C) 2010 the respective authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.goddag4j.io;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import org.goddag4j.Attribute;
import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class GoddagXMLReader implements XMLReader {

    private final Element root;
    private final Map<String, String> namespaceMap;
    private final Map<String, String> namespaceReverseMap;
    private final boolean wrapText;
    private ContentHandler handler;
    private String textNodeElementName;
    private String textNodeIdName;

    public GoddagXMLReader(Element root, Map<URI, String> namespaceMap, boolean wrapText) {
        this.root = root;
        this.wrapText = wrapText;

        this.namespaceMap = new HashMap<String, String>();
        this.namespaceReverseMap = new HashMap<String, String>();
        for (Map.Entry<URI, String> ns : namespaceMap.entrySet()) {
            final String uriStr = ns.getKey().toString();
            this.namespaceMap.put(uriStr, ns.getValue());
            this.namespaceReverseMap.put(ns.getValue(), uriStr);
        }

        final String goddagPrefix = this.namespaceMap.get(NamespaceMap.GODDAG_NS_URI);
        textNodeElementName = Element.getQName(goddagPrefix, "text");
        textNodeIdName = Element.getQName(goddagPrefix, "id");

    }

    public SAXSource getSAXSource() {
        return new SAXSource(this, new InputSource());
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return true;
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public void setEntityResolver(EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setContentHandler(ContentHandler handler) {
        this.handler = handler;
    }

    public ContentHandler getContentHandler() {
        return handler;
    }

    public void setErrorHandler(ErrorHandler handler) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        parse();
    }

    public void parse(String systemId) throws IOException, SAXException {
        parse();
    }

    private void parse() throws SAXException {
        if (handler == null) {
            throw new SAXException("No content handler registered");
        }

        handler.startDocument();
        for (Map.Entry<String, String> nsMapping : namespaceReverseMap.entrySet()) {
            handler.startPrefixMapping(nsMapping.getKey(), nsMapping.getValue());
        }
        handleElement(root);
        for (String prefix : namespaceReverseMap.keySet()) {
            handler.endPrefixMapping(prefix);
        }

        handler.endDocument();
    }

    protected void handleElement(Element element) throws SAXException {
        String uri = namespaceReverseMap.get(element.getPrefix());
        if (uri == null) {
            throw new SAXException("Unregistered namespace prefix: " + element.getPrefix());
        }

        handler.startElement(uri, element.getName(), element.getQName(), new SAXAttributesFacade(element));

        for (GoddagNode child : element.getChildren(root)) {
            switch (child.getNodeType()) {
            case ELEMENT:
                handleElement((Element) child);
                break;
            case TEXT:
                final Text textNode = (Text) child;
                if (wrapText) {
                    AttributesImpl atts = new AttributesImpl();
                    atts.addAttribute(NamespaceMap.GODDAG_NS_URI, "id", textNodeIdName, "CDATA",
                            Long.toString(textNode.node.getId()));
                    handler.startElement(NamespaceMap.GODDAG_NS_URI, "text", textNodeElementName, atts);
                }

                final char[] text = textNode.getText().toCharArray();
                handler.characters(text, 0, text.length);

                if (wrapText) {
                    handler.endElement(NamespaceMap.GODDAG_NS_URI, "text", textNodeElementName);
                }
                break;
            case PI:
                final ProcessingInstruction pi = (ProcessingInstruction) child;
                handler.processingInstruction(pi.getTarget(), pi.getInstruction());
                break;
            }
        }

        handler.endElement(uri, element.getName(), element.getQName());
    }

    private class SAXAttributesFacade implements Attributes {
        private final Attribute[] attributes;

        private SAXAttributesFacade(Element element) {
            List<Attribute> attrList = new ArrayList<Attribute>();
            for (Attribute a : element.getAttributes()) {
                attrList.add(a);
            }
            this.attributes = attrList.toArray(new Attribute[attrList.size()]);
        }

        public int getLength() {
            return attributes.length;
        }

        public String getURI(int index) {
            return namespaceReverseMap.get(attributes[index].getPrefix());
        }

        public String getLocalName(int index) {
            return attributes[index].getName();
        }

        public String getQName(int index) {
            return attributes[index].getQName();
        }

        public String getType(int index) {
            return "CDATA";
        }

        public String getValue(int index) {
            return attributes[index].getValue();
        }

        public int getIndex(String uri, String localName) {
            return getIndex(Element.getQName(namespaceMap.get(uri), localName));
        }

        public int getIndex(String qName) {
            for (int ac = 0; ac < attributes.length; ac++) {
                if (qName.equals(attributes[ac].getQName())) {
                    return ac;
                }
            }
            return -1;
        }

        public String getType(String uri, String localName) {
            return "CDATA";
        }

        public String getType(String qName) {
            return "CDATA";
        }

        public String getValue(String uri, String localName) {
            return getValue(Element.getQName(namespaceMap.get(uri), localName));
        }

        public String getValue(String qName) {
            for (Attribute a : attributes) {
                if (qName.equals(a.getQName())) {
                    return a.getValue();
                }
            }
            return null;
        }

    }
}
