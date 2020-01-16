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
package com.axelor.apps.bankpayment.ebics.client;

import java.io.Serializable;
import java.util.Locale;

/**
 * Optional information about the client product.
 *
 * @author Hachani
 */
public class EbicsProduct implements Serializable {

  /**
   * Creates a new product information element.
   *
   * @param name this is the name of the product. It is a mandatory field.
   * @param language this is the language. If you use null, the language of the default locale is
   *     used.
   * @param instituteID the institute, this is an optional value, you can leave this parameter
   *     empty.
   */
  public EbicsProduct(String name, String language, String instituteID) {
    this.name = name;
    if (language == null) {
      language = Locale.getDefault().getLanguage();
    } else {
      this.language = language;
    }
    this.instituteID = instituteID;
  }

  /** @return the name */
  public String getName() {
    return name;
  }

  /** @param name the name to set */
  public void setName(String name) {
    this.name = name;
  }

  /** @return the language */
  public String getLanguage() {
    return language;
  }

  /** @param language the language to set */
  public void setLanguage(String language) {
    this.language = language;
  }

  /** @return the instituteID */
  public String getInstituteID() {
    return instituteID;
  }

  /** @param instituteID the instituteID to set */
  public void setInstituteID(String instituteID) {
    this.instituteID = instituteID;
  }

  // --------------------------------------------------------------------
  // DATA MEMBERS
  // --------------------------------------------------------------------

  private transient String name;
  private String language;
  private String instituteID;

  private static final long serialVersionUID = 6400195827756653241L;
}
