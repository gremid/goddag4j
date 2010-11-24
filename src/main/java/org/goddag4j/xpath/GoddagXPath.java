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

import java.net.URI;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.io.NamespaceMap;

public class GoddagXPath {
    public static JXPathContext createContext(Element root, GoddagNode contextNode, NamespaceMap namespaceMap) {
        synchronized (GoddagXPath.class) {
            if (!registered) {
                JXPathContextReferenceImpl.addNodePointerFactory(new GoddagNodePointerFactory());
                registered = true;
            }
        }
        JXPathContext context = JXPathContext.newContext(new GoddagXPathNode(root, contextNode, namespaceMap));
        for (Map.Entry<URI, String> mapping : namespaceMap.entrySet()) {
            context.registerNamespace(mapping.getValue(), mapping.getKey().toString());
        }
        return context;
    }

    private static boolean registered = false;

}
