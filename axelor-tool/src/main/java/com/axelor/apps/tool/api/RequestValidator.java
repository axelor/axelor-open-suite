/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.tool.api;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.ws.rs.BadRequestException;

public class RequestValidator {

  public static void validateBody(RequestStructure body) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    Set<ConstraintViolation<RequestStructure>> constraintViolations = validator.validate(body);

    if (constraintViolations.size() > 0) {
      StringBuilder errorMsg = new StringBuilder("Some constraints are invalid : ");
      for (ConstraintViolation<RequestStructure> constraint : constraintViolations) {
        errorMsg
            .append(constraint.getPropertyPath())
            .append(" ")
            .append(constraint.getMessage())
            .append("; ");
      }
      throw new BadRequestException(String.valueOf(errorMsg));
    }
  }

  public static void validateBody(RequestPostStructure body) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    Set<ConstraintViolation<RequestPostStructure>> constraintViolations = validator.validate(body);

    if (constraintViolations.size() > 0) {
      StringBuilder errorMsg = new StringBuilder("Some constraints are invalid : ");
      for (ConstraintViolation<RequestPostStructure> constraint : constraintViolations) {
        errorMsg
            .append(constraint.getPropertyPath())
            .append(" ")
            .append(constraint.getMessage())
            .append("; ");
      }
      throw new BadRequestException(String.valueOf(errorMsg));
    }
  }
}
