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
package com.axelor.exception;

import com.axelor.db.EntityHelper;
import com.axelor.db.Model;

/** Exception specific to Axelor. */
public class AxelorException extends Exception {

  private static final long serialVersionUID = 1028105628735355226L;

  private final int category;
  private final Class<? extends Model> refClass;
  private final Long refId;

  /** Default constructor */
  @Deprecated
  public AxelorException() {
    this.refClass = null;
    this.refId = 0L;
    this.category = 0;
  }

  /**
   * Create an exception with his message and his type.
   *
   * @param message The exception message
   * @param category
   *     <ul>
   *       <li>1: Missing field
   *       <li>2: No unique key
   *       <li>3: No value
   *       <li>4: configuration error
   *       <li>5: CATEGORY_INCONSISTENCY
   *     </ul>
   */
  @Deprecated
  public AxelorException(String message, int category, Object... messageArgs) {
    this(category, String.format(message, messageArgs));
  }

  /**
   * Create an exception with a category and a message.
   *
   * @param category
   * @param message
   * @param messageArgs
   */
  public AxelorException(int category, String message, Object... messageArgs) {
    this(category, String.format(message, messageArgs));
  }

  public AxelorException(int category, String message) {
    super(message);
    this.refClass = null;
    this.refId = 0L;
    this.category = category;
  }

  /**
   * Create an exception with his cause and his type.
   *
   * @param cause The exception cause
   * @param category
   *     <ul>
   *       <li>1: Missing field
   *       <li>2: No unique key
   *       <li>3: No value
   *       <li>4: configuration error
   *       <li>5: CATEGORY_INCONSISTENCY
   *     </ul>
   *
   * @see Throwable
   */
  public AxelorException(Throwable cause, int category) {
    super(cause);
    this.refClass = null;
    this.refId = 0L;
    this.category = category;
  }

  /**
   * Create an exception with his message, his cause and his type.
   *
   * @param message The exception message
   * @param cause The exception cause
   * @param category The exception category
   *     <ul>
   *       <li>1: Missing field
   *       <li>2: No unique key
   *       <li>3: No value
   *       <li>4: configuration error
   *       <li>5: CATEGORY_INCONSISTENCY
   *     </ul>
   *
   * @see Throwable
   */
  @Deprecated
  public AxelorException(String message, Throwable cause, int category, Object... messageArgs) {
    this(cause, category, String.format(message, messageArgs));
  }

  /**
   * Create an exception with a cause, a category, and a message.
   *
   * @param cause
   * @param category
   * @param message
   * @param messageArgs
   */
  public AxelorException(Throwable cause, int category, String message, Object... messageArgs) {
    this(cause, category, String.format(message, messageArgs));
  }

  public AxelorException(Throwable cause, int category, String message) {
    super(message, cause);
    this.refClass = null;
    this.refId = 0L;
    this.category = category;
  }

  /**
   * Create an exception with a reference class, a category, and a message.
   *
   * @param refClass
   * @param category
   * @param message
   * @param messageArgs
   */
  public AxelorException(
      Class<? extends Model> refClass, int category, String message, Object... messageArgs) {
    this(refClass, category, String.format(message, messageArgs));
  }

  public AxelorException(Class<? extends Model> refClass, int category, String message) {
    super(message);
    this.refClass = refClass;
    this.refId = 0L;
    this.category = category;
  }

  /**
   * Create an exception with a cause, a reference class, and a category.
   *
   * @param cause
   * @param refClass
   * @param category
   */
  public AxelorException(Throwable cause, Class<? extends Model> refClass, int category) {
    super(cause);
    this.refClass = refClass;
    this.refId = 0L;
    this.category = category;
  }

  /**
   * Create an exception with a cause, a reference class, a category, and a message.
   *
   * @param cause
   * @param refClass
   * @param category
   * @param message
   * @param messageArgs
   */
  public AxelorException(
      Throwable cause,
      Class<? extends Model> refClass,
      int category,
      String message,
      Object... messageArgs) {
    this(cause, refClass, category, String.format(message, messageArgs));
  }

  public AxelorException(
      Throwable cause, Class<? extends Model> refClass, int category, String message) {
    super(message, cause);
    this.refClass = refClass;
    this.refId = 0L;
    this.category = category;
  }

  /**
   * Create an exception with a reference, a category, and a message.
   *
   * @param ref
   * @param category
   * @param message
   * @param messageArgs
   */
  public AxelorException(Model ref, int category, String message, Object... messageArgs) {
    this(ref, category, String.format(message, messageArgs));
  }

  public AxelorException(Model ref, int category, String message) {
    super(message);
    this.refClass = EntityHelper.getEntityClass(ref);
    this.refId = ref.getId();
    this.category = category;
  }

  /**
   * Create an exception with a cause, a reference, and a category.
   *
   * @param cause
   * @param ref
   * @param category
   */
  public AxelorException(Throwable cause, Model ref, int category) {
    super(cause);
    this.refClass = EntityHelper.getEntityClass(ref);
    this.refId = ref.getId();
    this.category = category;
  }

  /**
   * Create an exception with a cause, a reference, a category, and a message.
   *
   * @param cause
   * @param ref
   * @param category
   * @param message
   * @param messageArgs
   */
  public AxelorException(
      Throwable cause, Model ref, int category, String message, Object... messageArgs) {
    this(cause, ref, category, String.format(message, messageArgs));
  }

  public AxelorException(Throwable cause, Model ref, int category, String message) {
    super(message, cause);
    this.refClass = EntityHelper.getEntityClass(ref);
    this.refId = ref.getId();
    this.category = category;
  }

  /**
   * Get the category of exception
   *
   * @return
   *     <ul>
   *       <li>1: Missing field
   *       <li>2: No unique key
   *       <li>3: No value
   *       <li>4: configuration error
   *       <li>5: CATEGORY_INCONSISTENCY
   *     </ul>
   */
  public int getCategory() {
    return category;
  }

  /**
   * Get reference class.
   *
   * @return
   */
  public Class<? extends Model> getRefClass() {
    return refClass;
  }

  /**
   * Get reference ID.
   *
   * @return
   */
  public Long getRefId() {
    return refId;
  }
}
