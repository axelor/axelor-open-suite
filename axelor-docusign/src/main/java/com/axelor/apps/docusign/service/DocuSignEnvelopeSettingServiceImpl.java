package com.axelor.apps.docusign.service;

import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.google.inject.persist.Transactional;
import java.util.List;

public class DocuSignEnvelopeSettingServiceImpl implements DocuSignEnvelopeSettingService {

  @Override
  @Transactional
  public void addItemToReferenceSelection(MetaModel model) {
    MetaSelect metaSelect =
        Beans.get(MetaSelectRepository.class).findByName("docusign.envelope.related.to.select");
    List<MetaSelectItem> items = metaSelect.getItems();
    if (items != null && !items.stream().anyMatch(x -> x.getValue().equals(model.getFullName()))) {
      MetaSelectItem metaSelectItem = new MetaSelectItem();
      metaSelectItem.setTitle(model.getName());
      metaSelectItem.setValue(model.getFullName());
      metaSelectItem.setSelect(metaSelect);
      Beans.get(MetaSelectItemRepository.class).save(metaSelectItem);
    }
  }
}
