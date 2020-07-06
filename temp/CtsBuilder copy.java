package com.marklogic.hubstarter.cts;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import com.marklogic.client.query.StructuredQueryBuilder;

public class CtsBuilder {
    public static void main(String[] args) {

        CtsBuilder cb = new CtsBuilder();

        String query = cb.orQuery(
            cb.andQuery(
                cb.wordQuery(new String[]{ "test1" }, null, 23D),
                cb.wordQuery(new String[]{ "test2", "test3" }, null, null)
            ),
            cb.andQuery(
                cb.wordQuery(new String[]{ "test4" }, null, null),
                cb.wordQuery(new String[]{ "test5" }, new String[]{ "case-sensitive" }, null)
            ),
            cb.elementPairGeospatialQuery(
                QName.valueOf("{http://www.yell.com/2009/pfi2}wgs84"), 
                latitudeElementNames, 
                longitudeElementNames, 
                regions, 
                options, 
                weight
            )
        ).serialize();       

        System.out.println(query);
    }

    static final String NS_PREFIX = "cts";
    static final String NS = "http://marklogic.com/cts";
    static final String NS_DECLARE = " xmlns:" + NS_PREFIX + "=\"" + NS + "\"";

    public CtsBuilder() {
        this("query", true);
    }

    public class Def {
        String elementName;
        List<Def> options;
    }

    public class LeafDef extends Def {
        Double weight;
        String value;
    }

    public class BranchDef extends Def {
        List<Def> contents;
    }

    String elementName;
    String value;
    Double weight;
    List<CtsBuilder> container;


    CtsDef(String elementName, String value) {
        this.elementName = elementName;
        this.value = value;
    }

    CtsBuilder(String elementName, boolean isContainer) {
        this.elementName = elementName;

        if (isContainer) {
            container = new ArrayList<CtsBuilder>();
        }
    }

    public String serialize() {
        return serialize(true);
    }

    String serialize(boolean includeNamespace) {
        StringBuilder builder = new StringBuilder();
        builder.append("<").append(NS_PREFIX).append(":").append(elementName);
        if (includeNamespace) {
            builder.append(NS_DECLARE);
        }
        if (weight != null) {
            builder.append(" weight=\"").append(weight).append("\"");
        }
        builder.append(">");

        if (container != null) {
            for (CtsBuilder cb : container) {
                builder.append(cb.serialize(false));
            }
        } else if (value != null) {
            builder.append(value);
        }

        builder.append("</").append(NS_PREFIX).append(":").append(elementName).append(">");
        return builder.toString();
    }

    public CtsBuilder andQuery(CtsBuilder ... queries) {
        CtsBuilder query = new CtsBuilder("and-query", true);

        for (CtsBuilder q : queries) {
            query.container.add(q);
        }

        return query;
    }

    public CtsBuilder orQuery(CtsBuilder ... queries) {
        CtsBuilder query = new CtsBuilder("or-query", true);

        for (CtsBuilder q : queries) {
            query.container.add(q);
        }

        return query;
    }

    public CtsBuilder wordQuery(String[] words, String[] options, Double weight) {
        // need language

        CtsBuilder query = new CtsBuilder("word-query", true);

        for (String word : words) {
            query.container.add(new CtsBuilder("text", word));
        }

        query.addQueryOptions(options, weight);

        return query;
    }
    
    public CtsBuilder elementPairGeospatialQuery(
        QName elements,
        QName latitudeElementNames,
        QName longitudeElementNames,
        StructuredQueryBuilder.Region regions,
        String[] options,
        Double weight
    ) {
        CtsBuilder query = new CtsBuilder("element-pair-geospatial-query", true);

        // need to convert element name correctly
        query.container.add(new CtsBuilder("element", elements.getLocalPart()));
        
        query.addQueryOptions(options, weight);
        return query;
    }

    void addQueryOptions(String[] options, Double weight) {
        // Need special handling for lang= and maybe others
        if (options != null) {
            for (String option : options) {
                this.container.add(new CtsBuilder("option", option));
            }
        }

        this.weight = weight;
    }
}