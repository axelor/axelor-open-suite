/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.QIDefault;
import com.axelor.utils.api.ObjectFinder;
import com.axelor.utils.api.RequestStructure;
import java.math.BigDecimal;

public class QIResolutionDefaultRequest extends RequestStructure {

  protected Long id;
  protected Long qiDefaultId;
  protected BigDecimal quantity;
  protected String description;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getQiDefaultId() {
    return qiDefaultId;
  }

  public void setQiDefaultId(Long qiDefaultId) {
    this.qiDefaultId = qiDefaultId;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public QIDefault fetchQiDefault() {
    if (qiDefaultId == null || qiDefaultId == 0L) {
      return null;
    }
    return ObjectFinder.find(QIDefault.class, qiDefaultId, ObjectFinder.NO_VERSION);
  }
}
