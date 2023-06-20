package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRDataToExcludeConfig;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GdprDataToExcludeServiceImpl implements GdprDataToExcludeService {

  @Override
  public boolean isModelExcluded(
      List<GDPRDataToExcludeConfig> dataToExcludeConfig, MetaModel model) {
    return dataToExcludeConfig.stream()
        .map(GDPRDataToExcludeConfig::getMetaModel)
        .anyMatch(metaModel -> metaModel.equals(model));
  }

  @Override
  public List<MetaField> getFieldsToExclude(
      List<GDPRDataToExcludeConfig> dataToExcludeConfig, MetaModel model) {
    return dataToExcludeConfig.stream()
        .filter(gdprDataToExcludeConfig -> gdprDataToExcludeConfig.getMetaModel().equals(model))
        .map(GDPRDataToExcludeConfig::getMetaFields)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
