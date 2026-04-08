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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

public class ManufOrderMultiLevelPlanningServiceImpl
    implements ManufOrderMultiLevelPlanningService {

  protected ManufOrderService manufOrderService;
  protected ManufOrderRepository manufOrderRepo;
  protected AppProductionService appProductionService;
  protected PartnerRepository partnerRepository;
  protected ProductRepository productRepository;
  protected BillOfMaterialRepository billOfMaterialRepository;
  protected BillOfMaterialService billOfMaterialService;
  protected UnitConversionService unitConversionService;

  @Inject
  public ManufOrderMultiLevelPlanningServiceImpl(
      ManufOrderService manufOrderService,
      ManufOrderRepository manufOrderRepo,
      AppProductionService appProductionService,
      PartnerRepository partnerRepository,
      ProductRepository productRepository,
      BillOfMaterialRepository billOfMaterialRepository,
      BillOfMaterialService billOfMaterialService,
      UnitConversionService unitConversionService) {
    this.manufOrderService = manufOrderService;
    this.manufOrderRepo = manufOrderRepo;
    this.appProductionService = appProductionService;
    this.partnerRepository = partnerRepository;
    this.productRepository = productRepository;
    this.billOfMaterialRepository = billOfMaterialRepository;
    this.billOfMaterialService = billOfMaterialService;
    this.unitConversionService = unitConversionService;
  }

  @Override
  public List<ManufOrder> generateAllSubManufOrder(List<Product> productList, ManufOrder manufOrder)
      throws AxelorException {
    Integer depth = 0;
    List<ManufOrder> moList = new ArrayList<>();
    List<Pair<BillOfMaterial, BigDecimal>> childBomList =
        getToConsumeSubBomList(manufOrder.getBillOfMaterial(), manufOrder, productList);
    moList.addAll(this.generateChildMOs(manufOrder, childBomList, depth));
    return moList;
  }

  @Override
  public List<Pair<BillOfMaterial, BigDecimal>> getToConsumeSubBomList(
      BillOfMaterial billOfMaterial, ManufOrder mo, List<Product> productList)
      throws AxelorException {
    List<Pair<BillOfMaterial, BigDecimal>> bomList = new ArrayList<>();

    for (BillOfMaterialLine boml : billOfMaterial.getBillOfMaterialLineList()) {
      Product product = boml.getProduct();
      if (productList != null && !productList.contains(product)) {
        continue;
      }

      BigDecimal qtyReq =
          manufOrderService.computeToConsumeProdProductLineQuantity(
              mo.getBillOfMaterial().getQty(), mo.getQty(), boml.getQty());

      BillOfMaterial bom = boml.getBillOfMaterial();
      if (bom != null) {
        if (bom.getProdProcess() != null) {
          bomList.add(Pair.of(bom, qtyReq));
        }
      } else {
        BillOfMaterial defaultBOM = billOfMaterialService.getDefaultBOM(product, null);

        if ((product.getProductSubTypeSelect()
                    == ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT
                || product.getProductSubTypeSelect()
                    == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)
            && defaultBOM != null
            && defaultBOM.getProdProcess() != null) {
          bomList.add(Pair.of(defaultBOM, qtyReq));
        }
      }
    }
    return bomList;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<Long> planSelectedOrdersAndDiscardOthers(List<Map<String, Object>> manufOrders)
      throws AxelorException {
    validateNoOrphanChildSelected(manufOrders);

    List<Long> ids = new ArrayList<>();
    // Maps each draft MO's BOM ID to the ID of the real ManufOrder generated from it.
    // Used to resolve parent links for grandchildren, whose draft parentMO has id=null
    // but whose parentMO.billOfMaterial.id is preserved across the wizard round-trip.
    // Storing IDs (not entity references) avoids detached-entity errors caused by JPA
    // flush/clear operations inside generateManufOrder.
    Map<Long, Long> bomIdToGeneratedMOId = new HashMap<>();

    for (Map<String, Object> manufOrderMap : manufOrders) {
      ManufOrder draftMO = Mapper.toBean(ManufOrder.class, manufOrderMap);

      if (!(boolean) manufOrderMap.get("selected")) {
        continue;
      }

      Product product = productRepository.find(draftMO.getProduct().getId());

      BillOfMaterial billOfMaterial = draftMO.getBillOfMaterial();
      Long draftBomId = billOfMaterial != null ? billOfMaterial.getId() : null;
      billOfMaterial = billOfMaterialRepository.find(draftBomId);

      Partner clientPartner = draftMO.getClientPartner();
      if (ObjectUtils.notEmpty(clientPartner)) {
        clientPartner = partnerRepository.find(clientPartner.getId());
      }

      ManufOrder generated =
          manufOrderService.generateManufOrder(
              product,
              draftMO.getQty().multiply(billOfMaterial.getQty()),
              draftMO.getPrioritySelect(),
              ManufOrderService.IS_TO_INVOICE,
              billOfMaterial,
              draftMO.getPlannedStartDateT(),
              draftMO.getPlannedEndDateT(),
              ManufOrderOriginTypeProduction.ORIGIN_TYPE_OTHER);

      if (appProductionService.getAppProduction().getManageWorkshop()
          && generated.getWorkshopStockLocation() == null) {
        StockLocation parentWorkshop = null;
        ManufOrder draftParentMO = draftMO.getParentMO();
        if (draftParentMO != null) {
          if (draftParentMO.getId() != null) {
            ManufOrder dbParentMO = manufOrderRepo.find(draftParentMO.getId());
            parentWorkshop = dbParentMO.getWorkshopStockLocation();
          } else {
            BillOfMaterial parentBom = draftParentMO.getBillOfMaterial();
            if (parentBom != null) {
              Long parentMOId = bomIdToGeneratedMOId.get(parentBom.getId());
              if (parentMOId != null) {
                ManufOrder generatedParentMO = manufOrderRepo.find(parentMOId);
                parentWorkshop = generatedParentMO.getWorkshopStockLocation();
              }
            }
          }
        }
        generated.setWorkshopStockLocation(parentWorkshop);
      }

      generated.setClientPartner(clientPartner);

      setParentMo(draftMO, generated, bomIdToGeneratedMOId);

      if (draftBomId != null) {
        bomIdToGeneratedMOId.put(draftBomId, generated.getId());
      }

      ids.add(generated.getId());
    }
    return ids;
  }

  @Override
  public List<ManufOrder> getChildrenManufOrder(ManufOrder manufOrder) {
    return manufOrderRepo
        .all()
        .filter("self.parentMO = :manufOrder")
        .bind("manufOrder", manufOrder)
        .fetch();
  }

  protected void validateNoOrphanChildSelected(List<Map<String, Object>> manufOrders)
      throws AxelorException {
    Set<Long> selectedBomIds = collectSelectedBomIds(manufOrders);

    for (Map<String, Object> moMap : manufOrders) {
      if (!Boolean.TRUE.equals(moMap.get("selected"))) {
        continue;
      }
      Long draftParentBomId = extractDraftParentBomId(moMap);
      if (draftParentBomId != null && !selectedBomIds.contains(draftParentBomId)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(ProductionExceptionMessage.CHILD_MO_SELECTED_WITHOUT_PARENT),
            extractProductFullName(moMap));
      }
    }
  }

  /**
   * Collects the BOM IDs of all selected manufacturing orders from the wizard map list.
   *
   * @param manufOrders the list of draft MO maps from the wizard
   * @return set of BOM IDs belonging to selected MOs
   */
  protected Set<Long> collectSelectedBomIds(List<Map<String, Object>> manufOrders) {
    Set<Long> selectedBomIds = new HashSet<>();
    for (Map<String, Object> moMap : manufOrders) {
      if (!Boolean.TRUE.equals(moMap.get("selected"))) {
        continue;
      }
      Map<?, ?> bom = (Map<?, ?>) moMap.get("billOfMaterial");
      if (bom != null && bom.get("id") != null) {
        selectedBomIds.add(Long.valueOf(bom.get("id").toString()));
      }
    }
    return selectedBomIds;
  }

  /**
   * Returns the BOM ID of the draft parent MO of the given MO map, or {@code null} if there is no
   * draft parent. A parent is considered "draft" when it has no database id (id=null), meaning it
   * was generated by the wizard and not yet persisted. Persisted parents are skipped because they
   * cannot be orphaned.
   *
   * @param moMap the wizard map of the child MO
   * @return the parent BOM id, or null if the MO has no draft parent
   */
  protected Long extractDraftParentBomId(Map<String, Object> moMap) {
    Map<?, ?> parentMOMap = (Map<?, ?>) moMap.get("parentMO");
    if (parentMOMap == null || parentMOMap.get("id") != null) {
      // No parent, or parent already persisted — not a draft parent
      return null;
    }
    Map<?, ?> parentBom = (Map<?, ?>) parentMOMap.get("billOfMaterial");
    if (parentBom == null || parentBom.get("id") == null) {
      return null;
    }
    return Long.valueOf(parentBom.get("id").toString());
  }

  /**
   * Extracts the product full name from the given MO wizard map, or an empty string if absent.
   *
   * @param moMap the wizard map of the MO
   * @return the product full name, or an empty string
   */
  protected String extractProductFullName(Map<String, Object> moMap) {
    Map<?, ?> product = (Map<?, ?>) moMap.get("product");
    Object fullName = product != null ? product.get("fullName") : null;
    return fullName != null ? fullName.toString() : "";
  }

  protected void setParentMo(
      ManufOrder draftMO, ManufOrder generated, Map<Long, Long> bomIdToGeneratedMOId) {
    // Set parentMO: for root children the draft parentMO has a real id (saved entity);
    // for deeper levels the draft parentMO has id=null but its billOfMaterial.id identifies
    // the previously generated ManufOrder that acts as the parent.
    ManufOrder draftParentMO = draftMO.getParentMO();
    if (draftParentMO != null && draftParentMO.getId() != null) {
      generated.setParentMO(manufOrderRepo.find(draftParentMO.getId()));
    } else if (draftParentMO != null) {
      BillOfMaterial parentBom = draftParentMO.getBillOfMaterial();
      if (parentBom != null) {
        Long parentMOId = bomIdToGeneratedMOId.get(parentBom.getId());
        if (parentMOId != null) {
          generated.setParentMO(manufOrderRepo.find(parentMOId));
        }
      }
    }
  }

  protected List<ManufOrder> generateChildMOs(
      ManufOrder parentMO, List<Pair<BillOfMaterial, BigDecimal>> childBomList, Integer depth)
      throws AxelorException {
    List<ManufOrder> manufOrderList = new ArrayList<>();

    // prevent infinite loop
    if (depth >= 25) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.CHILD_BOM_TOO_MANY_ITERATION));
    }
    depth++;

    for (Pair<BillOfMaterial, BigDecimal> childBomPair : childBomList) {
      BillOfMaterial childBom = childBomPair.getLeft();
      BigDecimal qtyRequested = childBomPair.getRight();

      ManufOrder childMO =
          createDraftManufOrder(
              childBom.getProduct(),
              qtyRequested,
              parentMO.getPrioritySelect(),
              childBom,
              null,
              parentMO.getPlannedStartDateT());

      childMO.setManualMOSeq(this.getManualSequence());
      childMO.setParentMO(parentMO);
      childMO.setClientPartner(parentMO.getClientPartner());
      manufOrderList.add(childMO);

      manufOrderList.addAll(
          this.generateChildMOs(
              childMO, getToConsumeSubBomList(childMO.getBillOfMaterial(), childMO, null), depth));
    }
    return manufOrderList;
  }

  protected String getManualSequence() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
  }

  protected ManufOrder createDraftManufOrder(
      Product product,
      BigDecimal qtyRequested,
      int priority,
      BillOfMaterial billOfMaterial,
      LocalDateTime plannedStartDateT,
      LocalDateTime plannedEndDateT)
      throws AxelorException {

    ProdProcess prodProcess = billOfMaterial.getProdProcess();

    Unit unit = billOfMaterial.getUnit();
    if (unit != null && !unit.equals(product.getUnit())) {
      qtyRequested =
          unitConversionService.convert(
              product.getUnit(), unit, qtyRequested, qtyRequested.scale(), product);
    }
    return new ManufOrder(
        qtyRequested,
        billOfMaterial.getCompany(),
        null,
        priority,
        false,
        unit,
        billOfMaterial,
        product,
        prodProcess,
        plannedStartDateT,
        plannedEndDateT,
        ManufOrderRepository.STATUS_DRAFT,
        prodProcess.getOutsourcing());
  }
}
