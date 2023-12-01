package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;

public interface LanguageService {
  /**
   * This method saves the modifications of an existing Language obj into the related MetaSelectItem obj.
   * @param language
   * @param oldName
   * @param oldCode
   */
  public void saveExistingLanguageToMetaSelect(Language language, String oldName, String oldCode);

  /**
   * This method saves a Language obj into a new MetaSelectItem obj.
   * @param language
   * @param oldName
   * @param oldCode
   */
  public void saveNewLanguageToMetaSelect(Language language, String oldName, String oldCode);

  /**
   * When we remove a Language obj, the related record in select.language should also be removed.
   *
   * @param language
   */
  public void removeLanguageLinkedMetaSelectItem(Language language);
}
