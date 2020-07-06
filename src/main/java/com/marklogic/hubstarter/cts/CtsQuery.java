package com.marklogic.hubstarter.cts;

import java.util.ArrayList;
import java.util.List;

public class CtsQuery {
    String elementName;
    List<CtsQuery> nested;

    CtsQuery(String elementName) {
        this.elementName = elementName;
    }
}