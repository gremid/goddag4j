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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goddag4j.Attribute;
import org.goddag4j.Comment;
import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class GoddagXMLWriter extends BaseXMLReader {

    private final Element root;
    private final Map<String, String> namespaceMap;
    private final Map<String, String> namespaceReverseMap;
    private final boolean wrapText;
    private String textNodeElementName;
    private String textNodeIdName;

    public GoddagXMLWriter(Element root, Map<URI, String> namespaceMap, boolean wrapText) {
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

    protected void parse() throws SAXException {
        if (contentHandler == null) {
            throw new SAXException("No content handler registered");
        }

        contentHandler.startDocument();
        for (Map.Entry<String, String> nsMapping : namespaceReverseMap.entrySet()) {
            contentHandler.startPrefixMapping(nsMapping.getKey(), nsMapping.getValue());
        }
        handleElement(root);
        for (String prefix : namespaceReverseMap.keySet()) {
            contentHandler.endPrefixMapping(prefix);
        }

        contentHandler.endDocument();
    }

    protected void handleElement(Element element) throws SAXException {
        String uri = namespaceReverseMap.get(element.getPrefix());
        if (uri == null) {
            throw new SAXException("Unregistered namespace prefix: " + element.getPrefix());
        }

        contentHandler.startElement(uri, element.getName(), element.getQName(), new SAXAttributesFacade(element));

        for (GoddagTreeNode child : element.getChildren(root)) {
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
                    contentHandler.startElement(NamespaceMap.GODDAG_NS_URI, "text", textNodeElementName, atts);
                }

                final char[] text = textNode.getText().toCharArray();
                contentHandler.characters(text, 0, text.length);

                if (wrapText) {
                    contentHandler.endElement(NamespaceMap.GODDAG_NS_URI, "text", textNodeElementName);
                }
                break;
            case PI:
                final ProcessingInstruction pi = (ProcessingInstruction) child;
                contentHandler.processingInstruction(pi.getTarget(), pi.getInstruction());
                break;
            case COMMENT:
                if (lexicalHandler != null) {
                    final char[] comment = ((Comment) child).getContent().toCharArray();
                    lexicalHandler.comment(comment, 0, comment.length);
                }
            }
        }

        contentHandler.endElement(uri, element.getName(), element.getQName());
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
