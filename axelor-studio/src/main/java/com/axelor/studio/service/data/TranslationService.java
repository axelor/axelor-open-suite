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
	public void addTranslation(String key, String message, String lang, String module) {
		
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
		translation.setModule(module);
		translation.setMessage(message);
		
		metaTranslationRepo.save(translation);
	}
	
}
