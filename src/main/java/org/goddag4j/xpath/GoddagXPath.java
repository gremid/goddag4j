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
