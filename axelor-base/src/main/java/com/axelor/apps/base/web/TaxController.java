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
package com.axelor.apps.base.web;

import com.axelor.apps.account.db.Tax;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.tax.TaxArchiveService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class TaxController {

  public void archive(ActionRequest request, ActionResponse response) {
    try {
      applyArchive(request, response, true);
      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void unarchive(ActionRequest request, ActionResponse response) {
    try {
      applyArchive(request, response, false);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected void applyArchive(ActionRequest request, ActionResponse response, boolean archived) {
    List<Integer> ids = (List<Integer>) request.getContext().get("_ids");
    List<Long> idList = new ArrayList<>();

    if (!CollectionUtils.isEmpty(ids)) {
      idList = ids.stream().map(Integer::longValue).collect(Collectors.toList());
    } else {
      Tax tax = request.getContext().asType(Tax.class);
      if (tax.getId() != null) {
        idList = Collections.singletonList(tax.getId());
      }
    }
    if (CollectionUtils.isEmpty(idList)) {
      response.setError(I18n.get("Please select at least one record."));
    }
    if (archived) {
      Beans.get(TaxArchiveService.class).archive(idList);
    } else {
      Beans.get(TaxArchiveService.class).unarchive(idList);
    }
  }
}
