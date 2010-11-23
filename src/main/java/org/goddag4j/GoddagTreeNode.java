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

import static org.neo4j.graphdb.Direction.INCOMING;
import static org.neo4j.graphdb.Direction.OUTGOING;

import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.helpers.collection.NestingIterable;

public abstract class GoddagTreeNode extends GoddagNode {

    protected GoddagTreeNode(Node node) {
        super(node);
    }

    public Iterable<Element> getRoots() {
        return new NestingIterable<Element, Relationship>(node.getRelationships(GoddagEdge.CONTAINS, INCOMING)) {

            @Override
            protected Iterator<Element> createNestedIterator(Relationship item) {
                return GoddagEdge.getRoots(item).iterator();
            }
        };
    }

    private GoddagTreeNode adjacentTreeNode(GoddagEdge edgeType, Direction direction, Element root) {
        final Relationship r = GoddagEdge.find(node.getRelationships(edgeType, direction), root.node.getId());
        return (GoddagTreeNode) (r == null ? null : wrap(r.getOtherNode(node)));        
    }
    
    public GoddagTreeNode getParent(Element root) {
        return adjacentTreeNode(GoddagEdge.CONTAINS, INCOMING, root);
    }

    public GoddagTreeNode getFirstChild(Element root) {
        return adjacentTreeNode(GoddagEdge.HAS_FIRST_CHILD, OUTGOING, root);
    }

    public GoddagTreeNode getLastChild(Element root) {
        return adjacentTreeNode(GoddagEdge.IS_LAST_CHILD_OF, INCOMING, root);
    }

    public GoddagTreeNode getNextSibling(Element root) {
        return adjacentTreeNode(GoddagEdge.HAS_SIBLING, OUTGOING, root);
    }

    public GoddagTreeNode getPreviousSibling(Element root) {
        return adjacentTreeNode(GoddagEdge.HAS_SIBLING, INCOMING, root);
    }

    public boolean hasChildren(Element root) {
        return (GoddagEdge.find(node.getRelationships(GoddagEdge.HAS_FIRST_CHILD, OUTGOING), root.node.getId()) != null);
    }

    public Iterable<GoddagTreeNode> getChildren(final Element root) {
        return IteratorUtil.asIterable(new GoddagTreeNodeIterator(getFirstChild(root)) {

            @Override
            protected GoddagTreeNode getNext() {
                return current.getNextSibling(root);
            }
        });
    }

    public Iterable<GoddagTreeNode> getFollowingSiblings(final Element root) {
        return IteratorUtil.asIterable(new GoddagTreeNodeIterator(getNextSibling(root)) {

            @Override
            protected GoddagTreeNode getNext() {
                return current.getNextSibling(root);
            }
        });
    }

    public Iterable<GoddagTreeNode> getPrecedingSiblings(final Element root) {
        return IteratorUtil.asIterable(new GoddagTreeNodeIterator(getPreviousSibling(root)) {

            @Override
            protected GoddagTreeNode getNext() {
                return current.getPreviousSibling(root);
            }
        });
    }

    public Iterable<GoddagTreeNode> getAncestors(final Element root) {
        return IteratorUtil.asIterable(new GoddagTreeNodeIterator(getParent(root)) {

            @Override
            protected GoddagTreeNode getNext() {
                return current.getParent(root);
            }
        });
    }

    public Iterable<GoddagTreeNode> getDescendants(final Element root) {
        return IteratorUtil.asIterable(new GoddagTreeNodeIterator(getFirstChild(root)) {
            Stack<GoddagTreeNode> path = new Stack<GoddagTreeNode>();

            @Override
            protected GoddagTreeNode getNext() {
                GoddagTreeNode child = current.getFirstChild(root);
                if (child != null) {
                    path.push(current);
                    return child;
                }

                GoddagTreeNode sibling = current.getNextSibling(root);
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
        for (GoddagTreeNode child : getChildren(root)) {
            text.append(child.getText(root));
        }
        return text.toString();
    }

    public void copy(Element from, Element to) {
        for (GoddagTreeNode child : getChildren(from)) {
            insert(to, child, null);
            child.copy(from, to);
        }
    }

    public GoddagTreeNode insert(Element root, GoddagTreeNode child, GoddagTreeNode before) {
        assert (before == null || equals(before.getParent(root)));

        final GoddagTreeNode prevParent = child.getParent(root);
        if (prevParent != null) {
            prevParent.unlink(root, child);
        }
        
        final long rootId = root.node.getId();
        
        if (before == null) {
            final GoddagTreeNode lastChild = getLastChild(root);
            if (lastChild != null) {
                GoddagEdge.add(GoddagEdge.HAS_SIBLING, lastChild.node, child.node, rootId);
                GoddagEdge.remove(lastChild.node.getRelationships(GoddagEdge.IS_LAST_CHILD_OF, OUTGOING), rootId);
            }
            GoddagEdge.add(GoddagEdge.IS_LAST_CHILD_OF, child.node, node, rootId);

            final GoddagTreeNode firstChild = getFirstChild(root);
            if (firstChild == null) {
                GoddagEdge.add(GoddagEdge.HAS_FIRST_CHILD, node, child.node, rootId);
            }
        } else {
            Node prevNode = null;

            final Relationship prevRel = GoddagEdge.find(before.node.getRelationships(GoddagEdge.HAS_SIBLING, INCOMING), rootId);
            if (prevRel != null) {
                prevNode = prevRel.getStartNode();
                GoddagEdge.remove(Collections.singleton(prevRel), rootId);
            }

            GoddagEdge.add(GoddagEdge.HAS_SIBLING, child.node, before.node, rootId);

            if (prevNode != null) {
                GoddagEdge.add(GoddagEdge.HAS_SIBLING, prevNode, child.node, rootId);
            } else {
                final Relationship firstRel = GoddagEdge.find(node.getRelationships(GoddagEdge.HAS_FIRST_CHILD, OUTGOING), rootId);
                if (firstRel != null) {
                    GoddagEdge.remove(Collections.singleton(firstRel), rootId);
                }
                GoddagEdge.add(GoddagEdge.HAS_FIRST_CHILD, node, child.node, rootId);
            }
        }

        GoddagEdge.add(GoddagEdge.CONTAINS, node, child.node, rootId);

        return child;
    }

    public void merge(Element root, GoddagTreeNode toMerge) {
        merge(root, toMerge, toMerge.getNextSibling(root));
    }

    public void merge(Element root, GoddagTreeNode toMerge, GoddagTreeNode before) {
        GoddagTreeNode child = toMerge.getFirstChild(root);
        while (child != null) {
            final GoddagTreeNode next = child.getNextSibling(root);
            insert(root, child, before);
            child = next;
        }
        final GoddagTreeNode parent = toMerge.getParent(root);
        if (parent != null) {
            parent.remove(root, toMerge, false);
        }
    }

    public void remove(Element root, GoddagTreeNode child, boolean recursive) {
        assert equals(child.getParent(root));

        if (recursive) {
            GoddagTreeNode nextChild = child.getFirstChild(root);
            while (nextChild != null) {
                GoddagTreeNode current = nextChild;
                nextChild = nextChild.getNextSibling(root);
                child.remove(root, current, true);
            }
        }

        unlink(root, child);
        
        if (!node.hasRelationship()) {
            node.delete();
        }
    }

    private void unlink(Element root, GoddagTreeNode child) {
        final long rootId = root.node.getId();
        final Relationship prev = GoddagEdge.find(child.node.getRelationships(GoddagEdge.HAS_SIBLING, INCOMING), rootId);
        final Relationship next = GoddagEdge.find(child.node.getRelationships(GoddagEdge.HAS_SIBLING, OUTGOING), rootId);

        if (prev != null && next != null) {
            GoddagEdge.add(GoddagEdge.HAS_SIBLING, prev.getStartNode(), next.getEndNode(), rootId);
        }
        Node prevNode = null;
        if (prev != null) {
            prevNode = prev.getStartNode();
            GoddagEdge.remove(Collections.singleton(prev), rootId);
        } else {
            GoddagEdge.remove(child.node.getRelationships(GoddagEdge.HAS_FIRST_CHILD, INCOMING), rootId);
            if (next != null) {
                GoddagEdge.add(GoddagEdge.HAS_FIRST_CHILD, node, next.getEndNode(), rootId);
            }
        }

        if (next != null) {
            GoddagEdge.remove(Collections.singleton(next), rootId);
        } else {
            GoddagEdge.remove(child.node.getRelationships(GoddagEdge.IS_LAST_CHILD_OF, OUTGOING), rootId);
            if (prevNode != null) {
                GoddagEdge.add(GoddagEdge.IS_LAST_CHILD_OF, prevNode, node, rootId);
            }
        }

        GoddagEdge.remove(child.node.getRelationships(GoddagEdge.CONTAINS, INCOMING), rootId);
    }
    
    public void clear(Element root) {
        GoddagTreeNode child = getFirstChild(root);
        while (child != null) {
            GoddagTreeNode next = child.getNextSibling(root);
            remove(root, child, true);
            child = next;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof GoddagTreeNode) {
            return node.equals(((GoddagTreeNode) o).node);
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

    private static abstract class GoddagTreeNodeIterator implements Iterator<GoddagTreeNode> {
        protected GoddagTreeNode current;

        private GoddagTreeNodeIterator(GoddagTreeNode next) {
            this.current = next;
        }

        public boolean hasNext() {
            return (current != null);
        }

        public GoddagTreeNode next() {
            GoddagTreeNode node = current;
            current = getNext();
            return node;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected abstract GoddagTreeNode getNext();
    }
}
