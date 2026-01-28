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
package com.axelor.apps.stock.service.inventory;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.utils.BatchProcessorHelper;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.QueryBuilder;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected InventoryLineRepository inventoryLineRepository;
  protected InventoryLineService inventoryLineService;
  protected SequenceService sequenceService;
  protected InventoryRepository inventoryRepo;
  protected StockMoveRepository stockMoveRepo;
  protected StockMoveService stockMoveService;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected AppBaseService appBaseService;
  protected StockLocationRepository stockLocationRepository;
  protected InventoryValidateService inventoryValidateService;

  @Inject
  public InventoryService(
      InventoryLineRepository inventoryLineRepository,
      InventoryLineService inventoryLineService,
      SequenceService sequenceService,
      InventoryRepository inventoryRepo,
      StockMoveRepository stockMoveRepo,
      StockMoveService stockMoveService,
      StockLocationLineRepository stockLocationLineRepository,
      AppBaseService appBaseService,
      StockLocationRepository stockLocationRepository,
      InventoryValidateService inventoryValidateService) {
    this.inventoryLineRepository = inventoryLineRepository;
    this.inventoryLineService = inventoryLineService;
    this.sequenceService = sequenceService;
    this.inventoryRepo = inventoryRepo;
    this.stockMoveRepo = stockMoveRepo;
    this.stockMoveService = stockMoveService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.appBaseService = appBaseService;
    this.stockLocationRepository = stockLocationRepository;
    this.inventoryValidateService = inventoryValidateService;
  }

  public Inventory createInventory(
      LocalDate plannedStartDate,
      LocalDate plannedEndDate,
      String description,
      StockLocation stockLocation,
      boolean excludeOutOfStock,
      boolean includeObsolete,
      ProductFamily productFamily,
      ProductCategory productCategory)
      throws AxelorException {

    if (stockLocation == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_1));
    }

    Inventory inventory = new Inventory();

    inventory.setInventorySeq(this.getInventorySequence(stockLocation.getCompany()));

    inventory.setPlannedStartDateT(plannedStartDate.atStartOfDay(ZoneOffset.UTC));

    inventory.setPlannedEndDateT(plannedEndDate.atStartOfDay(ZoneOffset.UTC));

    inventory.setDescription(description);

    inventory.setFormatSelect(InventoryRepository.FORMAT_PDF);

    inventory.setStockLocation(stockLocation);

    inventory.setExcludeOutOfStock(excludeOutOfStock);

    inventory.setIncludeObsolete(includeObsolete);

    inventory.setProductCategory(productCategory);

    inventory.setProductFamily(productFamily);

    inventory.setStatusSelect(InventoryRepository.STATUS_DRAFT);

    return inventory;
  }

  public String getInventorySequence(Company company) throws AxelorException {

    String ref =
        sequenceService.getSequenceNumber(
            SequenceRepository.INVENTORY, company, Inventory.class, "inventorySeq", company);
    if (ref == null)
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_2) + " " + company.getName());

    return ref;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void planInventory(Inventory inventory) throws AxelorException {
    if (inventory.getStatusSelect() == null
        || inventory.getStatusSelect() != InventoryRepository.STATUS_DRAFT) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_PLAN_WRONG_STATUS));
    }
    inventory.setStatusSelect(InventoryRepository.STATUS_PLANNED);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void startInventory(Inventory inventory) throws AxelorException {
    if (inventory.getStatusSelect() == null
        || inventory.getStatusSelect() != InventoryRepository.STATUS_PLANNED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_START_WRONG_STATUS));
    }
    inventory.setStatusSelect(InventoryRepository.STATUS_IN_PROGRESS);

    Query<InventoryLine> query =
        inventoryLineRepository
            .all()
            .filter(
                "self.inventory.id = :inventoryId and self.stockLocation IS NOT NULL and self.id > :lastSeenId")
            .bind("inventoryId", inventory.getId())
            .order("id");

    BatchProcessorHelper.of().<InventoryLine>forEachByQuery(query, this::updateCurrentQty);
  }

  protected void updateCurrentQty(InventoryLine inventoryLine) {
    StockLocation stockLocation = inventoryLine.getStockLocation();
    Product product = inventoryLine.getProduct();
    TrackingNumber trackingNumber = inventoryLine.getTrackingNumber();

    String query =
        "self.product = :product AND (self.stockLocation  = :stockLocation OR self.detailsStockLocation = :stockLocation)";

    Query<StockLocationLine> stockLocationLineQuery =
        stockLocationLineRepository
            .all()
            .bind("product", product)
            .bind("stockLocation", stockLocation);

    if (ObjectUtils.notEmpty(trackingNumber)) {
      query += " AND self.trackingNumber = :trackingNumber";
      stockLocationLineQuery.bind("trackingNumber", trackingNumber);
    }

    BigDecimal currentQty =
        stockLocationLineQuery
            .filter(query)
            .fetchStream()
            .map(StockLocationLine::getCurrentQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    inventoryLine.setCurrentQty(currentQty);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void completeInventory(Inventory inventory) throws AxelorException {
    if (inventory.getStatusSelect() == null
        || inventory.getStatusSelect() != InventoryRepository.STATUS_IN_PROGRESS) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_COMPLETE_WRONG_STATUS));
    }
    validateInventoryLineList(inventory);
    inventory.setStatusSelect(InventoryRepository.STATUS_COMPLETED);
    inventory.setCompletedBy(AuthUtils.getUser());
  }

  public void validateInventory(Inventory inventory) throws AxelorException {
    inventoryValidateService.validate(inventory);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void draftInventory(Inventory inventory) throws AxelorException {
    if (inventory.getStatusSelect() == null
        || inventory.getStatusSelect() != InventoryRepository.STATUS_CANCELED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_DRAFT_WRONG_STATUS));
    }
    inventory.setStatusSelect(InventoryRepository.STATUS_DRAFT);
    inventory.setValidatedBy(null);
    inventory.setValidatedOn(null);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void cancel(Inventory inventory) throws AxelorException {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(InventoryRepository.STATUS_DRAFT);
    authorizedStatus.add(InventoryRepository.STATUS_PLANNED);
    authorizedStatus.add(InventoryRepository.STATUS_IN_PROGRESS);
    authorizedStatus.add(InventoryRepository.STATUS_COMPLETED);
    authorizedStatus.add(InventoryRepository.STATUS_VALIDATED);

    if (inventory.getStatusSelect() == null
        || !authorizedStatus.contains(inventory.getStatusSelect())) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_CANCEL_WRONG_STATUS));
    }
    List<StockMove> stockMoveList =
        stockMoveRepo
            .all()
            .filter("self.inventory.id = :inventoryId")
            .bind("inventoryId", inventory.getId())
            .fetch();

    for (StockMove stockMove : stockMoveList) {
      stockMoveService.cancel(stockMoveRepo.find(stockMove.getId()));
    }

    inventory = inventoryRepo.find(inventory.getId());
    inventory.setStatusSelect(InventoryRepository.STATUS_CANCELED);
    inventoryRepo.save(inventory);
  }

  @Transactional(rollbackOn = {Exception.class})
  public Boolean fillInventoryLineList(Inventory inventory) throws AxelorException {

    if (inventory.getStockLocation() == null) {
      throw new AxelorException(
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_1));
    }

    this.initInventoryLines(inventory);

    final boolean[] anyScanned = {false};
    final boolean[] anyCreated = {false};
    final Set<Long> trackedProductIdsAtInventoryLocation = new HashSet<>();

    final long inventoryId = inventory.getId();
    final Inventory[] inventoryHolder = {inventory};

    BatchProcessorHelper batchHelper =
        BatchProcessorHelper.builder().flushAfterBatch(false).build();

    Query<StockLocationLine> prePass =
        buildSllFilterQuery(inventory)
            .add("self.trackingNumber IS NOT NULL")
            .add("self.detailsStockLocation = :stockLocation")
            .add("self.id > :lastSeenId")
            .bind("stockLocation", inventory.getStockLocation())
            .build()
            .order("id");

    batchHelper.<StockLocationLine, AxelorException>forEachByQuery(
        prePass,
        sll -> {
          anyScanned[0] = true;
          final Product product = sll.getProduct();
          if (product != null) {
            trackedProductIdsAtInventoryLocation.add(product.getId());
          }
        });

    inventoryHolder[0] = inventoryRepo.find(inventoryId);

    Query<StockLocationLine> mainPass =
        buildSllFilterQuery(inventory).add("self.id > :lastSeenId").build().order("id");

    BatchProcessorHelper.of()
        .<StockLocationLine, AxelorException>forEachByQuery(
            mainPass,
            sll -> {
              anyScanned[0] = true;
              final Product product = sll.getProduct();
              if (product == null) {
                return;
              }
              if (sll.getTrackingNumber() != null
                  || !trackedProductIdsAtInventoryLocation.contains(product.getId())) {
                this.createInventoryLine(inventoryHolder[0], sll);
                anyCreated[0] = true;
              }
            },
            () -> inventoryHolder[0] = inventoryRepo.find(inventoryId));

    if (!anyScanned[0]) {
      return null;
    }
    return anyCreated[0];
  }

  public QueryBuilder<StockLocationLine> buildSllFilterQuery(Inventory inventory) {

    QueryBuilder<StockLocationLine> qb = QueryBuilder.of(StockLocationLine.class);
    if (inventory.getIncludeSubStockLocation()) {
      Set<StockLocation> all = new HashSet<>();
      all.add(inventory.getStockLocation());
      all = this.getStockLocations(all);
      qb.add(
              "(self.stockLocation IN (:stockLocations) OR self.detailsStockLocation IN (:stockLocations))")
          .bind("stockLocations", all);
    } else {
      qb.add("(self.stockLocation = :stockLocation OR self.detailsStockLocation = :stockLocation)")
          .bind("stockLocation", inventory.getStockLocation());
    }

    if (inventory.getExcludeOutOfStock()) {
      qb.add("self.currentQty > 0");
    }

    if (!inventory.getIncludeObsolete()) {
      qb.add("(self.product.endDate > :endDate OR self.product.endDate IS NULL)")
          .bind("endDate", inventory.getPlannedEndDateT().toLocalDate());
    }

    if (inventory.getProductFamily() != null) {
      qb.add("self.product.productFamily = :productFamily")
          .bind("productFamily", inventory.getProductFamily());
    }

    if (inventory.getProductCategory() != null) {
      qb.add("self.product.productCategory = :productCategory")
          .bind("productCategory", inventory.getProductCategory());
    }

    if (inventory.getProduct() != null) {
      qb.add("self.product = :product").bind("product", inventory.getProduct());
    }

    if (StringUtils.notEmpty(inventory.getFromRack())) {
      qb.add("self.rack >= :fromRack").bind("fromRack", inventory.getFromRack());
    }
    if (StringUtils.notEmpty(inventory.getToRack())) {
      qb.add("self.rack <= :toRack").bind("toRack", inventory.getToRack());
    }

    return qb;
  }

  public Set<StockLocation> getStockLocations(Set<StockLocation> stockLocationSet) {
    Set<StockLocation> newStockLocationSet = new HashSet<>(stockLocationSet);
    List<StockLocation> stockLocationList = null;

    if (CollectionUtils.isNotEmpty(newStockLocationSet)) {
      stockLocationList =
          stockLocationRepository
              .all()
              .filter("self.parentStockLocation IN (?)", newStockLocationSet)
              .fetch();
    }
    if (CollectionUtils.isNotEmpty(stockLocationList)) {
      int oldSize = newStockLocationSet.size();
      newStockLocationSet.addAll(stockLocationList);
      int newSize = newStockLocationSet.size();
      if (newSize > oldSize) {
        newStockLocationSet = getStockLocations(newStockLocationSet);
      }
    }

    return newStockLocationSet;
  }

  @Transactional(rollbackOn = Exception.class)
  public InventoryLine createInventoryLine(Inventory inventory, StockLocationLine stockLocationLine)
      throws AxelorException {
    InventoryLine inventoryLine =
        inventoryLineService.createInventoryLine(
            inventory,
            stockLocationLine.getProduct(),
            stockLocationLine.getCurrentQty(),
            stockLocationLine.getRack(),
            stockLocationLine.getTrackingNumber(),
            null,
            null,
            stockLocationLine.getStockLocation(),
            stockLocationLine.getDetailsStockLocation());
    return inventoryLineRepository.save(inventoryLine);
  }

  public void initInventoryLines(Inventory inventory) {

    if (inventory.getInventoryLineList() == null) {
      inventory.setInventoryLineList(new ArrayList<>());
    } else {
      inventory.getInventoryLineList().clear();
    }
  }

  public Boolean hasRelatedStockMoves(Inventory inventory) {
    return stockMoveRepo
            .all()
            .filter("self.inventory.id = :inventoryId")
            .bind("inventoryId", inventory.getId())
            .count()
        > 0;
  }

  protected void validateInventoryLineList(Inventory inventory) throws AxelorException {
    Long invalidLineCount =
        inventoryLineRepository
            .all()
            .filter("self.inventory = :inventory and self.realQty is null")
            .bind("inventory", inventory)
            .count();
    if (invalidLineCount > 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(StockExceptionMessage.INVENTORY_VALIDATE_INVENTORY_LINE_LIST));
    }
  }

  public String computeTitle(Inventory entity) {
    return entity.getStockLocation().getName()
        + (StringUtils.notEmpty(entity.getDescription())
            ? "-" + org.apache.commons.lang3.StringUtils.abbreviate(entity.getDescription(), 10)
            : "");
  }

  protected String pairKey(Long productId, Long locationId) {
    return productId + ":" + locationId;
  }
}
