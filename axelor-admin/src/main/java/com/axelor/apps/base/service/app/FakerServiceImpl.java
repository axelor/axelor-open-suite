package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.exceptions.IExceptionMessages;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.github.javafaker.Faker;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class FakerServiceImpl implements FakerService {
  @Override
  public String generateFakeData(Faker faker, FakerApiField fakerApiField) throws AxelorException {

    Object instance = null;
    try {

      Method field = faker.getClass().getMethod(fakerApiField.getClassName());
      instance = field.invoke(faker);

    } catch (NoSuchMethodException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.FAKER_CLASS_DOES_NOT_EXIST),
          fakerApiField.getClassName());

    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.FAKER_METHOD_ERROR),
          fakerApiField.getClassName());
    }
    try {

      Method method = instance.getClass().getMethod(fakerApiField.getMethodName());
      return method.invoke(instance).toString();

    } catch (NoSuchMethodException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.FAKER_METHOD_DOES_NOT_EXIST),
          fakerApiField.getMethodName());

    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessages.FAKER_METHOD_ERROR),
          fakerApiField.getMethodName());
    }
  }
}
