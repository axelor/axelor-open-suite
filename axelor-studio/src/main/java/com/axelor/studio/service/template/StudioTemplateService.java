/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.template;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.poi.util.TempFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.data.ImportException;
import com.axelor.data.ImportTask;
import com.axelor.data.Listener;
import com.axelor.data.xml.XMLImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.studio.db.StudioTemplate;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.db.repo.StudioTemplateRepository;
import com.axelor.studio.service.data.ImportScript;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class StudioTemplateService {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private TemplateXmlCreator xmlCreator;

	@Inject
	private StudioTemplateRepository templateRepo;

	@Inject
	private MetaFiles metaFiles;

	@Inject
	private ImportScript importScript;

	public MetaFile export(String templateName) {

		List<String> models = new ArrayList<String>();
		models.add("MetaSelect,self.name in (select metaSelect.name from MetaField where customised = true)");
		models.add("MetaModel,customised = true");
		models.add("RightManagement");
		models.add("ViewBuilder,viewType != 'dashboard'");
		models.add("ViewBuilder,viewType = 'dashboard'");
		models.add("MenuBuilder");
		models.add("Wkf");
		models.add("ReportBuilder");
		models.add("ActionBuilder");
		models.add("Template");

		File file = TempFile.createTempFile("template", ".xml");
		xmlCreator.createXml(models, file);

		return createMetaZip(templateName, file);

	}

	public String importTemplate(StudioTemplate template) {

		if (!dependencyImported(template)) {
			setTemplateImported(template, false);
			return I18n.get("Dependency not imported.") + " "
					+ I18n.get("Please import all dependency templates first.");
		}

		MetaFile templateFile = template.getMetaFile();

		if (!isZip(templateFile)) {
			setTemplateImported(template, false);
			return I18n.get("Uploaded file is not zip file");
		}

		File templateXml = unzip(templateFile);
		if (templateXml == null) {
			setTemplateImported(template, false);
			return I18n.get("Not a valid zip");
		}

		File configFile = getConfigFile();
		if (configFile == null) {
			setTemplateImported(template, false);
			return I18n
					.get("Issue in configuration. Please contact administrator");
		}

		Boolean imported = importXml(configFile, templateXml);
		if (imported) {
			setTemplateImported(template, true);
			return I18n.get("Template imported successfully");
		}

		setTemplateImported(template, false);
		return I18n.get("Issue in template import please check the log");
	}

	private MetaFile createMetaZip(String templateName, File file) {

		try {
			if (!file.exists()) {
				log.debug("No data file generated");
				return null;
			}
			File zipFile = File.createTempFile("Template", ".zip");
			FileOutputStream fileOut = new FileOutputStream(zipFile);
			ZipOutputStream zipOut = new ZipOutputStream(fileOut);
			ZipEntry zipEntry = new ZipEntry(templateName + ".xml");
			zipOut.putNextEntry(zipEntry);
			FileInputStream inStream = new FileInputStream(file);

			byte[] buffer = new byte[1024];
			int len;
			while ((len = inStream.read(buffer)) > 0) {
				zipOut.write(buffer, 0, len);
			}
			inStream.close();

			zipOut.closeEntry();
			zipOut.close();
			file.delete();

			if (zipFile.exists()) {
				inStream = new FileInputStream(zipFile);
				MetaFile metaFile = metaFiles.upload(inStream, templateName
						+ ".zip");
				zipFile.delete();
				return metaFile;
			}

			log.debug("Error in zip creation");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private File unzip(MetaFile metaFile) {
		try {
			byte[] buffer = new byte[1024];
			File zipFile = MetaFiles.getPath(metaFile).toFile();
			ZipInputStream stream = new ZipInputStream(new FileInputStream(
					zipFile));
			ZipEntry entry = stream.getNextEntry();
			if (entry != null) {
				String fileName = entry.getName();
				File file = new File(zipFile.getParent(), fileName);
				FileOutputStream outStream = new FileOutputStream(file);
				int len;
				while ((len = stream.read(buffer)) > 0) {
					outStream.write(buffer, 0, len);
				}
				outStream.close();
				stream.close();
				return file;
			}
			stream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private File getConfigFile() {

		try {
			File configFile = File.createTempFile("template-config", ".xml");
			InputStream inputStream = this.getClass().getResourceAsStream(
					"/template/template-config.xml");
			FileOutputStream outputStream = new FileOutputStream(configFile);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = inputStream.read(bytes)) != -1)
				outputStream.write(bytes, 0, read);
			outputStream.close();
			return configFile;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public Boolean importXml(File config, final File data) {

		log.debug("Importing config : {}, data: {}", config.getAbsolutePath(),
				data.getAbsolutePath());
		XMLImporter importer = new XMLImporter(config.getAbsolutePath());
		final List<Boolean> statusList = new ArrayList<Boolean>();

		importer.addListener(new Listener() {

			@Override
			public void imported(Integer total, Integer success) {
				log.debug("Total records: {}", total);
				log.debug("Success records: {}", success);
			}

			@Override
			public void imported(Model model) {

			}

			@Override
			public void handle(Model model, Exception e) {
				log.debug("Error in import: {}", e);
				statusList.add(false);
			}
		});

		importer.run(new ImportTask() {
			@Override
			public void configure() throws IOException {
				input("studio.xml", data);
			}

			@Override
			public boolean handle(ImportException exception) {
				log.debug("Import error : " + exception);
				statusList.add(false);
				return false;
			}

			@Override
			public boolean handle(IOException e) {
				log.debug("IO error : " + e);
				statusList.add(false);
				return true;
			}

			@Override
			public boolean handle(ClassNotFoundException e) {
				log.debug("Class not found: " + e);
				statusList.add(false);
				return false;
			}

		});

		log.debug("Status list: {}", statusList);

		// delete temporary files
		config.delete();
		data.delete();

		for (Boolean status : statusList) {
			if (!status) {
				return false;
			}
		}

		return true;

	}

	@Transactional
	public Object saveEntity(Object bean, Map<String, Object> values) {

		if (bean instanceof WkfTransition) {
			bean = importScript.updateTransitionNode(bean, values);
		}

		assert bean instanceof Model;

		Model entity = (Model) bean;

		return JPA.save(entity);

	}

	private boolean isZip(MetaFile metaFile) {

		try {
			File file = MetaFiles.getPath(metaFile).toFile();
			BufferedInputStream inStream = new BufferedInputStream(
					new FileInputStream(file));
			DataInputStream in = new DataInputStream(inStream);
			int test;
			test = in.readInt();
			in.close();
			return test == 0x504b0304;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return false;

	}

	@Transactional
	public void setTemplateImported(StudioTemplate template, Boolean imported) {

		template.setImported(imported);

		templateRepo.save(template);

	}

	private boolean dependencyImported(StudioTemplate template) {

		String dependency = template.getDependsOn();
		if (Strings.isNullOrEmpty(dependency)) {
			return true;
		}

		for (String name : Arrays.asList(dependency.split(","))) {

			template = templateRepo.findByName(name.trim());

			if (template == null || !template.getImported()) {
				return false;
			}
		}

		return true;
	}
}
