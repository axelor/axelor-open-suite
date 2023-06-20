package com.axelor.apps.gdpr.service.response;

import com.axelor.apps.gdpr.db.GDPRDataToExcludeConfig;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import java.util.List;

public interface GdprDataToExcludeService {

  boolean isModelExcluded(List<GDPRDataToExcludeConfig> dataToExcludeConfig, MetaModel model);

  List<MetaField> getFieldsToExclude(
      List<GDPRDataToExcludeConfig> dataToExcludeConfig, MetaModel model);
}
