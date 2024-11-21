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
package com.axelor.apps.quality.rest.dto;

import com.axelor.apps.quality.db.ControlEntry;
import com.axelor.utils.api.ResponseStructure;

public class ControlEntryProgressValuesResponse extends ResponseStructure {

  protected Integer sampleCompletelyControlled;
  protected Integer characteristicCompletelyControlled;
  protected Integer sampleControlledOnCharacteristic;
  protected Integer characteristicControlledOnSample;

  public ControlEntryProgressValuesResponse(
      ControlEntry controlEntry,
      Integer sampleCompletelyControlled,
      Integer characteristicCompletelyControlled,
      Integer sampleControlledOnCharacteristic,
      Integer characteristicControlledOnSample) {
    super(controlEntry.getVersion());
    this.sampleCompletelyControlled = sampleCompletelyControlled;
    this.characteristicCompletelyControlled = characteristicCompletelyControlled;
    this.sampleControlledOnCharacteristic = sampleControlledOnCharacteristic;
    this.characteristicControlledOnSample = characteristicControlledOnSample;
  }

  public Integer getSampleCompletelyControlled() {
    return sampleCompletelyControlled;
  }

  public Integer getCharacteristicCompletelyControlled() {
    return characteristicCompletelyControlled;
  }

  public Integer getSampleControlledOnCharacteristic() {
    return sampleControlledOnCharacteristic;
  }

  public Integer getCharacteristicControlledOnSample() {
    return characteristicControlledOnSample;
  }
}
