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
package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.service.LanguageService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import javax.persistence.EntityManager;

public class LanguageBaseRepository extends LanguageRepository {

  protected LanguageService languageService;

  @Inject
  public LanguageBaseRepository(LanguageService languageService) {
    this.languageService = languageService;
  }

  /**
   * This save method is overridden in LanguageBaseRepository. The Language obj should be linked
   * with selection: select.language. When an existing Language object is modified or a new Language
   * object is created, the record should be synchronized.
   *
   * @param entity the entity object to save
   * @return
   */
  @Override
  public Language save(Language entity) {
    // Get the original object by EntityManager.
    EntityManager em = JPA.em().getEntityManagerFactory().createEntityManager();
    Language oldLanguageObject = em.find(Language.class, entity.getId());
    if (oldLanguageObject != null) {
      // Save modifications of an existing Language obj and link to MetaSelectItem.
      languageService.saveExistingLanguageToMetaSelect(
          entity, oldLanguageObject.getName(), oldLanguageObject.getCode());
    } else {
      // Create a new Language obj and link to MetaSelectItem
      languageService.saveNewLanguageToMetaSelect(entity);
    }
    return super.save(entity);
  }

  /**
   * This save method is overridden in LanguageBaseRepository. The Language obj should be linked
   * with selection: select.language. When a Language obj is about to remove, the related record in
   * select.language should also be removed.
   *
   * @param entity the entity object
   */
  @Override
  public void remove(Language entity) {
    // Remove the corresponding value in MetaSelectItem before remove the Language obj.
    languageService.removeLanguageLinkedMetaSelectItem(entity);
    super.remove(entity);
  }
}
