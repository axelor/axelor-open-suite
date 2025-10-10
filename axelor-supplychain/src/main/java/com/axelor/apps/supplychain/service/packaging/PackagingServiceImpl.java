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
package com.axelor.apps.supplychain.service.packaging;

import com.axelor.apps.supplychain.db.Packaging;
import com.axelor.apps.supplychain.db.repo.PackagingRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;

public class PackagingServiceImpl implements PackagingService {

  protected PackagingRepository packagingRepository;

  @Inject
  public PackagingServiceImpl(PackagingRepository packagingRepository) {
    this.packagingRepository = packagingRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void addChildPackaging(Packaging packaging) {
    Packaging childPackaging = new Packaging();
    childPackaging.setParentPackaging(packaging);
    packagingRepository.save(childPackaging);
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public void removePackagings(List<Packaging> packagingList) {
    for (Packaging packaging : packagingList) {
      packagingRepository.remove(packaging);
    }
  }
}
