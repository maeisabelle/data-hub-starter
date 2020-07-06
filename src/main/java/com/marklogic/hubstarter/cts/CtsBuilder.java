package com.marklogic.hubstarter.cts;

import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.marklogic.client.query.StructuredQueryBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CtsBuilder {
    public static void main(String[] args) throws Exception {

        CtsBuilder cb = new CtsBuilder();

        String query = cb
                .orQuery(
                        cb.andQuery(cb.wordQuery(new String[] { "test1" }, null, 23D),
                                cb.wordQuery(new String[] { "test2", "test3" }, null, null)),
                        cb.andQuery(cb.wordQuery(new String[] { "test4" }, null, null),
                                cb.wordQuery(new String[] { "test5" }, new String[] { "case-sensitive" }, null)),
                        cb.elementPairGeospatialQuery(
                            QName.valueOf("{http://www.yell.com/2009/pfi2}wgs84"), 
                            QName.valueOf("{http://www.yell.com/2009/pfi2}latitude"), 
                            QName.valueOf("{http://www.yell.com/2009/pfi2}longitude"), 
                            cb.box(-90, -180, 90, 180), 
                            null, 
                            12D
                        )
                )    
                .serialize();

        System.out.println(query);
    }

    static final String NS_PREFIX = "cts";
    static final String NS = "http://marklogic.com/cts";
    static final String ANON_NS_1 = "_1";

    Document doc;
    Element element;
    Transformer transformer;

    public CtsBuilder() throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    
        this.doc = dBuilder.newDocument();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        this.transformer = transformerFactory.newTransformer();
        this.transformer.setOutputProperty("omit-xml-declaration", "yes"); 
    }

    CtsBuilder(Document doc, Element element, Transformer transformer) {
        this.doc = doc;
        this.element = element;
        this.transformer = transformer;
    }

    public String serialize() throws TransformerException {
        DOMSource source = new DOMSource(element);
        StreamResult result = new StreamResult(new StringWriter());

        transformer.transform(source, result);

        return result.getWriter().toString();
    }

    CtsBuilder newBuilder(Element element) {
        return new CtsBuilder(this.doc, element, this.transformer);
    }

    void addChildQuery(CtsBuilder query) {
        this.element.appendChild(query.element);
    }

    void addQueryOptions(String[] options, Double weight) {
        // Need special handling for lang= and maybe others
        if (options != null) {
            for (String option : options) {
                Element e = doc.createElementNS(NS, "option");
                e.setTextContent(option);
                element.appendChild(e);
            }
        }

        if (weight != null) {
            element.setAttribute("weight", weight.toString());
        }
    }

    /**************************
     * Query definitions
     **************************/

    public CtsBuilder afterQuery(long timestamp) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "after-query"));

        Element text = doc.createElementNS(NS, "timestamp");
        text.setTextContent(Long.toUnsignedString(timestamp));
        cb.element.appendChild(text);

        return cb;
    }

    public CtsBuilder andNotQuery(CtsBuilder positiveQuery, CtsBuilder negativeQuery) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "and-not-query"));

        Element positive = doc.createElementNS(NS, "positive");
        cb.element.appendChild(positive);
        positive.appendChild(positiveQuery.element);

        Element negative = doc.createElementNS(NS, "negative");
        cb.element.appendChild(negative);
        negative.appendChild(negativeQuery.element);

        return cb;
    }

    public CtsBuilder andQuery(CtsBuilder ... queries) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "and-query"));

        for (CtsBuilder query : queries) {
            cb.addChildQuery(query);;
        }

        return cb;
    }

    public CtsBuilder beforeQuery(long timestamp) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "before-query"));

        Element text = doc.createElementNS(NS, "timestamp");
        text.setTextContent(Long.toUnsignedString(timestamp));
        cb.element.appendChild(text);

        return cb;
    }
    
    public CtsBuilder boostQuery(CtsBuilder matchingQuery, CtsBuilder boostingQuery) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "boost-query"));

        Element matching = doc.createElementNS(NS, "matching-query");
        cb.element.appendChild(matching);
        matching.appendChild(matchingQuery.element);

        Element boost = doc.createElementNS(NS, "boosting-query");
        cb.element.appendChild(boost);
        boost.appendChild(boostingQuery.element);

        return cb;
    }

    public CtsBuilder collectionQuery(String[] uris) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "collection-query"));

        for (String uri : uris) {
            Element uriParam = doc.createElementNS(NS, "uri");
            uriParam.setTextContent(uri);
            cb.element.appendChild(uriParam);
        }

        return cb;
    }

    public CtsBuilder directoryQuery(String[] uris, String depth) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "directory-query"));

        for (String uri : uris) {
            Element uriParam = doc.createElementNS(NS, "uri");
            uriParam.setTextContent(uri);
            cb.element.appendChild(uriParam);
        }

        if ("infinity".equals(depth)) {
            cb.element.setAttribute("depth", "infinity");
        }

        return cb;
    }

    public CtsBuilder documentFragmentQuery(CtsBuilder query) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "document-fragment-query"));
        cb.addChildQuery(query);

        return cb;
    }

    public CtsBuilder documentQuery(String[] uris) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "document-query"));

        for (String uri : uris) {
            Element uriParam = doc.createElementNS(NS, "uri");
            uriParam.setTextContent(uri);
            cb.element.appendChild(uriParam);
        }

        return cb;
    }

    public CtsBuilder elementAttributePairGeospatialQuery(
        QName elementNames,
        QName latitudeAttributeNames,
        QName longitudeAttributeNames,
        Region regions,
        String[] options,
        Double weight
    ) {
        CtsBuilder query = newBuilder(doc.createElementNS(NS, "element-attribute-pair-geospatial-query"));

        // these should actually allow for multiple QNames each - keep as just one for now
        query.element.appendChild(namedElementParam(elementNames, "element"));
        query.element.appendChild(namedElementParam(latitudeAttributeNames, "latitude"));
        query.element.appendChild(namedElementParam(longitudeAttributeNames, "longitude"));

        query.element.appendChild(regionParam(regions, "region"));

        query.addQueryOptions(options, weight);
        return query;
    }




    public CtsBuilder orQuery(CtsBuilder ... queries) {
        CtsBuilder cb = newBuilder(doc.createElementNS(NS, "or-query"));

        for (CtsBuilder query : queries) {
            cb.addChildQuery(query);;
        }

        return cb;
    }

    public CtsBuilder wordQuery(String[] words, String[] options, Double weight) {
        CtsBuilder query = newBuilder(doc.createElementNS(NS, "word-query"));

        for (String word : words) {
            Element text = doc.createElementNS(NS, "text");
            text.setTextContent(word);
            query.element.appendChild(text);
        }

        query.addQueryOptions(options, weight);

        return query;
    }


    public CtsBuilder elementChildGeospatialQuery(
        QName parentElementNames,
        QName childElementNames,
        Region regions,
        String[] options,
        Double weight
    ) {
        CtsBuilder query = newBuilder(doc.createElementNS(NS, "element-child-geospatial-query"));

        // these should actually allow for multiple QNames each - keep as just one for now
        query.element.appendChild(namedElementParam(parentElementNames, "element"));
        query.element.appendChild(namedElementParam(childElementNames, "child"));

        query.element.appendChild(regionParam(regions, "region"));

        query.addQueryOptions(options, weight);
        return query;
    }

    public CtsBuilder elementGeospatialQuery(
        QName elementNames,
        Region regions,
        String[] options,
        Double weight
    ) {
        CtsBuilder query = newBuilder(doc.createElementNS(NS, "element-child-geospatial-query"));

        // these should actually allow for multiple QNames each - keep as just one for now
        query.element.appendChild(namedElementParam(elementNames, "element"));

        query.element.appendChild(regionParam(regions, "region"));

        query.addQueryOptions(options, weight);
        return query;
    }

    public CtsBuilder elementPairGeospatialQuery(
        QName elementNames,
        QName latitudeElementNames,
        QName longitudeElementNames,
        Region regions,
        String[] options,
        Double weight
    ) {
        CtsBuilder query = newBuilder(doc.createElementNS(NS, "element-pair-geospatial-query"));

        // these should actually allow for multiple QNames each - keep as just one for now
        query.element.appendChild(namedElementParam(elementNames, "element"));
        query.element.appendChild(namedElementParam(latitudeElementNames, "latitude"));
        query.element.appendChild(namedElementParam(longitudeElementNames, "longitude"));

        query.element.appendChild(regionParam(regions, "region"));

        query.addQueryOptions(options, weight);
        return query;
    }

    // https://docs.marklogic.com/cts:geospatial-region-query
    // https://docs.marklogic.com/cts:json-property-child-geospatial-query
    // https://docs.marklogic.com/cts:json-property-geospatial-query
    // https://docs.marklogic.com/cts:json-property-pair-geospatial-query
    // https://docs.marklogic.com/cts:path-geospatial-query

    Element namedElementParam(QName elementName, String paramName) {
        Element elementParam = doc.createElementNS(NS, paramName);
        String ns = elementName.getNamespaceURI();
        if (ns != null) {
            String prefix = elementName.getPrefix();
            if (prefix == null) {
                prefix = ANON_NS_1;
            }
            elementParam.setAttribute("xmlns:" + prefix, ns);
            elementParam.setTextContent(prefix + ":" + elementName.getLocalPart());
        } else {
            elementParam.setTextContent(elementName.getLocalPart());
        }

        return elementParam;
    }

    Element regionParam(Region region, String paramName) {
        Element regionParam = doc.createElementNS(NS, paramName);
        regionParam.setAttribute("xsi:type", region.getType());
        regionParam.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        regionParam.setTextContent(region.toString());

        return regionParam;
    }

    protected class Region {
        public String getType() {
            return "cst:" + getClass().getSimpleName().toLowerCase();
        }
    }

    protected class Point extends Region {
        private double lat = 0.0;
        private double lon = 0.0;

        public Point(double lat, double lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public String toString() {
            return lat + "," + lon;
        }
    }

    public Point point(double lat, double lon) {
        return new Point(lat, lon);
    }

    protected class Circle extends Region {
        private double radius = 0.0;
        private Point center = null;

        public Circle(double radius, Point center) {
            this.radius = radius;
            this.center = center;
        }

        public String toString() {
            return "@" + radius + " " + center.toString();
        }
    }

    public Circle circle(double radius, Point center) {
        return new Circle (radius, center)
    }

    protected class Box extends Region {
        private double south, west, north, east;

        public Box(double south, double west, double north, double east) {
            this.south = south;
            this.west = west;
            this.north = north;
            this.east = east;
        }

        public String toString() {
            return "[" + south + "," + west + "," + north + "," +  east + "]";
        }
    }

    public Box box(double south, double west, double north, double east) {
        return new Box(south, west, north, east);
    }

    protected class Polygon extends Region {
        private Stream<Point> vertices;

        public Polygon(Point[] vertices) {
            this.vertices = Stream.of(vertices);
        }

        public String toString() {
            // this may not be any faster than just looping with a StringBuilder but trying out Streams
            return String.join(" ", vertices.map(v -> v.toString()).collect(Collectors.toList()));
        }
    }

    public Polygon polygon(Point[] vertices) {
        return new Polygon(vertices);
    }


}