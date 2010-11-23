package org.goddag4j.xpath;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.io.NamespaceMap;

class GoddagXPathNode {
    final Element root;
    final GoddagNode node;
    final NamespaceMap namespaceMap;

    GoddagXPathNode(Element root, GoddagNode node, NamespaceMap namespaceMap) {
        this.root = root;
        this.node = node;
        this.namespaceMap = namespaceMap;
    }

    GoddagXPathNode derive(GoddagNode node) {
        return (node == null ? null : new GoddagXPathNode(root, node, namespaceMap));
    }

    GoddagTreeNode treeNode() {
        if (node instanceof GoddagTreeNode) {
            return (GoddagTreeNode) node;
        }
        throw new IllegalStateException(node + " is not a tree node");
    }

    GoddagXPathNode getLastChild() {
        return derive(treeNode().getLastChild(root));
    }

    GoddagXPathNode getPreviousSibling() {
        return derive(treeNode().getPreviousSibling(root));
    }

    GoddagXPathNode getNextSibling() {
        return derive(treeNode().getNextSibling(root));
    }

    GoddagXPathNode getFirstChild() {
        return derive(treeNode().getFirstChild(root));
    }

    boolean hasChildren() {
        return treeNode().hasChildren(root);
    }

    Iterable<GoddagTreeNode> getChildren() {
        return treeNode().getChildren(root);
    }

    GoddagXPathNode getParent() {
        return derive(treeNode().getParent(root));
    }

    String getText() {
        return treeNode().getText(root);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && (obj instanceof GoddagXPathNode)) {
            return node.equals(((GoddagXPathNode) obj).node);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return node.hashCode();
    }

}
