/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
package com.axelor.apps.tool.file;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public final class CsvTool {

	private CsvTool(){
		
	}
	
	/**
	 * Méthode permettant de lire le contenu d'un fichier
	 * @param fileName
	 * 			Le nom du fichier
	 * @return
	 * 			Une liste de tableau de valeur contenant l'ensemble des lignes
	 * @throws IOException 
	 * @throws AxelorException 
	 */
	public static List<String[]> cSVFileReader(String fileName, char separator) throws IOException {
		
		CSVReader reader;
		List<String[]> myEntries;
		
		reader = new CSVReader(new FileReader(fileName), separator);
		myEntries = reader.readAll();
		reader.close();
		
		return myEntries;
		
	}
	
	/*
	 * Format Windows, sans double quote et CR/LF à chaque fin de ligne
	 */
	public static CSVWriter setCsvFile(final String filePath, final String fileName, char separator) throws IOException {
		
		java.io.Writer w = new FileWriter(filePath + File.separator + fileName);
		return new CSVWriter(w, separator, CSVWriter.NO_QUOTE_CHARACTER, "\r\n");
		
	}
	
	public static CSVWriter setCsvFile(final String filePath, final String fileName, char separator, char quoteChar) throws IOException {
		
		java.io.Writer w = new FileWriter(filePath + File.separator + fileName);
		return new CSVWriter(w, separator, quoteChar, "\r\n");
		
	}
	
	
	public static void  csvWriter(String filePath, String fileName, char separator, String[] headers, List<String[]> dataList) throws IOException 
	{
		CSVWriter reconWriter = setCsvFile(filePath, fileName, separator);	
		if(headers != null)  {
			reconWriter.writeNext(headers);
		}
		reconWriter.writeAll(dataList);
		reconWriter.flush();                
		try
		{
			reconWriter.close();
		}
		catch (IOException e) {

			reconWriter = null;
		}
	}
	
	public static void  csvWriter(String filePath, String fileName, char separator, char quoteChar, String[] headers, List<String[]> dataList) throws IOException 
	{
		CSVWriter reconWriter = setCsvFile(filePath, fileName, separator, quoteChar);	
		if(headers != null)  {
			reconWriter.writeNext(headers);
		}
		reconWriter.writeAll(dataList);
		reconWriter.flush();                
		try
		{
			reconWriter.close();
		}
		catch (IOException e) {

			reconWriter = null;
		}
	}
	
}
