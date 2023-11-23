package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
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
  protected LanguageRepository languageRepository;

  private static final String SELECT_NAME = "select.language";

  @Inject
  public LanguageServiceImpl(
      MetaSelectRepository metaSelectRepository,
      MetaSelectItemRepository metaSelectItemRepository,
      LanguageRepository languageRepository) {
    this.metaSelectRepository = metaSelectRepository;
    this.metaSelectItemRepository = metaSelectItemRepository;
    this.languageRepository = languageRepository;
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
    // find metaSelect object
    Query<MetaSelect> metaSelectQuery =
        metaSelectRepository.all().filter("self.name = :selectName");
    metaSelectQuery.bind("selectName", SELECT_NAME);
    MetaSelect metaSelect = metaSelectQuery.fetchOne();
    if (metaSelect == null) return;
    MetaSelectItem metaSelectItem;
    if (!isNew) {
      metaSelectItem = findMetaSelectItem(oldName, oldCode);
      if (metaSelectItem == null) return;
    } else {
      metaSelectItem = new MetaSelectItem();
    }
    metaSelectItem.setTitle(language.getName());
    metaSelectItem.setValue(language.getCode());
    metaSelectItem.setOrder((int) getCountMetaSelectItemFromQuery());
    metaSelect.addItem(metaSelectItem);
    metaSelectItemRepository.save(metaSelectItem);
  }

  /**
   * Find the metaSelectItem
   *
   * @param name
   * @param code
   * @return metaSelectItem
   */
  public MetaSelectItem findMetaSelectItem(String name, String code) {
    Query<MetaSelectItem> metaSelectItemQuery =
        metaSelectItemRepository.all().filter("self.title = :oldName and self.value = :oldCode");
    metaSelectItemQuery.bind("oldName", name).bind("oldCode", code);
    return metaSelectItemQuery.fetchOne();
  }

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

  @Transactional
  public void updateOrderNumber(int orderNumber) {
    //    javax.persistence.Query query =
    //        JPA.em()
    //            .createQuery(
    //                " update MetaSelectItem "
    //                    + " set order = order - 1 "
    //                    + " Where order > :orderNumber")
    //            .setParameter("orderNumber", orderNumber);

    javax.persistence.Query query =
        JPA.em()
            .createNativeQuery(
                "UPDATE meta_select_item "
                    + "SET order_seq = order_seq - 1 "
                    + "FROM meta_select "
                    + "WHERE meta_select.id = meta_select_item.select_id "
                    + "AND meta_select_item.order_seq > :orderNumber "
                    + "AND meta_select.name = :selectName ")
            .setParameter("orderNumber", orderNumber)
            .setParameter("selectName", SELECT_NAME);
  }
}
