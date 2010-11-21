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

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagEdge.EdgeType;
import org.goddag4j.GoddagNode.NodeType;
import org.goddag4j.Text;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

public class GoddagGraphMLWriter {

    private static final String GRAPHML_XSD_LOCATION = "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    
    private final XMLStreamWriter out;

    public GoddagGraphMLWriter(XMLStreamWriter out) {
        this.out = out;
    }
    
    public void write(GoddagNode start) throws XMLStreamException {
        out.writeStartDocument();

        out.writeStartElement("graphml");
        out.writeDefaultNamespace(GRAPHML_NS);
        out.writeNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        out.writeAttribute("xsi:schemaLocation", GRAPHML_NS + " " + GRAPHML_XSD_LOCATION);

        declareKeys();

        out.writeStartElement("graph");
        out.writeAttribute("id", "G");
        out.writeAttribute("edgedefault", "directed");

        final TraversalDescription td = createTraversalDescription();
        final Node startNode = start.node;

        for (Node node : td.uniqueness(Uniqueness.NODE_GLOBAL).traverse(startNode).nodes()) {
            writeNodeElement(GoddagNode.wrap(node));
        }

        for (Relationship r : td.uniqueness(Uniqueness.RELATIONSHIP_GLOBAL).traverse(startNode).relationships()) {
            writeEdgeElement(r);
        }

        out.writeEndElement();

        out.writeEndElement();

        out.writeEndDocument();
    }

    protected void declareKeys() throws XMLStreamException {
        out.writeEmptyElement("key");
        out.writeAttribute("id", "nt");
        out.writeAttribute("for", "node");
        out.writeAttribute("attr.name", "node-type");
        out.writeAttribute("attr.type", "string");

        out.writeEmptyElement("key");
        out.writeAttribute("id", "et");
        out.writeAttribute("for", "edge");
        out.writeAttribute("attr.name", "edge-type");
        out.writeAttribute("attr.type", "string");

        out.writeEmptyElement("key");
        out.writeAttribute("id", "en");
        out.writeAttribute("for", "node");
        out.writeAttribute("attr.name", "element-name");
        out.writeAttribute("attr.type", "string");

        out.writeEmptyElement("key");
        out.writeAttribute("id", "txt");
        out.writeAttribute("for", "node");
        out.writeAttribute("attr.name", "text");
        out.writeAttribute("attr.type", "string");
    }

    protected void writeNodeElement(GoddagNode node) throws XMLStreamException {
        out.writeStartElement("node");
        out.writeAttribute("id", getNodeId(node.node));

        final NodeType nt = node.getNodeType();

        out.writeStartElement("data");
        out.writeAttribute("key", "nt");
        out.writeCharacters(nt.name().toLowerCase());
        out.writeEndElement();

        switch (nt) {
        case ELEMENT:
            out.writeStartElement("data");
            out.writeAttribute("key", "en");
            out.writeCharacters(((Element) node).getQName());
            out.writeEndElement();
            break;
        case TEXT:
            out.writeStartElement("data");
            out.writeAttribute("key", "txt");
            out.writeCharacters(((Text) node).getText());
            out.writeEndElement();
            break;

        }

        out.writeEndElement();
    }

    protected void writeEdgeElement(Relationship r) throws XMLStreamException {
        out.writeStartElement("edge");
        out.writeAttribute("id", getEdgeId(r));
        out.writeAttribute("source", getNodeId(r.getStartNode()));
        out.writeAttribute("target", getNodeId(r.getEndNode()));

        out.writeStartElement("data");
        out.writeAttribute("key", "et");
        out.writeCharacters(r.getType().name().toLowerCase());
        out.writeEndElement();

        out.writeEndElement();
    }

    protected String getNodeId(Node node) {
        return "n" + node.getId();
    }

    protected String getEdgeId(Relationship r) {
        return "e" + r.getId();
    }

    protected TraversalDescription createTraversalDescription() {
        return Traversal.description().relationships(EdgeType.CONTAINS);
    }
}
