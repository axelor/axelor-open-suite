/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import static com.axelor.apps.base.db.repo.FakerApiFieldParametersRepository.*;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.db.FakerApiFieldParameters;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import org.apache.commons.lang3.ClassUtils;

public class FakerServiceImpl implements FakerService {

  public static final String DATE_PATTERN = "yyyy-MM-dd";

  @Override
  public Object generateFakeData(FakerApiField fakerApiField) throws AxelorException {
    Faker faker = new Faker(new Locale(AuthUtils.getUser().getLanguage()));
    Object instance = null;
    try {

      Method field = faker.getClass().getMethod(fakerApiField.getClassName());
      instance = field.invoke(faker);

    } catch (NoSuchMethodException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_CLASS_DOES_NOT_EXIST),
          fakerApiField.getClassName());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_ERROR),
          fakerApiField.getClassName());
    }
    try {
      List<FakerApiFieldParameters> fakerApiFieldParameters =
          fakerApiField.getFakerFieldParametersList();

      Method method;
      if (fakerApiFieldParameters == null || fakerApiFieldParameters.isEmpty()) {
        method = instance.getClass().getMethod(fakerApiField.getMethodName());
        return method.invoke(instance);
      } else {
        Class[] params = convertParametersToParamsArray(fakerApiFieldParameters);
        Object[] paramValues = convertParametersToValueArray(fakerApiFieldParameters);
        method = instance.getClass().getMethod(fakerApiField.getMethodName(), params);
        return method.invoke(instance, paramValues);
      }

    } catch (NoSuchMethodException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_DOES_NOT_EXIST),
          fakerApiField.getMethodName());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_ERROR),
          fakerApiField.getMethodName());
    } catch (ClassNotFoundException | ParseException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_PARAMS_ERROR),
          fakerApiField.getMethodName());
    }
  }

  /**
   * Called to check if the faker field configuration is correct if correct, return an example
   * output
   *
   * @param fakerApiField
   * @return an example output
   */
  @Override
  public String checkMethod(FakerApiField fakerApiField) throws AxelorException {
    Faker faker = new Faker(new Locale(AuthUtils.getUser().getLanguage()));

    Object instance = getClassMethod(faker, fakerApiField);

    if (instance == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_CLASS_NAME_ERROR));
    }

    if (Arrays.stream(instance.getClass().getDeclaredMethods())
        .noneMatch(method -> method.getName().equals(fakerApiField.getMethodName()))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_NAME_ERROR));
    }
    return checkParams(instance, fakerApiField);
  }

  /**
   * converts FakerApiFieldParameters to an array of params to be used when getting method with
   * reflection
   *
   * @param fakerApiFieldParameters
   * @return
   * @throws ClassNotFoundException
   */
  protected Class[] convertParametersToParamsArray(
      List<FakerApiFieldParameters> fakerApiFieldParameters) throws ClassNotFoundException {
    Class[] params = new Class[fakerApiFieldParameters.size()];
    int i = 0;
    for (FakerApiFieldParameters fieldParameters : fakerApiFieldParameters) {
      params[i] = ClassUtils.getClass(fieldParameters.getParamType());
      i++;
    }
    return params;
  }

  /**
   * converts FakerApiFieldParameters to an array of values to be used when invoking method with
   * reflection
   *
   * @param fakerApiFieldParameters
   * @return
   */
  protected Object[] convertParametersToValueArray(
      List<FakerApiFieldParameters> fakerApiFieldParameters)
      throws ParseException, IllegalArgumentException {
    Object[] paramValues = new Object[fakerApiFieldParameters.size()];
    int i = 0;
    for (FakerApiFieldParameters fieldParameters : fakerApiFieldParameters) {
      paramValues[i] = convertValueToType(fieldParameters);
      i++;
    }
    return paramValues;
  }

  /**
   * cast String params into their real type (only primitive and String supported)
   *
   * @param fieldParameters
   * @return
   */
  protected Object convertValueToType(FakerApiFieldParameters fieldParameters)
      throws ParseException, IllegalArgumentException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
    switch (fieldParameters.getParamType()) {
      case FAKER_API_FIELD_PARAM_TYPE_INT:
        return Integer.parseInt(fieldParameters.getParamValue());
      case FAKER_API_FIELD_PARAM_TYPE_DOUBLE:
        return Double.parseDouble(fieldParameters.getParamValue());
      case FAKER_API_FIELD_PARAM_TYPE_LONG:
        return Long.parseLong(fieldParameters.getParamValue());
      case FAKER_API_FIELD_PARAM_TYPE_BOOLEAN:
        return Boolean.parseBoolean(fieldParameters.getParamValue());
      case FAKER_API_FIELD_PARAM_TYPE_TIMEUNIT:
        return TimeUnit.valueOf(fieldParameters.getParamValue());
      case FAKER_API_FIELD_PARAM_TYPE_DATE:
        return simpleDateFormat.parse(fieldParameters.getParamValue());
      case FAKER_API_FIELD_PARAM_TYPE_TIMESTAMP:
        Date parsedDate = simpleDateFormat.parse(fieldParameters.getParamValue());
        return new Timestamp(parsedDate.getTime());
      default:
        return fieldParameters.getParamValue();
    }
  }

  /**
   * use Reflection to get the class method (first level of faker)
   *
   * @param faker
   * @param fakerApiField
   * @return
   */
  protected Object getClassMethod(Faker faker, FakerApiField fakerApiField) {
    try {
      Method classMethod = faker.getClass().getDeclaredMethod(fakerApiField.getClassName());
      return classMethod.invoke(faker);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      return null;
    }
  }

  /**
   * check if params are well configured, if ok, output an example of result
   *
   * @param classMethod
   * @param fakerApiField
   * @return an example output
   */
  public String checkParams(Object classMethod, FakerApiField fakerApiField)
      throws AxelorException {
    List<FakerApiFieldParameters> fakerApiFieldParameters =
        fakerApiField.getFakerFieldParametersList();

    try {
      Method fakerMethod;
      if (fakerApiFieldParameters == null || fakerApiFieldParameters.isEmpty()) {
        fakerMethod = classMethod.getClass().getMethod(fakerApiField.getMethodName());
        return fakerMethod.invoke(classMethod).toString();
      } else if (fakerApiFieldParameters.stream()
          .anyMatch(
              fieldParameters ->
                  fieldParameters.getParamValue() == null
                      || fieldParameters.getParamType() == null)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BaseExceptionMessage.FAKER_METHOD_MISSING_PARAMS));
      }

      Class[] params = convertParametersToParamsArray(fakerApiFieldParameters);
      Object[] paramValues = convertParametersToValueArray(fakerApiFieldParameters);
      fakerMethod = classMethod.getClass().getMethod(fakerApiField.getMethodName(), params);
      return fakerMethod.invoke(classMethod, paramValues).toString();

    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_CONFIGURATION_ERROR));
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_PARAMETERS_CONFIGURATION_ERROR));
    } catch (ParseException | IllegalArgumentException e) {
      throw new AxelorException(
          e,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.FAKER_METHOD_PARAMETERS_VALUE_ERROR));
    }
  }
}
