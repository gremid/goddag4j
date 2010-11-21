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

package org.goddag4j;

import static org.goddag4j.GoddagNodeEdge.ALIGNMENT_AXIS;
import static org.goddag4j.GoddagNodeEdge.DESCENDANT_AXIS;
import static org.goddag4j.GoddagNodeEdge.FIRST_CHILD;
import static org.goddag4j.GoddagNodeEdge.LAST_CHILD;
import static org.goddag4j.GoddagNodeEdge.SIBLING_AXIS;
import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IterableWrapper;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.helpers.collection.NestingIterable;
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
        return new NestingIterable<Element, Relationship>(node.getRelationships(DESCENDANT_AXIS, INCOMING)) {

            @Override
            protected Iterator<Element> createNestedIterator(Relationship item) {
                return GoddagNodeEdge.getRoots(item).iterator();
            }
        };
    }

    public void stream(Element root, GoddagEventHandler handler) {
        for (GoddagNode child : getChildren(root)) {
            child.stream(root, handler);
        }
    }

    public GoddagNode getParent(Element root) {
        final Relationship parentRel = GoddagNodeEdge.find(node.getRelationships(DESCENDANT_AXIS, INCOMING), root.node.getId());
        return (parentRel == null ? null : wrap(parentRel.getOtherNode(node)));
    }

    public GoddagNode getFirstChild(Element root) {
        final Relationship firstRel = GoddagNodeEdge.find(node.getRelationships(FIRST_CHILD, OUTGOING), root.node.getId());
        return (firstRel == null ? null : wrap(firstRel.getOtherNode(node)));
    }

    public GoddagNode getLastChild(Element root) {
        final Relationship lastRel = GoddagNodeEdge.find(node.getRelationships(LAST_CHILD, INCOMING), root.node.getId());
        return (lastRel == null ? null : wrap(lastRel.getOtherNode(node)));
    }

    public GoddagNode getNextSibling(Element root) {
        final Relationship nextRel = GoddagNodeEdge.find(node.getRelationships(SIBLING_AXIS, OUTGOING), root.node.getId());
        return (nextRel == null ? null : wrap(nextRel.getOtherNode(node)));
    }

    public GoddagNode getPreviousSibling(Element root) {
        final Relationship prevRel = GoddagNodeEdge.find(node.getRelationships(SIBLING_AXIS, INCOMING), root.node.getId());
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

        final long rootId = root.node.getId();
        if (before == null) {
            final GoddagNode lastChild = getLastChild(root);
            if (lastChild != null) {
                GoddagNodeEdge.add(SIBLING_AXIS, lastChild.node, newChild.node, rootId);
                GoddagNodeEdge.remove(lastChild.node.getRelationships(LAST_CHILD, OUTGOING), rootId);
            }
            GoddagNodeEdge.add(LAST_CHILD, newChild.node, node, rootId);
            
            final GoddagNode firstChild = getFirstChild(root);
            if (firstChild == null) {
                GoddagNodeEdge.add(FIRST_CHILD, node, newChild.node, rootId);
            }
        } else {
            Node prevNode = null;
            
            final Relationship prevRel = GoddagNodeEdge.find(before.node.getRelationships(SIBLING_AXIS, INCOMING), rootId);
            if (prevRel != null) {
                prevNode = prevRel.getStartNode();
                GoddagNodeEdge.remove(Collections.singleton(prevRel), rootId);
            }

            GoddagNodeEdge.add(SIBLING_AXIS, newChild.node, before.node, rootId);

            if (prevNode != null) {
                GoddagNodeEdge.add(SIBLING_AXIS, prevNode, newChild.node, rootId);
            } else {
                final Relationship firstRel = GoddagNodeEdge.find(node.getRelationships(FIRST_CHILD, OUTGOING), rootId);
                if (firstRel != null) {
                    GoddagNodeEdge.remove(Collections.singleton(firstRel), rootId);
                }
                GoddagNodeEdge.add(FIRST_CHILD, node, newChild.node, rootId);
            }
        }
        
        GoddagNodeEdge.add(DESCENDANT_AXIS, node, newChild.node, rootId);

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

        final long rootId = root.node.getId();
        final Relationship prev = GoddagNodeEdge.find(toRemove.node.getRelationships(SIBLING_AXIS, INCOMING), rootId);
        final Relationship next = GoddagNodeEdge.find(toRemove.node.getRelationships(SIBLING_AXIS, OUTGOING), rootId);
        
        if (prev != null && next != null) {
            GoddagNodeEdge.add(SIBLING_AXIS, prev.getStartNode(), next.getEndNode(), rootId);
        }
        Node prevNode = null;
        if (prev != null) {
            prevNode = prev.getStartNode();
            GoddagNodeEdge.remove(Collections.singleton(prev), rootId);
        } else {
            GoddagNodeEdge.remove(toRemove.node.getRelationships(FIRST_CHILD, INCOMING), rootId);
            if (next != null) {
                GoddagNodeEdge.add(FIRST_CHILD, node, next.getEndNode(), rootId);
            }
        }

        if (next != null) {
            GoddagNodeEdge.remove(Collections.singleton(next), rootId);
        } else {
            GoddagNodeEdge.remove(toRemove.node.getRelationships(LAST_CHILD, OUTGOING), rootId);
            if (prevNode != null) {
                GoddagNodeEdge.add(LAST_CHILD, prevNode, node, rootId);
            }
        }

        GoddagNodeEdge.remove(toRemove.node.getRelationships(DESCENDANT_AXIS, INCOMING), rootId);
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
