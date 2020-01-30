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
package com.axelor.apps.tool;

import com.axelor.db.Model;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public final class StringTool {
  private static final String[] FILENAME_SEARCH_LIST =
      new String[] {"*", "\"", "/", "\\", "?", "%", ":", "|", "<", ">"};
  private static final String[] FILENAME_REPLACEMENT_LIST =
      new String[] {"#", "'", "_", "_", "_", "_", "_", "_", "_", "_"};

  private StringTool() {}

  /**
   * First character lower
   *
   * @param string
   * @return
   */
  public static String toFirstLower(String string) {

    return string.replaceFirst("\\w", Character.toString(string.toLowerCase().charAt(0)));
  }

  /**
   * First character upper.
   *
   * @param string
   * @return
   */
  public static String toFirstUpper(String string) {

    return string.replaceFirst("\\w", Character.toString(string.toUpperCase().charAt(0)));
  }

  /**
   * Complete string with fixed length.
   *
   * @param s
   * @param fillChar
   * @param size
   * @return
   */
  public static String fillStringRight(String s, char fillChar, int size) {

    String string = s;

    if (string.length() < size) {
      string += fillString(fillChar, size - string.length());
    } else {
      string = truncRight(string, size);
    }

    return string;
  }

  /**
   * Complete string with fixed length.
   *
   * @param s
   * @param fillChar
   * @param size
   * @return
   */
  public static String fillStringLeft(String s, char fillChar, int size) {

    String string = s;

    if (string.length() < size) {
      string = fillString(fillChar, size - string.length()) + string;
    } else {
      string = truncLeft(string, size);
    }

    return string;
  }

  /**
   * Truncate string with the first chars at size.
   *
   * @param s
   * @param size
   * @return
   */
  public static String truncRight(String s, int size) {

    String string = s;

    if (string.length() > size) {
      string = string.substring(0, size);
    }

    return string;
  }

  /**
   * Truncate string with the s length subtract size at s length.
   *
   * @param s
   * @param size
   * @return
   */
  public static String truncLeft(String s, int size) {

    String string = s;

    if (string.length() > size) {
      string = string.substring(string.length() - size, string.length());
    }

    return string;
  }

  /**
   * Create string with one char * count.
   *
   * @param fillChar
   * @param count
   * @return
   */
  public static String fillString(char fillChar, int count) {

    // creates a string of 'x' repeating characters
    char[] chars = new char[count];
    java.util.Arrays.fill(chars, fillChar);
    return new String(chars);
  }

  public static String deleteAccent(String s) {

    String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
    return temp.replaceAll("[^\\p{ASCII}]", "");
  }

  public static boolean equalsIgnoreCaseAndAccents(String s1, String s2) {
    return deleteAccent(s1).equalsIgnoreCase(deleteAccent(s2));
  }

  /**
   * Check if string s contain only digital character
   *
   * @param s
   * @return
   */
  public static boolean isDigital(String s) {

    for (Character c : s.toCharArray()) {

      if (!Character.isDigit(c)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Extract a string from right
   *
   * @param string
   * @param startIndex
   * @param lenght
   * @return
   */
  public static String extractStringFromRight(String string, int startIndex, int lenght) {
    String extractString = "";

    try {

      if (string != null
          && startIndex >= 0
          && lenght >= 0
          && string.length() - startIndex + lenght <= string.length()) {
        extractString =
            string.substring(string.length() - startIndex, string.length() - startIndex + lenght);
      }
    } catch (Exception ex) {
      return "";
    }

    return extractString;
  }

  /**
   * Fonction permettant de convertir un tableau de bytes en une chaine hexadécimale Convert to
   * hexadecimal string from bytes table
   *
   * @param bytes
   * @return
   */
  public static String getHexString(byte[] bytes) {

    StringBuilder sb = new StringBuilder(bytes.length * 2);
    String s = "";

    for (byte b : bytes) {

      s = String.format("%x", b);
      if (s.length() == 1) {
        sb.append('0');
      }
      sb.append(s);
    }

    return sb.toString();
  }

  /**
   * Fonction permettant de mettre la première lettre d'une chaine de caractère en majuscule
   *
   * @param s
   * @return
   */
  public static String capitalizeFirstLetter(String s) {
    if (s == null) {
      return null;
    }
    if (s.length() == 0) {
      return s;
    }
    StringBuilder result = new StringBuilder(s);
    result.replace(0, 1, result.substring(0, 1).toUpperCase());
    return result.toString();
  }

  /**
   * Get a list of integers from a string.
   *
   * @param string
   * @return list of integers
   */
  public static List<Integer> getIntegerList(String string) {
    return string != null
        ? Arrays.stream(string.split("\\D+")).map(Integer::valueOf).collect(Collectors.toList())
        : new ArrayList<>();
  }

  /**
   * Retrieve an ID list string from a collection of model objects. If the collection is empty, this
   * method will return "0".
   *
   * @param collection
   * @return
   */
  public static String getIdListString(Collection<? extends Model> collection) {
    List<Long> idList = new ArrayList<>();
    String idString;
    if (collection.isEmpty()) {
      idString = "0";
    } else {
      for (Model item : collection) {
        if (item != null) {
          idList.add(item.getId());
        }
      }
      idString = idList.stream().map(l -> l.toString()).collect(Collectors.joining(","));
    }
    return idString;
  }

  public static String getFilename(String name) {
    return StringUtils.replaceEach(name, FILENAME_SEARCH_LIST, FILENAME_REPLACEMENT_LIST);
  }

  /**
   * Some strings cannot be over 255 char because of database restriction. Cut it to 252 char then
   * add "..." to indicate the string has been cut.
   *
   * @return the cut string
   */
  public static String cutTooLongString(String str) {
    int defaultDbStrLength = 255;
    String fillString = "...";
    if (str.length() > defaultDbStrLength) {
      return str.substring(0, defaultDbStrLength - fillString.length()) + fillString;
    } else {
      return str;
    }
  }
}
