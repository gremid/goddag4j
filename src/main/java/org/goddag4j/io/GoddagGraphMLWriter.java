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

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.goddag4j.Element;
import org.goddag4j.GoddagEdge;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagNode.NodeType;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.Text;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class GoddagGraphMLWriter extends BaseXMLReader {

    private static final String GRAPHML_XSD_LOCATION = "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";

    private Map<String, String> attrs = new HashMap<String, String>(10);
    private final GoddagTreeNode start;

    public GoddagGraphMLWriter(GoddagTreeNode start) {
        this.start = start;
    }

    @Override
    protected void parse() throws IOException, SAXException {
        contentHandler.startDocument();

        final AttributesImpl rootAttrs = new AttributesImpl();
        rootAttrs.addAttribute(XML_NS_URI, "xsi", "xmlns:xsi", "CDATA", W3C_XML_SCHEMA_INSTANCE_NS_URI);
        rootAttrs.addAttribute(W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation", "xsi:schemaLocation", "CDATA", GRAPHML_NS + " "
                + GRAPHML_XSD_LOCATION);
        contentHandler.startElement(GRAPHML_NS, "graphml", "graphml", rootAttrs);

        declareKeys();

        attrs.put("id", "G");
        attrs.put("edgeDefault", "directed");
        startElement("graph");

        final TraversalDescription td = createTraversalDescription();
        final Node startNode = start.node;

        for (Node node : td.uniqueness(Uniqueness.NODE_GLOBAL).traverse(startNode).nodes()) {
            writeNodeElement(GoddagNode.wrap(node));
        }

        for (Relationship r : td.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).traverse(startNode).relationships()) {
            writeEdgeElement(r);
        }

        endElement("graph");

        contentHandler.endElement(GRAPHML_NS, "graphml", "graphml");

        contentHandler.endDocument();
    }

    protected void declareKeys() throws IOException, SAXException {
        attrs.put("id", "nt");
        attrs.put("for", "node");
        attrs.put("attr.name", "node-type");
        attrs.put("attr.type", "string");
        startElement("key");
        endElement("key");

        attrs.put("id", "et");
        attrs.put("for", "edge");
        attrs.put("attr.name", "edge-type");
        attrs.put("attr.type", "string");
        startElement("key");
        endElement("key");

        attrs.put("id", "en");
        attrs.put("for", "node");
        attrs.put("attr.name", "element-name");
        attrs.put("attr.type", "string");
        startElement("key");
        endElement("key");

        attrs.put("id", "txt");
        attrs.put("for", "node");
        attrs.put("attr.name", "text");
        attrs.put("attr.type", "string");
        startElement("key");
        endElement("key");
    }

    protected void writeNodeElement(GoddagNode node) throws IOException, SAXException {
        attrs.put("id", getNodeId(node.node));
        startElement("node");

        final NodeType nt = node.getNodeType();

        attrs.put("key", "nt");
        startElement("data");
        text(nt.name().toLowerCase());
        endElement("data");

        switch (nt) {
        case ELEMENT:
            attrs.put("key", "en");
            startElement("data");
            text(((Element) node).getQName());
            endElement("data");
            break;
        case TEXT:
            attrs.put("key", "text");
            startElement("data");
            text(((Text) node).getText());
            endElement("data");
            break;

        }

        endElement("node");
    }

    protected void writeEdgeElement(Relationship r) throws IOException, SAXException {
        attrs.put("id", getEdgeId(r));
        attrs.put("source", getNodeId(r.getStartNode()));
        attrs.put("target", getNodeId(r.getEndNode()));
        startElement("edge");

        attrs.put("key", "et");
        startElement("data");
        text(r.getType().name().toLowerCase());
        endElement("data");

        endElement("edge");
    }

    private void startElement(String name) throws SAXException {
        final AttributesImpl attrs = new AttributesImpl();
        for (Map.Entry<String, String> attr : this.attrs.entrySet()) {
            attrs.addAttribute(GRAPHML_NS, attr.getKey(), attr.getKey(), "CDATA", attr.getValue());
        }
        this.attrs.clear();
        contentHandler.startElement(GRAPHML_NS, name, name, attrs);
    }

    private void endElement(String name) throws SAXException {
        contentHandler.endElement(GRAPHML_NS, name, name);
    }

    private void text(String text) throws SAXException {
        final char[] textBuf = text.toCharArray();
        contentHandler.characters(textBuf, 0, textBuf.length);
    }

    protected String getNodeId(Node node) {
        return "n" + node.getId();
    }

    protected String getEdgeId(Relationship r) {
        return "e" + r.getId();
    }

    protected TraversalDescription createTraversalDescription() {
        return Traversal.description().relationships(GoddagEdge.CONTAINS);
    }
}
