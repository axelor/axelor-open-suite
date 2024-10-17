package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Tag;
import com.axelor.common.StringUtils;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;

public class TagServiceImpl implements TagService {

  protected MetaModelRepository metaModelRepository;

  @Inject
  public TagServiceImpl(MetaModelRepository metaModelRepository) {
    this.metaModelRepository = metaModelRepository;
  }

  @Override
  public void addMetaModelToTag(Tag tag, String fullName) {
    if (!StringUtils.isEmpty(fullName)) {
      MetaModel metaModel =
          metaModelRepository.all().filter("self.fullName = ?", fullName).fetchOne();

      if (metaModel != null) {
        tag.addConcernedModelSetItem(metaModel);
      }
    }
  }
}
