package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;

public interface LanguageService {
  /**
   * If we are modifying an existing object, pass 'false' to isNew. Otherwise, if we want to save a
   * new Language obj, pass 'true' to isNew, and pass 'null' to oldName and oldCode.
   *
   * @param language
   * @param oldName
   * @param oldCode
   * @param isNew
   */
  public void saveLanguageToMetaSelect(
      Language language, String oldName, String oldCode, boolean isNew);

  /**
   * When we remove a Language obj, the related record in select.language should also be removed.
   *
   * @param language
   */
  public void removeLanguageLinkedMetaSelectItem(Language language);
}
