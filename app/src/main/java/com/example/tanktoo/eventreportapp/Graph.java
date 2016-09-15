package com.example.tanktoo.eventreportapp;

import java.util.HashMap;
import java.util.Map;


public class Graph {
	String identifier;
	String type;
	Map<String, Attribute> attributeList = new HashMap<String, Attribute>();
	Map<String, Graph> childList = new HashMap<String, Graph>();
	Map<String, String> prefixList = new HashMap<String, String>();
	
	public void setIdentifierType(String identifier, String type){
		this.identifier = identifier;
		this.type = type;
	}
	public void addAttribute(String name, String type, String value){
		this.attributeList.put(name, new Attribute(type, value));
	}
	public void addAttribute(String name, String value){
		this.attributeList.put(name, new Attribute(value));
	}
	public void addPrefix(String abbr, String url){
		this.prefixList.put(abbr, url);
	}
	public void addChild(String childIdentifier, Graph child){
		this.childList.put(childIdentifier, child);
	}
	public boolean hasChilds(){
		if(!this.childList.isEmpty())
			return true;
		return false;
	}
	public String toString(){
		return identifier + " " + type;
	}
	
	public String toN3(){
		String n3 = "";
		for (Map.Entry<String, String>entry : this.prefixList.entrySet()) {
		    n3 += "@prefix\t" + entry.getKey() + ":\t<" + entry.getValue() + "> .\n";
		}
		n3 += this.identifier + "\ta\t" + this.type + " ;\n";
		n3 += this.toN3attributes();
		n3 += this.toN3childs();
		n3 = n3.replaceFirst("(?s)(.*)" + ";", "$1" + ".");	//replace last
		return n3;
	}

	private String toN3attributes(){
		String n3 = "";
		for (Map.Entry<String, Attribute>entry : this.attributeList.entrySet()) {
			if(entry.getValue().type.equals("class"))
				n3 += "\t" + entry.getKey() + "\t" + entry.getValue().value + " ;\n";
			else
				n3 += "\t" + entry.getKey() + "\t\"" + entry.getValue().value + (entry.getValue().type.equals("String") ? "\"" : "\"^^xsd:" + entry.getValue().type) + " ;\n";
		}
		return n3;
	}
	
	private String toN3childs(){
		String n3 = "";
		for (Map.Entry<String, Graph>entry : this.childList.entrySet()) {
		    n3 += "\t" + entry.getKey() + "\t[\t a\t" + entry.getValue().type + ";\n";
		    n3 += entry.getValue().toN3attributes();
		    if(entry.getValue().hasChilds())
		    	n3 += entry.getValue().toN3childs();
		    n3 = n3.replaceFirst("(?s)(.*)" + ";", "$1" + "");	//replace last
		    n3 += "\t] ;";
		}
		return n3;
	}
	
	class Attribute{
		String type;
		String value;
		
		public Attribute(String type, String value){
			this.type = type;
			this.value = value;
		}
		public Attribute(String value){
			this.type = "String";
			this.value = value;
		}
	}
}
