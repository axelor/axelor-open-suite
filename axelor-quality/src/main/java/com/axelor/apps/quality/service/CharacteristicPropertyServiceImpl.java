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
