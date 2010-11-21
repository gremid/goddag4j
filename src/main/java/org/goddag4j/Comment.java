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

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

public class Comment extends GoddagNode {

    private static final String PREFIX = GoddagNode.PREFIX + ".comment";

    protected Comment(Node node) {
        super(node);
    }

    public static Comment create(GraphDatabaseService db, String content) {
        Comment comment = new Comment(db.createNode());
        comment.setNodeType(GoddagNodeType.COMMENT);
        comment.setContent(content);
        return comment;
    }

    @Override
    public void stream(Element root, GoddagEventHandler handler) {
        handler.comment(this);
    }
    
    @Override
    public String getText(Element root) {
        return "";
    }

    public String getContent() {
        return (String) node.getProperty(PREFIX + ".content");
    }

    public void setContent(String content) {
        node.setProperty(PREFIX + ".content", content);
    }

    @Override
    public String toString() {
        return "<" + PREFIX + " '" + getContent() + "'/> " + node.toString();
    }
}
