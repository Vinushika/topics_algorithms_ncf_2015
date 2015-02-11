package a_huffman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/*
 * Corresponding Fields in mappings:
 * Pos	|Field Name							|Line Offset	|# Options	|Number of Bits
 * -----+-----------------------------------+---------------+-----------+--------------
 * 0	|exchange							|9				|17			|5
 * 1	|Quote Condition					|62				|26			|5
 * 2	|Bid Exchange						|67				|17			|5
 * 3	|Ask Exchange						|68				|17			|5
 * 4	|Quote Cancel/Correction			|87				|3			|2
 * 5	|Source of Quote					|88				|2			|1
 * 6	|Retail Interest Indicator			|89				|4			|2
 * 7	|Short Sale Retail Indicator		|90				|5			|3
 * 8	|LULD BBO Indicator(CQS)			|91				|3			|2
 * 9	|LULD BBO Indicator(UTP)			|92				|4			|2
 * 10	|FINRA ADF MPID Indicator			|93				|4			|2
 * 11	|SIP-generated Message Identifier	|94				|2			|1
 * 12	|National BBO LULD Indicator		|95				|10			|4
 */


//Dictionaries for Huffman Code...not actually used due to complexity of single-bit I/O

public class MappingChars {

	private static HashMap<Byte, Integer> createMap(ArrayList<Byte> arr) {
		HashMap<Byte, Integer> map = new HashMap<Byte, Integer>();
		for (int i = 0; i < arr.size(); i++) {
			map.put(arr.get(i), i);
		}
		return map;
	}
	
	
	protected static ArrayList<HashMap<Byte, Integer>> mapFields()
	{
		ArrayList<HashMap<Byte, Integer>> mappings = new ArrayList<HashMap<Byte, Integer>>();
		ArrayList<Byte> exchanges = new ArrayList<Byte>(Arrays.asList(
						new Byte("A"), new Byte("B"), new Byte("C"),
						new Byte("D"), new Byte("I"), new Byte("J"),
						new Byte("K"), new Byte("M"), new Byte("N"),
						new Byte("T"), new Byte("P"), new Byte("S"),
						new Byte("Q"), new Byte("W"), new Byte("X"),
						new Byte("Y"), new Byte("Z")
						));
		mappings.add(createMap(exchanges));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
						new Byte("A"), new Byte("B"), new Byte("C"),
						new Byte("D"), new Byte("E"), new Byte("F"),
						new Byte("G"), new Byte("H"), new Byte("I"), 
						new Byte("J"), new Byte("K"), new Byte("L"), 
						new Byte("M"), new Byte("N"), new Byte("O"),
						new Byte("P"), new Byte("Q"), new Byte("R"),
						new Byte("S"), new Byte("T"), new Byte("U"),
						new Byte("V"), new Byte("W"), new Byte("X"),
						new Byte("Y"), new Byte("Z")	))));
		mappings.add(createMap(exchanges));
		mappings.add(createMap(exchanges));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("A"), new Byte("B"), new Byte("C")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("C"), new Byte("N")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("A"), new Byte("B"), new Byte("C"), new Byte(" ")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("A"), new Byte("D"), new Byte("C"), 
				new Byte(" "), new Byte("E")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("A"), new Byte("B"), new Byte(" ")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("A"), new Byte("B"), new Byte("C"), new Byte(" ")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("0"), new Byte("1"), new Byte("2"), new Byte("3")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("E"), new Byte(" ")	))));
		mappings.add(createMap(new ArrayList<Byte>(Arrays.asList(
				new Byte("A"), new Byte("B"), new Byte("C"),
				new Byte("D"), new Byte("E"), new Byte("F"),
				new Byte("G"), new Byte("H"), new Byte("I"),
				new Byte(" ")	))));
		
		
		return mappings;
	}
	
	private static HashMap<Integer, Byte> reverseMap(HashMap<Byte, Integer> map)
	{
		HashMap<Integer, Byte> reverseMap = new HashMap<Integer, Byte>();
		for (Byte key : map.keySet()) {
			reverseMap.put(map.get(key), key);
		}
		return reverseMap;
	}
	
	protected static ArrayList<HashMap<Integer, Byte>> reverseMapFields(ArrayList<HashMap<Byte, Integer>> maps)
	{
		ArrayList<HashMap<Integer, Byte>> newMappings = new ArrayList<HashMap<Integer, Byte>>();
		for (int i = 0; i < maps.size(); i++)
		{
			newMappings.add(reverseMap(maps.get(i)));
		}
		return newMappings;
	}
}
