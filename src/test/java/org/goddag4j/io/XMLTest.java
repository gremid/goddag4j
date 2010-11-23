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

import java.util.Collections;

import org.goddag4j.Element;
import org.goddag4j.GraphDatabaseTestContext;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

public class XMLTest extends GraphDatabaseTestContext {
    private final InputSource xml = new InputSource(getClass().getResource("/george-algabal-tei.xml").toString());

    @Test
    public void readWrite() throws Exception {
        final Element algabal = new GoddagXMLReader(db, NamespaceMap.TEI_MAP).parse(xml);
        this.root.insert(this.root, algabal, null);
        
        Assert.assertNotNull(algabal);
        Assert.assertTrue(algabal.getDescendants(algabal).iterator().hasNext());
        dump(Collections.singleton(algabal));
        
        dump(new GoddagXMLWriter(algabal, NamespaceMap.TEI_MAP, true).toSAXSource());
    }
}
