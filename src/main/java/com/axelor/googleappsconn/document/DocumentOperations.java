package com.axelor.googleappsconn.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Scanner;
import java.util.StringTokenizer;

import com.axelor.googleappsconn.drive.FileTypes;
import com.axelor.googleappsconn.drive.GoogleDrive;
import com.axelor.googleappsconn.drive.GoogleFile;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

//import org.apache.poi.hwpf.HWPFDocument;
//import org.apache.poi.hwpf.extractor.WordExtractor;
//import org.apache.poi.hwpf.usermodel.CharacterRun;
//import org.apache.poi.hwpf.usermodel.Range;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
//import org.apache.poi.xwpf.usermodel.XWPFTable;
//import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import org.odftoolkit.odfdom.doc.OdfTextDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTText;

/**
 * class to perform operations relevant to document like create, read, save,
 * remove
 */
public class DocumentOperations {

	GoogleDrive driveService;
	/**
	 * counstructor that will instantiate google drive service object.
	 * @param currentUserDriveService GoogleDrive
	 */
	public DocumentOperations(GoogleDrive passedService) {
		driveService = passedService;
	}
	/**
	 * creates the document with the template file and the documentContent
	 * passed and outputs the data to the passed outputFileName
	 * @param targetFileName String
	 * @param dataMap Map<String,Object> map of key-value for text remplacement
	 * @param templateFile java.io.File
	 * @param directoryId String parent directory id
	 **/
	public GoogleFile createDocumentWithTemplate(String targetFileName, String directoryId,
			Map<String, Object> dataMap, File templateFile) throws Exception {

		File localFile = new File(GoogleDrive.USER_HOME_DOCUMENTS,targetFileName + ".docx");
		FileOutputStream fileOutputStream = new FileOutputStream(localFile);
		XWPFDocument document = new XWPFDocument(new FileInputStream(templateFile));
		// set values in paragraphs
		for (XWPFParagraph p : document.getParagraphs()) {
			for (XWPFRun r : p.getRuns()) {
				for (CTText ct : r.getCTR().getTList()) {
					String str = ct.getStringValue();
					if (str == null)
						continue;
					Iterator<String> iterator = dataMap.keySet().iterator();
					String key = "";
					String value = "";
					Object dataObj = null;
					while (iterator.hasNext()) {
						key = iterator.next();
						if (key == null || str == null || !str.contains(key))
							continue;
						else {
							dataObj = dataMap.get(key);
							if (dataObj == null) {								
								continue;
							}
							if (dataObj.getClass().equals(ArrayList.class)) {
								ArrayList<?> listObjects = (ArrayList<?>) dataObj;
								value = "";
								for (Object obj : listObjects) {
									if (!value.equals(""))
										value += ", " + obj.toString();
									else
										value += obj.toString();
								}
							} else
								value = dataObj.toString();
							if (value == null)
								value = "";
							key = "${" + key + "}";							
							str = str.replace(key, value);
							ct.setStringValue(str);
						}
					}					
				}
			}
		}
		// set values in Tables
		String cellValue = "";
		for (XWPFTable t : document.getTables()) {
			for (int i = 0; i < t.getNumberOfRows(); i++) {
				XWPFTableRow row = t.getRow(i);
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph paragraph : cell.getParagraphs()) {
						cellValue = paragraph.getText();
						if (cellValue == null)
							continue;
						Iterator<String> iterator = dataMap.keySet().iterator();
						String key, value = "";
						while (iterator.hasNext()) {
							key = iterator.next();
							if (key == null || !cellValue.contains(key))
								continue;
							if (dataMap.get(key) == null)
								continue;
							if (!dataMap.get(key).getClass()
									.equals(ArrayList.class))
								value = dataMap.get(key).toString();
							if (value == null)
								value = "";
							key = "${" + key + "}";
							cellValue = cellValue.replace(key, value);
							cell.removeParagraph(0);
							cell.addParagraph().createRun().setText(cellValue);
						}
					}
				}
			}
		}
		
///-------------
		
		// set null in paragraphs for remaining keys
		for (XWPFParagraph p : document.getParagraphs()) {
			for (XWPFRun r : p.getRuns()) {
				for (CTText ct : r.getCTR().getTList()) {
					String str = ct.getStringValue();
					if (str == null)
						continue;
					int startIndex,endIndex;
					startIndex = str.indexOf("${");
					endIndex = str.indexOf("}");
					if(startIndex == -1 || endIndex == -1 ) continue;
					String replace = str.substring(startIndex, endIndex + 1);
					if(replace == null) continue;
					str = str.replace(replace, "");
					ct.setStringValue(str);										
				}
			}
		}
		// set values in Tables	for remaining keys	
		for (XWPFTable t : document.getTables()) {
			for (int i = 0; i < t.getNumberOfRows(); i++) {
				XWPFTableRow row = t.getRow(i);
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph paragraph : cell.getParagraphs()) {
						cellValue = paragraph.getText();
						if (cellValue == null)
							continue;
						int startIndex,endIndex;
						startIndex = cellValue.indexOf("${");
						endIndex = cellValue.indexOf("}");
						if(startIndex == -1 || endIndex == -1 ) continue;
						String replace = cellValue.substring(startIndex, endIndex + 1);
						if(replace == null) continue;
						cellValue = cellValue.replace(replace, "");
						cell.removeParagraph(0);
						cell.addParagraph().createRun().setText(cellValue);
					}
				}
			}
		}
		
		document.write(fileOutputStream);
		fileOutputStream.close();
		GoogleFile uploadedFile = driveService.createFile(targetFileName,
				FileTypes.MS_DOCUMENT, localFile, directoryId, true);
		uploadedFile.setFileType(FileTypes.GOOGLE_DOC_FILE);
		uploadedFile.setFileSize(localFile.length());
		return uploadedFile;
	}
	/**
	 * creates a document with passed mime-type, name and data. the upload of
	 * file is also included here.
	 * @param fileName String
	 * @param data String
	 * @param mimeType String
	 * @param directoryId String parent directory Id
	 * @return uploadedFile GoogleFile
	 * @throws Exception
	 */
	public GoogleFile createDocument(String fileName, String data,
			String mimeType, String directoryId) throws Exception {
		
		java.io.File localFile = null;
		long fileSize = 0L;
		boolean converToGoogleFormat = false;		
		if (mimeType.equals(FileTypes.MS_DOCUMENT)) {			
			if (!fileName.endsWith(".docx"))
				fileName += ".docx";
			localFile = createDocumentX(fileName, data);
		} else if (mimeType.equals(FileTypes.OPEN_DOCUMENT)) {			
			if (!fileName.endsWith(".odt"))
				fileName += ".odt";
			localFile = createDocumentOpen(fileName, data);
		} else if (mimeType.equals(FileTypes.PLAIN_TEXT)) {			
			if (!fileName.endsWith(".txt"))
				fileName += ".txt";
			localFile = createDocumentText(fileName, data);
		} else if (mimeType.equals(FileTypes.PDF_DOCUMENT)) {			
			if (!fileName.endsWith(".pdf"))
				fileName += ".pdf";
			localFile = createDocumentPDF(fileName, data);
		} else if (mimeType.equals(FileTypes.CSV_FILE)) {			
			if (!fileName.endsWith(".csv"))
				fileName += ".csv";
			localFile = createDocumentCSV(fileName, data);
		} else if (mimeType.equals(FileTypes.GOOGLE_DOC_FILE)) {			
			localFile = createDocumentX(fileName, data);
			mimeType = FileTypes.MS_DOCUMENT;
			converToGoogleFormat = true;
		}
		fileSize = localFile.length();
		// createFile will create google file with passed mime-type and upload it
		GoogleFile uploadedFile = driveService.createFile(localFile.getName(),
						mimeType, localFile, directoryId, converToGoogleFormat);
		if (converToGoogleFormat) {
			uploadedFile.setFileType(FileTypes.GOOGLE_DOC_FILE);
			uploadedFile.setFileSize(fileSize);
		}
		return uploadedFile;
	}
	/**
	 * this method is called by createDocument() method to create only Microsoft
	 * Document(.docx), so used will not call this method directly.
	 * @param fileName String
	 * @param data String
	 * @return createdFile java.io.File
	 * @throws Exception
	 */
	private java.io.File createDocumentX(String fileName, String data) throws Exception {
		
		XWPFDocument document = new XWPFDocument();
		XWPFRun run;
		XWPFParagraph paragraph;
		List<String> dataList = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(data, "\n");
		while (stringTokenizer.hasMoreElements()) {
			dataList.add(stringTokenizer.nextToken());
		}
		for (String partData : dataList) {
			paragraph = document.createParagraph();
			paragraph.setAlignment(ParagraphAlignment.LEFT);
			run = paragraph.createRun();
			run.setText(partData);
		}
		java.io.File file = new java.io.File(GoogleDrive.USER_HOME_DOCUMENTS, fileName);
		if (file.exists())
			file.delete();
		file.createNewFile();
		document.write(new FileOutputStream(file));
		return file;
	}
	/**
	 * this method is called by createDocument() method to create only Microsoft
	 * Document(.doc), so used will not call this method directly.
	 * @param fileName String
	 * @param data String
	 * @return createdFile java.io.File
	 * @throws Exception
	 */
	private java.io.File createDocumentDoc(String fileName, String data) throws Exception {
		
		XWPFDocument document = new XWPFDocument();
		XWPFParagraph tmpParagraph = document.createParagraph();
		XWPFRun tmpRun = tmpParagraph.createRun();
		List<String> dataList = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(data, "\n");
		while (stringTokenizer.hasMoreElements()) {
			dataList.add(stringTokenizer.nextToken());
		}
		for (String partData : dataList) {
			tmpParagraph = document.createParagraph();
			tmpParagraph.setAlignment(ParagraphAlignment.LEFT);
			tmpRun = tmpParagraph.createRun();
			tmpRun.setText(partData);
		}
		java.io.File file = new java.io.File(GoogleDrive.USER_HOME_DOCUMENTS, fileName);
		if (file.exists())
			file.delete();
		file.createNewFile();
		document.write(new FileOutputStream(file));
		return file;
	}
	/**
	 * this method is called by createDocument() method to create only Open
	 * Document (.odt), so used will not call this method directly.
	 * @param fileName String
	 * @param data String
	 * @return createdFile java.io.File
	 * @throws Exception
	 */
	private java.io.File createDocumentOpen(String fileName, String data) throws Exception {
		
		OdfTextDocument odt = OdfTextDocument.newTextDocument();
		List<String> dataList = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(data, "\n");
		while (stringTokenizer.hasMoreElements()) {
			dataList.add(stringTokenizer.nextToken());
		}
		for (String partData : dataList)
			odt.newParagraph().addContent(partData + "\n");
		java.io.File createdFile = new java.io.File(GoogleDrive.USER_HOME_DOCUMENTS,fileName);
		odt.save(new FileOutputStream(createdFile));
		return createdFile;
	}
	/**
	 * creates a text document on local drive to be uploaded on google drive.
	 * @param fileName String
	 * @param data String
	 * @return textFile java.io.File
	 * @throws Exception
	 */
	private java.io.File createDocumentText(String fileName, String data) throws Exception {
		
		java.io.File textFile = new java.io.File(GoogleDrive.USER_HOME_DOCUMENTS, fileName);
		if (!textFile.exists())
			textFile.createNewFile();
		PrintWriter fileWriter = new PrintWriter(textFile);
		StringTokenizer dataTokenizer = new StringTokenizer(data, "\n");
		String line = "";
		while (dataTokenizer.hasMoreElements()) {
			line = dataTokenizer.nextElement().toString();
			fileWriter.println(line);
		}
		fileWriter.close();
		return textFile;
	}
	/**
	 * creates a PDF document on local drive to be uploaded on Google Drive.
	 * @param fileName String
	 * @param data String
	 * @return pdfFile java.io.File
	 * @throws Exception
	 */
	private File createDocumentPDF(String fileName, String data) throws Exception {

		Document document = new Document();
		File file = new File(GoogleDrive.USER_HOME_DOCUMENTS, fileName);
		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
		document.open();
		writer.setPageEmpty(true);
		document.newPage();
		writer.setPageEmpty(true);
		document.add(new Paragraph(data));
		document.close();
		return file;
	}
	/**
	 * creates a CSV file on local drive.
	 * @param fileName String
	 * @param data String
	 * @return csvFile java.io.File
	 * @throws Exception
	 */
	private java.io.File createDocumentCSV(String fileName, String data)
			throws Exception {
		return createDocumentText(fileName, data);
	}
	/**
	 * updates the document of passed fileId with passed new Data.
	 * @param fileId String
	 * @param newData String new content for file
	 * @return updatedFile GoogleFile
	 * @throws Exception
	 */
	public GoogleFile updateDocument(String fileId, String newData)
			throws Exception {

		GoogleFile fileToUpdate = driveService.searchGoogleFile(fileId);
		java.io.File createdFile = null;		
		if (fileToUpdate.getFileType().equals(FileTypes.MS_DOCUMENT)) {
			if (fileToUpdate.getFileName().endsWith(".doc"))
				createdFile = createDocumentDoc(fileToUpdate.getFileName(),newData);
			else
				createdFile = createDocumentX(fileToUpdate.getFileName(),newData);
		} else if (fileToUpdate.getFileType().equals(FileTypes.OPEN_DOCUMENT))
			createdFile = createDocumentOpen(fileToUpdate.getFileName(),newData);		
		else if (fileToUpdate.getFileType().equals(FileTypes.PLAIN_TEXT))
			createdFile = createDocumentText(fileToUpdate.getFileName(),newData);		
		else if (fileToUpdate.getFileType().equals(FileTypes.GOOGLE_DOC_FILE))
			createdFile = createDocumentX(fileToUpdate.getFileName() + ".docx",newData);		
		else if (fileToUpdate.getFileType().equals(FileTypes.CSV_FILE))
			createdFile = createDocumentCSV(fileToUpdate.getFileName() + ".csv", newData);
		else
			return fileToUpdate;
		GoogleFile updatedFile = driveService.updateFile(fileId, fileToUpdate,createdFile);
		return updatedFile;
	}
	/**
	 * rename a document on google drive.
	 * @param fileId String
	 * @param newName String a new name for document.
	 * @throws IOException
	 */
	public void renameDocument(String fileId, String newName) throws IOException {
		driveService.rename(fileId, newName);
	}
}
