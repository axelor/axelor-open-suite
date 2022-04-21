package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.db.mapper.Property;
import com.axelor.exception.AxelorException;
import com.github.javafaker.Faker;

public interface AnonymizeService {
  Object anonymizeValue(
      Object object,
      Property property,
      boolean useFakeData,
      FakerApiField fakerApiField,
      Faker faker)
      throws AxelorException;
}
