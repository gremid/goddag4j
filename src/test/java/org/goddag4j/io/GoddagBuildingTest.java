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

import static org.goddag4j.io.NamespaceMap.TEI_MAP;

import java.net.URL;
import java.util.Collections;

import org.goddag4j.Element;
import org.goddag4j.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

public class GoddagBuildingTest extends BaseTest {
    private static final URL TEST_XML_RESOURCE = GoddagBuildingTest.class.getResource("/george-algabal-tei.xml");

    @Test
    public void build() throws Exception {
        final Element root = GoddagIOUtil.parse(new InputSource(TEST_XML_RESOURCE.toString()), TEI_MAP, db);
        this.root.insert(this.root, root, null);
        
        Assert.assertNotNull(root);
        Assert.assertTrue(root.getDescendants(root).iterator().hasNext());

        GoddagIOUtil.dump(Collections.singleton(root), System.out);
    }
}
