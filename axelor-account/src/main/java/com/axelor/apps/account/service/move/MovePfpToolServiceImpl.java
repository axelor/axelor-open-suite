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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class MovePfpToolServiceImpl implements MovePfpToolService {

  protected InvoiceTermPfpToolService invoiceTermPfpToolService;

  @Inject
  public MovePfpToolServiceImpl(InvoiceTermPfpToolService invoiceTermPfpToolService) {
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
  }

  @Override
  public Integer checkOtherInvoiceTerms(Move move) {
    if (move == null || CollectionUtils.isEmpty(move.getMoveLineList())) {
      return null;
    }

    List<InvoiceTerm> invoiceTermList = getInvoiceTermList(move);
    if (!CollectionUtils.isEmpty(invoiceTermList)) {
      return invoiceTermPfpToolService.checkOtherInvoiceTerms(invoiceTermList);
    }
    return null;
  }

  @Override
  public void fillMovePfpValidateStatus(Move move) {
    Integer pfpStatus = checkOtherInvoiceTerms(move);
    if (pfpStatus != null) {
      move.setPfpValidateStatusSelect(pfpStatus);
    }
  }

  protected List<InvoiceTerm> getInvoiceTermList(Move move) {
    List<InvoiceTerm> invoiceTermList = new ArrayList<>();
    if (CollectionUtils.isNotEmpty(move.getMoveLineList())) {
      invoiceTermList.addAll(
          move.getMoveLineList().stream()
              .map(MoveLine::getInvoiceTermList)
              .filter(Objects::nonNull)
              .flatMap(Collection::stream)
              .collect(Collectors.toList()));
    }
    return invoiceTermList;
  }
}
