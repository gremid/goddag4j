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
