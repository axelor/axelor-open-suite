package com.axelor.studio.service.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.studio.service.ConfigurationService;
import com.google.inject.Inject;

public class SelectionBuilderService {
	
	private final Logger log = LoggerFactory.getLogger(SelectionBuilderService.class);
	
	@Inject
	private MetaSelectRepository metaSelectRepo;
	
	@Inject
	private ConfigurationService configService;

	public void build() throws AxelorException {
		
		List<String> modules = configService.getCustomizedModuleNames();
		if (modules.isEmpty()) {
			configService.removeViewFile("Selection.xml");
			return;
		}
		
		List<MetaSelect> metaSelects = metaSelectRepo.all()
				.filter("self.module in (?1)", configService.getCustomizedModuleNames()).fetch();
		
		log.debug("Meta selects to process: {}", metaSelects);
		if (metaSelects.isEmpty()) {
			configService.removeViewFile("Selection.xml");
			return;
		}
		
		Map<String, StringBuilder> moduleMap = new HashMap<String, StringBuilder>();
		
		for (MetaSelect metaSelect : metaSelects) {
			String module = metaSelect.getModule();
			StringBuilder builder = moduleMap.get(module);
			if (builder == null) {
				builder = new StringBuilder();
			}
			builder.append("\n\t<selection name=\"" + metaSelect.getName() + "\" >");
			for (MetaSelectItem item : metaSelect.getItems()) {
				builder.append("\n\t\t<option value=\"" + item.getValue() + "\">"
						     + item.getTitle() + "</option>");
			}
			builder.append("\n\t</selection>");
			
			moduleMap.put(module, builder);
			
		}
		
		for (String module : moduleMap.keySet()) {
			StringBuilder sb = new StringBuilder(
					"<?xml version='1.0' encoding='UTF-8'?>\n");
			sb.append("<object-views")
					.append(" xmlns='")
					.append(ObjectViews.NAMESPACE)
					.append("'")
					.append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
					.append(" xsi:schemaLocation='")
					.append(ObjectViews.NAMESPACE)
					.append(" ")
					.append(ObjectViews.NAMESPACE + "/" + "object-views_"
							+ ObjectViews.VERSION + ".xsd").append("'")
					.append(">\n\n").append(moduleMap.get(module).toString())
					.append("\n</object-views>");
			File selectionFile = new File(configService.getViewDir(module, true), "Selection.xml");
			try {
				writeFile(selectionFile, sb.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
		
	private void writeFile(File file, String content) throws IOException {


		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(content);
		fileWriter.close();

	}

}
