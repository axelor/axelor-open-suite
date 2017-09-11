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
package com.axelor.apps.base.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

import org.asciidoctor.Asciidoctor;

import com.axelor.apps.base.db.DocumentGenerator;
import com.axelor.apps.base.service.app.AsciidocExporterService;
import com.axelor.apps.base.service.app.DocumentGeneratorService;
import com.axelor.apps.base.service.reader.DataReader;
import com.axelor.apps.base.service.reader.ExcelReader;
import com.axelor.exception.AxelorException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class DocumentGeneratorController {

	@Inject
	private DocumentGeneratorService documentGeneratorService;

	@Inject
	private AsciidocExporterService asciidocExportService;

	@Inject
	private MetaFiles metaFiles;
	
	private String fileName = null;

	public void generateDocOutputFile(ActionRequest request, ActionResponse response)
			throws IOException, AxelorException {

		DocumentGenerator documentGenerator = request.getContext().asType(DocumentGenerator.class);
		String fileName = documentGenerator.getDocInputFile().getFileName().substring(0, documentGenerator.getDocInputFile().getFileName().indexOf("."));
		this.fileName = fileName;
		
		if (!documentGenerator.getDocInputFile().getFileType().equals("application/zip")) {
			response.setError("Issue with input file. Please input only zip file");
			return;
		}
		DataReader reader = new ExcelReader();
		MetaFile excelFile = null;
		
		File workspace = documentGeneratorService.createFinalWorkspace(documentGenerator.getDocInputFile());
		File[] excelFiles = documentGeneratorService.getExcelFiles(workspace.getAbsolutePath());
		File finalPath = documentGeneratorService.getFinalPath(workspace.getAbsolutePath());

		BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(finalPath + File.separator + "Doc_" + fileName + ".adoc"));
		bufferedWriter.write(":toc: left");
		bufferedWriter.write("\n= " + "Documentation");
		BufferedReader bufferedReader = null;
		
		for (File file : excelFiles) {
			excelFile = metaFiles.upload(file);

			if (excelFile != null) {

				String imgPath = documentGeneratorService.getImgPath(finalPath.getAbsolutePath(),
						excelFile.getFileName().substring(0, excelFile.getFileName().indexOf(".")));
				
				File asciidocFile = asciidocExportService.export(excelFile, reader,
						excelFile.getFileName().substring(0, excelFile.getFileName().indexOf(".")), imgPath,
						finalPath.getAbsolutePath());
				
				documentGeneratorService.mergeAsciidoc(asciidocFile, bufferedReader, bufferedWriter);
				documentGeneratorService.removeTempAsciidoc(asciidocFile);
			}
		}
		bufferedWriter.close();
		this.getHtmlOrPdf(request, response, documentGenerator, finalPath);

		documentGeneratorService.removeTempDirectories(workspace);
	}

	private void getHtmlOrPdf(ActionRequest request, ActionResponse response, DocumentGenerator documentGenerator,
			File finalPath) throws IOException {

		Asciidoctor asciidoctor = Asciidoctor.Factory.create();

		File asciidocFile = documentGeneratorService.getAsciidocFile(finalPath.getAbsolutePath());
		
		if (asciidocFile.length() > 26) {
			if (documentGenerator.getExportType().equals("html")) {
	
				documentGeneratorService.convertAsciidocToHtml(asciidoctor, asciidocFile);
				response.setValue("docOutputFile", documentGeneratorService.getZipFile(finalPath.getAbsolutePath(), fileName));
	
			} else {
	
				documentGeneratorService.convertAsciidocToPdf(asciidoctor, asciidocFile);
				response.setValue("docOutputFile", documentGeneratorService.getPdfFile(finalPath.getAbsolutePath()));
			}
		} else {
			response.setError("Issue with input file. Please check the format");
		}
	}
}
