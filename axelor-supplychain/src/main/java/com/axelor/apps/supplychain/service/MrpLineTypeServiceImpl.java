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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.util.List;

public class MrpLineTypeServiceImpl implements MrpLineTypeService {

  protected MrpLineTypeRepository mrpLineTypeRepository;

  @Inject
  public MrpLineTypeServiceImpl(MrpLineTypeRepository mrpLineTypeRepository) {
    this.mrpLineTypeRepository = mrpLineTypeRepository;
  }

  @Override
  public MrpLineType getMrpLineType(int elementSelect, int mrpTypeSelect) {

    return getMrpLineTypeQuery(elementSelect, mrpTypeSelect).fetchOne();
  }

  @Override
  public List<MrpLineType> getMrpLineTypeList(int elementSelect, int mrpTypeSelect) {

    return getMrpLineTypeQuery(elementSelect, mrpTypeSelect).fetch();
  }

  protected Query<MrpLineType> getMrpLineTypeQuery(int elementSelect, int mrpTypeSelect) {
    int applicationFieldSelect = getApplicationField(mrpTypeSelect);
    return mrpLineTypeRepository
        .all()
        .filter(
            "self.elementSelect = ?1 and self.applicationFieldSelect LIKE ?2",
            elementSelect,
            "%" + applicationFieldSelect + "%");
  }

  protected int getApplicationField(int mrpTypeSelect) {

    switch (mrpTypeSelect) {
      case MrpRepository.MRP_TYPE_MRP:
        return MrpLineTypeRepository.APPLICATION_FIELD_MRP;
      case MrpRepository.MRP_TYPE_MPS:
        return MrpLineTypeRepository.APPLICATION_FIELD_MPS;
      default:
        return 0;
    }
  }
}
