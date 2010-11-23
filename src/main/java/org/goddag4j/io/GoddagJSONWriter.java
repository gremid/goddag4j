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

package org.goddag4j.io;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.codehaus.jackson.JsonGenerator;
import org.goddag4j.Attribute;
import org.goddag4j.Comment;
import org.goddag4j.Element;
import org.goddag4j.GoddagNode.NodeType;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;

public class GoddagJSONWriter {

    private final Map<URI, String> namespaces;
    private Map<NodeType, Set<GoddagTreeNode>> writeLog = new HashMap<NodeType, Set<GoddagTreeNode>>();

    public GoddagJSONWriter(Map<URI, String> namespaces) {
        this.namespaces = namespaces;
    }
    
    public void write(Iterable<Element> roots, JsonGenerator out) throws IOException {
        writeLog.clear();
        for (NodeType nt : NodeType.values()) {
            writeLog.put(nt, new HashSet<GoddagTreeNode>());
        }

        out.writeStartObject();

        out.writeArrayFieldStart("trees");
        for (Element root : roots) {
            writeTree(out, root, root);
        }
        out.writeEndArray();

        out.writeArrayFieldStart("nodes");
        for (NodeType nt : NodeType.values()) {
            out.writeStartArray();
            for (GoddagTreeNode node : writeLog.get(nt)) {
                writeNode(out, node, nt);
            }
            out.writeEndArray();
        }
        out.writeEndArray();

        if (!namespaces.isEmpty()) {
            out.writeObjectFieldStart("namespaces");
            for (URI ns : namespaces.keySet()) {
                out.writeStringField(namespaces.get(ns), ns.toString());
            }
            out.writeEndObject();
        }
        out.writeEndObject();
        writeLog.clear();
    }

    private void writeTree(JsonGenerator out, Element root, GoddagTreeNode node) throws IOException {
        final NodeType nt = node.getNodeType();
        writeLog.get(nt).add(node);

        out.writeStartObject();
        out.writeNumberField("id", node.node.getId());
        out.writeNumberField("nt", nt.ordinal());
        if (node.hasChildren(root)) {
            out.writeArrayFieldStart("ch");
            for (GoddagTreeNode child : node.getChildren(root)) {
                writeTree(out, root, child);
            }
            out.writeEndArray();
        }
        out.writeEndObject();
    }

    private void writeNode(JsonGenerator out, GoddagTreeNode node, NodeType nt) throws IOException {
        out.writeStartArray();
        out.writeNumber(node.node.getId());

        switch (nt) {
        case TEXT:
            out.writeString(((Text) node).getText());
            break;
        case ELEMENT:
            Element element = (Element) node;
            out.writeString(element.getQName());
            out.writeStartArray();
            for (Attribute a : element.getAttributes()) {
                out.writeStartArray();
                out.writeString(a.getQName());
                out.writeString(a.getValue());
                out.writeEndArray();
            }
            out.writeEndArray();
            break;
        case COMMENT:
            out.writeString(((Comment) node).getContent());
            break;
        case PI:
            ProcessingInstruction pi = (ProcessingInstruction) node;
            out.writeString(pi.getTarget());
            out.writeString(pi.getInstruction());
            break;
        }
        doWriteNode(out, node, nt);
        out.writeEndArray();
    }

    protected void doWriteNode(JsonGenerator out, GoddagTreeNode node, NodeType nt) {
    }
}
