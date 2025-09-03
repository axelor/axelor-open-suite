/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.project.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.project.db.Project;
import java.io.IOException;
import java.math.BigDecimal;
import org.codehaus.groovy.control.CompilationFailedException;

public interface UnitConversionForProjectService {

  /**
   * Convert a value from a unit to another
   *
   * @param startUnit The starting unit
   * @param endUnit The end unit
   * @param value The value to convert
   * @param scale The wanted scale of the result
   * @param project Optional, a project used for complex conversions. Input null if needless.
   * @return The converted value with the specified scale
   * @throws AxelorException
   */
  BigDecimal convert(Unit startUnit, Unit endUnit, BigDecimal value, int scale, Project project)
      throws AxelorException;

  /**
   * Get the conversion coefficient between two units from a conversion list. If the start unit and
   * the end unit can not be found in the list, then the units are swapped. If there still isn't any
   * result, an Exception is thrown.
   *
   * @param startUnit The start unit
   * @param endUnit The end unit
   * @param project Optional, a product used for complex conversions. Input null if needless.
   * @return A conversion coefficient to convert from startUnit to endUnit.
   * @throws AxelorException The required units are not found in the conversion list.
   * @throws CompilationFailedException
   * @throws ClassNotFoundException
   * @throws IOException
   */
  BigDecimal getCoefficient(Unit startUnit, Unit endUnit, Project project)
      throws AxelorException, CompilationFailedException, ClassNotFoundException, IOException;
}
