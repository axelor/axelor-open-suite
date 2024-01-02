package com.axelor.apps.base.service;

public class RegistrationNumberValidationFRA implements RegistrationNumberValidation{
    public boolean computeRegistrationCodeValidity(String registrationCode) {
        int sum = 0;
        boolean isOddNumber = true;
        registrationCode = registrationCode.replace(" ", "");
        if (registrationCode.length() != 14) {
            return false;
        }
        int i = registrationCode.length() - 1;
        while (i > -1) {
            int number = Character.getNumericValue(registrationCode.charAt(i));
            if (number < 0) {
                i--;
                continue;
            }
            if (!isOddNumber) {
                number *= 2;
            }
            if (number < 10) {
                sum += number;
            } else {
                number -= 10;
                sum += number + 1;
            }
            i--;
            isOddNumber = !isOddNumber;
        }
        return sum % 10 == 0;
    }
}
