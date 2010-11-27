package org.goddag4j.token;

import org.goddag4j.Element;
import org.goddag4j.Text;

public interface TokenMarkupGenerator {

    void generate(Iterable<Text> input, Element to);

}
