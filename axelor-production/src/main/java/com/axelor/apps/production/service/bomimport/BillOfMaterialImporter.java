/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service.bomimport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.imports.importer.Importer;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.apps.production.db.BillOfMaterialImport;
import com.axelor.apps.production.db.BillOfMaterialImportLine;
import com.axelor.apps.production.db.repo.BillOfMaterialImportLineRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialImportRepository;
import com.axelor.data.csv.CSVImporter;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillOfMaterialImporter extends Importer {

  protected final int FETCH_LIMIT = 10;
  protected final BillOfMaterialImportLineService billOfMaterialImportLineService;
  protected final BillOfMaterialImportRepository billOfMaterialImportRepository;
  protected final BillOfMaterialImportLineRepository billOfMaterialImportLineRepository;
  protected final List<BillOfMaterialImportLine> billOfMaterialImportLineList = new ArrayList<>();
  protected BillOfMaterialImport billOfMaterialImport;

  @Inject
  public BillOfMaterialImporter(
      BillOfMaterialImportLineService billOfMaterialImportLineService,
      BillOfMaterialImportRepository billOfMaterialImportRepository,
      BillOfMaterialImportLineRepository billOfMaterialImportLineRepository) {
    this.billOfMaterialImportLineService = billOfMaterialImportLineService;
    this.billOfMaterialImportRepository = billOfMaterialImportRepository;
    this.billOfMaterialImportLineRepository = billOfMaterialImportLineRepository;
  }

  @Override
  protected ImportHistory process(String bind, String data, Map<String, Object> importContext)
      throws IOException {

    CSVImporter importer = new CSVImporter(bind, data);

    ImporterListener listener =
        new ImporterListener(getConfiguration().getName()) {
          @Override
          public void handle(Model bean, Exception e) {
            if (billOfMaterialImport != null) {
              Throwable rootCause = Throwables.getRootCause(e);
              TraceBackService.trace(
                  new AxelorException(
                      rootCause,
                      billOfMaterialImport,
                      TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                      rootCause.getMessage()));
            }
            super.handle(bean, e);
          }

          @Override
          public void imported(Integer total, Integer success) {
            try {
              linkLineToBillOfMaterialImport(
                  billOfMaterialImport, billOfMaterialImportLineList, this);
            } catch (Exception e) {
              this.handle(null, e);
            }
            super.imported(total, success);
          }

          @Override
          public void imported(Model bean) {
            addBillOfMaterialImportLine(bean);
            super.imported(bean);
          }
        };

    importer.addListener(listener);
    if (importContext == null) {
      importContext = new HashMap<>();
    }
    importContext.put("BillOfMaterialImport", billOfMaterialImport);
    importer.setContext(importContext);
    importer.run();
    return addHistory(listener);
  }

  protected void addBillOfMaterialImportLine(Model bean) {
    if (bean.getClass().equals(BillOfMaterialImportLine.class)) {
      BillOfMaterialImportLine billOfMaterialImportLine = (BillOfMaterialImportLine) bean;
      billOfMaterialImportLineList.add(billOfMaterialImportLine);
    }
  }

  public void addBillOfMaterialImport(BillOfMaterialImport billOfMaterialImport) {
    this.billOfMaterialImport = billOfMaterialImport;
  }

  @Override
  protected ImportHistory process(String bind, String data) throws IOException {
    return process(bind, data, null);
  }

  protected void linkLineToBillOfMaterialImport(
      BillOfMaterialImport billOfMaterialImport,
      List<BillOfMaterialImportLine> billOfMaterialImportLineList,
      ImporterListener listener) {
    if (billOfMaterialImport != null) {
      int i = 0;
      for (BillOfMaterialImportLine billOfMaterialImportLine : billOfMaterialImportLineList) {
        billOfMaterialImportLine =
            billOfMaterialImportLineRepository.find(billOfMaterialImportLine.getId());
        setDescriptionAndBillOfMaterialImport(
            billOfMaterialImport, listener, billOfMaterialImportLine);
        setBoMLevel(listener, billOfMaterialImportLine);
        if (i % FETCH_LIMIT == 0) {
          JPA.clear();
        }
        i++;
      }
    }
  }

  @Transactional
  protected void setDescriptionAndBillOfMaterialImport(
      BillOfMaterialImport billOfMaterialImport,
      ImporterListener listener,
      BillOfMaterialImportLine billOfMaterialImportLine) {
    try {
      if (billOfMaterialImportLine != null) {
        billOfMaterialImport = billOfMaterialImportRepository.find(billOfMaterialImport.getId());
        billOfMaterialImport.addBillOfMaterialImportLineListItem(billOfMaterialImportLine);
        billOfMaterialImportLine.setDescription(billOfMaterialImport.getDocumentMetaFile());
        billOfMaterialImportLine.setBillOfMaterialImport(billOfMaterialImport);
      }
    } catch (Exception e) {
      listener.handle(billOfMaterialImportLine, e);
    }
  }

  @Transactional
  protected void setBoMLevel(
      ImporterListener listener, BillOfMaterialImportLine billOfMaterialImportLine) {
    try {
      billOfMaterialImportLineService.computeBoMLevel(billOfMaterialImportLine);
    } catch (Exception e) {
      listener.handle(billOfMaterialImportLine, e);
    }
  }
}
