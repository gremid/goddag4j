package org.goddag4j.xpath;

import java.util.Locale;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;
import org.apache.commons.jxpath.ri.model.dom.DOMPointerFactory;
import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;

class GoddagNodePointerFactory implements NodePointerFactory {
    public static final int ORDER = (DOMPointerFactory.DOM_POINTER_FACTORY_ORDER - 1);

    public int getOrder() {
        return ORDER;
    }

    public NodePointer createNodePointer(QName name, Object object, Locale locale) {
        if (!(object instanceof GoddagXPathNode)) {
            return null;

        }
        final GoddagXPathNode xPathNode = (GoddagXPathNode) object;
        final GoddagTreeNode node = (GoddagTreeNode) xPathNode.node;
        final GoddagTreeNode parentNode = node.getParent(xPathNode.root);
        if (parentNode == null) {
            return new GoddagNodePointer(xPathNode, locale);
        } else {
            final QName parentName = new QName(((Element) parentNode).getName());
            return createNodePointer(createNodePointer(parentName, xPathNode.derive(parentNode), locale), name, xPathNode);
        }
    }

    public NodePointer createNodePointer(NodePointer parent, QName name, Object object) {
        return (object instanceof GoddagXPathNode ? new GoddagNodePointer((GoddagXPathNode) object, parent) : null);
    }

}
