package org.goddag4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class ProcessingInstruction extends GoddagNode {

    public static final String PREFIX = GoddagNode.PREFIX + ".pi";

    protected ProcessingInstruction(Node node) {
        super(node);
    }

    public static ProcessingInstruction create(GraphDatabaseService db, String target, String instruction) {
        ProcessingInstruction pi = new ProcessingInstruction(db.createNode());
        pi.setNodeType(GoddagNodeType.PI);
        pi.setTarget(target);
        pi.setInstruction(instruction);
        return pi;
    }

    @Override
    public void stream(Element root, GoddagEventHandler handler) {
        handler.processingInstruction(this);
    }

    @Override
    public String getText(Element root) {
        return "";
    }

    public String getTarget() {
        return (String) node.getProperty(PREFIX + ".target");
    }

    public void setTarget(String target) {
        node.setProperty(PREFIX + ".target", target);
    }

    public String getInstruction() {
        return (String) node.getProperty(PREFIX + ".instruction");
    }

    public void setInstruction(String instruction) {
        node.setProperty(PREFIX + ".instruction", instruction);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getTarget() + "'/> " + node.toString();
    }
}
