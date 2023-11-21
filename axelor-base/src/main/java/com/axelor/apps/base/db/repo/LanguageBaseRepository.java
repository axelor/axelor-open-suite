package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.web.LanguageService;
import com.axelor.db.JPA;
import com.axelor.inject.Beans;
import javax.persistence.EntityManager;

public class LanguageBaseRepository extends LanguageRepository {
  @Override
  public Language save(Language entity) {
    EntityManager em = JPA.em().getEntityManagerFactory().createEntityManager();
    Language oldLanguageObject;
    LanguageService languageService = Beans.get(LanguageService.class);
    oldLanguageObject = em.find(Language.class, entity.getId());
    if (oldLanguageObject != null) {
      String oldName = oldLanguageObject.getName();
      String oldCode = oldLanguageObject.getCode();
      languageService.saveExistingLanguageObejctToMetaSelect(entity, oldName, oldCode);
    } else {
      languageService.saveNewLanguageObejctToMetaSelect(entity);
    }
    return super.save(entity);
  }

  @Override
  public void remove(Language entity) {

    LanguageService languageService = Beans.get(LanguageService.class);
    languageService.removeLanguageLinkedMetaSelectItem(entity);
    super.remove(entity);
  }
}
