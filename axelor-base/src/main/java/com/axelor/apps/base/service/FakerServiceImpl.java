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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.FakerApiField;
import com.axelor.apps.base.db.FakerApiFieldParameters;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.AdminExceptionMessage;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ActionResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.datafaker.Faker;
import org.apache.commons.lang3.ClassUtils;

public class FakerServiceImpl implements FakerService {

  public static final String KEY_SUCCESS = "success";
  public static final String KEY_ERROR = "error";

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
      List<FakerApiFieldParameters> fakerApiFieldParameters =
          fakerApiField.getFakerFieldParametersList();

      Method method;
      if (fakerApiFieldParameters == null || fakerApiFieldParameters.isEmpty()) {
        method = instance.getClass().getMethod(fakerApiField.getMethodName());
        return method.invoke(instance).toString();
      } else {
        Class[] params = convertParametersToParamsArray(fakerApiFieldParameters);
        Object[] paramValues = convertParametersToValueArray(fakerApiFieldParameters);
        method = instance.getClass().getMethod(fakerApiField.getMethodName(), params);
        return method.invoke(instance, paramValues).toString();
      }

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
    } catch (ClassNotFoundException | ParseException e) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AdminExceptionMessage.FAKER_METHOD_PARAMS_ERROR),
          fakerApiField.getMethodName());
    }
  }

  /**
   * Called to check if the faker field configuration is correct
   *
   * @param fakerApiField
   * @param response
   */
  @Override
  public void checkMethod(FakerApiField fakerApiField, ActionResponse response) {
    Faker faker = new Faker(new Locale(AuthUtils.getUser().getLanguage()));

    Object instance = getClassMethod(faker, fakerApiField);

    if (instance == null) {
      response.setError(I18n.get("Error in class name. Please check."));
      return;
    }

    if (Arrays.stream(instance.getClass().getDeclaredMethods())
        .noneMatch(method -> method.getName().equals(fakerApiField.getMethodName()))) {
      response.setError(I18n.get("Error in method name. Please check."));
      return;
    }
    Map<String, String> results = checkParams(instance, fakerApiField);

    if (results.containsKey(KEY_ERROR)) {
      response.setError(results.get(KEY_ERROR));
    } else if (results.containsKey(KEY_SUCCESS)) {
      response.setAlert(results.get(KEY_SUCCESS));
    }
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
      List<FakerApiFieldParameters> fakerApiFieldParameters) throws ParseException {
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
      throws ParseException {
    switch (fieldParameters.getParamType()) {
      case "int":
        return Integer.parseInt(fieldParameters.getParamValue());
      case "double":
        return Double.parseDouble(fieldParameters.getParamValue());
      case "long":
        return Long.parseLong(fieldParameters.getParamValue());
      case "boolean":
        return Boolean.parseBoolean(fieldParameters.getParamValue());
      case "java.util.concurrent.TimeUnit":
        return TimeUnit.valueOf(fieldParameters.getParamValue());
      case "java.util.Date":
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        return simpleDateFormat.parse(fieldParameters.getParamValue());
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
   * @return
   */
  public Map<String, String> checkParams(Object classMethod, FakerApiField fakerApiField) {
    Map<String, String> resultMessage = new HashMap<>();

    List<FakerApiFieldParameters> fakerApiFieldParameters =
        fakerApiField.getFakerFieldParametersList();

    try {
      Method fakerMethod;
      if (fakerApiFieldParameters == null || fakerApiFieldParameters.isEmpty()) {
        fakerMethod = classMethod.getClass().getMethod(fakerApiField.getMethodName());
        resultMessage.put(
            KEY_SUCCESS,
            String.format(
                I18n.get("The faker API field is valide. An example output is : %s."),
                fakerMethod.invoke(classMethod).toString()));
      } else if (fakerApiFieldParameters.stream()
          .anyMatch(
              fieldParameters ->
                  fieldParameters.getParamValue() == null
                      || fieldParameters.getParamType() == null)) {

        resultMessage.put(
            KEY_ERROR, I18n.get("Please check yours params, both fields must be filled."));
      } else {

        Class[] params = convertParametersToParamsArray(fakerApiFieldParameters);
        Object[] paramValues = convertParametersToValueArray(fakerApiFieldParameters);
        fakerMethod = classMethod.getClass().getMethod(fakerApiField.getMethodName(), params);
        resultMessage.put(
            KEY_SUCCESS,
            String.format(
                I18n.get("The faker API field is valide. An example output is : %s."),
                fakerMethod.invoke(classMethod, paramValues).toString()));
      }
    } catch (InvocationTargetException | IllegalAccessException e) {
      TraceBackService.trace(e);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      resultMessage.put(KEY_ERROR, I18n.get("Please check your parameters configuration."));
    } catch (ParseException e) {
      resultMessage.put(KEY_ERROR, I18n.get("Please check your parameters value format."));
    }

    return resultMessage;
  }
}
