package com.axelor.apps.base.service.app;

import com.axelor.db.mapper.Property;

public interface AnonymizeService {
  Object anonymizeValue(Object object, Property property);
}
