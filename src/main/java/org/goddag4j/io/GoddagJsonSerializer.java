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
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagNodeType;
import org.goddag4j.ProcessingInstruction;
import org.goddag4j.Text;

public class GoddagJsonSerializer {

    private Map<GoddagNodeType, Set<GoddagNode>> serialized = new HashMap<GoddagNodeType, Set<GoddagNode>>();

    public void serialize(JsonGenerator out, Iterable<Element> roots) throws IOException {
        serialize(out, NamespaceMap.EMPTY_MAP, roots);
    }
    
    public void serialize(JsonGenerator out, Map<URI, String> namespaces, Iterable<Element> roots) throws IOException {
        for (GoddagNodeType nt : GoddagNodeType.values()) {
            serialized.put(nt, new HashSet<GoddagNode>());
        }

        out.writeStartObject();

        out.writeArrayFieldStart("trees");
        for (Element root : roots) {
            serializeTree(out, root, root);
        }
        out.writeEndArray();

        out.writeArrayFieldStart("nodes");
        for (GoddagNodeType nt : GoddagNodeType.values()) {
            out.writeStartArray();
            for (GoddagNode node : serialized.get(nt)) {
                serializeNode(out, node, nt);
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
    }

    protected void serializeTree(JsonGenerator out, Element root, GoddagNode node) throws IOException {
        final GoddagNodeType nt = node.getNodeType();
        serialized.get(nt).add(node);

        out.writeStartObject();
        out.writeNumberField("id", node.node.getId());
        out.writeNumberField("nt", nt.ordinal());
        if (node.hasChildren(root)) {
            out.writeArrayFieldStart("ch");
            for (GoddagNode child : node.getChildren(root)) {
                serializeTree(out, root, child);
            }
            out.writeEndArray();
        }
        out.writeEndObject();
    }

    protected void serializeNode(JsonGenerator out, GoddagNode node, GoddagNodeType nt) throws IOException {
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
        serializeAdditionalNodeData(out, node, nt);
        out.writeEndArray();
    }

    protected void serializeAdditionalNodeData(JsonGenerator out, GoddagNode node, GoddagNodeType nt) {
    }
}
