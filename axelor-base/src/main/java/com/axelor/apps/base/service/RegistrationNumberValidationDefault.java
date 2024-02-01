package com.axelor.apps.base.service;

public class RegistrationNumberValidationDefault extends RegistrationNumberValidation {
  public boolean computeRegistrationCodeValidity(String registrationCode) {
    return true;
  }
}
