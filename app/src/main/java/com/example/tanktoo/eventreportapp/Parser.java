package com.example.tanktoo.eventreportapp;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {


	public Graph parse(String string){
		Graph g = new Graph();
		string = prefixes(string, g);
		string = masterTriple(string, g);
		string = innerTriples(string, g);
		triple(string, g);
		return g;
	}

	public String masterTriple(String string, Graph g){
		Pattern pattern = Pattern.compile("(.*?[^;.\\[]\\s+?a\\s+?.*?\\s*?[;\\.])", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(string);
		String masterTriple = "";
		while(matcher.find()){
			masterTriple = matcher.group(0);
//			System.out.println("##### master " + masterTriple);
			String[] tmp = masterTriple.trim().split("\\s+");
			g.setIdentifierType(tmp[0], tmp[2]);
			break;	//needed?
		}
		return string.replace(masterTriple, "");
	}

	public String innerTriples(String string, Graph g){
		Pattern pattern = Pattern.compile("(?:\\w+:\\w+)\\s*\\[.*\\]\\s*[;\\.]", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(string);
		String withoutinnerTriples = string;
		while(matcher.find()){
			String triple = matcher.group(0);
			withoutinnerTriples = withoutinnerTriples.replace(triple, "");

			Pattern identifierPattern = Pattern.compile(".+(?=\\[)", Pattern.DOTALL);
			Matcher identifierMatcher = identifierPattern.matcher(triple);
			String identifier = "";
			while(identifierMatcher.find())
				identifier = identifierMatcher.group().trim();

			Pattern typePattern = Pattern.compile("(?<=\\[).+?(?=\\s*?;)", Pattern.DOTALL);
			Matcher typeMatcher = typePattern.matcher(triple);
			String type = "";
			while(typeMatcher.find())
				type = typeMatcher.group().trim().split("\\s+")[1];
			triple = triple.replaceAll(identifier + "\\s*\\[\\s*a\\s*" + type + "\\s*;","");
			triple = triple.replaceFirst("(?s)(.*)" + "]", "$1" + "");
			Graph gg = new Graph();
			gg.setIdentifierType(identifier, type);
			g.addChild(identifier, gg);
			this.triple(triple, gg);
		}

		return withoutinnerTriples;
	}

	public void triple(String string, Graph g){
//		System.out.println("Triple: " + string);
		Pattern pattern = Pattern.compile(".*?((;|\\.)(?=(?:[^\\\"]|\\\"[^\\\"]*\\\")*$))", Pattern.DOTALL);	//find .; not in quotes
		Matcher matcher = pattern.matcher(string);
		while(matcher.find()){
			String triple = matcher.group();
			String[] tmp = triple.trim().split("\\s+");
			String name = tmp[0];
			String value = tmp[1];
			if(value.contains(";"))
				value = value.replace(";", "");
			if(value.contains("\"")){
				value = value.replaceAll("\"", "");
				if(value.contains("^^xsd:")){
					String[] values = value.split("\\^\\^xsd:");
					value = values[0];
					String type = values[1];
					g.addAttribute(name, type, value);
				}else
					g.addAttribute(name, value);
			}else{
				g.addAttribute(name, "class", value);
			}
		}
	}

	public String prefixes(String string, Graph g){
		Pattern pattern = Pattern.compile("@prefix.+?[\\s*|>]\\.", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(string);
		String withoutPrefix = string;
		while(matcher.find()){
			String prefix = matcher.group(0);
			withoutPrefix = withoutPrefix.replace(prefix, "");

			String abbr = "";
			Pattern abbrPattern = Pattern.compile("(?<=@prefix).*?(?=:)", Pattern.DOTALL);
			Matcher abbrMatcher = abbrPattern.matcher(prefix);
			while(abbrMatcher.find())
				abbr = abbrMatcher.group();

			String url = "";
			Pattern urlPattern = Pattern.compile("(?<=\\<).*?(?=>)", Pattern.DOTALL);
			Matcher urlMatcher = urlPattern.matcher(prefix);
			while(urlMatcher.find())
				url = urlMatcher.group();
			g.addPrefix(abbr, url);
		}
		return withoutPrefix;
	}




	public static void main(String[] arg){
		String MESSAGE = "@prefix geo:   <http://www.w3.org/2003/01/geo/wgs84_pos#> ."
				+ "@prefix sao:   <http://purl.oclc.org/NET/UNIS/sao/sao#> ."
				+ "@prefix tl:    <http://purl.org/NET/c4dm/timeline.owl#> ."
				+ "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> ."
				+ "@prefix prov:  <http://www.w3.org/ns/prov#> ."
				+ "@prefix ec:    <http://purl.oclc.org/NET/UNIS/sao/ec#> ."
				+ "sao:0190cecb-522e-4552-a6ca-01299545dc5c"
				+ "		a                ec:PublicParking ;"
				+ "     ec:hasSource     \"SENSOR_0816d088-3af8-540e-b89b-d99ac63fa886\" ;"
				+ "		sao:hasLevel     \"2\"^^xsd:long ;"
				+ "		sao:hasLocation  [ a        geo:Instant ;"
				+ "		geo:lat  \"56.15\"^^xsd:double ;"
				+ "		geo:lon  \"10.216667\"^^xsd:double"
				+ "		] ;"
				+ "		sao:hasType      ec:TransportationEvent ;"
				+ "		tl:time          \"2016-08-05T10:02:55.958Z\"^^xsd:dateTime .";
		Parser parser = new Parser();
		Graph g = parser.parse(MESSAGE);
		System.out.println(g.attributeList.get("sao:hasType").value);
		System.out.println(g.childList.get("sao:hasLocation").attributeList.get("geo:lat").value);
		System.out.println(g.toN3());
	}

}


