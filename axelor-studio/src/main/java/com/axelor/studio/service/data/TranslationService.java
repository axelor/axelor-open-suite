package com.axelor.studio.service.data;

import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TranslationService {
	
	@Inject
	private MetaTranslationRepository metaTranslationRepo;
	
	public String getTranslation(String key, String lang) {
		
		if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(lang)) {
			return null;
		}
		
		MetaTranslation translation = metaTranslationRepo.findByKey(key, lang);
		if (translation != null) {
			return translation.getMessage();
		}
		
		return null;
	}
	
	@Transactional
	public void addTranslation(String key, String message, String lang) {
		
		if (Strings.isNullOrEmpty(key) || Strings.isNullOrEmpty(message) || Strings.isNullOrEmpty(lang)) {
			return;
		}
		
		if (key.equals(message)) {
			return;
		}
		
		MetaTranslation translation = metaTranslationRepo.findByKey(key, lang);
		if (translation == null) {
			translation = new MetaTranslation();
			translation.setLanguage(lang);
			translation.setKey(key);
		}
		
		translation.setMessage(message);
		
		metaTranslationRepo.save(translation);
	}
	
}
