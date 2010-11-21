package org.goddag4j;

import static org.goddag4j.GoddagNodeEdge.ALIGNMENT_AXIS;
import static org.goddag4j.GoddagNodeEdge.DESCENDANT_AXIS;
import static org.goddag4j.GoddagNodeEdge.FIRST_CHILD;
import static org.goddag4j.GoddagNodeEdge.LAST_CHILD;
import static org.goddag4j.GoddagNodeEdge.SIBLING_AXIS;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.Iterator;
import java.util.Stack;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.Traversal;

public abstract class GoddagNode {
    public static final GoddagNodeType[] NODE_TYPES = GoddagNodeType.values();
    public static final String PREFIX = "goddag";
    public static final String NODE_TYPE_PROPERTY = PREFIX + ".nt";
    public final Node node;

    protected GoddagNode(Node node) {
        this.node = node;
    }

    public GoddagNodeType getNodeType() {
        return getNodeType(node);
    }

    public Iterable<Element> getRoots() {
        final GraphDatabaseService db = node.getGraphDatabase();
        return new IterableWrapper<Element, Relationship>(node.getRelationships(DESCENDANT_AXIS, INCOMING)) {

            @Override
            protected Element underlyingObjectToObject(Relationship object) {
                return (Element) wrap(db.getNodeById(GoddagNodeEdge.getRoot(object)));
            }
        };
    }

    public void stream(Element root, GoddagEventHandler handler) {
        for (GoddagNode child : getChildren(root)) {
            child.stream(root, handler);
        }
    }

    public GoddagNode getParent(Element root) {
        final long rootId = root.node.getId();
        final Relationship parentRel = GoddagNodeEdge.find(node.getRelationships(DESCENDANT_AXIS, INCOMING), rootId);
        return (parentRel == null ? null : wrap(parentRel.getOtherNode(node)));
    }

    public GoddagNode getFirstChild(Element root) {
        final long rootId = root.node.getId();
        final Relationship firstRel = GoddagNodeEdge.find(node.getRelationships(FIRST_CHILD, OUTGOING), rootId);
        return (firstRel == null ? null : wrap(firstRel.getOtherNode(node)));
    }

    public GoddagNode getLastChild(Element root) {
        final long rootId = root.node.getId();
        final Relationship lastRel = GoddagNodeEdge.find(node.getRelationships(LAST_CHILD, INCOMING), rootId);
        return (lastRel == null ? null : wrap(lastRel.getOtherNode(node)));
    }

    public GoddagNode getNextSibling(Element root) {
        final long rootId = root.node.getId();
        final Relationship nextRel = GoddagNodeEdge.find(node.getRelationships(SIBLING_AXIS, OUTGOING), rootId);
        return (nextRel == null ? null : wrap(nextRel.getOtherNode(node)));
    }

    public GoddagNode getPreviousSibling(Element root) {
        final long rootId = root.node.getId();
        final Relationship prevRel = GoddagNodeEdge.find(node.getRelationships(SIBLING_AXIS, INCOMING), rootId);
        return (prevRel == null ? null : wrap(prevRel.getOtherNode(node)));
    }

    public boolean hasChildren(Element root) {
        return (GoddagNodeEdge.find(node.getRelationships(FIRST_CHILD, OUTGOING), root.node.getId()) != null);
    }

    public Iterable<GoddagNode> getChildren(final Element root) {
        return IteratorUtil.asIterable(new GoddagNodeIterator(getFirstChild(root)) {

            @Override
            protected GoddagNode getNext() {
                return current.getNextSibling(root);
            }
        });
    }

    public Iterable<GoddagNode> getFollowingSiblings(final Element root) {
        return IteratorUtil.asIterable(new GoddagNodeIterator(getNextSibling(root)) {

            @Override
            protected GoddagNode getNext() {
                return current.getNextSibling(root);
            }
        });
    }

    public Iterable<GoddagNode> getPrecedingSiblings(final Element root) {
        return IteratorUtil.asIterable(new GoddagNodeIterator(getPreviousSibling(root)) {

            @Override
            protected GoddagNode getNext() {
                return current.getPreviousSibling(root);
            }
        });
    }

    public Iterable<GoddagNode> getAncestors(final Element root) {
        return IteratorUtil.asIterable(new GoddagNodeIterator(getParent(root)) {

            @Override
            protected GoddagNode getNext() {
                return current.getParent(root);
            }
        });
    }

    public Iterable<GoddagNode> getDescendants(final Element root) {
        return IteratorUtil.asIterable(new GoddagNodeIterator(getFirstChild(root)) {
            Stack<GoddagNode> path = new Stack<GoddagNode>();

            @Override
            protected GoddagNode getNext() {
                GoddagNode child = current.getFirstChild(root);
                if (child != null) {
                    path.push(current);
                    return child;
                }

                GoddagNode sibling = current.getNextSibling(root);
                if (sibling != null) {
                    return sibling;
                }

                while (!path.isEmpty()) {
                    sibling = path.pop().getNextSibling(root);
                    if (sibling != null) {
                        return sibling;
                    }
                }

                return null;
            }
        });
    }

    public String getText(Element root) {
        StringBuilder text = new StringBuilder();
        for (GoddagNode child : getChildren(root)) {
            text.append(child.getText(root));
        }
        return text.toString();
    }

    public Iterable<GoddagNode> getAlignedNodes() {
        return new GoddageNodeWrappingIterable(Traversal.description().relationships(ALIGNMENT_AXIS)
                .filter(Traversal.returnAllButStartNode()).traverse(node).nodes());
    }

    public Iterable<GoddagNode> traverse(TraversalDescription traversal) {
        return new GoddageNodeWrappingIterable(traversal.traverse(node).nodes());
    }

    public void copy(Element from, Element to) {
        for (GoddagNode child : getChildren(from)) {
            insert(to, child, null);
            child.copy(from, to);
        }
    }

    public GoddagNode insert(Element root, GoddagNode newChild, GoddagNode before) {
        assert (before == null || equals(before.getParent(root)));

        final GoddagNode prevParent = newChild.getParent(root);
        if (prevParent != null) {
            prevParent.remove(root, newChild);
        }

        long rootId = root.node.getId();
        final Node newChildNode = newChild.node;
        if (before == null) {
            GoddagNode lastChild = getLastChild(root);
            if (lastChild != null) {
                Node lastChildNode = lastChild.node;
                GoddagNodeEdge.setRoot(lastChildNode.createRelationshipTo(newChildNode, SIBLING_AXIS), rootId);
                GoddagNodeEdge.find(lastChildNode.getRelationships(LAST_CHILD, OUTGOING), rootId).delete();
            }
            GoddagNodeEdge.setRoot(newChildNode.createRelationshipTo(node, LAST_CHILD), rootId);
            GoddagNode firstChild = getFirstChild(root);
            if (firstChild == null) {
                GoddagNodeEdge.setRoot(node.createRelationshipTo(newChildNode, FIRST_CHILD), rootId);
            }
        } else {
            Node nextNode = before.node;
            Node prevNode = null;
            Relationship prevRel = GoddagNodeEdge.find(nextNode.getRelationships(SIBLING_AXIS, INCOMING), rootId);
            if (prevRel != null) {
                prevNode = prevRel.getStartNode();
                prevRel.delete();
            }

            GoddagNodeEdge.setRoot(newChildNode.createRelationshipTo(nextNode, SIBLING_AXIS), rootId);

            if (prevNode != null) {
                GoddagNodeEdge.setRoot(prevNode.createRelationshipTo(newChildNode, SIBLING_AXIS), rootId);
            } else {
                Relationship firstRel = GoddagNodeEdge.find(nextNode.getRelationships(FIRST_CHILD, INCOMING), rootId);
                if (firstRel != null) {
                    firstRel.delete();
                }
                GoddagNodeEdge.setRoot(node.createRelationshipTo(newChildNode, FIRST_CHILD), rootId);
            }
        }
        GoddagNodeEdge.setRoot(node.createRelationshipTo(newChildNode, DESCENDANT_AXIS), rootId);

        return newChild;
    }

    public void merge(Element root, GoddagNode toMerge) {
        merge(root, toMerge, toMerge.getNextSibling(root));
    }
    
    public void merge(Element root, GoddagNode toMerge, GoddagNode before) {
        GoddagNode child = toMerge.getFirstChild(root);
        while (child != null) {
            final GoddagNode next = child.getNextSibling(root);
            insert(root, child, before);
            child = next;
        }
        final GoddagNode parent = toMerge.getParent(root);
        if (parent != null) {
            parent.remove(root, toMerge);
        }
    }

    public void remove(Element root, GoddagNode toRemove) {
        assert equals(toRemove.getParent(root));

        final Node nodeToRemove = toRemove.node;
        final long rootId = root.node.getId();

        Relationship prev = GoddagNodeEdge.find(nodeToRemove.getRelationships(SIBLING_AXIS, INCOMING), rootId);
        Relationship next = GoddagNodeEdge.find(nodeToRemove.getRelationships(SIBLING_AXIS, OUTGOING), rootId);
        if (prev != null && next != null) {
            GoddagNodeEdge.setRoot(prev.getStartNode().createRelationshipTo(next.getEndNode(), SIBLING_AXIS), rootId);
        }
        Node prevNode = null;
        if (prev != null) {
            prevNode = prev.getStartNode();
            prev.delete();
        } else {
            GoddagNodeEdge.find(nodeToRemove.getRelationships(FIRST_CHILD, INCOMING), rootId).delete();
            if (next != null) {
                GoddagNodeEdge.setRoot(node.createRelationshipTo(next.getEndNode(), FIRST_CHILD), rootId);
            }
        }

        if (next != null) {
            next.delete();
        } else {
            GoddagNodeEdge.find(nodeToRemove.getRelationships(LAST_CHILD, OUTGOING), rootId).delete();
            if (prevNode != null) {
                GoddagNodeEdge.setRoot(prevNode.createRelationshipTo(node, LAST_CHILD), rootId);
            }
        }

        GoddagNodeEdge.find(nodeToRemove.getRelationships(DESCENDANT_AXIS, INCOMING), rootId).delete();
    }

    public boolean delete(Element root) {
        GoddagNode nextChild = getFirstChild(root);
        while (nextChild != null) {
            GoddagNode current = nextChild;
            nextChild = nextChild.getNextSibling(root);
            current.delete(root);
        }

        GoddagNode parent = getParent(root);
        if (parent != null) {
            parent.remove(root, this);
        }

        if (!node.hasRelationship()) {
            node.delete();
            return true;
        }
        return false;
    }

    public void clear(Element root) {
        GoddagNode child = getFirstChild(root);
        while (child != null) {
            GoddagNode next = child.getNextSibling(root);
            child.delete(root);
            child = next;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof GoddagNode) {
            return node.equals(((GoddagNode) o).node);
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

    @Override
    public String toString() {
        return "<" + PREFIX + "/> " + node.toString();
    }

    protected static GoddagNodeType getNodeType(Node node) {
        return (GoddagNodeType) NODE_TYPES[(Integer) node.getProperty(NODE_TYPE_PROPERTY)];
    }

    protected void setNodeType(GoddagNodeType nodeType) {
        node.setProperty(NODE_TYPE_PROPERTY, nodeType.ordinal());
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

    public static GoddagNode wrap(Node node) {
        final GoddagNodeType nodeType = getNodeType(node);
        switch (nodeType) {
        case TEXT:
            return new Text(node);
        case ELEMENT:
            return new Element(node);
        case COMMENT:
            return new Comment(node);
        case PI:
            return new ProcessingInstruction(node);
        default:
            throw new IllegalArgumentException(nodeType.toString());
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

    private static abstract class GoddagNodeIterator implements Iterator<GoddagNode> {
        protected GoddagNode current;

        private GoddagNodeIterator(GoddagNode next) {
            this.current = next;
        }

        public boolean hasNext() {
            return (current != null);
        }

        public GoddagNode next() {
            GoddagNode node = current;
            current = getNext();
            return node;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected abstract GoddagNode getNext();
    }
}
