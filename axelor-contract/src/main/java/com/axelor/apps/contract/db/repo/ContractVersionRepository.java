/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.contract.db.repo;

import com.axelor.apps.contract.db.ContractVersion;
import com.axelor.db.JpaRepository;

public class ContractVersionRepository extends JpaRepository<ContractVersion> {

  public static final int DRAFT_VERSION = 1;
  public static final int WAITING_VERSION = 2;
  public static final int ONGOING_VERSION = 3;
  public static final int TERMINATED_VERSION = 4;

  public ContractVersionRepository() {
    super(ContractVersion.class);
  }
}
