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

import org.neo4j.graphdb.Node;

public class Attribute {

    public static final String PREFIX = GoddagNode.PREFIX + ".attr";
    public final Node node;

    public Attribute(Node node) {
        this.node = node;
    }

    public String getPrefix() {
        return (String) node.getProperty(PREFIX + ".prefix");
    }

    public void setPrefix(String prefix) {
        node.setProperty(PREFIX + ".prefix", prefix);
    }

    public String getName() {
        return (String) node.getProperty(PREFIX + ".name");
    }

    public void setName(String name) {
        node.setProperty(PREFIX + ".name", name);
    }

    public String getQName() {
        return Element.getQName(getPrefix(), getName());
    }
    
    public String getValue() {
        return (String) node.getProperty(PREFIX + ".value");
    }

    public void setValue(String value) {
        node.setProperty(PREFIX + ".value", value);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getQName() + "'/> " + node.toString();
    }
}
