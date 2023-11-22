package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Language;

public interface LanguageService {

  public void saveExistingLanguageObjectToMetaSelect(
      Language language, String oldName, String oldCode);

  public void saveNewLanguageObjectToMetaSelect(Language language);

  public void removeLanguageLinkedMetaSelectItem(Language language);

  public void convertToLowercase(Language language);
}
