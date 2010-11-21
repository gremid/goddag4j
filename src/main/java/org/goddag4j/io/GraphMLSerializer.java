package org.goddag4j.io;

import static org.goddag4j.GoddagNodeEdge.DESCENDANT_AXIS;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagNodeType;
import org.goddag4j.Text;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.kernel.Traversal;
import org.neo4j.kernel.Uniqueness;

public class GraphMLSerializer {

    private static final String GRAPHML_XSD_LOCATION = "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
    private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
    protected XMLStreamWriter out;

    public void write(XMLStreamWriter out, GoddagNode start) throws XMLStreamException {
        this.out = out;

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

        final GoddagNodeType nt = node.getNodeType();

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
        return Traversal.description().relationships(DESCENDANT_AXIS);
    }
}
