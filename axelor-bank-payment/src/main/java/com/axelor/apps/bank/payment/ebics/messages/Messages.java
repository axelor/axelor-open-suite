/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
package com.axelor.apps.bank.payment.ebics.messages;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A mean to manage application messages.
 *
 * @author Hachani
 *
 */
public class Messages {

  /**
   * Return the corresponding value of a given key and string parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param params the parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, String param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and integer parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param param the parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, int param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and parameters.
   * @param key the given key
   * @param bundleName the bundle name
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return resourceBundle.getString(key);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and string parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @param params the parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, Locale locale, String param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and integer parameter.
   * @param key the given key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @param param the parameter
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, Locale locale, int param) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return MessageFormat.format(resourceBundle.getString(key), param);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Return the corresponding value of a given key and parameters.
   * @param key the given key
   * @param bundleName the bundle name
   * @param locale the bundle locale
   * @return the corresponding key value
   */
  public static String getString(String key, String bundleName, Locale locale) {
    try {
      ResourceBundle		resourceBundle;

      resourceBundle = ResourceBundle.getBundle(bundleName, locale);
      return resourceBundle.getString(key);
    } catch (MissingResourceException e) {
      return "!!" + key + "!!";
    }
  }

  /**
   * Sets the default locale.
   * @param locale the locale
   */
  public static void setLocale(Locale locale) {
    Messages.locale = locale;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private static Locale					locale;
}
