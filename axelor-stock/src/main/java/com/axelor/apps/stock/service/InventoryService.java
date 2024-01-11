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
package com.axelor.apps.stock.service;

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.StringHTMLListBuilder;
import com.axelor.utils.file.CsvTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final String PRODUCT_NAME = I18n.get("Product Name");
  private final String PRODUCT_CODE = I18n.get("Product Code");
  private final String PRODUCT_CATEGORY = I18n.get("Product category");
  private final String RACK = I18n.get("Rack");
  private final String TRACKING_NUMBER = I18n.get("Tracking Number");
  private final String CURRENT_QUANTITY = I18n.get("Current Quantity");
  private final String REAL_QUANTITY = I18n.get("Real Quantity");
  private final String DESCRIPTION = I18n.get("Description");
  private final String LAST_INVENTORY_DATE = I18n.get("Last Inventory date");
  private final String STOCK_LOCATION = I18n.get("Stock Location");

  static final int INVENTORY_LINE_WITHOUT_STOCK_LOCATION_DISPLAY_LIMIT = 15;

  protected InventoryLineRepository inventoryLineRepository;
  protected InventoryLineService inventoryLineService;
  protected SequenceService sequenceService;
  protected StockConfigService stockConfigService;
  protected ProductRepository productRepo;
  protected InventoryRepository inventoryRepo;
  protected StockMoveRepository stockMoveRepo;
  protected StockLocationLineService stockLocationLineService;
  protected StockMoveService stockMoveService;
  protected StockMoveLineService stockMoveLineService;
  protected StockLocationLineRepository stockLocationLineRepository;
  protected TrackingNumberRepository trackingNumberRepository;
  protected AppBaseService appBaseService;
  protected StockLocationRepository stockLocationRepository;

  @Inject
  public InventoryService(
      InventoryLineService inventoryLineService,
      SequenceService sequenceService,
      StockConfigService stockConfigService,
      ProductRepository productRepo,
      InventoryRepository inventoryRepo,
      StockMoveRepository stockMoveRepo,
      StockLocationLineService stockLocationLineService,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockLocationLineRepository stockLocationLineRepository,
      TrackingNumberRepository trackingNumberRepository,
      AppBaseService appBaseService,
      StockLocationRepository stockLocationRepository,
      InventoryLineRepository inventoryLineRepository) {
    this.inventoryLineService = inventoryLineService;
    this.sequenceService = sequenceService;
    this.stockConfigService = stockConfigService;
    this.productRepo = productRepo;
    this.inventoryRepo = inventoryRepo;
    this.stockMoveRepo = stockMoveRepo;
    this.stockLocationLineService = stockLocationLineService;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationLineRepository = stockLocationLineRepository;
    this.trackingNumberRepository = trackingNumberRepository;
    this.appBaseService = appBaseService;
    this.stockLocationRepository = stockLocationRepository;
    this.inventoryLineRepository = inventoryLineRepository;
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
            SequenceRepository.INVENTORY, company, Inventory.class, "inventorySeq");
    if (ref == null)
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_2) + " " + company.getName());

    return ref;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Path importFile(Inventory inventory) throws AxelorException {

    HashMap<String, InventoryLine> inventoryLineMap = this.getInventoryLines(inventory);

    Path filePath = MetaFiles.getPath(inventory.getImportFile());
    List<String[]> data = this.getDatas(filePath);

    List<String> headers = Arrays.asList(data.get(0));
    inventory.clearInventoryLineList();

    data.remove(0); /* Skip headers */

    for (String[] line : data) {
      inventory.addInventoryLineListItem(
          createInventoryLine(inventory, inventoryLineMap, headers, line));
    }

    inventoryRepo.save(inventory);

    return filePath;
  }

  protected InventoryLine createInventoryLine(
      Inventory inventory,
      HashMap<String, InventoryLine> inventoryLineMap,
      List<String> headers,
      String[] line)
      throws AxelorException {
    if (line.length < 6) {
      throw new AxelorException(
          new Throwable(I18n.get(StockExceptionMessage.INVENTORY_3_LINE_LENGHT)),
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_3));
    }

    String code = line[headers.indexOf(PRODUCT_CODE)].replace("\"", "");
    String rack = line[headers.indexOf(RACK)].replace("\"", "");
    String trackingNumberSeq = line[headers.indexOf(TRACKING_NUMBER)].replace("\"", "");
    String description = line[headers.indexOf(DESCRIPTION)].replace("\"", "");
    String stockLocationName = line[headers.indexOf(STOCK_LOCATION)].replace("\"", "");
    StockLocation stockLocation = null;
    if (stockLocationName != null) {
      stockLocation = stockLocationRepository.findByName(stockLocationName);
    }
    String key = code + trackingNumberSeq + stockLocationName;
    BigDecimal realQty = getRealQty(inventory, headers, line);
    BigDecimal currentQty = getCurrentQty(inventory, headers, line);
    Product product = getProduct(inventory, code);

    if (product == null
        || !product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
      throw new AxelorException(
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_4) + " " + code);
    }

    if (inventoryLineMap.containsKey(key)) {
      return copyAndEditInventoryLine(inventoryLineMap.get(key), description, realQty);
    } else {
      return createInventoryLine(
          inventory,
          rack,
          trackingNumberSeq,
          description,
          realQty,
          currentQty,
          product,
          stockLocation);
    }
  }

  protected InventoryLine copyAndEditInventoryLine(
      InventoryLine inventoryLine, String description, BigDecimal realQty) throws AxelorException {

    // There is not one to many for inventoryLine, so true or false is the same.
    InventoryLine inventoryLineResult = inventoryLineRepository.copy(inventoryLine, true);
    inventoryLineResult.setRealQty(realQty);
    inventoryLineResult.setDescription(description);
    if (inventoryLineResult.getTrackingNumber() != null) {
      inventoryLineResult.getTrackingNumber().setCounter(realQty);
    }
    inventoryLineService.compute(inventoryLineResult, inventoryLineResult.getInventory());
    return inventoryLineResult;
  }

  protected InventoryLine createInventoryLine(
      Inventory inventory,
      String rack,
      String trackingNumberSeq,
      String description,
      BigDecimal realQty,
      BigDecimal currentQty,
      Product product,
      StockLocation stockLocation)
      throws AxelorException {

    return inventoryLineService.createInventoryLine(
        inventory,
        product,
        currentQty,
        rack,
        this.getTrackingNumber(trackingNumberSeq, product, realQty),
        realQty,
        description,
        stockLocation,
        null);
  }

  protected Product getProduct(Inventory inventory, String code) throws AxelorException {
    List<Product> productList =
        productRepo
            .all()
            .filter("self.code = :code AND self.dtype = 'Product'")
            .bind("code", code)
            .fetch();
    if (CollectionUtils.isNotEmpty(productList)) {
      if (productList.size() > 1) {
        throw new AxelorException(
            inventory,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.INVENTORY_12) + " " + code);
      }
      return productList.get(0);
    } else {
      throw new AxelorException(
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_4) + " " + code);
    }
  }

  protected BigDecimal getCurrentQty(Inventory inventory, List<String> headers, String[] line)
      throws AxelorException {
    int qtyScale = appBaseService.getAppBase().getNbDecimalDigitForQty();
    try {
      return new BigDecimal(line[headers.indexOf(CURRENT_QUANTITY)].replace("\"", ""))
          .setScale(qtyScale, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      throw new AxelorException(
          new Throwable(I18n.get(StockExceptionMessage.INVENTORY_3_CURRENT_QUANTITY)),
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_3));
    }
  }

  protected BigDecimal getRealQty(Inventory inventory, List<String> headers, String[] line)
      throws AxelorException {
    int qtyScale = appBaseService.getAppBase().getNbDecimalDigitForQty();
    try {
      if (!StringUtils.isBlank(line[headers.indexOf(REAL_QUANTITY)])) {
        return new BigDecimal(line[headers.indexOf(REAL_QUANTITY)])
            .setScale(qtyScale, RoundingMode.HALF_UP);
      }

    } catch (NumberFormatException e) {
      throw new AxelorException(
          new Throwable(I18n.get(StockExceptionMessage.INVENTORY_3_REAL_QUANTITY)),
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_3));
    }
    return null;
  }

  public List<String[]> getDatas(Path filePath) throws AxelorException {

    List<String[]> data = null;
    char separator = ';';
    try {
      data = CsvTool.cSVFileReader(filePath.toString(), separator);
    } catch (Exception e) {
      throw new AxelorException(
          e.getCause(),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_5));
    }

    if (data == null || data.isEmpty()) {
      throw new AxelorException(
          new Throwable(I18n.get(StockExceptionMessage.INVENTORY_3_DATA_NULL_OR_EMPTY)),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_3));
    }

    return data;
  }

  public HashMap<String, InventoryLine> getInventoryLines(Inventory inventory) {
    HashMap<String, InventoryLine> inventoryLineMap = new HashMap<>();

    for (InventoryLine line : inventory.getInventoryLineList()) {
      StringBuilder key = new StringBuilder();
      if (line.getProduct() != null) {
        key.append(line.getProduct().getCode());
      }
      if (line.getTrackingNumber() != null) {
        key.append(line.getTrackingNumber().getTrackingNumberSeq());
      }
      if (line.getStockLocation() != null) {
        key.append(line.getStockLocation().getName());
      }

      inventoryLineMap.put(key.toString(), line);
    }

    return inventoryLineMap;
  }

  public TrackingNumber getTrackingNumber(String sequence, Product product, BigDecimal realQty) {

    TrackingNumber trackingNumber = null;

    if (!StringUtils.isEmpty(sequence)) {
      trackingNumber =
          trackingNumberRepository
              .all()
              .filter("self.trackingNumberSeq = ?1 and self.product = ?2", sequence, product)
              .fetchOne();

      if (trackingNumber == null) {
        trackingNumber = new TrackingNumber();
        trackingNumber.setTrackingNumberSeq(sequence);
        trackingNumber.setProduct(product);
        trackingNumber.setCounter(realQty);
      }
    }

    return trackingNumber;
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
    inventory.getInventoryLineList().stream().forEach(this::updateCurrentQty);
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

  @Transactional(rollbackOn = {Exception.class})
  public void validateInventory(Inventory inventory) throws AxelorException {
    if (inventory.getStatusSelect() == null
        || inventory.getStatusSelect() != InventoryRepository.STATUS_COMPLETED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_VALIDATE_WRONG_STATUS));
    }

    checkMissingStockLocation(inventory.getInventoryLineList());
    inventory.setValidatedOn(
        appBaseService.getTodayDateTime(inventory.getCompany()).toLocalDateTime());
    inventory.setStatusSelect(InventoryRepository.STATUS_VALIDATED);
    inventory.setValidatedBy(AuthUtils.getUser());
    generateStockMoves(inventory, true);
    generateStockMoves(inventory, false);
    storeLastInventoryData(inventory);
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
            .filter("self.originTypeSelect = :originTypeSelect AND self.originId = :originId")
            .bind("originTypeSelect", StockMoveRepository.ORIGIN_INVENTORY)
            .bind("originId", inventory.getId())
            .fetch();

    for (StockMove stockMove : stockMoveList) {
      stockMoveService.cancel(stockMove);
    }

    inventory.setStatusSelect(InventoryRepository.STATUS_CANCELED);
  }

  protected void storeLastInventoryData(Inventory inventory) {
    Map<Pair<Product, TrackingNumber>, BigDecimal> realQties = new HashMap<>();
    Map<Product, BigDecimal> consolidatedRealQties = new HashMap<>();
    Map<Product, String> realRacks = new HashMap<>();

    List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();

    if (inventoryLineList != null) {
      for (InventoryLine inventoryLine : inventoryLineList) {
        Product product = inventoryLine.getProduct();
        TrackingNumber trackingNumber = inventoryLine.getTrackingNumber();

        realQties.put(Pair.of(product, trackingNumber), inventoryLine.getRealQty());

        BigDecimal realQty = consolidatedRealQties.getOrDefault(product, BigDecimal.ZERO);
        realQty = realQty.add(inventoryLine.getRealQty());
        consolidatedRealQties.put(product, realQty);

        realRacks.put(product, inventoryLine.getRack());
      }
    }

    List<StockLocationLine> stockLocationLineList =
        inventory.getStockLocation().getStockLocationLineList();

    if (stockLocationLineList != null) {
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        Product product = stockLocationLine.getProduct();
        BigDecimal realQty = consolidatedRealQties.get(product);
        if (realQty != null) {
          stockLocationLine.setLastInventoryRealQty(realQty);
          stockLocationLine.setLastInventoryDateT(
              inventory.getValidatedOn().atZone(ZoneOffset.UTC));
        }

        String rack = realRacks.get(product);
        if (rack != null) {
          stockLocationLine.setRack(rack);
        }
      }
    }

    List<StockLocationLine> detailsStockLocationLineList =
        inventory.getStockLocation().getDetailsStockLocationLineList();

    if (detailsStockLocationLineList != null) {
      for (StockLocationLine detailsStockLocationLine : detailsStockLocationLineList) {
        Product product = detailsStockLocationLine.getProduct();
        TrackingNumber trackingNumber = detailsStockLocationLine.getTrackingNumber();
        BigDecimal realQty = realQties.get(Pair.of(product, trackingNumber));
        if (realQty != null) {
          detailsStockLocationLine.setLastInventoryRealQty(realQty);
          detailsStockLocationLine.setLastInventoryDateT(
              inventory.getValidatedOn().atZone(ZoneOffset.UTC));
        }

        String rack = realRacks.get(product);
        if (rack != null) {
          detailsStockLocationLine.setRack(rack);
        }
      }
    }
  }

  public void generateStockMoves(Inventory inventory, boolean isEnteringStock)
      throws AxelorException {
    Set<StockLocation> stockLocationSet = new HashSet<>();

    for (InventoryLine inventoryLine : inventory.getInventoryLineList()) {
      if (inventoryLine.getStockLocation() != null) {
        stockLocationSet.add(inventoryLine.getStockLocation());
      }
    }

    InventoryLineRepository inventoryLineRepo = Beans.get(InventoryLineRepository.class);
    for (StockLocation stockLocation : stockLocationSet) {
      List<InventoryLine> inventoryLineList =
          inventoryLineRepo
              .all()
              .filter(
                  "self.stockLocation.id = ? AND self.inventory.id = ?",
                  stockLocation.getId(),
                  inventory.getId())
              .fetch();

      generateStockMove(inventory, inventoryLineList, isEnteringStock);
    }
  }

  /**
   * Generate a stock move from an inventory.
   *
   * @param inventory a realized inventory.
   * @param isEnteringStock whether we want to create incoming or upcoming stock move of this
   *     inventory.
   * @return the generated stock move.
   * @throws AxelorException
   */
  public StockMove generateStockMove(
      Inventory inventory, List<InventoryLine> inventoryLineList, boolean isEnteringStock)
      throws AxelorException {

    StockLocation toStockLocation;
    StockLocation fromStockLocation;
    Company company = inventory.getCompany();
    if (isEnteringStock) {
      toStockLocation = inventoryLineList.get(0).getStockLocation();
      fromStockLocation =
          stockConfigService.getInventoryVirtualStockLocation(
              stockConfigService.getStockConfig(company));
    } else {
      toStockLocation =
          stockConfigService.getInventoryVirtualStockLocation(
              stockConfigService.getStockConfig(company));
      fromStockLocation = inventoryLineList.get(0).getStockLocation();
    }

    String inventorySeq = inventory.getInventorySeq();

    LocalDate inventoryDate = inventory.getPlannedStartDateT().toLocalDate();
    LocalDate realDate = inventory.getValidatedOn().toLocalDate();
    StockMove stockMove =
        stockMoveService.createStockMove(
            null,
            null,
            company,
            fromStockLocation,
            toStockLocation,
            realDate,
            inventoryDate,
            null,
            StockMoveRepository.TYPE_INTERNAL);

    stockMove.setName(inventorySeq);

    stockMove.setOriginTypeSelect(StockMoveRepository.ORIGIN_INVENTORY);
    stockMove.setOriginId(inventory.getId());
    stockMove.setOrigin(inventorySeq);

    for (InventoryLine inventoryLine : inventoryLineList) {
      generateStockMoveLines(inventoryLine, stockMove, isEnteringStock);
    }
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {

      stockMoveService.plan(stockMove);
      stockMoveService.copyQtyToRealQty(stockMove);
      stockMoveService.realize(stockMove, false);
    }
    return stockMove;
  }

  /**
   * Generate lines for the given stock move. Depending if we are creating an incoming or outgoing
   * stock move, we only create stock move line with positive quantity.
   *
   * @param inventoryLine an inventory line
   * @param stockMove a stock move being created
   * @param isEnteringStock whether we are creating an incoming or outgoing stock move.
   * @throws AxelorException
   */
  protected void generateStockMoveLines(
      InventoryLine inventoryLine, StockMove stockMove, boolean isEnteringStock)
      throws AxelorException {
    Product product = inventoryLine.getProduct();
    TrackingNumber trackingNumber = inventoryLine.getTrackingNumber();
    BigDecimal diff = inventoryLine.getRealQty().subtract(inventoryLine.getCurrentQty());
    if (!isEnteringStock) {
      diff = diff.negate();
    }
    if (diff.signum() > 0) {
      BigDecimal avgPrice;
      StockLocationLine stockLocationLine =
          stockLocationLineService.getStockLocationLine(stockMove.getToStockLocation(), product);
      if (stockLocationLine != null) {
        avgPrice = stockLocationLine.getAvgPrice();
      } else {
        avgPrice = BigDecimal.ZERO;
      }

      StockMoveLine stockMoveLine =
          stockMoveLineService.createStockMoveLine(
              product,
              product.getName(),
              product.getDescription(),
              diff,
              avgPrice,
              avgPrice,
              product.getUnit(),
              stockMove,
              StockMoveLineService.TYPE_NULL,
              false,
              BigDecimal.ZERO);
      if (stockMoveLine == null) {
        throw new AxelorException(
            inventoryLine.getInventory(),
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(StockExceptionMessage.INVENTORY_7)
                + " "
                + inventoryLine.getInventory().getInventorySeq());
      }
      if (trackingNumber != null && stockMoveLine.getTrackingNumber() == null) {
        stockMoveLine.setTrackingNumber(trackingNumber);
      }
    }
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

    List<? extends StockLocationLine> stockLocationLineList = this.getStockLocationLines(inventory);

    if (stockLocationLineList != null) {
      Boolean succeed = false;
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        if (ObjectUtils.isEmpty(stockLocationLine.getTrackingNumber())) {
          long numberOfTrackingNumberOnAProduct =
              stockLocationLineList.stream()
                  .filter(
                      sll ->
                          stockLocationLine.getProduct() != null
                              && stockLocationLine.getProduct().equals(sll.getProduct())
                              && sll.getTrackingNumber() != null
                              && inventory.getStockLocation().equals(sll.getDetailsStockLocation()))
                  .count();

          if (numberOfTrackingNumberOnAProduct != 0) {
            continue;
          }
        }
        inventory.addInventoryLineListItem(this.createInventoryLine(inventory, stockLocationLine));
        succeed = true;
      }
      inventoryRepo.save(inventory);
      return succeed;
    }
    return null;
  }

  public List<StockLocationLine> getStockLocationLines(Inventory inventory) {
    String query = "";
    List<Object> params = new ArrayList<>();
    Set<StockLocation> stockLocations = new HashSet<>();

    if (inventory.getIncludeSubStockLocation()) {
      stockLocations.add(inventory.getStockLocation());
      stockLocations = this.getStockLocations(stockLocations);
      if (CollectionUtils.isNotEmpty(stockLocations)) {
        query = "(self.stockLocation IN (?) OR self.detailsStockLocation IN (?))";
        params.add(stockLocations);
        params.add(stockLocations);
      }
    } else {
      query = "(self.stockLocation = ? OR self.detailsStockLocation = ?)";
      params.add(inventory.getStockLocation());
      params.add(inventory.getStockLocation());
    }

    if (inventory.getExcludeOutOfStock()) {
      query += " and self.currentQty > 0";
    }

    if (!inventory.getIncludeObsolete()) {
      query += " and (self.product.endDate > ? or self.product.endDate is null)";
      params.add(inventory.getPlannedEndDateT().toLocalDate());
    }

    if (inventory.getProductFamily() != null) {
      query += " and self.product.productFamily = ?";
      params.add(inventory.getProductFamily());
    }

    if (inventory.getProductCategory() != null) {
      query += " and self.product.productCategory = ?";
      params.add(inventory.getProductCategory());
    }

    if (inventory.getProduct() != null) {
      query += " and self.product = ?";
      params.add(inventory.getProduct());
    }

    if (!Strings.isNullOrEmpty(inventory.getFromRack())) {
      query += " and self.rack >= ?";
      params.add(inventory.getFromRack());
    }

    if (!Strings.isNullOrEmpty(inventory.getToRack())) {
      query += " and self.rack <= ?";
      params.add(inventory.getToRack());
    }

    return stockLocationLineRepository.all().filter(query, params.toArray()).fetch();
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

  public InventoryLine createInventoryLine(Inventory inventory, StockLocationLine stockLocationLine)
      throws AxelorException {

    return inventoryLineService.createInventoryLine(
        inventory,
        stockLocationLine.getProduct(),
        stockLocationLine.getCurrentQty(),
        stockLocationLine.getRack(),
        stockLocationLine.getTrackingNumber(),
        null,
        null,
        stockLocationLine.getStockLocation(),
        stockLocationLine.getDetailsStockLocation());
  }

  public void initInventoryLines(Inventory inventory) {

    if (inventory.getInventoryLineList() == null) {
      inventory.setInventoryLineList(new ArrayList<InventoryLine>());
    } else {
      inventory.getInventoryLineList().clear();
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public MetaFile exportInventoryAsCSV(Inventory inventory) throws IOException {

    List<String[]> list = new ArrayList<>();

    AppSettings appSettings = AppSettings.get();
    final Locale locale =
        Optional.ofNullable(
                LocaleUtils.toLocale(appSettings.get(AvailableAppSettings.DATA_EXPORT_LOCALE)))
            .orElse(AppFilter.getLocale());
    final String separator =
        Optional.ofNullable(appSettings.get(AvailableAppSettings.DATA_EXPORT_SEPARATOR))
            .orElse(";");
    L10n dateFormat = L10n.getInstance(locale);

    for (InventoryLine inventoryLine : inventory.getInventoryLineList()) {
      String[] item = new String[10];
      String realQty = "";

      item[0] = (inventoryLine.getProduct() == null) ? "" : inventoryLine.getProduct().getName();
      item[1] = (inventoryLine.getProduct() == null) ? "" : inventoryLine.getProduct().getCode();
      item[2] =
          (inventoryLine.getProduct() == null)
              ? ""
              : ((inventoryLine.getProduct().getProductCategory() == null)
                  ? ""
                  : inventoryLine.getProduct().getProductCategory().getName());
      item[3] = (inventoryLine.getRack() == null) ? "" : inventoryLine.getRack();
      item[4] =
          (inventoryLine.getTrackingNumber() == null)
              ? ""
              : inventoryLine.getTrackingNumber().getTrackingNumberSeq();
      item[5] = inventoryLine.getCurrentQty().toString();
      if (inventoryLine.getRealQty() != null
          && inventory.getStatusSelect() != InventoryRepository.STATUS_DRAFT
          && inventory.getStatusSelect() != InventoryRepository.STATUS_PLANNED) {
        realQty = inventoryLine.getRealQty().toString();
      }
      item[6] = realQty;
      item[7] = (inventoryLine.getDescription() == null) ? "" : inventoryLine.getDescription();

      String lastInventoryDateTString = "";
      StockLocationLine stockLocationLine =
          stockLocationLineService.getStockLocationLine(
              inventory.getStockLocation(), inventoryLine.getProduct());
      if (stockLocationLine != null) {
        ZonedDateTime lastInventoryDateT = stockLocationLine.getLastInventoryDateT();
        lastInventoryDateTString =
            lastInventoryDateT == null ? "" : dateFormat.format(lastInventoryDateT.toLocalDate());
      }
      item[8] = lastInventoryDateTString;
      item[9] =
          (inventoryLine.getStockLocation() == null)
              ? ""
              : inventoryLine.getStockLocation().getName();
      list.add(item);
    }

    Collections.sort(
        list,
        new Comparator<String[]>() { // sort the list by code product
          @Override
          public int compare(String[] strings, String[] otherStrings) {
            return strings[1].compareTo(otherStrings[1]);
          }
        });

    String fileName = computeExportFileName(inventory);

    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();

    log.debug("File Located at: {}", file.getPath());

    String[] headers = {
      PRODUCT_NAME,
      PRODUCT_CODE,
      PRODUCT_CATEGORY,
      RACK,
      TRACKING_NUMBER,
      CURRENT_QUANTITY,
      REAL_QUANTITY,
      DESCRIPTION,
      LAST_INVENTORY_DATE,
      STOCK_LOCATION
    };
    CsvTool.csvWriter(file.getParent(), file.getName(), separator.charAt(0), '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      return Beans.get(MetaFiles.class).upload(is, fileName + ".csv");
    }
  }

  public String computeExportFileName(Inventory inventory) {
    return I18n.get("Inventory")
        + "_"
        + inventory.getInventorySeq()
        + "_"
        + appBaseService
            .getTodayDate(inventory.getCompany())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  public List<StockMove> findStockMoves(Inventory inventory) {
    return stockMoveRepo
        .all()
        .filter(
            "self.originTypeSelect = ?1 AND self.originId = ?2",
            StockMoveRepository.ORIGIN_INVENTORY,
            inventory.getId())
        .fetch();
  }

  public String computeTitle(Inventory entity) {
    return entity.getStockLocation().getName()
        + (!Strings.isNullOrEmpty(entity.getDescription())
            ? "-" + StringUtils.abbreviate(entity.getDescription(), 10)
            : "");
  }

  protected void validateInventoryLineList(Inventory inventory) throws AxelorException {
    if (inventory.getInventoryLineList().stream()
        .anyMatch(inventoryLine -> inventoryLine.getRealQty() == null)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(StockExceptionMessage.INVENTORY_VALIDATE_INVENTORY_LINE_LIST));
    }
  }

  protected void checkMissingStockLocation(List<InventoryLine> inventoryLineList)
      throws AxelorException {
    List<InventoryLine> inventoryLinesWithMissingStockLocation = new ArrayList<>();
    for (InventoryLine inventoryLine : inventoryLineList) {
      if (inventoryLine.getStockLocation() == null) {
        inventoryLinesWithMissingStockLocation.add(inventoryLine);
      }
    }

    StringHTMLListBuilder stringHTMLListInventoryLine = new StringHTMLListBuilder();
    inventoryLinesWithMissingStockLocation.stream()
        .limit(INVENTORY_LINE_WITHOUT_STOCK_LOCATION_DISPLAY_LIMIT)
        .forEach(
            inventoryLine -> {
              if (inventoryLine.getTrackingNumber() == null) {
                stringHTMLListInventoryLine.append(inventoryLine.getProduct().getFullName());
              } else {
                stringHTMLListInventoryLine.append(
                    String.format(
                        "%s : %s",
                        inventoryLine.getProduct().getFullName(),
                        inventoryLine.getTrackingNumber().getTrackingNumberSeq()));
              }
            });

    if (!inventoryLinesWithMissingStockLocation.isEmpty()) {
      if (inventoryLinesWithMissingStockLocation.size()
          > INVENTORY_LINE_WITHOUT_STOCK_LOCATION_DISPLAY_LIMIT) {
        stringHTMLListInventoryLine.append("...");
      }
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          String.format(
              I18n.get(StockExceptionMessage.INVENTORY_LINE_STOCK_LOCATION_MISSING),
              stringHTMLListInventoryLine));
    }
  }
}
