package org.goddag4j;

import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;

public class MultiRootedTree implements Iterable<Element> {

    private final RelationshipType rootRelation;
    private final Node node;

    protected MultiRootedTree(Node node, RelationshipType rootRelation) {
        this.node = node;
        this.rootRelation = rootRelation;
    }

    public Element newRoot(String prefix, String name) {
        final Element root = Element.create(node.getGraphDatabase(), prefix, name);
        addRoot(root);
        return root;
    }

    public void addRoot(Element root) {
        node.createRelationshipTo(root.node, rootRelation);
    }

    public Iterator<Element> iterator() {
        return new IterableWrapper<Element, Relationship>(node.getRelationships(rootRelation, OUTGOING)) {

            @Override
            protected Element underlyingObjectToObject(Relationship object) {
                return (Element) GoddagNode.wrap(object.getOtherNode(node));
            }
        }.iterator();
    }

    public Element findRoot(String prefix, String name) {
        for (Element root : this) {
            if (name.equals(root.getName()) && prefix.equals(root.getPrefix())) {
                return root;
            }
        }
        return null;
    }

    public Element getRoot(String prefix, String name) {
        Element root = findRoot(prefix, name);
        if (root == null) {
            root = newRoot(prefix, name);
        }
        return root;
    }

    public void delete() {
        List<Relationship> relationships = new ArrayList<Relationship>();
        IteratorUtil.addToCollection(node.getRelationships(rootRelation, OUTGOING).iterator(), relationships);

        for (Relationship r : relationships) {
            final Element root = (Element) GoddagNode.wrap(r.getEndNode());
            r.delete();
            root.delete(root);
        }
    }

    public static final RelationshipType ROOT_RELATION = new RelationshipType() {

        public String name() {
            return GoddagNode.PREFIX + ".root";
        }
    };
}
