package org.goddag4j;

import org.neo4j.graphdb.Node;

public class Attribute {

    public static final String PREFIX = GoddagNode.PREFIX + ".attr";
    public final Node node;

    protected Attribute(Node node) {
        this.node = node;
    }

    public String getPrefix() {
        return (String) node.getProperty(PREFIX + ".prefix");
    }

    public void setPrefix(String prefix) {
        node.setProperty(PREFIX + ".prefix", prefix);
    }

    public String getName() {
        return (String) node.getProperty(PREFIX + ".name");
    }

    public void setName(String name) {
        node.setProperty(PREFIX + ".name", name);
    }

    public String getQName() {
        return Element.getQName(getPrefix(), getName());
    }
    
    public String getValue() {
        return (String) node.getProperty(PREFIX + ".value");
    }

    public void setValue(String value) {
        node.setProperty(PREFIX + ".value", value);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getQName() + "'/> " + node.toString();
    }
}
