package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LanguageServiceImpl implements LanguageService {
  protected MetaSelectRepository metaSelectRepository;
  protected MetaSelectItemRepository metaSelectItemRepository;

  private static final String SELECT_NAME = "select.language";
  private static final String NATIVE_SELECT_LANGUAGE_MODULE = "axelor-core";
  private static final String NEW_SELECT_LANGUAGE_MODULE = "axelor-base";

  @Inject
  public LanguageServiceImpl(
      MetaSelectRepository metaSelectRepository,
      MetaSelectItemRepository metaSelectItemRepository) {
    this.metaSelectRepository = metaSelectRepository;
    this.metaSelectItemRepository = metaSelectItemRepository;
  }

  /**
   * If we are modifying an existing object, pass 'false' to isNew. Otherwise, if we want to save a
   * new Language obj, pass 'true' to isNew, and pass 'null' to oldName and oldCode.
   *
   * @param language
   * @param oldName
   * @param oldCode
   * @param isNew
   */
  @Transactional
  @Override
  public void saveLanguageToMetaSelect(
      Language language, String oldName, String oldCode, boolean isNew) {
    MetaSelect metaSelect;
    MetaSelectItem metaSelectItem;
    if (!isNew) {
      metaSelectItem = findMetaSelectItem(oldName, oldCode);
      if (metaSelectItem == null) return;
      metaSelectItem.setTitle(language.getName());
      metaSelectItem.setValue(language.getCode());
      metaSelectItemRepository.save(metaSelectItem);
    } else {
      // new metaSelectItem
      Query<MetaSelect> metaSelectQuery =
          metaSelectRepository
              .all()
              .filter("self.name = :selectName and self.module = :newModuleName");
      metaSelectQuery
          .bind("selectName", SELECT_NAME)
          .bind("newModuleName", NEW_SELECT_LANGUAGE_MODULE);
      metaSelect = metaSelectQuery.fetchOne();
      if (metaSelect == null) {
        metaSelect = copyMetaSelect();
      }
      metaSelectItem = new MetaSelectItem();
      metaSelectItem.setOrder((int) getCountMetaSelectItemFromQuery());
      metaSelectItem.setTitle(language.getName());
      metaSelectItem.setValue(language.getCode());
      metaSelect.addItem(metaSelectItem);
      metaSelectRepository.save(metaSelect);
    }
  }

  /**
   * Find the metaSelectItem according to Langauge obj's name and code.
   *
   * @param name
   * @param code
   * @return metaSelectItem
   */
  public MetaSelectItem findMetaSelectItem(String name, String code) {
    Query<MetaSelectItem> metaSelectItemQuery =
        metaSelectItemRepository
            .all()
            .filter(
                "self.title = :oldName and self.value = :oldCode and self.select.module = :moduleName");
    metaSelectItemQuery
        .bind("oldName", name)
        .bind("oldCode", code)
        .bind("moduleName", NEW_SELECT_LANGUAGE_MODULE);
    return metaSelectItemQuery.fetchOne();
  }

  /**
   * Count the number of metaSelectItem
   *
   * @return long : count number
   */
  public long getCountMetaSelectItemFromQuery() {
    Query<MetaSelectItem> metaSelectItemQuery =
        metaSelectItemRepository.all().filter("self.select.name = :selectName");
    metaSelectItemQuery.bind("selectName", SELECT_NAME);
    return metaSelectItemQuery.count();
  }

  /**
   * When we remove a Language obj, the related record in select.language should also be removed.
   *
   * @param language
   */
  @Transactional
  @Override
  public void removeLanguageLinkedMetaSelectItem(Language language) {
    MetaSelectItem metaSelectItem = findMetaSelectItem(language.getName(), language.getCode());
    if (metaSelectItem == null) return;
    updateOrderNumber(metaSelectItem.getOrder());
    metaSelectItemRepository.remove(metaSelectItem);
  }

  /**
   * Before remove an MetaSelectItem, we have to update other records' order_seq field.
   * @param orderNumber
   */
  @Transactional
  public void updateOrderNumber(int orderNumber) {
    javax.persistence.Query query =
        JPA.em()
            .createNativeQuery(
                "UPDATE meta_select_item "
                    + "SET order_seq = order_seq - 1 "
                    + "FROM meta_select "
                    + "WHERE meta_select.id = meta_select_item.select_id "
                    + "AND meta_select_item.order_seq > :orderNumber "
                    + "AND meta_select.name = :selectName "
                    + "AND meta_select.module = :moduleName ")
            .setParameter("orderNumber", orderNumber)
            .setParameter("selectName", SELECT_NAME)
            .setParameter("moduleName", NEW_SELECT_LANGUAGE_MODULE);
    query.executeUpdate();
  }

  /**
   * Fetch the native MetaSelect and the overriddenMetaSelect. Copy MetaSelectItems from the native
   * to the new one. If it is the first time we add a new Language object, we should create a new
   * MetaSelect object with a name field of "select.language", a module field of "axelor-base" and a
   * priority field of the original priority + 1. Then, we should copy all the existing
   * MetaSelectItems from the original MetaSelect to the new MetaSelect.
   */
  @Transactional
  protected MetaSelect copyMetaSelect() {
    // fetch the native MetaSelect
    Query<MetaSelect> metaSelectQuery =
        metaSelectRepository
            .all()
            .filter("self.name = :selectName and self.module = :nativeModuleName");
    metaSelectQuery
        .bind("selectName", SELECT_NAME)
        .bind("nativeModuleName", NATIVE_SELECT_LANGUAGE_MODULE);
    MetaSelect nativeMetaSelect = metaSelectQuery.fetchOne();
    if (nativeMetaSelect == null) return null;
    MetaSelect overriddenMetaSelect = getOverriddenMetaSelectFromNativeMetaSelect(nativeMetaSelect);
    return metaSelectRepository.save(overriddenMetaSelect);
  }

  /**
   * create a new MetaSelect obj that has the same metaSelectItems as the native one.
   *
   * @param nativeMetaSelect
   * @return overriddenMetaSelect
   */
  protected MetaSelect getOverriddenMetaSelectFromNativeMetaSelect(MetaSelect nativeMetaSelect) {
    MetaSelect overriddenMetaSelect = new MetaSelect();
    Integer oldPriority = nativeMetaSelect.getPriority();
    overriddenMetaSelect.setName(SELECT_NAME);
    overriddenMetaSelect.setModule(NEW_SELECT_LANGUAGE_MODULE);
    overriddenMetaSelect.setPriority(oldPriority + 1);
    return overriddenMetaSelect;
  }
}
