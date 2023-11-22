package com.axelor.apps.base.web;

import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class LanguageServiceImpl implements LanguageService {
  protected MetaSelectRepository metaSelectRepository;
  protected MetaSelectItemRepository metaSelectItemRepository;
  protected LanguageRepository languageRepository;

  @Transactional
  @Override
  public void convertToLowercase(Language language) {
    Language languageObj = Beans.get(LanguageRepository.class).find(language.getId());
    languageObj.setCode(languageObj.getCode().toLowerCase());
    languageRepository.save(languageObj);
  }

  @Inject
  public LanguageServiceImpl(
      MetaSelectRepository metaSelectRepository,
      MetaSelectItemRepository metaSelectItemRepository,
      LanguageRepository languageRepository) {
    this.metaSelectRepository = metaSelectRepository;
    this.metaSelectItemRepository = metaSelectItemRepository;
    this.languageRepository = languageRepository;
  }

  @Transactional
  @Override
  public void saveExistingLanguageObjectToMetaSelect(
      Language language, String oldName, String oldCode) {
    Query<MetaSelect> metaSelectQuery =
        metaSelectRepository.all().filter("self.name = :selectName");
    metaSelectQuery.bind("selectName", "select.language");
    List<MetaSelect> metaSelectList = metaSelectQuery.fetch();
    MetaSelect metaSelect = metaSelectList.get(0);

    Query<MetaSelectItem> metaSelectItemQuery =
        metaSelectItemRepository.all().filter("self.title = :oldName and self.value = :oldCode");
    metaSelectItemQuery.bind("oldName", oldName).bind("oldCode", oldCode);
    List<MetaSelectItem> metaSelectItemList = metaSelectItemQuery.fetch();
    MetaSelectItem metaSelectItem = metaSelectItemList.get(0);
    metaSelectItem.setTitle(language.getName());
    metaSelectItem.setValue(language.getCode());
    metaSelectItem.setSelect(metaSelect);
    metaSelect.addItem(metaSelectItem);
    metaSelectItemRepository.save(metaSelectItem);
    metaSelectRepository.save(metaSelect);
  }

  @Transactional
  @Override
  public void saveNewLanguageObjectToMetaSelect(Language language) {
    Query<MetaSelect> metaSelectQuery =
        metaSelectRepository.all().filter("self.name = :selectName");
    metaSelectQuery.bind("selectName", "select.language");
    List<MetaSelect> metaSelectList = metaSelectQuery.fetch();
    MetaSelect metaSelect = metaSelectList.get(0);

    MetaSelectItem metaSelectItem = new MetaSelectItem();
    metaSelectItem.setTitle(language.getName());
    metaSelectItem.setValue(language.getCode());
    metaSelectItem.setSelect(metaSelect);
    metaSelect.addItem(metaSelectItem);
    metaSelectItemRepository.save(metaSelectItem);
    metaSelectRepository.save(metaSelect);
  }

  @Transactional
  @Override
  public void removeLanguageLinkedMetaSelectItem(Language language) {
    MetaSelectItemRepository metaSelectItemRepository = Beans.get(MetaSelectItemRepository.class);
    Query<MetaSelectItem> metaSelectItemQuery =
        metaSelectItemRepository.all().filter("self.title = :oldName and self.value = :oldCode");
    metaSelectItemQuery.bind("oldName", language.getName()).bind("oldCode", language.getCode());
    List<MetaSelectItem> metaSelectItemList = metaSelectItemQuery.fetch();
    MetaSelectItem metaSelectItem = metaSelectItemList.get(0);
    metaSelectItemRepository.remove(metaSelectItem);
  }
}
