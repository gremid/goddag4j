package org.goddag4j;

import org.goddag4j.Element;
import org.junit.After;
import org.junit.Before;

public abstract class GoddagTestBase extends GraphDbBasedTest {

    protected Element root;

    @Before
    public void createNodeContext() {
        root = Element.create(db, "tei", "text");
    }

    @After
    public void removeNodeContext() {
        root.delete(root);
    }
}
