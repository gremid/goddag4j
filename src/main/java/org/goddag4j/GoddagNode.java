package org.goddag4j;

import org.neo4j.graphdb.Node;
import org.neo4j.helpers.collection.IterableWrapper;

public abstract class GoddagNode {
    public enum NodeType {
        TEXT, ELEMENT, COMMENT, PI, ATTRIBUTE;
    }

    public static final NodeType[] NODE_TYPES = NodeType.values();
    public static final String PREFIX = "goddag";
    public static final String NODE_TYPE_PROPERTY = PREFIX + ".nt";

    public final Node node;

    protected GoddagNode(Node node) {
        this.node = node;
    }

    public NodeType getNodeType() {
        return getNodeType(node);
    }

    protected static NodeType getNodeType(Node node) {
        return (NodeType) NODE_TYPES[(Integer) node.getProperty(NODE_TYPE_PROPERTY)];
    }

    protected void setNodeType(NodeType nodeType) {
        node.setProperty(NODE_TYPE_PROPERTY, nodeType.ordinal());
    }

    public static GoddagNode wrap(Node node) {
        final NodeType nodeType = getNodeType(node);
        switch (nodeType) {
        case TEXT:
            return new Text(node);
        case ELEMENT:
            return new Element(node);
        case COMMENT:
            return new Comment(node);
        case PI:
            return new ProcessingInstruction(node);
        case ATTRIBUTE:
            return new Attribute(node);
        default:
            throw new IllegalArgumentException(nodeType.toString());
        }
    }

    protected String getOptionalStringProperty(String propertyName) {
        return getOptionalStringProperty(node, propertyName);
    }

    protected void setOptionalStringProperty(String propertyName, String propertyValue) {
        setOptionalStringProperty(node, propertyName, propertyValue);
    }

    public static String getOptionalStringProperty(Node node, String propertyName) {
        return (String) node.getProperty(propertyName, null);
    }

    public static void setOptionalStringProperty(Node node, String propertyName, String propertyValue) {
        if (propertyValue == null) {
            node.removeProperty(propertyName);
        } else {
            node.setProperty(propertyName, propertyValue);
        }
    }
    

    public static class GoddageNodeWrappingIterable extends IterableWrapper<GoddagNode, Node> {

        public GoddageNodeWrappingIterable(Iterable<Node> iterableToWrap) {
            super(iterableToWrap);
        }

        @Override
        protected GoddagNode underlyingObjectToObject(Node object) {
            return wrap(object);
        }
    }
}
