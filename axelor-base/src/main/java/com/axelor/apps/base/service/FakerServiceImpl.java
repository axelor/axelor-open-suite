/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.AdminExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import net.datafaker.Faker;

public class FakerServiceImpl implements FakerService {
  @Override
  public String generateFakeData(FakerApiField fakerApiField) throws AxelorException {
    Faker faker = new Faker(new Locale(AuthUtils.getUser().getLanguage()));
    Object instance = null;
    try {

      Method field = faker.getClass().getMethod(fakerApiField.getClassName());
      instance = field.invoke(faker);

    } catch (NoSuchMethodException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AdminExceptionMessage.FAKER_CLASS_DOES_NOT_EXIST),
          fakerApiField.getClassName());

    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AdminExceptionMessage.FAKER_METHOD_ERROR),
          fakerApiField.getClassName());
    }
    try {

      Method method = instance.getClass().getMethod(fakerApiField.getMethodName());
      return method.invoke(instance).toString();

    } catch (NoSuchMethodException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AdminExceptionMessage.FAKER_METHOD_DOES_NOT_EXIST),
          fakerApiField.getMethodName());

    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AdminExceptionMessage.FAKER_METHOD_ERROR),
          fakerApiField.getMethodName());
    }
  }
}
