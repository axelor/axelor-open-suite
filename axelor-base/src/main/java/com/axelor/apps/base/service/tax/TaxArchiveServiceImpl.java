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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxLineRepository;
import com.axelor.apps.base.db.repo.TaxBaseRepository;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class TaxArchiveServiceImpl implements TaxArchiveService {

  protected TaxLineRepository taxLineRepository;
  protected TaxBaseRepository taxRepository;

  @Inject
  public TaxArchiveServiceImpl(
      TaxLineRepository taxLineRepository, TaxBaseRepository taxRepository) {
    this.taxLineRepository = taxLineRepository;
    this.taxRepository = taxRepository;
  }

  @Override
  public void archive(List<Long> idList) {
    for (Long id : idList) {
      setArchived(taxRepository.find(id), true);
    }
  }

  @Override
  public void unarchive(List<Long> idList) {
    for (Long id : idList) {
      setArchived(taxRepository.find(id), false);
    }
  }

  @Transactional(rollbackOn = Exception.class)
  protected void setArchived(Tax tax, boolean archived) {
    tax.setArchived(archived);
    List<TaxLine> taxLineList =
        taxLineRepository.all().filter("self.tax.id = :taxId").bind("taxId", tax.getId()).fetch();
    if (CollectionUtils.isEmpty(taxLineList)) {
      return;
    }
    for (TaxLine taxLine : taxLineList) {
      taxLine.setArchived(archived);
    }
  }
}
