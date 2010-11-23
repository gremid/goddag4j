package org.goddag4j.xpath;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.NamespaceResolver;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.compiler.ProcessingInstructionTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.goddag4j.Attribute;
import org.goddag4j.Comment;
import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagNode.NodeType;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;

class GoddagNodePointer extends NodePointer {

    private static final long serialVersionUID = -107362549103075131L;

    private final GoddagXPathNode xPathNode;
    private final String id;
    private NamespaceResolver localNamespaceResolver;

    public GoddagNodePointer(GoddagXPathNode xPathNode, Locale locale, String id) {
        super(null, locale);
        this.xPathNode = xPathNode;
        this.id = null;
    }

    public GoddagNodePointer(GoddagXPathNode xPathNode, Locale locale) {
        this(xPathNode, locale, null);
    }

    public GoddagNodePointer(GoddagXPathNode xPathNode, NodePointer parent) {
        super(parent);
        this.xPathNode = xPathNode;
        this.id = null;
    }

    public NodeIterator childIterator(final NodeTest test, final boolean reverse, final NodePointer startWith) {
        return new NodeIterator() {
            private GoddagXPathNode child = (startWith == null ? null : (GoddagXPathNode) startWith.getImmediateNode());
            private int position = 0;

            public NodePointer getNodePointer() {
                if (position == 0) {
                    setPosition(1);
                }
                return (child == null ? null : new GoddagNodePointer(child, GoddagNodePointer.this));
            }

            public int getPosition() {
                return position;
            }

            public boolean setPosition(int position) {
                while (this.position < position) {
                    if (!next()) {
                        return false;
                    }
                }
                while (this.position > position) {
                    if (!previous()) {
                        return false;
                    }
                }
                return true;
            }

            private boolean previous() {
                position--;
                if (!reverse) {
                    if (position == 0) {
                        child = null;
                    } else if (child == null) {
                        child = xPathNode.getLastChild();
                    } else {
                        child = child.getPreviousSibling();
                    }
                    while (child != null && !testNode(child, test)) {
                        child = child.getPreviousSibling();
                    }
                } else {
                    child = child.getNextSibling();
                    while (child != null && !testNode(child, test)) {
                        child = child.getNextSibling();
                    }
                }
                return (child != null);
            }

            private boolean next() {
                position++;
                if (!reverse) {
                    if (position == 1) {
                        if (child == null) {
                            child = xPathNode.getFirstChild();
                        } else {
                            child = child.getNextSibling();
                        }
                    } else {
                        child = child.getNextSibling();
                    }
                    while (child != null && !testNode(child, test)) {
                        child = child.getNextSibling();
                    }
                } else {
                    if (position == 1) {
                        if (child == null) {
                            child = xPathNode.getLastChild();
                        } else {
                            child = child.getPreviousSibling();
                        }
                    } else {
                        child = child.getPreviousSibling();
                    }
                    while (child != null && !testNode(child, test)) {
                        child = child.getPreviousSibling();
                    }
                }
                return (child != null);
            }
        };
    }

    public NodeIterator attributeIterator(final QName name) {
        if (!(xPathNode.node instanceof Element)) {
            return null;
        }

        final String prefix = name.getPrefix();
        final String namespace = (prefix == null ? null : this.parent.getNamespaceResolver().getNamespaceURI(prefix));
        final NodeNameTest nameTest = new NodeNameTest(name, namespace);

        final List<GoddagXPathNode> attributes = new ArrayList<GoddagXPathNode>();
        for (Attribute attr : ((Element) xPathNode.node).getAttributes()) {
            GoddagXPathNode attrXPathNode = xPathNode.derive(attr);
            if (testNode(attrXPathNode, nameTest)) {
                attributes.add(attrXPathNode);
            }

        }
        return new NodeIterator() {

            private int position = 0;

            public NodePointer getNodePointer() {
                if (position == 0) {
                    if (!setPosition(1)) {
                        return null;
                    }
                    position = 0;
                }
                int index = position - 1;
                if (index < 0) {
                    index = 0;
                }
                return new GoddagNodePointer(attributes.get(index), GoddagNodePointer.this);
            }

            public int getPosition() {
                return position;
            }

            public boolean setPosition(int position) {
                this.position = position;
                return (position >= 1 && position <= attributes.size());
            }
        };
    }

    public QName getName() {
        if (xPathNode.node instanceof Element) {
            return new QName(getNamespaceURI(), getLocalName());
        } else if (xPathNode.node instanceof ProcessingInstruction) {
            return new QName(null, ((ProcessingInstruction) xPathNode.node).getTarget());
        }
        return new QName(null, null);
    }

    public synchronized NamespaceResolver getNamespaceResolver() {
        if (localNamespaceResolver == null) {
            localNamespaceResolver = new NamespaceResolver(super.getNamespaceResolver());
            localNamespaceResolver.setNamespaceContextPointer(this);
        }
        return localNamespaceResolver;
    }

    public String getNamespaceURI(String prefix) {
        return null;
    }

    public String getDefaultNamespaceURI() {
        return null;
    }

    public Object getBaseValue() {
        return xPathNode.node;
    }

    public Object getImmediateNode() {
        return xPathNode;
    }

    @Override
    public Object getNode() {
        return xPathNode.node;
    }

    public boolean isActual() {
        return true;
    }

    public boolean isCollection() {
        return false;
    }

    public int getLength() {
        return 1;
    }

    public boolean isLeaf() {
        return (xPathNode.node.getNodeType() == NodeType.ELEMENT ? !xPathNode.hasChildren() : true);
    }

    public boolean isLanguage(String lang) {
        GoddagXPathNode current = xPathNode;
        while (current != null) {
            if (current.node.getNodeType() == NodeType.ELEMENT) {
                Attribute attr = ((Element) current.node).getAttribute("xml", "lang");
                if (attr != null) {
                    return attr.getValue().toUpperCase(Locale.ENGLISH).startsWith(lang.toUpperCase(Locale.ENGLISH));
                }
            }
            current = current.getParent();
        }

        return super.isLanguage(lang);
    }

    public void setValue(Object value) {
        throw new UnsupportedOperationException("Cannot modify GODDAG nodes");
    }

    public String asPath() {
        if (id != null) {
            return "id('" + escape(id) + "')";
        }

        StringBuffer buffer = new StringBuffer();
        if (parent != null) {
            buffer.append(parent.asPath());
        }
        if (xPathNode.node instanceof Element) {
            if (parent instanceof GoddagNodePointer) {
                if (buffer.length() == 0 || buffer.charAt(buffer.length() - 1) != '/') {
                    buffer.append('/');
                }
                String ln = getLocalName();
                String nsURI = getNamespaceURI();
                if (nsURI == null) {
                    buffer.append(ln);
                    buffer.append('[');
                    buffer.append(getRelativePositionByName()).append(']');
                } else {
                    String prefix = getNamespaceResolver().getPrefix(nsURI);
                    if (prefix != null) {
                        buffer.append(prefix);
                        buffer.append(':');
                        buffer.append(ln);
                        buffer.append('[');
                        buffer.append(getRelativePositionByName());
                        buffer.append(']');
                    } else {
                        buffer.append("node()");
                        buffer.append('[');
                        buffer.append(getRelativePositionOfElement());
                        buffer.append(']');
                    }
                }
            }
        } else if (xPathNode.node instanceof Text) {
            buffer.append("/text()");
            buffer.append('[');
            buffer.append(getRelativePositionOfTextNode()).append(']');
        } else if (xPathNode.node instanceof ProcessingInstruction) {
            buffer.append("/processing-instruction(\'");
            buffer.append(((ProcessingInstruction) xPathNode.node).getTarget()).append("')");
            buffer.append('[');
            buffer.append(getRelativePositionOfPI()).append(']');
        }
        return buffer.toString();
    }

    public boolean testNode(NodeTest test) {
        return testNode(xPathNode, test);
    }

    /**
     * Test a Node.
     * 
     * @param node
     *            to test
     * @param test
     *            to execute
     * @return true if node passes test
     */
    public boolean testNode(GoddagXPathNode node, NodeTest test) {
        if (test == null) {
            return true;
        }
        if (test instanceof NodeNameTest) {
            String prefix = null;
            String localName = null;
            String namespaceURI = null;
            if (xPathNode.node instanceof Element) {
                Element element = (Element) xPathNode.node;
                prefix = element.getPrefix();
                localName = element.getName();
                namespaceURI = xPathNode.namespaceMap.getNamespaceURI(element.getPrefix()).toString();
            } else if (xPathNode.node instanceof Attribute) {
                Attribute attr = (Attribute) xPathNode.node;
                prefix = attr.getPrefix();
                localName = attr.getName();
                namespaceURI = xPathNode.namespaceMap.getNamespaceURI(attr.getPrefix()).toString();
            } else {
                return false;
            }

            NodeNameTest nodeNameTest = (NodeNameTest) test;
            final QName testName = nodeNameTest.getNodeName();
            final String testNamespaceURI = nodeNameTest.getNamespaceURI();
            final boolean wildcard = nodeNameTest.isWildcard();
            final String testPrefix = testName.getPrefix();

            if (wildcard && testPrefix == null) {
                return true;
            }
            if (wildcard || testName.getName().equals(localName)) {
                return equalStrings(testNamespaceURI, namespaceURI) || namespaceURI == null && equalStrings(testPrefix, prefix);
            }
            return false;
        }
        if (test instanceof NodeTypeTest) {
            switch (((NodeTypeTest) test).getNodeType()) {
            case Compiler.NODE_TYPE_NODE:
                return (xPathNode.node instanceof GoddagNode);
            case Compiler.NODE_TYPE_TEXT:
                return (xPathNode.node instanceof Text);
            case Compiler.NODE_TYPE_COMMENT:
                return (xPathNode.node instanceof Comment);
            case Compiler.NODE_TYPE_PI:
                return (xPathNode.node instanceof ProcessingInstruction);
            default:
                return false;
            }
        }
        if (test instanceof ProcessingInstructionTest && (xPathNode.node instanceof ProcessingInstruction)) {
            String testPI = ((ProcessingInstructionTest) test).getTarget();
            String nodePI = ((ProcessingInstruction) xPathNode.node).getTarget();
            return testPI.equals(nodePI);
        }
        return false;
    }

    private int getRelativePositionByName() {
        int count = 1;
        if (xPathNode.node.getNodeType() == NodeType.ELEMENT) {
            Element element = (Element) xPathNode.node;
            String name = element.getName();
            GoddagXPathNode n = xPathNode.getPreviousSibling();
            while (n != null) {
                if (n.node.getNodeType() == NodeType.ELEMENT) {
                    String nm = ((Element) n.node).getName();
                    if (nm.equals(name)) {
                        count++;
                    }
                }
                n = n.getPreviousSibling();
            }
        }
        return count;
    }

    private int getRelativePositionOfElement() {
        int count = 1;
        GoddagXPathNode n = xPathNode.getPreviousSibling();
        while (n != null) {
            if (n.node instanceof Element) {
                count++;
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

    private int getRelativePositionOfTextNode() {
        int count = 1;
        GoddagXPathNode n = xPathNode.getPreviousSibling();
        while (n != null) {
            if (n.node instanceof Text) {
                count++;
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

    private int getRelativePositionOfPI() {
        int count = 1;
        final String target = ((ProcessingInstruction) xPathNode.node).getTarget();
        GoddagXPathNode n = xPathNode.getPreviousSibling();
        while (n != null) {
            if ((n.node instanceof ProcessingInstruction) && ((ProcessingInstruction) n.node).getTarget().equals(target)) {
                count++;
            }
            n = n.getPreviousSibling();
        }
        return count;
    }

    private static boolean equalStrings(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }
        s1 = s1 == null ? "" : s1.trim();
        s2 = s2 == null ? "" : s2.trim();
        return s1.equals(s2);
    }

    public boolean equals(Object object) {
        if (object != null && object instanceof GoddagNodePointer) {
            return xPathNode.equals(((GoddagNodePointer) object).xPathNode);
        }
        return super.equals(object);
    }

    public static String getPrefix(GoddagNode node) {
        return null;
    }

    public int hashCode() {
        return xPathNode.hashCode();
    }

    public String getLocalName() {
        return (xPathNode.node.getNodeType() == NodeType.ELEMENT ? ((Element) xPathNode.node).getName() : null);
    }

    public String getNamespaceURI() {
        return (xPathNode.node.getNodeType() == NodeType.ELEMENT ? xPathNode.namespaceMap.getNamespaceURI(
                ((Element) xPathNode.node).getPrefix()).toString() : null);
    }

    public Object getValue() {
        if (xPathNode.node instanceof Comment) {
            return ((Comment) xPathNode.node).getContent();
        } else if (xPathNode.node instanceof ProcessingInstruction) {
            String text = ((ProcessingInstruction) xPathNode.node).getInstruction();
            return (text == null ? "" : text);
        } else if (xPathNode.node instanceof GoddagTreeNode) {
            return xPathNode.getText();
        } else if (xPathNode.node instanceof Attribute) {
            return ((Attribute) xPathNode.node).getValue();
        }
        return null;
    }

    public Pointer getPointerByID(JXPathContext context, String id) {
        return new NullPointer(getLocale(), id);
    }

    public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
        final GoddagXPathNode node1 = (GoddagXPathNode) pointer1.getImmediateNode();
        final GoddagXPathNode node2 = (GoddagXPathNode) pointer2.getImmediateNode();
        if (node1.equals(node2)) {
            return 0;
        }

        if ((node1.node instanceof Attribute) && !(node2.node instanceof Attribute)) {
            return -1;
        }
        if (!(node1.node instanceof Attribute) && (node2.node instanceof Attribute)) {
            return 1;
        }

        if (!(xPathNode.node instanceof GoddagTreeNode)) {
            return 0;
        }

        if ((node1.node instanceof Attribute) && (node2.node instanceof Attribute)) {
            return 0;
        }

        GoddagXPathNode current = xPathNode.getFirstChild();
        while (current != null) {
            if (current.equals(node1)) {
                return -1;
            }
            if (current.equals(node2)) {
                return 1;
            }
            current = current.getNextSibling();
        }
        return 0;
    }

}
