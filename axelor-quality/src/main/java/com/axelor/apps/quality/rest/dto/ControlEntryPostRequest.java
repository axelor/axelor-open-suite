package com.axelor.apps.quality.rest.dto;

import com.axelor.utils.api.RequestPostStructure;
import javax.validation.constraints.NotNull;

public class ControlEntryPostRequest extends RequestPostStructure {

  @NotNull protected Long controlEntryId;
  protected Long characteristicId;
  protected Long sampleId;

  public Long getControlEntryId() {
    return controlEntryId;
  }

  public void setControlEntryId(Long controlEntryId) {
    this.controlEntryId = controlEntryId;
  }

  public Long getCharacteristicId() {
    return characteristicId;
  }

  public void setCharacteristicId(Long characteristicId) {
    this.characteristicId = characteristicId;
  }

  public Long getSampleId() {
    return sampleId;
  }

  public void setSampleId(Long sampleId) {
    this.sampleId = sampleId;
  }
}
