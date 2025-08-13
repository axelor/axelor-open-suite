package com.axelor.apps.quality.service;

import com.axelor.apps.quality.db.CharacteristicProperty;
import com.axelor.apps.quality.db.repo.CharacteristicPropertyRepository;
import com.google.inject.Inject;

public class CharacteristicPropertyServiceImpl implements CharacteristicPropertyService {

  protected final CharacteristicPropertyRepository characteristicPropertyRepository;

  @Inject
  public CharacteristicPropertyServiceImpl(
      CharacteristicPropertyRepository characteristicRepository) {
    this.characteristicPropertyRepository = characteristicRepository;
  }

  @Override
  public boolean hasSimilarName(CharacteristicProperty characteristic) {

    return characteristicPropertyRepository
            .all()
            .filter("LOWER(self.name) = LOWER(:name) and self.id != :id")
            .bind("name", characteristic.getName())
            .bind("id", characteristic.getId() == null ? 0 : characteristic.getId())
            .count()
        > 0;
  }
}
