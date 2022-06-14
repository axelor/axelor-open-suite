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
