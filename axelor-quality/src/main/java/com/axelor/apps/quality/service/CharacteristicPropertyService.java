package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.CharacteristicProperty;

public interface CharacteristicPropertyService {
  boolean hasSimilarName(CharacteristicProperty characteristic);
}
