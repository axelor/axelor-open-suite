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
package com.axelor.apps.production.service.bomimport;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.ImportConfiguration;
import com.axelor.apps.base.db.ImportHistory;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.imports.ImportService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialImport;
import com.axelor.apps.production.db.BillOfMaterialImportLine;
import com.axelor.apps.production.db.repo.BillOfMaterialImportRepository;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialLineService;
import com.axelor.auth.AuthUtils;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BillOfMaterialImportServiceImpl implements BillOfMaterialImportService {

  protected final AppBaseService appBaseService;
  protected final ImportService importService;
  protected final BillOfMaterialLineService billOfMaterialLineService;
  protected final ImportConfigurationRepository importConfigurationRepository;
  protected final BillOfMaterialImportRepository billOfMaterialImportRepository;
  protected final BillOfMaterialRepository billOfMaterialRepository;
  protected final BillOfMaterialImporter billOfMaterialImporter;
  protected final MetaFiles metaFiles;
  protected final DMSService dmsService;

  @Inject
  public BillOfMaterialImportServiceImpl(
      AppBaseService appBaseService,
      ImportService importService,
      BillOfMaterialLineService billOfMaterialLineService,
      ImportConfigurationRepository importConfigurationRepository,
      BillOfMaterialImportRepository billOfMaterialImportRepository,
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialImporter billOfMaterialImporter,
      MetaFiles metaFiles,
      DMSService dmsService) {
    this.appBaseService = appBaseService;
    this.importService = importService;
    this.billOfMaterialLineService = billOfMaterialLineService;
    this.importConfigurationRepository = importConfigurationRepository;
    this.billOfMaterialImportRepository = billOfMaterialImportRepository;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialImporter = billOfMaterialImporter;
    this.metaFiles = metaFiles;
    this.dmsService = dmsService;
  }

  @Override
  public ImportHistory processImport(BillOfMaterialImport billOfMaterialImport)
      throws AxelorException, IOException {
    billOfMaterialImporter.addBillOfMaterialImport(billOfMaterialImport);
    return billOfMaterialImporter.init(createImportConfiguration(billOfMaterialImport)).run();
  }

  @Transactional(rollbackOn = {Exception.class})
  protected ImportConfiguration createImportConfiguration(
      BillOfMaterialImport billOfMaterialImport) {
    ImportConfiguration importConfiguration = new ImportConfiguration();

    importConfiguration.setUser(AuthUtils.getUser());
    importConfiguration.setName(billOfMaterialImport.getName());
    importConfiguration.setBindMetaFile(billOfMaterialImport.getImportSource().getBindingFile());
    importConfiguration.setDataMetaFile(billOfMaterialImport.getImportMetaFile());
    importConfiguration.setStartDateTime(appBaseService.getTodayDateTime().toLocalDateTime());

    return importConfigurationRepository.save(importConfiguration);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setStatusToImported(BillOfMaterialImport billOfMaterialImport) {
    billOfMaterialImport = billOfMaterialImportRepository.find(billOfMaterialImport.getId());
    billOfMaterialImport.setStatusSelect(BillOfMaterialImportRepository.STATUS_IMPORTED);
    billOfMaterialImportRepository.save(billOfMaterialImport);
  }

  @Override
  public void createBoMFromImport(BillOfMaterialImport billOfMaterialImport)
      throws AxelorException {
    if (billOfMaterialImport != null
        && billOfMaterialImport.getMainBillOfMaterialGenerated() != null) {
      return;
    }
    Optional<BillOfMaterialImportLine> billOfMaterialImportLineOp = Optional.empty();
    if (billOfMaterialImport != null
        && billOfMaterialImport.getBillOfMaterialImportLineList() != null) {
      billOfMaterialImportLineOp =
          billOfMaterialImport.getBillOfMaterialImportLineList().stream()
              .filter(line -> line.getBomLevel() == 0)
              .findFirst();
    }
    billOfMaterialImportLineOp.orElseThrow(
        () ->
            new AxelorException(
                billOfMaterialImport,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                ProductionExceptionMessage.BOM_IMPORT_NO_MAIN_BILL_OF_MATERIALS_GENERATED));

    generateBillOfMaterialFromImportLine(billOfMaterialImportLineOp.get());

    attachMetaFile(billOfMaterialImport);
  }

  protected void attachMetaFile(BillOfMaterialImport billOfMaterialImport) throws AxelorException {
    MetaFile metaFile = billOfMaterialImport.getDocumentMetaFile();

    if (metaFile != null) {
      if (metaFile.getFileType().equals("application/zip")) {
        String zipFilePath = MetaFiles.getPath(metaFile).toFile().getAbsolutePath();
        dmsService.unzip(zipFilePath, billOfMaterialImport.getMainBillOfMaterialGenerated());
      } else {
        metaFiles.attach(
            billOfMaterialImport.getDocumentMetaFile(),
            billOfMaterialImport.getDocumentMetaFile().getFileName(),
            billOfMaterialImport.getMainBillOfMaterialGenerated());
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Optional<BillOfMaterial> generateBillOfMaterialFromImportLine(
      BillOfMaterialImportLine billOfMaterialImportLine) {
    BillOfMaterial billOfMaterial = new BillOfMaterial();
    setBillOfMaterialFields(billOfMaterialImportLine, billOfMaterial);

    if (billOfMaterialImportLine.getBillOfMaterialImportLineList() == null
        || billOfMaterialImportLine.getBillOfMaterialImportLineList().isEmpty()) {
      return Optional.empty();
    }

    if (billOfMaterialImportLine.getBillOfMaterialImportLineList() != null) {
      for (BillOfMaterialImportLine billOfMaterialImportLineChild :
          billOfMaterialImportLine.getBillOfMaterialImportLineList()) {
        generateBillOfMaterialFromImportLine(billOfMaterialImportLineChild)
            .map(
                bom -> {
                  if (billOfMaterial.getProduct() != null
                      && billOfMaterial.getProduct().getProductSubTypeSelect()
                          == ProductRepository.PRODUCT_SUB_TYPE_COMPONENT) {
                    billOfMaterial
                        .getProduct()
                        .setProductSubTypeSelect(
                            ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT);
                  }
                  return bom;
                })
            .ifPresentOrElse(
                childBom -> {
                  billOfMaterial.addBillOfMaterialLineListItem(
                      billOfMaterialLineService.createFromBillOfMaterial(childBom));
                },
                () ->
                    billOfMaterial.addBillOfMaterialLineListItem(
                        billOfMaterialLineService.createBillOfMaterialLine(
                            billOfMaterialImportLineChild.getProduct(),
                            billOfMaterial,
                            billOfMaterialImportLineChild.getQuantity(),
                            billOfMaterialImportLineChild.getUnit(),
                            null,
                            false)));
      }
    }

    billOfMaterialImportLine
        .getBillOfMaterialImport()
        .addBillOfMaterialGeneratedListItem(billOfMaterial);

    billOfMaterialImportLine
        .getBillOfMaterialImport()
        .setMainBillOfMaterialGenerated(billOfMaterial);
    return Optional.of(billOfMaterialRepository.save(billOfMaterial));
  }

  protected void setBillOfMaterialFields(
      BillOfMaterialImportLine billOfMaterialImportLine, BillOfMaterial billOfMaterial) {
    billOfMaterial.setName(billOfMaterialImportLine.getName());
    billOfMaterial.setCompany(AuthUtils.getUser().getActiveCompany());
    billOfMaterial.setProduct(billOfMaterialImportLine.getProduct());
    billOfMaterial.setQty(billOfMaterialImportLine.getQuantity());
    billOfMaterial.setUnit(billOfMaterialImportLine.getUnit());
    billOfMaterial.setStatusSelect(BillOfMaterialRepository.STATUS_APPLICABLE);
    billOfMaterial.setDefineSubBillOfMaterial(true);
    billOfMaterial.setWorkshopStockLocation(AuthUtils.getUser().getWorkshopStockLocation());
    billOfMaterial.setMark(billOfMaterialImportLine.getMark());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setStatusToValidated(BillOfMaterialImport billOfMaterialImport) {
    billOfMaterialImport = billOfMaterialImportRepository.find(billOfMaterialImport.getId());
    billOfMaterialImport.setStatusSelect(BillOfMaterialImportRepository.STATUS_VALIDATED);
    billOfMaterialImportRepository.save(billOfMaterialImport);
  }

  @Override
  public List<Product> getCreatedProducts(BillOfMaterialImport billOfMaterialImport) {
    return billOfMaterialImport.getBillOfMaterialImportLineList().stream()
        .filter(BillOfMaterialImportLine::getIsCreatedProduct)
        .map(BillOfMaterialImportLine::getProduct)
        .collect(Collectors.toList());
  }
}
