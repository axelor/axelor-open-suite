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
package com.axelor.apps.base.rest.dto;

import com.axelor.meta.db.MetaTranslation;

public class TranslationResponse {

  private String key;
  private String value;
  private Number version;

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public Number getVersion() {
    return version;
  }

  public void setVersion(Number version) {
    this.version = version;
  }

  public static TranslationResponse build(MetaTranslation metaTranslation) {
    TranslationResponse translation = new TranslationResponse();
    translation.setKey(metaTranslation.getKey().replace("mobile_app_", ""));
    translation.setValue(metaTranslation.getMessage());
    translation.setVersion(metaTranslation.getVersion());
    return translation;
  }
}
