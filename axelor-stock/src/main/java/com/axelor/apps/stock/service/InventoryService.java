/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.stock.service;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.ProductFamily;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.db.repo.TrackingNumberRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.tool.file.CsvTool;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private InventoryLineService inventoryLineService;

  @Inject private SequenceService sequenceService;

  @Inject private StockConfigService stockConfigService;

  @Inject private ProductRepository productRepo;

  @Inject private InventoryRepository inventoryRepo;

  @Inject private StockMoveRepository stockMoveRepo;

  @Inject private StockLocationLineService stockLocationLineService;

  public Inventory createInventory(
      LocalDate date,
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
          I18n.get(IExceptionMessage.INVENTORY_1));
    }

    Inventory inventory = new Inventory();

    inventory.setInventorySeq(this.getInventorySequence(stockLocation.getCompany()));

    inventory.setDateT(date.atStartOfDay(ZoneOffset.UTC));

    inventory.setDescription(description);

    inventory.setFormatSelect(IAdministration.PDF);

    inventory.setStockLocation(stockLocation);

    inventory.setExcludeOutOfStock(excludeOutOfStock);

    inventory.setIncludeObsolete(includeObsolete);

    inventory.setProductCategory(productCategory);

    inventory.setProductFamily(productFamily);

    inventory.setStatusSelect(InventoryRepository.STATUS_DRAFT);

    return inventory;
  }

  public String getInventorySequence(Company company) throws AxelorException {

    String ref = sequenceService.getSequenceNumber(SequenceRepository.INVENTORY, company);
    if (ref == null)
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVENTORY_2) + " " + company.getName());

    return ref;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Path importFile(Inventory inventory) throws AxelorException {

    List<InventoryLine> inventoryLineList = inventory.getInventoryLineList();
    Path filePath = MetaFiles.getPath(inventory.getImportFile());
    List<String[]> data = this.getDatas(filePath);

    HashMap<String, InventoryLine> inventoryLineMap = this.getInventoryLines(inventory);

    for (String[] line : data) {
      if (line.length < 6)
        throw new AxelorException(
            new Throwable(I18n.get(IExceptionMessage.INVENTORY_3_LINE_LENGHT)),
            inventory,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INVENTORY_3));

      String code = line[1].replace("\"", "");
      String rack = line[2].replace("\"", "");
      String trackingNumberSeq = line[3].replace("\"", "");

      BigDecimal realQty;
      try {
        realQty = new BigDecimal(line[5].replace("\"", ""));
      } catch (NumberFormatException e) {
        throw new AxelorException(
            new Throwable(I18n.get(IExceptionMessage.INVENTORY_3_REAL_QUANTITY)),
            inventory,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INVENTORY_3));
      }

      String description = line[6].replace("\"", "");

      if (inventoryLineMap.containsKey(code)) {
        inventoryLineMap.get(code).setRealQty(realQty);
        inventoryLineMap.get(code).setDescription(description);
      } else {
        BigDecimal currentQty;
        try {
          currentQty = new BigDecimal(line[4].replace("\"", ""));
        } catch (NumberFormatException e) {
          throw new AxelorException(
              new Throwable(I18n.get(IExceptionMessage.INVENTORY_3_CURRENT_QUANTITY)),
              inventory,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.INVENTORY_3));
        }

        InventoryLine inventoryLine = new InventoryLine();
        List<Product> productList =
            productRepo.all().filter("self.code = :code").bind("code", code).fetch();
        if (productList != null && !productList.isEmpty()) {
          if (productList.size() > 1) {
            throw new AxelorException(
                inventory,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(IExceptionMessage.INVENTORY_12) + " " + code);
          }
        }
        Product product = productList.get(0);
        if (product == null
            || !product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE))
          throw new AxelorException(
              inventory,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.INVENTORY_4) + " " + code);
        inventoryLine.setProduct(product);
        inventoryLine.setInventory(inventory);
        inventoryLine.setRack(rack);
        inventoryLine.setCurrentQty(currentQty);
        inventoryLine.setRealQty(realQty);
        inventoryLine.setDescription(description);
        inventoryLine.setTrackingNumber(this.getTrackingNumber(trackingNumberSeq));
        inventoryLineList.add(inventoryLine);
      }
    }
    inventory.setInventoryLineList(inventoryLineList);

    inventoryRepo.save(inventory);
    return filePath;
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
          I18n.get(IExceptionMessage.INVENTORY_5));
    }

    if (data == null || data.isEmpty()) {
      throw new AxelorException(
          new Throwable(I18n.get(IExceptionMessage.INVENTORY_3_DATA_NULL_OR_EMPTY)),
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVENTORY_3));
    }

    data.remove(0); /* Skip headers */
    return data;
  }

  public HashMap<String, InventoryLine> getInventoryLines(Inventory inventory) {
    HashMap<String, InventoryLine> inventoryLineMap = new HashMap<>();

    for (InventoryLine line : inventory.getInventoryLineList()) {
      String key = "";
      if (line.getProduct() != null) {
        key += line.getProduct().getCode();
      }
      if (line.getTrackingNumber() != null) {
        key += line.getTrackingNumber().getTrackingNumberSeq();
      }

      inventoryLineMap.put(key, line);
    }

    return inventoryLineMap;
  }

  public TrackingNumber getTrackingNumber(String sequence) {

    if (sequence != null && !sequence.isEmpty()) {
      return Beans.get(TrackingNumberRepository.class).findBySeq(sequence);
    }
    return null;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public StockMove validateInventory(Inventory inventory) throws AxelorException {
    AppBaseService baseService = Beans.get(AppBaseService.class);
    StockMove stockMove = generateStockMove(inventory);
    storeLastInventoryData(inventory);
    inventory.setStatusSelect(InventoryRepository.STATUS_VALIDATED);
    inventory.setValidatedBy(AuthUtils.getUser());
    inventory.setValidatedOn(baseService.getTodayDate());
    return stockMove;
  }

  private void storeLastInventoryData(Inventory inventory) {
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
          stockLocationLine.setLastInventoryDateT(inventory.getDateT());
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
          detailsStockLocationLine.setLastInventoryDateT(inventory.getDateT());
        }

        String rack = realRacks.get(product);
        if (rack != null) {
          detailsStockLocationLine.setRack(rack);
        }
      }
    }
  }

  public StockMove generateStockMove(Inventory inventory) throws AxelorException {

    StockLocation toStockLocation = inventory.getStockLocation();
    Company company = toStockLocation.getCompany();
    StockMoveService stockMoveService = Beans.get(StockMoveService.class);
    StockMoveLineService stockMoveLineService = Beans.get(StockMoveLineService.class);

    if (company == null) {
      throw new AxelorException(
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVENTORY_6),
          toStockLocation.getName());
    }

    String inventorySeq = inventory.getInventorySeq();

    StockMove stockMove =
        this.createStockMoveHeader(
            inventory, company, toStockLocation, inventory.getDateT().toLocalDate(), inventorySeq);

    for (InventoryLine inventoryLine : inventory.getInventoryLineList()) {
      BigDecimal currentQty = inventoryLine.getCurrentQty();
      BigDecimal realQty = inventoryLine.getRealQty();
      Product product = inventoryLine.getProduct();
      TrackingNumber trackingNumber = inventoryLine.getTrackingNumber();

      if (currentQty.compareTo(realQty) != 0) {
        BigDecimal diff = realQty.subtract(currentQty);
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
                product.getUnit(),
                stockMove,
                StockMoveLineService.TYPE_NULL,
                false,
                BigDecimal.ZERO);
        if (stockMoveLine == null) {
          throw new AxelorException(
              inventory,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.INVENTORY_7) + " " + inventorySeq);
        }
        if (trackingNumber != null && stockMoveLine.getTrackingNumber() == null) {
          stockMoveLine.setTrackingNumber(trackingNumber);
        }
      }
    }
    if (stockMove.getStockMoveLineList() != null) {

      stockMoveService.plan(stockMove);
      stockMoveService.copyQtyToRealQty(stockMove);
      stockMoveService.realize(stockMove, false);
    }
    return stockMove;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public StockMove cancel(Inventory inventory) throws AxelorException {
    StockMove stockMove = stockMoveRepo.findByName(inventory.getInventorySeq());

    if (stockMove != null) {
      StockMoveService stockMoveService = Beans.get(StockMoveService.class);
      stockMoveService.cancel(stockMove);
    }

    inventory.setStatusSelect(InventoryRepository.STATUS_CANCELED);

    return stockMove;
  }

  public StockMove createStockMoveHeader(
      Inventory inventory,
      Company company,
      StockLocation toStockLocation,
      LocalDate inventoryDate,
      String name)
      throws AxelorException {

    StockMove stockMove =
        Beans.get(StockMoveService.class)
            .createStockMove(
                null,
                null,
                company,
                stockConfigService.getInventoryVirtualStockLocation(
                    stockConfigService.getStockConfig(company)),
                toStockLocation,
                inventoryDate,
                inventoryDate,
                null,
                StockMoveRepository.TYPE_INTERNAL);

    stockMove.setName(name);

    return stockMove;
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Boolean fillInventoryLineList(Inventory inventory) throws AxelorException {

    if (inventory.getStockLocation() == null) {
      throw new AxelorException(
          inventory,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.INVENTORY_1));
    }

    this.initInventoryLines(inventory);

    List<? extends StockLocationLine> stockLocationLineList = this.getStockLocationLines(inventory);

    if (stockLocationLineList != null) {
      Boolean succeed = false;
      for (StockLocationLine stockLocationLine : stockLocationLineList) {
        if (stockLocationLine.getTrackingNumber()
            == null) { // if no tracking number on stockLocationLine, check if there is a tracking
          // number on the product
          long numberOfTrackingNumberOnAProduct =
              Beans.get(StockLocationLineRepository.class)
                  .all()
                  .filter(
                      "self.product = ?1 AND self.trackingNumber IS NOT null AND self.detailsStockLocation = ?2",
                      stockLocationLine.getProduct(),
                      inventory.getStockLocation())
                  .count();

          if (numberOfTrackingNumberOnAProduct != 0) { // there is a tracking number on the product
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

  public List<? extends StockLocationLine> getStockLocationLines(Inventory inventory) {

    String query = "(self.stockLocation = ? OR self.detailsStockLocation = ?)";
    List<Object> params = new ArrayList<>();

    params.add(inventory.getStockLocation());
    params.add(inventory.getStockLocation());

    if (inventory.getExcludeOutOfStock()) {
      query += " and self.currentQty > 0";
    }

    if (!inventory.getIncludeObsolete()) {
      query += " and (self.product.endDate > ? or self.product.endDate is null)";
      params.add(inventory.getDateT().toLocalDate());
    }

    if (inventory.getProductFamily() != null) {
      query += " and self.product.productFamily = ?";
      params.add(inventory.getProductFamily());
    }

    if (inventory.getProductCategory() != null) {
      query += " and self.product.productCategory = ?";
      params.add(inventory.getProductCategory());
    }

    return Beans.get(StockLocationLineRepository.class)
        .all()
        .filter(query, params.toArray())
        .fetch();
  }

  public InventoryLine createInventoryLine(
      Inventory inventory, StockLocationLine stockLocationLine) {

    return inventoryLineService.createInventoryLine(
        inventory,
        stockLocationLine.getProduct(),
        stockLocationLine.getCurrentQty(),
        stockLocationLine.getRack(),
        stockLocationLine.getTrackingNumber());
  }

  public void initInventoryLines(Inventory inventory) {

    if (inventory.getInventoryLineList() == null) {
      inventory.setInventoryLineList(new ArrayList<InventoryLine>());
    } else {
      inventory.getInventoryLineList().clear();
    }
  }

  @Transactional
  public void exportInventoryAsCSV(Inventory inventory) throws IOException {

    List<String[]> list = new ArrayList<>();

    for (InventoryLine inventoryLine : inventory.getInventoryLineList()) {
      String[] item = new String[7];
      String realQty = "";

      item[0] = (inventoryLine.getProduct() == null) ? "" : inventoryLine.getProduct().getName();
      item[1] = (inventoryLine.getProduct() == null) ? "" : inventoryLine.getProduct().getCode();
      item[2] = (inventoryLine.getRack() == null) ? "" : inventoryLine.getRack();
      item[3] =
          (inventoryLine.getTrackingNumber() == null)
              ? ""
              : inventoryLine.getTrackingNumber().getTrackingNumberSeq();
      item[4] = inventoryLine.getCurrentQty().toString();
      if (inventory.getStatusSelect() > InventoryRepository.STATUS_DRAFT) {
        realQty = inventoryLine.getRealQty().toString();
      }
      item[5] = realQty;
      item[6] = (inventoryLine.getDescription() == null) ? "" : inventoryLine.getDescription();
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

    String fileName = I18n.get("Inventory") + "_" + inventory.getInventorySeq() + ".csv";
    String filePath = AppSettings.get().get("file.upload.dir");
    Path path = Paths.get(filePath, fileName);
    File file = path.toFile();

    log.debug("File Located at: {}", path);

    String[] headers = {
      I18n.get("Product Name"),
      I18n.get("Product Code"),
      I18n.get("Rack"),
      I18n.get("Tracking Number"),
      I18n.get("Current Quantity"),
      I18n.get("Real Quantity"),
      I18n.get("Description")
    };
    CsvTool.csvWriter(filePath, fileName, ';', '"', headers, list);

    try (InputStream is = new FileInputStream(file)) {
      Beans.get(MetaFiles.class).attach(is, fileName, inventory);
    }
  }
}
