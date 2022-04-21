package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;

public interface AnonymizeService {
  /**
   * return a hash, hard-coded value or fake generated value depending on the type of the object
   * given.
   *
   * @param object
   * @param property
   * @param useFakeData
   * @param fakerApiField
   * @return
   * @throws AxelorException if an error occurs when generating a fake value.
   */
  Object anonymizeValue(
      Object object, Property property, boolean useFakeData, FakerApiField fakerApiField)
      throws AxelorException;
}
