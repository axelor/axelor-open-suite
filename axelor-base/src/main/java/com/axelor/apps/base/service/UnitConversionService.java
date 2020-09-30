/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.UnitConversion;
import com.axelor.apps.base.db.repo.UnitConversionRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.tool.template.TemplateMaker;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class UnitConversionService {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final char TEMPLATE_DELIMITER = '$';
  private static final int DEFAULT_COEFFICIENT_SCALE = 12;
  protected TemplateMaker maker;

  @Inject protected AppBaseService appBaseService;

  @Inject protected UnitConversionRepository unitConversionRepo;

  /**
   * Convert a value from a unit to another
   *
   * @param startUnit The starting unit
   * @param endUnit The end unit
   * @param value The value to convert
   * @param scale The wanted scale of the result
   * @param product Optional, a product used for complex conversions. Input null if needless.
   * @return The converted value with the specified scale
   * @throws AxelorException
   */
  public BigDecimal convert(
      Unit startUnit, Unit endUnit, BigDecimal value, int scale, Product product)
      throws AxelorException {

    if ((startUnit == null && endUnit == null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.UNIT_CONVERSION_3));
    }

    if (startUnit == null && endUnit != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.UNIT_CONVERSION_2));
    }

    if (endUnit == null && startUnit != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.UNIT_CONVERSION_4));
    }

    if (startUnit.equals(endUnit)) return value;
    else {
      try {
        BigDecimal coefficient =
            this.getCoefficient(unitConversionRepo.all().fetch(), startUnit, endUnit, product);

        return value.multiply(coefficient).setScale(scale, RoundingMode.HALF_EVEN);
      } catch (IOException | ClassNotFoundException e) {
        TraceBackService.trace(e);
      }
    }
    return value;
  }

  /**
   * Get the conversion coefficient between two units from a conversion list. If the start unit and
   * the end unit can not be found in the list, then the units are swapped. If there still isn't any
   * result, an Exception is thrown.
   *
   * @param unitConversionList A list of conversions between units
   * @param startUnit The start unit
   * @param endUnit The end unit
   * @param product Optional, a product used for complex conversions. INput null if needless.
   * @return A conversion coefficient to convert from startUnit to endUnit.
   * @throws AxelorException The required units are not found in the conversion list.
   * @throws CompilationFailedException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  public BigDecimal getCoefficient(
      List<? extends UnitConversion> unitConversionList,
      Unit startUnit,
      Unit endUnit,
      Product product)
      throws AxelorException, CompilationFailedException, ClassNotFoundException, IOException {
    /* Looking for the start unit and the end unit in the unitConversionList to get the coefficient */
    if (product != null) {
      this.maker =
          new TemplateMaker(
                  AuthUtils.getUser().getActiveCompany() != null ? AuthUtils.getUser().getActiveCompany().getTimezone() : "",
              Locale.FRENCH,
              TEMPLATE_DELIMITER,
              TEMPLATE_DELIMITER);
      this.maker.setContext(product, "Product");
    }
    String eval = null;
    for (UnitConversion unitConversion : unitConversionList) {

      if (unitConversion.getStartUnit().equals(startUnit)
          && unitConversion.getEndUnit().equals(endUnit)) {
        if (unitConversion.getTypeSelect() == UnitConversionRepository.TYPE_COEFF) {
          return unitConversion.getCoef();
        } else if (product != null) {
          maker.setTemplate(unitConversion.getFormula());
          eval = maker.make();
          CompilerConfiguration conf = new CompilerConfiguration();
          ImportCustomizer customizer = new ImportCustomizer();
          customizer.addStaticStars("java.lang.Math");
          conf.addCompilationCustomizers(customizer);
          Binding binding = new Binding();
          GroovyShell shell = new GroovyShell(binding, conf);
          return new BigDecimal(shell.evaluate(eval).toString());
        }
      }

      /* The endUnit become the start unit and the startUnit become the end unit */

      if (unitConversion.getStartUnit().equals(endUnit)
          && unitConversion.getEndUnit().equals(startUnit)) {
        if (unitConversion.getTypeSelect() == UnitConversionRepository.TYPE_COEFF
            && unitConversion.getCoef().compareTo(BigDecimal.ZERO) != 0) {
          return BigDecimal.ONE.divide(
              unitConversion.getCoef(), DEFAULT_COEFFICIENT_SCALE, RoundingMode.HALF_EVEN);
        } else if (product != null) {
          maker.setTemplate(unitConversion.getFormula());
          eval = maker.make();
          CompilerConfiguration conf = new CompilerConfiguration();
          ImportCustomizer customizer = new ImportCustomizer();
          customizer.addStaticStars("java.lang.Math");
          conf.addCompilationCustomizers(customizer);
          Binding binding = new Binding();
          GroovyShell shell = new GroovyShell(binding, conf);
          BigDecimal result = new BigDecimal(shell.evaluate(eval).toString());
          if (result.compareTo(BigDecimal.ZERO) != 0) {
            return BigDecimal.ONE.divide(result, DEFAULT_COEFFICIENT_SCALE, RoundingMode.HALF_EVEN);
          }
        }
      }
    }
    /* If there is no startUnit and endUnit in the UnitConversion list so we throw an exception */
    throw new AxelorException(
        TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
        I18n.get(IExceptionMessage.UNIT_CONVERSION_1),
        startUnit.getName(),
        endUnit.getName());
  }
}
