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
