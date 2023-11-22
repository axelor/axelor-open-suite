package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.web.LanguageService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import javax.persistence.EntityManager;

public class LanguageBaseRepository extends LanguageRepository {
  @Override
  public Language save(Language entity) {
    // Get the original object by EntityManager.
    EntityManager em = JPA.em().getEntityManagerFactory().createEntityManager();
    Language oldLanguageObject = em.find(Language.class, entity.getId());
    LanguageService languageService = Beans.get(LanguageService.class);

    if (oldLanguageObject != null) {
      // Save modifications of an existing Language obj and link to MetaSelectItem.
      languageService.saveExistingLanguageObjectToMetaSelect(
          entity, oldLanguageObject.getName(), oldLanguageObject.getCode());
    } else {
      // Create a new Language obj and link to MetaSelectItem
      languageService.saveNewLanguageObjectToMetaSelect(entity);
    }
    return super.save(entity);
  }

  @Override
  public void remove(Language entity) {
    // Remove the corresponding value in MetaSelectItem before remove the Language obj.
    LanguageService languageService = Beans.get(LanguageService.class);
    languageService.removeLanguageLinkedMetaSelectItem(entity);
    super.remove(entity);
  }
}
