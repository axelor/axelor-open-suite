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

import com.axelor.app.AppSettings;
import com.axelor.app.AvailableAppSettings;
import com.axelor.app.internal.AppFilter;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.inventory.dto.InventoryCsvRowDto;
import com.axelor.apps.stock.utils.BatchProcessorHelper;
import com.axelor.common.StringUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.file.temp.TempFiles;
import com.axelor.i18n.I18n;
import com.axelor.i18n.L10n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.helpers.file.CsvHelper;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryImportExportServiceImpl implements InventoryImportExportService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final String X_MARK = "X";

  protected final AppBaseService appBaseService;
  protected final StockLocationLineFetchService stockLocationLineFetchService;
  protected final MetaFiles metaFiles;
  protected final InventoryLineService inventoryLineService;
  protected final InventoryRepository inventoryRepo;
  protected final ProductRepository productRepo;
  protected final StockLocationRepository stockLocationRepository;
  protected final InventoryLineRepository inventoryLineRepository;
  protected final TrackingNumberRepository trackingNumberRepository;

  @Inject
  public InventoryImportExportServiceImpl(
      AppBaseService appBaseService,
      StockLocationLineFetchService stockLocationLineFetchService,
      MetaFiles metaFiles,
      InventoryLineService inventoryLineService,
      InventoryRepository inventoryRepo,
      ProductRepository productRepo,
      StockLocationRepository stockLocationRepository,
      InventoryLineRepository inventoryLineRepository,
      TrackingNumberRepository trackingNumberRepository) {
    this.appBaseService = appBaseService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
    this.metaFiles = metaFiles;
    this.inventoryLineService = inventoryLineService;
    this.inventoryRepo = inventoryRepo;
    this.productRepo = productRepo;
    this.stockLocationRepository = stockLocationRepository;
    this.inventoryLineRepository = inventoryLineRepository;
    this.trackingNumberRepository = trackingNumberRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Path importFile(Inventory inventory) throws AxelorException {
    final Long inventoryId = inventory.getId();
    Path filePath = MetaFiles.getPath(inventory.getImportFile());

    final long startMillis = System.currentTimeMillis();
    Map<String, Long> inventoryLineMap = this.getInventoryLines(inventory);
    List<Long> inventoryLineList = getInventoryLineList(inventory);

    List<CSVRecord> data = this.getDatas(filePath);
    log.debug(
        "Inventory import started: seq='{}' id={} file={}",
        inventory.getInventorySeq(),
        inventoryId,
        filePath);

    int lineCount = 0;
    final int batchSize = getBatchSize();

    inventory = inventoryRepo.find(inventoryId);

    for (CSVRecord line : data) {
      createInventoryLine(inventory, inventoryLineMap, line);
      if (++lineCount % batchSize == 0) {
        log.debug("Imported {} records so far for inventory id={}", lineCount, inventoryId);
        JPA.flush();
        JPA.clear();
        inventory = inventoryRepo.find(inventoryId);
      }
    }

    clearOldInventoryLines(inventoryLineList);

    long elapsedMs = (System.currentTimeMillis() - startMillis);
    log.info(
        "Inventory import finished for id={} - processed {} records ({} s)",
        inventory.getId(),
        lineCount,
        TimeUnit.SECONDS.convert(elapsedMs, TimeUnit.MILLISECONDS));

    return filePath;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MetaFile exportInventoryAsCSV(Inventory inventory) throws IOException {

    List<String[]> list = new ArrayList<>();
    AppSettings appSettings = AppSettings.get();
    final Locale locale =
        Optional.ofNullable(
                LocaleUtils.toLocale(appSettings.get(AvailableAppSettings.DATA_EXPORT_LOCALE)))
            .orElse(AppFilter.getLocale());

    L10n dateFormat = L10n.getInstance(locale);

    Long stockLocationId = inventory.getStockLocation().getId();
    final StockLocation[] stockLocationHolder = {inventory.getStockLocation()};

    Integer statusSelect = inventory.getStatusSelect();

    Query<InventoryLine> queryBase =
        inventoryLineRepository
            .all()
            .filter("self.inventory.id = :inventoryId and self.id > :lastSeenId")
            .bind("inventoryId", inventory.getId())
            .order("id")
            .order("product.code");

    BatchProcessorHelper.of()
        .<InventoryLine>forEachByQuery(
            queryBase,
            inventoryLine -> {
              String[] item =
                  createCsvRow(inventoryLine, statusSelect, stockLocationHolder[0], dateFormat);
              list.add(item);
            },
            () -> {
              stockLocationHolder[0] = stockLocationRepository.find(stockLocationId);
            });

    return createAndUploadCsvFile(inventory, list);
  }

  protected String[] createCsvRow(
      InventoryLine inventoryLine,
      Integer inventoryStatus,
      StockLocation stockLocation,
      L10n dateFormat) {
    String[] item = new String[11];
    item[0] = Optional.ofNullable(inventoryLine.getProduct()).map(Product::getName).orElse("");
    item[1] = Optional.ofNullable(inventoryLine.getProduct()).map(Product::getCode).orElse("");
    item[2] =
        Optional.ofNullable(inventoryLine.getProduct())
            .map(Product::getProductCategory)
            .map(ProductCategory::getName)
            .orElse("");
    item[3] = Optional.ofNullable(inventoryLine.getRack()).orElse("");

    item[4] =
        Optional.ofNullable(inventoryLine.getTrackingNumber())
            .map(TrackingNumber::getTrackingNumberSeq)
            .orElse("");
    item[5] = inventoryLine.getCurrentQty().toString();

    item[6] =
        Optional.ofNullable(inventoryLine.getRealQty())
            .filter(
                qty ->
                    inventoryStatus != InventoryRepository.STATUS_DRAFT
                        && inventoryStatus != InventoryRepository.STATUS_PLANNED)
            .map(BigDecimal::toString)
            .orElse("");

    item[7] = Optional.ofNullable(inventoryLine.getDescription()).orElse("");

    StockLocationLine stockLocationLine =
        stockLocationLineFetchService.getStockLocationLine(
            stockLocation, inventoryLine.getProduct());
    item[8] =
        Optional.ofNullable(stockLocationLine)
            .map(StockLocationLine::getLastInventoryDateT)
            .map(ZonedDateTime::toLocalDate)
            .map(dateFormat::format)
            .orElse("");
    item[9] =
        Optional.ofNullable(inventoryLine.getStockLocation())
            .map(StockLocation::getName)
            .orElse("");

    item[10] =
        inventoryLineService.isPresentInStockLocation(inventoryLine)
            ? X_MARK
            : inventoryLine.getPrice().toString();
    return item;
  }

  protected MetaFile createAndUploadCsvFile(Inventory inventory, List<String[]> list)
      throws IOException {

    final String separator =
        Optional.ofNullable(AppSettings.get().get(AvailableAppSettings.DATA_EXPORT_SEPARATOR))
            .orElse(";");
    String fileName = computeExportFileName(inventoryRepo.find(inventory.getId()));
    File file = TempFiles.createTempFile(fileName, ".csv").toFile();

    log.debug("File Located at: {}", file.getPath());

    String[] headers = InventoryCsvRowDto.headers();
    CsvHelper.csvWriter(file.getParent(), file.getName(), separator.charAt(0), '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      return metaFiles.upload(is, fileName + ".csv");
    }
  }

  protected Map<String, Long> getInventoryLines(Inventory inventory) {
    Map<String, Long> inventoryLineMap = new HashMap<>();

    forEachInventoryLine(
        inventory,
        line -> {
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
          inventoryLineMap.put(key.toString(), line.getId());
        });

    return inventoryLineMap;
  }

  protected List<Long> getInventoryLineList(Inventory inventory) {
    List<Long> inventoryLineIds = new ArrayList<>();

    forEachInventoryLine(inventory, line -> inventoryLineIds.add(line.getId()));

    return inventoryLineIds;
  }

  protected List<CSVRecord> getDatas(Path filePath) throws AxelorException {
    List<CSVRecord> data;
    char separator = ';';
    try {
      data = CsvHelper.csvFileReader(filePath.toString(), separator);
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
    log.debug(
        "CSV parsing complete: {} records read from {}",
        (data == null ? 0 : data.size()),
        filePath);
    return data;
  }

  protected InventoryLine createInventoryLine(
      Inventory inventory, Map<String, Long> inventoryLineMap, CSVRecord line)
      throws AxelorException {
    if (line.size() < 6) {
      throw new AxelorException(
          new Throwable(I18n.get(StockExceptionMessage.INVENTORY_3_LINE_LENGHT)),
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_3));
    }

    InventoryCsvRowDto row = InventoryCsvRowDto.from(line);

    String code = row.getProductCode();
    String rack = row.getRack();
    String trackingNumberSeq = row.getTrackingNumber();
    String description = row.getDescription();
    String stockLocationName = row.getStockLocation();
    StockLocation stockLocation = null;
    if (stockLocationName != null) {
      stockLocation = stockLocationRepository.findByName(stockLocationName);
    }
    String key = code + trackingNumberSeq + stockLocationName;
    BigDecimal realQty = getRealQty(inventory, row.getRealQuantity());
    BigDecimal currentQty = getCurrentQty(inventory, row.getCurrentQuantity());
    Product product = getProduct(inventory, code);
    BigDecimal price = getPrice(row.getPrice());

    if (product == null
        || !product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {
      throw new AxelorException(
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_4) + " " + code);
    }

    InventoryLine inventoryLine;
    if (inventoryLineMap.containsKey(key)) {
      inventoryLine =
          copyAndEditInventoryLine(inventoryLineMap.get(key), description, realQty, price);
    } else {
      inventoryLine =
          createInventoryLine(
              inventory,
              rack,
              trackingNumberSeq,
              description,
              realQty,
              currentQty,
              product,
              stockLocation);
      inventoryLine.setPrice(price);
    }

    if (inventoryLineService.isPresentInStockLocation(inventoryLine)) {
      inventoryLine.setPrice(BigDecimal.ZERO);
    }
    return inventoryLineRepository.save(inventoryLine);
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

  protected InventoryLine copyAndEditInventoryLine(
      Long inventoryLineId, String description, BigDecimal realQty, BigDecimal price)
      throws AxelorException {
    InventoryLine inventoryLine = inventoryLineRepository.find(inventoryLineId);
    InventoryLine inventoryLineResult = inventoryLineRepository.copy(inventoryLine, true);
    inventoryLineResult.setRealQty(realQty);
    inventoryLineResult.setDescription(description);
    inventoryLineResult.setPrice(price);
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

  protected TrackingNumber getTrackingNumber(String sequence, Product product, BigDecimal realQty) {
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
      }
    }
    return trackingNumber;
  }

  protected BigDecimal getRealQty(Inventory inventory, String realQtyValue) throws AxelorException {
    int qtyScale = appBaseService.getAppBase().getNbDecimalDigitForQty();
    try {
      if (!StringUtils.isBlank(realQtyValue)) {
        return new BigDecimal(realQtyValue).setScale(qtyScale, RoundingMode.HALF_UP);
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

  protected BigDecimal getCurrentQty(Inventory inventory, String currentQtyValue)
      throws AxelorException {
    int qtyScale = appBaseService.getAppBase().getNbDecimalDigitForQty();
    try {
      return new BigDecimal(currentQtyValue).setScale(qtyScale, RoundingMode.HALF_UP);
    } catch (NumberFormatException e) {
      throw new AxelorException(
          new Throwable(I18n.get(StockExceptionMessage.INVENTORY_3_CURRENT_QUANTITY)),
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(StockExceptionMessage.INVENTORY_3));
    }
  }

  protected BigDecimal getPrice(String price) {
    if (StringUtils.notEmpty(price)) {
      if (X_MARK.equals(price)) {
        return BigDecimal.ZERO;
      }
      return new BigDecimal(price);
    }
    return BigDecimal.ZERO;
  }

  protected void clearOldInventoryLines(List<Long> inventoryLineIdList) {
    if (CollectionUtils.isEmpty(inventoryLineIdList)) {
      return;
    }

    BatchProcessorHelper.of()
        .<InventoryLine>forEachByIds(
            InventoryLine.class, Set.copyOf(inventoryLineIdList), inventoryLineRepository::remove);
  }

  protected String computeExportFileName(Inventory inventory) {
    return I18n.get("Inventory")
        + "_"
        + inventory.getInventorySeq()
        + "_"
        + appBaseService
            .getTodayDate(inventory.getCompany())
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
  }

  protected void forEachInventoryLine(Inventory inventory, Consumer<InventoryLine> action) {
    Query<InventoryLine> queryBase =
        inventoryLineRepository
            .all()
            .filter("self.inventory.id = :inventoryId AND self.id > :lastSeenId")
            .bind("inventoryId", inventory.getId())
            .order("id");

    BatchProcessorHelper helper = BatchProcessorHelper.builder().flushAfterBatch(false).build();
    helper.<InventoryLine>forEachByQuery(queryBase, action);
  }

  protected int getBatchSize() {
    Integer limit = appBaseService.getAppBase().getDefaultBatchFetchLimit();
    return limit > 0 ? limit : 20;
  }
}
