package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.exception.AxelorException;
import com.github.javafaker.Faker;

public interface FakerService {
  /**
   * Generate fake values depending on the class and method name given with the Faker API .
   *
   * @param faker
   * @param fakerApiField
   * @return
   * @throws AxelorException if the class or method given doesn't exist.
   */
  String generateFakeData(Faker faker, FakerApiField fakerApiField) throws AxelorException;
}
