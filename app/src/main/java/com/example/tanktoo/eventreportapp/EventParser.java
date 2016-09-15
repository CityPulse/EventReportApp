package com.example.tanktoo.eventreportapp;

/**
 * Created by tanktoo on 11/08/16.
 */
public class EventParser {
    Parser parser = new Parser();

    public Event parseEvent(String strEvent){
        Event event = new Event();

        Graph eventGraph = this.parser.parse(strEvent);
        event.setIdentifier(eventGraph.identifier);
        event.setEventClass(eventGraph.type.split(":")[1]);
        event.setType(eventGraph.attributeList.get("sao:hasType").value.split(":")[1]);
        event.setSource(eventGraph.attributeList.get("ec:hasSource").value);
        event.setLevel(Integer.parseInt(eventGraph.attributeList.get("sao:hasLevel").value));
        event.setDate(eventGraph.attributeList.get("tl:time").value);
        event.setLatitude(Double.parseDouble(eventGraph.childList.get("sao:hasLocation").attributeList.get("geo:lat").value));
        event.setLongitude(Double.parseDouble(eventGraph.childList.get("sao:hasLocation").attributeList.get("geo:lon").value));
        return event;
    }

    public String parseEvent(Event event){
        Graph eventGraph = new Graph();
        eventGraph.addPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        eventGraph.addPrefix("sao", "http://purl.oclc.org/NET/UNIS/sao/sao#");
        eventGraph.addPrefix("tl", "http://purl.org/NET/c4dm/timeline.owl#");
        eventGraph.addPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        eventGraph.addPrefix("prov", "http://www.w3.org/ns/prov#");
        eventGraph.addPrefix("ec", "http://purl.oclc.org/NET/UNIS/sao/ec#");
        eventGraph.setIdentifierType(event.getIdentifier(), "ec:" + event.getEventClass());
        eventGraph.addAttribute("ec:hasSource", event.getSource());
        eventGraph.addAttribute("tl:time", "dateTime", event.getStringDate());
        eventGraph.addAttribute("sao:hasLevel", "long", String.valueOf(event.getLevel()));
        eventGraph.addAttribute("sao:hasType", "class", "ec:" + event.getType());
        Graph child = new Graph();
        child.setIdentifierType("", "geo:Instant");
        child.addAttribute("geo:lat", "double", String.valueOf(event.getLatitude()));
        child.addAttribute("geo:lon", "double", String.valueOf(event.getLongitude()));
        eventGraph.addChild("sao:hasLocation", child);
        return eventGraph.toN3();
    }
}

