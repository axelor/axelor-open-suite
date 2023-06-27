package com.axelor.apps.base.service;

import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;

public interface AnonymizerLineService {

  String getFakerApiFieldDomain(MetaField metaField, MetaJsonField metaJsonField);
}
