/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TranslationRestServiceImpl implements TranslationRestService {

  protected MetaTranslationRepository translationRepo;

  @Inject
  public TranslationRestServiceImpl(MetaTranslationRepository translationRepo) {
    this.translationRepo = translationRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Integer createNewTranslation(Map<String, String> translationMap, String language)
      throws AxelorException {
    Iterator<String> keys = translationMap.keySet().iterator();
    int addedTranslation = 0;

    while (keys.hasNext()) {
      String currentKey = keys.next();

      Query<MetaTranslation> query =
          translationRepo.all().filter("self.language = :language " + "AND self.key LIKE :key");
      query.bind("language", language);
      query.bind("key", "mobile_app_" + currentKey);

      List<MetaTranslation> translationList = query.fetch();
      if (translationList.size() == 0) {
        MetaTranslation newTranslation = new MetaTranslation();
        newTranslation.setKey("mobile_app_" + currentKey);
        newTranslation.setMessage(translationMap.get(currentKey));
        newTranslation.setLanguage(language);
        translationRepo.save(newTranslation);
        addedTranslation++;
      }
    }
    return addedTranslation;
  }
}
