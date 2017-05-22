package com.axelor.studio.service.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.tool.file.CsvTool;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.studio.service.ConfigurationService;
import com.google.inject.Inject;

public class TranslationBuilderService {
	
	@Inject
	private MetaTranslationRepository metaTranslationRepo;
	
	@Inject
	private ConfigurationService configService;
	
	private final static String[] HEADERS = new String[] {"key", "message", "comments", "context" };
	
	public void build() throws AxelorException {
		
		for (String module : configService.getCustomizedModuleNames()) {
			List<MetaTranslation> translations = metaTranslationRepo
					.all()
					.filter("self.module = ?1", module)
					.fetch();
			
			if (translations.isEmpty()) {
				continue;
			}
			
			List<String> keys  = new ArrayList<>();
			List<String[]> messages = new ArrayList<String[]>(); 
			List<String[]> messagesEN = new ArrayList<String[]>(); 
			List<String[]> messagesFR = new ArrayList<String[]>(); 
			
			for (MetaTranslation translation : translations) {
				String key = translation.getKey();
				if (!keys.contains(key)) {
					keys.add(translation.getKey());
					messages.add(new String[]{key, null, null, null});
				}
				
				if (translation.getLanguage().equals("fr")) {
					messagesFR.add(new String[]{key, translation.getMessage(), null, null});
				}
				else {
					messagesEN.add(new String[]{key, translation.getMessage(), null, null});
				}
			}
			
			String i18nDir = configService.getTranslationDir(module, true).getAbsolutePath();
			
			try {
				CsvTool.csvWriter(i18nDir, "messages.csv", ',',  HEADERS, messages);
				CsvTool.csvWriter(i18nDir, "messages_fr.csv", ',',  HEADERS, messagesFR);
				CsvTool.csvWriter(i18nDir, "messages_en.csv", ',',  HEADERS, messagesEN);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
