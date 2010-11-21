package org.goddag4j;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class Comment extends GoddagNode {

    private static final String PREFIX = GoddagNode.PREFIX + ".comment";

    protected Comment(Node node) {
        super(node);
    }

    public static Comment create(GraphDatabaseService db, String content) {
        Comment comment = new Comment(db.createNode());
        comment.setNodeType(GoddagNodeType.COMMENT);
        comment.setContent(content);
        return comment;
    }

    @Override
    public void stream(Element root, GoddagEventHandler handler) {
        handler.comment(this);
    }
    
    @Override
    public String getText(Element root) {
        return "";
    }

    public String getContent() {
        return (String) node.getProperty(PREFIX + ".content");
    }

    public void setContent(String content) {
        node.setProperty(PREFIX + ".content", content);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getContent() + "'/> " + node.toString();
    }
}
