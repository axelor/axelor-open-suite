/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.tool;

import java.text.Normalizer;

public final class StringTool {
	
	private StringTool(){
		
	}
	
	/**
	 * First character lower
	 * 
	 * @param string
	 * @return
	 */
	public static String toFirstLower(String string){
		
		return string.replaceFirst("\\w", Character.toString(string.toLowerCase().charAt(0)));
		
	}
	
	/**
	 * First character upper.
	 * 
	 * @param string
	 * @return
	 */
	public static String toFirstUpper(String string){
		
		return string.replaceFirst("\\w", Character.toString(string.toUpperCase().charAt(0)));
		
	}
	
	/**
	 * Complete string with fixed length.
	 * 
	 * @param string
	 * @return
	 */
	public static String fillStringRight(String s, char fillChar, int size){
		
		String string = s;
		
		if (string.length() < size) { string += fillString(fillChar, size - string.length()); }
		else { string = truncRight(string, size); }
			
		return string;
		
	}
	
	/**
	 * Complete string with fixed length.
	 * 
	 * @param string
	 * @return
	 */
	public static String fillStringLeft(String s, char fillChar, int size){

		String string = s;
		
		if (string.length() < size) { string = fillString(fillChar, size - string.length()) + string; }
		else { string = truncLeft(string, size); }
			
		return string;
		
	}
	
	/**
	 * Truncate string with the first chars at size.
	 * 
	 * @param s
	 * @param size
	 * @return
	 */
	public static String truncRight(String s, int size){

		String string = s;
		
		if (string.length() > size) { string = string.substring(0, size); }
		
		return string;
	}
	
	
	/**
	 * Truncate string with the s length subtract size at s length.
	 * 
	 * @param s
	 * @param size
	 * @return
	 */
	public static String truncLeft(String s, int size){

		String string = s;
		
		if (string.length() > size) { string = string.substring(string.length()-size, string.length()); }
		
		return string;
	}
	
	
	/**
	 * Create string with one char * count.
	 * 
	 * @param fillChar
	 * @param count
	 * @return
	 */
	private static String fillString(char fillChar, int count){  
       
	   // creates a string of 'x' repeating characters  
       char[] chars = new char[count];  
       java.util.Arrays.fill(chars, fillChar);  
       return new String(chars);
       
	}  
	
	
	public static String deleteAccent(String s)  {
		
		String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
		return temp.replaceAll("[^\\p{ASCII}]", "");
		
	}
	
	
	/**
	 * Check if string s contain only digital character
	 * @param s
	 * @return
	 */
	public static boolean isDigital(String s)  {
		
		for(Character c : s.toCharArray())  {
			
			if(!Character.isDigit(c)) { return false; }
		}
		
		return true;
	}
	
	
	/**
	 * Extract a string from right
	 * 
	 * @param string
	 * @param startIndex
	 * @param lenght
	 * @return
	 */
	public static String extractStringFromRight(String string,int startIndex,int lenght){
		String extractString = "";
		
		try{
			
			if(string != null && startIndex >= 0 && lenght >= 0 && string.length()-startIndex+lenght <= string.length()){
				extractString = string.substring(string.length()-startIndex, string.length()-startIndex+lenght);
			}
		}
		catch(Exception ex){return "";}
		
		return extractString;
	}
	
	/**
	 * Fonction permettant de convertir un tableau de bytes en une chaine hexadécimale
	 * Convert to hexadecimal string from bytes table
	 * @param bytes
	 * @return
	 */
	public static String getHexString(byte[] bytes) {
		
		StringBuilder sb = new StringBuilder(bytes.length*2);
		String s = "";
		
		for (byte b : bytes) {
			
			s = String.format("%x", b);
			if(s.length() == 1) { sb.append('0'); }
			sb.append( s );
		}
		
		return sb.toString();
	}
	
	
	/**
	 * Fonction permettant de mettre la première lettre d'une chaine de caractère en majuscule
	 * @param value
	 * 			Une chaine de caractère
	 * @return
	 */
	public static String capitalizeFirstLetter(String s) {
		if (s == null) {
			return null;
		}
		if (s.length() == 0) {
			return s;
		}
		StringBuilder result = new StringBuilder(s);
		result.replace(0, 1, result.substring(0, 1).toUpperCase());
		return result.toString();
	}
	
}
