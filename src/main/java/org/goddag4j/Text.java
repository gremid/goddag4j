package org.goddag4j;

import static org.goddag4j.GoddagNodeEdge.DESCENDANT_AXIS;
import static org.neo4j.graphdb.Direction.INCOMING;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class Text extends GoddagNode {

    private static final String PREFIX = GoddagNode.PREFIX + ".text";

    protected Text(Node node) {
        super(node);
    }

    public static Text create(GraphDatabaseService db, String content) {
        Text contentNode = new Text(db.createNode());
        contentNode.setNodeType(GoddagNodeType.TEXT);
        contentNode.setText(content);
        return contentNode;
    }

    @Override
    public void stream(Element root, GoddagEventHandler handler) {
        handler.text(this);
    }

    @Override
    public String getText(Element root) {
        return getText();
    }

    public String getText() {
        return (String) node.getProperty(PREFIX);
    }

    public void setText(String content) {
        node.setProperty(PREFIX, content);
    }

    public Text[] split(Integer[] positions) {
        final String content = getText();
        final int contentLength = content.length();

        final GraphDatabaseService db = node.getGraphDatabase();
        List<Text> segments = new ArrayList<Text>(positions.length + 1);
        int lastPos = 0;
        for (int pc = 0; pc < positions.length; pc++) {
            if (positions[pc] < 0 || positions[pc] >= contentLength) {
                throw new IllegalArgumentException(Integer.toString(positions[pc]));
            }
            if (lastPos == positions[pc]) {
                continue;
            }
            segments.add(Text.create(db, content.substring(lastPos, positions[pc])));
            if ((pc + 1) == positions.length) {
                segments.add(Text.create(db, content.substring(positions[pc])));
            } else {
                lastPos = positions[pc];
            }
        }

        if (segments.isEmpty()) {
            return new Text[] { this };
        }

        for (Relationship r : node.getRelationships(DESCENDANT_AXIS, INCOMING)) {
            Element root = (Element) wrap(node.getGraphDatabase().getNodeById(GoddagNodeEdge.getRoot(r)));
            GoddagNode parent = wrap(r.getOtherNode(node));
            for (Text segment : segments) {
                parent.insert(root, segment, this);
            }
            delete(root);
        }
        return segments.toArray(new Text[0]);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getText() + "' /> " + node.toString();
    }
}
