/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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

public final class ContextTool {

  public static String SPAN_CLASS_WARNING = "label-warning";
  public static String SPAN_CLASS_IMPORTANT = "label-important";

  /**
   * Function that split a message that we want display in a label to avoid to exceed the panel and
   * create a popup size issue
   *
   * @param message The message to format
   * @param spanClass The span class (label-warning, label-important...)
   * @param length The max length of the message
   * @return
   */
  public static String formatLabel(String message, String spanClass, int length) {
    if (message.length() > 80) {
      String formattedMessage =
          String.format(
              "<span class='label %s'>%s</span>", spanClass, message.substring(0, length));
      formattedMessage +=
          String.format(
              "<br/><span class='label %s'>%s</span>", spanClass, message.substring(length));
      return formattedMessage;
    } else {
      return String.format("<span class='label %s'>%s</span>", spanClass, message);
    }
  }
}
