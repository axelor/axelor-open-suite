/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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

import com.axelor.rpc.Context;
import org.apache.poi.ss.formula.functions.T;

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

  /**
   * Function that returns the object instance corresponding to a certain depth of parent contexts
   *
   * @param context The context from which to get the parent
   * @param klass The class of the desired parent
   * @param depth The depth from which to get the parent
   * @return The desired parent, or null if it doesn't exist or is of a different class
   */
  public static <T> T getContextParent(Context context, Class<T> klass, int depth) {
    for (int i = 0; i < depth; i++) {
      if (context.getParent() == null) {
        return null;
      } else {
        context = context.getParent();
      }
    }

    if (context.containsKey("_model") && context.get("_model").equals(klass.getName())) {
      return context.asType(klass);
    } else {
      return null;
    }
  }
}
