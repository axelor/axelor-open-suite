package com.axelor.apps.tool;

import com.google.common.base.Strings;

public class ComputeNameTool {

  public static String computeSimpleFullName(String firstName, String lastName, Long id) {
    if (!Strings.isNullOrEmpty(lastName) && !Strings.isNullOrEmpty(firstName)) {
      return lastName + " " + firstName;
    } else if (!Strings.isNullOrEmpty(lastName)) {
      return lastName;
    } else if (!Strings.isNullOrEmpty(firstName)) {
      return firstName;
    } else {
      return "" + id;
    }
  }

  public static String computeFullName(
      String firstName, String lastName, String sequence, Long id) {
    if (!Strings.isNullOrEmpty(sequence)) {
      return sequence + " - " + lastName + " " + firstName;
    }
    return computeSimpleFullName(firstName, lastName, id);
  }
}
