package org.goddag4j;

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IterableWrapper;

public class Element extends GoddagNode {

    private static final String PREFIX = GoddagNode.PREFIX + ".element";

    protected Element(Node node) {
        super(node);
    }

    public static Element create(GraphDatabaseService db, String prefix, String name) {
        Element root = new Element(db.createNode());
        root.setNodeType(GoddagNodeType.ELEMENT);
        root.setName(name);
        root.setPrefix(prefix);
        return root;
    }

    @Override
    public void stream(Element root, GoddagEventHandler handler) {
        handler.startElement(this);
        super.stream(root, handler);
        handler.endElement(this);
    }

    public Iterable<Attribute> getAttributes() {
        return new IterableWrapper<Attribute, Relationship>(node.getRelationships(ATTRIBUTE)) {

            @Override
            protected Attribute underlyingObjectToObject(Relationship object) {
                return new Attribute(object.getOtherNode(node));
            }
        };
    }

    public Attribute getAttribute(String attributePrefix, String attributeName) {
        String qName = getQName(attributePrefix, attributeName);
        for (Attribute attr : getAttributes()) {
            if (qName.equals(attr.getQName())) {
                return attr;
            }
        }
        return null;
    }

    public String getAttributeValue(String attributePrefix, String attributeName) {
        final Attribute attribute = getAttribute(attributePrefix, attributeName);
        return (attribute == null ? null : attribute.getValue());
    }

    public Attribute setAttribute(String attributePrefix, String attributeName, String attributeValue) {
        Attribute attr = getAttribute(attributePrefix, attributeName);
        if (attr == null) {
            final Node attrNode = node.getGraphDatabase().createNode();
            node.createRelationshipTo(attrNode, ATTRIBUTE);

            attr = new Attribute(attrNode);
            attr.setPrefix(attributePrefix);
            attr.setName(attributeName);
        }

        attr.setValue(attributeValue);
        return attr;
    }

    public void removeAttribute(String attributePrefix, String attributeName) {
        String qName = getQName(attributePrefix, attributeName);
        Attribute toDelete = null;
        for (Attribute attr : getAttributes()) {
            if (qName.equals(attr.getQName())) {
                toDelete = attr;
                break;
            }
        }
        if (toDelete == null) {
            throw new IllegalArgumentException(qName);
        }

        final Node node = toDelete.node;
        node.getSingleRelationship(ATTRIBUTE, INCOMING).delete();
        node.delete();
    }

    public String getPrefix() {
        return getOptionalStringProperty(PREFIX + ".prefix");
    }

    public void setPrefix(String prefix) {
        setOptionalStringProperty(PREFIX + ".prefix", prefix);
    }

    public String getName() {
        return (String) node.getProperty(PREFIX + ".name");
    }

    public void setName(String name) {
        node.setProperty(PREFIX + ".name", name);
    }

    @Override
    public boolean delete(Element root) {
        if (super.delete(root)) {
            List<Relationship> toDelete = new ArrayList<Relationship>();
            for (Relationship attrRel : node.getRelationships(ATTRIBUTE, OUTGOING)) {
                toDelete.add(attrRel);
            }
            for (Relationship r : toDelete) {
                r.delete();
                r.getEndNode().delete();
            }
            return true;
        }
        return false;
    }

    public static String getQName(String prefix, String name) {
        return (prefix == null || prefix.length() == 0 ? "" : prefix + ":") + name;
    }

    public String getQName() {
        return getQName(getPrefix(), getName());
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getQName() + "'/> " + node.toString();
    }

    protected static final RelationshipType ATTRIBUTE = new RelationshipType() {

        public String name() {
            return GoddagNode.PREFIX + ".attr";
        }
    };

}
