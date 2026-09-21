/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.utils.api.RequestStructure;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * A measured value of a control entry sample line. Only the value matching the type of the control
 * type field is read: {@code decimalValue}, {@code textValue}, {@code booleanValue} or {@code
 * selectionValue}, the latter being the id of the {@link
 * com.axelor.apps.quality.db.CharacteristicProperty} picked among the allowed values of the field.
 */
public class ControlTypeFieldValuePutRequest extends RequestStructure {

  @NotNull protected Long id;
  protected BigDecimal decimalValue;
  protected String textValue;
  protected Boolean booleanValue;
  protected Long selectionValue;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public BigDecimal getDecimalValue() {
    return decimalValue;
  }

  public void setDecimalValue(BigDecimal decimalValue) {
    this.decimalValue = decimalValue;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(Boolean booleanValue) {
    this.booleanValue = booleanValue;
  }

  public Long getSelectionValue() {
    return selectionValue;
  }

  public void setSelectionValue(Long selectionValue) {
    this.selectionValue = selectionValue;
  }
}
