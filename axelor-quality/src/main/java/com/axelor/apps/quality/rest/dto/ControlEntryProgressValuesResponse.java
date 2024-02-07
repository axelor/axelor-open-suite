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
