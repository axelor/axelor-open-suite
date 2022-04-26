package com.axelor.apps.tool.api;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

public class RequestValidator {

  public static void validateBody(RequestStructure body) {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    Set<ConstraintViolation<RequestStructure>> constraintViolations = validator.validate(body);

    if (constraintViolations.size() > 0) {
      System.out.println("Some constraints are invalid : ");
      for (ConstraintViolation<RequestStructure> contraintes : constraintViolations) {
        System.out.println(
            contraintes.getRootBeanClass().getSimpleName()
                + "."
                + contraintes.getPropertyPath()
                + " "
                + contraintes.getMessage());
      }
    }
  }
}
