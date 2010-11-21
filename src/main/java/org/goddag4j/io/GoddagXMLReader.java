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
import java.util.Map;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.goddag4j.Comment;
import org.goddag4j.Element;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;
import org.neo4j.graphdb.GraphDatabaseService;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

public class GoddagXMLReader extends DefaultHandler2 {

    private final GraphDatabaseService db;
    private final Map<URI, String> namespaces;

    private StringBuilder characterData;
    private Stack<Element> childAxis;
    private Element result;

    public GoddagXMLReader(GraphDatabaseService db, Map<URI, String> namespaces) {
        this.db = db;
        this.namespaces = namespaces;
    }

    public Element parse(InputSource source) throws SAXException, IOException {
        final XMLReader reader = XMLReaderFactory.createXMLReader();
        attachTo(reader);
        reader.parse(source);
        return result;
    }

    public void attachTo(XMLReader xmlReader) throws SAXException {
        xmlReader.setFeature("http://xml.org/sax/features/namespaces", true);
        xmlReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        xmlReader.setProperty("http://xml.org/sax/properties/lexical-handler", this);
        xmlReader.setContentHandler(this);
    }

    public Element result() {
        return result;
    }

    @Override
    public void startDocument() throws SAXException {
        childAxis = new Stack<Element>();
        result = null;
        characterData = new StringBuilder();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
        String elementPrefix = null;
        if (uri != null && uri.length() > 0) {
            elementPrefix = namespaces.get(URI.create(uri));
            if (elementPrefix == null) {
                throw new SAXException("Unknown XML namespace: " + uri);
            }
        }

        final Element element = new Element(db, elementPrefix, localName);
        for (int ac = 0; ac < attrs.getLength(); ac++) {
            String attrUri = attrs.getURI(ac);
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attrUri)) {
                continue;
            }
            String attrPrefix = null;
            if (attrUri != null && attrUri.length() > 0) {
                attrPrefix = namespaces.get(URI.create(attrUri));
                if (attrPrefix == null) {
                    throw new SAXException("Unknown XML namespace: " + attrUri);
                }
            } else {
                attrPrefix = elementPrefix;
            }
            element.setAttribute(attrPrefix, attrs.getLocalName(ac), attrs.getValue(ac));
        }

        if (childAxis.isEmpty()) {
            result = element;
        } else {
            createTextNode();
            childAxis.peek().insert(result, element, null);
        }
        childAxis.push(element);
    }

    private void createTextNode() {
        if (characterData.length() > 0) {
            childAxis.peek().insert(result, new Text(db, characterData.toString()), null);
            characterData = new StringBuilder();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        createTextNode();
        childAxis.pop();
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (result != null) {
            childAxis.peek().insert(result, new ProcessingInstruction(db, target, data), null);
        }
    }

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {
        if (result != null) {
            childAxis.peek().insert(result, new Comment(db, new String(ch, start, length)), null);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        characterData.append(ch, start, length);
    }
}
