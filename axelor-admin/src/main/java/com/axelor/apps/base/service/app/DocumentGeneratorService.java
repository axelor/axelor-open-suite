/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.io.Files;

public class DocumentGeneratorService {
	
	protected Logger log = LoggerFactory.getLogger(DocumentGeneratorService.class);
	
	@Inject
	private MetaFiles metaFiles;
	
	private List<String> fileList = new ArrayList<String>();
	
	public File createFinalWorkspace(MetaFile metaFile) throws IOException {
		
		File data = MetaFiles.getPath(metaFile).toFile();
		File finalWorkspace = Files.createTempDir();
		finalWorkspace.mkdir();
		
		if (isZip(data)) {
			unZip(data, finalWorkspace); 
		}
		return finalWorkspace;
	}
	
	private boolean isZip(File file) {
		
		return Files.getFileExtension(file.getName()).equals("zip"); 
	}
	
	private void unZip(File file, File directory) throws ZipException, IOException {
		
		File extractFile = null;
		FileOutputStream fileOutputStream = null;
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();

		while (entries.hasMoreElements()) {
			
			ZipEntry entry = entries.nextElement();

			extractFile = new File(directory, entry.getName());
			
			if (entry.isDirectory()) {
				extractFile.mkdirs();
				continue;
			} else {
				extractFile.getParentFile();
				extractFile.createNewFile();
			}

			fileOutputStream = new FileOutputStream(extractFile);
			InputStream entryInputStream = zipFile.getInputStream(entry);
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			
			while ((bytesRead = entryInputStream.read(buffer)) != -1) {
				
				fileOutputStream.write(buffer, 0, bytesRead);
			}
		}
		zipFile.close();
	}
	
	public File getFinalPath(String directory) throws IOException {
		
		File newDirectory = new File(directory + File.separator + "Final");
		newDirectory.mkdirs();
		
		File dir = new File(directory);
		File[] imgDir = dir.listFiles();
		
		for (File file : imgDir) {
			if (file.isDirectory()) {
				if (!file.getName().equals("Final")) {
					File imgFolder = new File(newDirectory.getAbsolutePath() + File.separator + file.getName());
					imgFolder.mkdirs();
					FileUtils.copyDirectory(file, imgFolder);
				}
			}
		}
		return newDirectory;
	}
	
	public String getImgPath(String directory, String imgFolderName) {
	
		File dir = new File(directory);
		File[] imgDir = dir.listFiles();
		
		String imgPath = "";
		for (File file : imgDir) {
			if (file.isDirectory()) {
				if (file.getName().equals(imgFolderName)) {
					imgPath = file.getAbsolutePath();
				}
			}
		}
		return imgPath;
	}
	
	public File[] getExcelFiles(String directory) throws IOException {
		
		File dir = new File(directory);
		File[] allExcelFiles = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".xlsx");
			}
		});
		
		return allExcelFiles;
	}
	
	@SuppressWarnings("resource")
	public void mergeAsciidoc(File asciidocFile, BufferedReader br, BufferedWriter writer) throws IOException {
		
		br = new BufferedReader(new FileReader(asciidocFile));
		String s1 = null;
		
        while ((s1 = br.readLine()) != null){ 
        	writer.write(s1);
        	writer.write("\n");
        }
        writer.write("\n\n");
	}
	
	public void removeTempAsciidoc(File asciidocFile) throws IOException {
		
		if (asciidocFile.isDirectory()) {
			FileUtils.deleteDirectory(asciidocFile); 
		} else { 
			asciidocFile.delete();
		}
	}
	
	public File getAsciidocFile(String finalPath) {
		
		File finalDir = new File(finalPath);
		File asciidocFile = null;
		
		for (File file : finalDir.listFiles()) {
			if (!file.isDirectory()) {
				asciidocFile = file;
			}
		}
		return asciidocFile;
	}
	
	public void convertAsciidocToHtml(Asciidoctor asciidoctor, File asciidocFile) {
		
		Map<String, Object> options = OptionsBuilder.options().safe(SafeMode.SAFE).asMap();
		asciidoctor.convertFile(asciidocFile, options);
	}
	
	public MetaFile getZipFile(String directory, String fileName) throws IOException {
		
		File tempDir = Files.createTempDir();
		File zipFile = new File(tempDir, "Doc_" + fileName + ".zip");

		this.generateFileList(new File(directory));
		this.zipIt(directory, zipFile);
    	
		MetaFile metaZipFile = metaFiles.upload(zipFile);
		FileUtils.deleteDirectory(tempDir);
		return metaZipFile;
	}
	
	private void zipIt(String directory, File zipFile) throws IOException {
		
		byte[] buffer = new byte[1024];
		FileOutputStream fos = new FileOutputStream(zipFile);
    	ZipOutputStream zos = new ZipOutputStream(fos);
    	
    	for (String file : this.fileList) {
    		
    		String name = file.substring(directory.length() + 1, file.length());
    		
    		ZipEntry ze = new ZipEntry(name);
    		zos.putNextEntry(ze);
    		
    		FileInputStream in = new FileInputStream(file);
    		
    		int len;
        	while ((len = in.read(buffer)) > 0) {
        		zos.write(buffer, 0, len);
        	}
        	in.close();
    	}
    	zos.closeEntry();
    	zos.close();
	}
	
	private void generateFileList(File directory) {
		
		File[] files = directory.listFiles();
		
        if (files != null && files.length > 0) {
            for (File file : files) {
            	
                if (file.isFile()) {
                    fileList.add(file.getAbsolutePath());
                    
                } else {
                	
                	generateFileList(file);
                }
            }
        }
	}
	
	public void convertAsciidocToPdf(Asciidoctor asciidoctor, File asciidocFile) {
		
		Options options = new Options();
		options.setBackend("pdf");
		asciidoctor.convertFile(asciidocFile, options);
	}
	
	public MetaFile getPdfFile(String directory) throws IOException {
		
		MetaFile pdfFile = null;
		
		File dir = new File(directory);
		File[] allFiles = dir.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".pdf");
			}
		});
		
		for (File file : allFiles) {
			pdfFile = metaFiles.upload(file);
		}
		
		return pdfFile;
	}
	
	public void removeTempDirectories(File workspace) throws IOException {
	
		if (workspace.isDirectory()) {
			FileUtils.deleteDirectory(workspace); 
		} else { 
			workspace.delete();
		}
	}
}
