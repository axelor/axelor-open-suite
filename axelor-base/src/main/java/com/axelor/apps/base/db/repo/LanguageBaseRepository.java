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
            languageService.saveNewLanguageToMetaSelect(entity, null, null);
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
