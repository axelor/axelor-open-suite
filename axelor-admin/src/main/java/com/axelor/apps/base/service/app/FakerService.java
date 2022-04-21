package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.exception.AxelorException;
import com.github.javafaker.Faker;

public interface FakerService {
  String generateFakeData(Faker faker, FakerApiField fakerApiField) throws AxelorException;
}
