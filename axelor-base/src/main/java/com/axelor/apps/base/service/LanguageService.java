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
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;

public interface LanguageService {
  /**
   * This method saves the modifications of an existing Language obj into the related MetaSelectItem
   * obj.
   *
   * @param language
   * @param oldName
   * @param oldCode
   */
  public void saveExistingLanguageToMetaSelect(Language language, String oldName, String oldCode);

  /**
   * This method saves a Language obj into a new MetaSelectItem obj.
   *
   * @param language
   */
  public void saveNewLanguageToMetaSelect(Language language);

  /**
   * When we remove a Language obj, the related record in select.language should also be removed.
   *
   * @param language
   */
  public void removeLanguageLinkedMetaSelectItem(Language language);
}
