/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base;

import com.axelor.db.Model;

/**
 * New exception class to handle issues when sending/generating message in a workflow. The new
 * exception type will be used to avoid rollback and returns a warning to the user without blocking
 * the process.
 */
public class AxelorMessageException extends AxelorException {

  public AxelorMessageException(int category, String message, Object... messageArgs) {
    super(category, message, messageArgs);
  }

  public AxelorMessageException(int category, String message) {
    super(category, message);
  }

  public AxelorMessageException(Throwable cause, int category) {
    super(cause, category);
  }

  public AxelorMessageException(
      Throwable cause, int category, String message, Object... messageArgs) {
    super(cause, category, message, messageArgs);
  }

  public AxelorMessageException(Throwable cause, int category, String message) {
    super(cause, category, message);
  }

  public AxelorMessageException(
      Class<? extends Model> refClass, int category, String message, Object... messageArgs) {
    super(refClass, category, message, messageArgs);
  }

  public AxelorMessageException(Class<? extends Model> refClass, int category, String message) {
    super(refClass, category, message);
  }

  public AxelorMessageException(Throwable cause, Class<? extends Model> refClass, int category) {
    super(cause, refClass, category);
  }

  public AxelorMessageException(
      Throwable cause,
      Class<? extends Model> refClass,
      int category,
      String message,
      Object... messageArgs) {
    super(cause, refClass, category, message, messageArgs);
  }

  public AxelorMessageException(
      Throwable cause, Class<? extends Model> refClass, int category, String message) {
    super(cause, refClass, category, message);
  }

  public AxelorMessageException(Model ref, int category, String message, Object... messageArgs) {
    super(ref, category, message, messageArgs);
  }

  public AxelorMessageException(Model ref, int category, String message) {
    super(ref, category, message);
  }

  public AxelorMessageException(Throwable cause, Model ref, int category) {
    super(cause, ref, category);
  }

  public AxelorMessageException(
      Throwable cause, Model ref, int category, String message, Object... messageArgs) {
    super(cause, ref, category, message, messageArgs);
  }

  public AxelorMessageException(Throwable cause, Model ref, int category, String message) {
    super(cause, ref, category, message);
  }
}
