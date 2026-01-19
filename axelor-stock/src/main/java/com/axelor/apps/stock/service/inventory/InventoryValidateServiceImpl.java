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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.Inventory;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.stock.utils.BatchProcessorHelper;
import com.axelor.auth.AuthUtils;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InventoryValidateServiceImpl implements InventoryValidateService {

  protected final InventoryLineRepository inventoryLineRepository;
  protected final InventoryRepository inventoryRepo;
  protected final StockMoveRepository stockMoveRepo;
  protected final StockMoveLineRepository stockMoveLineRepo;
  protected final StockLocationRepository stockLocationRepository;
  protected final StockConfigService stockConfigService;
  protected final StockMoveService stockMoveService;
  protected final StockMoveLineService stockMoveLineService;
  protected final StockLocationLineFetchService stockLocationLineFetchService;
  protected final InventoryStockLocationUpdateService inventoryStockLocationUpdateService;
  protected final AppBaseService appBaseService;
  protected final InventoryLineService inventoryLineService;

  protected static final int INVENTORY_LINE_WITHOUT_STOCK_LOCATION_DISPLAY_LIMIT = 15;

  @Inject
  public InventoryValidateServiceImpl(
      InventoryLineRepository inventoryLineRepository,
      InventoryRepository inventoryRepo,
      StockMoveRepository stockMoveRepo,
      StockMoveLineRepository stockMoveLineRepo,
      StockLocationRepository stockLocationRepository,
      StockConfigService stockConfigService,
      StockMoveService stockMoveService,
      StockMoveLineService stockMoveLineService,
      StockLocationLineFetchService stockLocationLineFetchService,
      InventoryStockLocationUpdateService inventoryStockLocationUpdateService,
      AppBaseService appBaseService,
      InventoryLineService inventoryLineService) {
    this.inventoryLineRepository = inventoryLineRepository;
    this.inventoryRepo = inventoryRepo;
    this.stockMoveRepo = stockMoveRepo;
    this.stockMoveLineRepo = stockMoveLineRepo;
    this.stockLocationRepository = stockLocationRepository;
    this.stockConfigService = stockConfigService;
    this.stockMoveService = stockMoveService;
    this.stockMoveLineService = stockMoveLineService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
    this.inventoryStockLocationUpdateService = inventoryStockLocationUpdateService;
    this.appBaseService = appBaseService;
    this.inventoryLineService = inventoryLineService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(Inventory inventory) throws AxelorException {
    if (inventory.getStatusSelect() == null
        || inventory.getStatusSelect() != InventoryRepository.STATUS_COMPLETED) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(StockExceptionMessage.INVENTORY_VALIDATE_WRONG_STATUS));
    }

    checkMissingStockLocation(inventory);
    inventory.setValidatedOn(
        appBaseService.getTodayDateTime(inventory.getCompany()).toLocalDateTime());
    inventory.setStatusSelect(InventoryRepository.STATUS_VALIDATED);
    inventory.setValidatedBy(AuthUtils.getUser());

    generateStockMoves(inventory, true);
    generateStockMoves(inventory, false);

    inventoryStockLocationUpdateService.storeLastInventoryData(inventory);
  }

  public void generateStockMoves(Inventory inventory, boolean isEnteringStock)
      throws AxelorException {

    final Long inventoryId = inventory.getId();

    Company company = inventory.getCompany();
    StockConfig stockConfig = stockConfigService.getStockConfig(company);
    StockLocation virtualInventoryLocation =
        stockConfigService.getInventoryVirtualStockLocation(stockConfig);

    Map<StockMoveLocationKey, Long> stockMoveIdMap = new HashMap<>();

    final Inventory[] inventoryHolder = {inventoryRepo.find(inventoryId)};
    final StockLocation[] virtualInvLocHolder = {
      virtualInventoryLocation != null
          ? stockLocationRepository.find(virtualInventoryLocation.getId())
          : null
    };

    Query<InventoryLine> query =
        inventoryLineRepository
            .all()
            .filter(
                "self.inventory.id = :inventoryId and self.stockLocation is not null and self.id > :lastSeenId")
            .bind("inventoryId", inventoryId)
            .order("id");

    BatchProcessorHelper.builder()
        .build()
        .<InventoryLine, AxelorException>forEachByQuery(
            query,
            inventoryLine -> {
              StockLocation realStockLocation = inventoryLine.getStockLocation();
              StockLocation fromStockLocation =
                  isEnteringStock ? virtualInvLocHolder[0] : realStockLocation;
              StockLocation toStockLocation =
                  isEnteringStock ? realStockLocation : virtualInvLocHolder[0];

              StockMoveLocationKey key =
                  new StockMoveLocationKey(fromStockLocation.getId(), toStockLocation.getId());
              Long stockMoveId = stockMoveIdMap.get(key);
              StockMove stockMove;
              if (stockMoveId == null) {
                stockMove =
                    generateStockMove(
                        inventoryHolder[0], isEnteringStock, fromStockLocation, toStockLocation);
                stockMoveIdMap.put(key, stockMove.getId());
              } else {
                stockMove = getStockMove(stockMoveId);
              }

              generateStockMoveLines(
                  stockMove,
                  fromStockLocation,
                  toStockLocation,
                  List.of(inventoryLine),
                  isEnteringStock);
            },
            () -> {
              inventoryHolder[0] = inventoryRepo.find(inventoryId);
              virtualInvLocHolder[0] =
                  Optional.ofNullable(virtualInventoryLocation)
                      .map(StockLocation::getId)
                      .map(stockLocationRepository::find)
                      .orElse(null);
            });

    realizeGeneratedStockMoves(stockMoveIdMap);
  }

  /**
   * Generate a stock move from an inventory.
   *
   * @param inventory a realized inventory.
   * @param isEnteringStock whether we want to create incoming or upcoming stock move of this
   *     inventory.
   * @param virtualInventoryLocation
   * @return the generated stock move.
   * @throws AxelorException
   */
  public StockMove generateStockMove(
      Inventory inventory,
      boolean isEnteringStock,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {

    Company company = inventory.getCompany();
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

    stockMove.setInventory(inventory);
    stockMove.setOrigin(inventorySeq);

    return stockMoveRepo.save(stockMove);
  }

  protected void realizeGeneratedStockMoves(Map<StockMoveLocationKey, Long> stockMoveIdMap)
      throws AxelorException {
    if (ObjectUtils.isEmpty(stockMoveIdMap)) {
      return;
    }
    List<Long> ids =
        stockMoveIdMap.values().stream()
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(ids)) {
      return;
    }
    for (Long id : ids) {
      StockMove stockMove = getStockMove(id);
      stockMoveRealize(stockMove);
    }
  }

  protected void stockMoveRealize(StockMove stockMove) throws AxelorException {
    long stockMoveLinesCount = getStockMoveLinesCount(stockMove);
    if (stockMoveLinesCount == 0l) {
      return;
    }
    stockMoveService.plan(getStockMove(stockMove));
    stockMoveService.copyQtyToRealQty(getStockMove(stockMove));
    stockMoveService.realize(getStockMove(stockMove), false);
  }

  protected void generateStockMoveLines(
      StockMove stockMove,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      List<InventoryLine> inventoryLineList,
      boolean isEnteringStock)
      throws AxelorException {
    for (InventoryLine inventoryLine : inventoryLineList) {
      generateStockMoveLines(
          inventoryLine, stockMove, isEnteringStock, fromStockLocation, toStockLocation);
    }
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
      InventoryLine inventoryLine,
      StockMove stockMove,
      boolean isEnteringStock,
      StockLocation fromStockLocation,
      StockLocation toStockLocation)
      throws AxelorException {
    Product product = inventoryLine.getProduct();
    TrackingNumber trackingNumber = inventoryLine.getTrackingNumber();
    BigDecimal diff = inventoryLine.getRealQty().subtract(inventoryLine.getCurrentQty());
    if (!isEnteringStock) {
      diff = diff.negate();
    }
    if (diff.signum() > 0) {

      StockLocationLine stockLocationLine =
          stockLocationLineFetchService.getStockLocationLine(toStockLocation, product);
      BigDecimal unitPrice = getAvgPrice(stockLocationLine);
      if (!inventoryLineService.isPresentInStockLocation(inventoryLine)) {
        unitPrice = inventoryLine.getPrice();
      }

      StockMoveLine stockMoveLine =
          stockMoveLineService.createStockMoveLine(
              product,
              product.getName(),
              product.getDescription(),
              diff,
              unitPrice,
              unitPrice,
              product.getUnit(),
              stockMove,
              StockMoveLineService.TYPE_NULL,
              false,
              BigDecimal.ZERO,
              fromStockLocation,
              toStockLocation);
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
      stockMoveLineRepo.save(stockMoveLine);
    }
  }

  protected BigDecimal getAvgPrice(StockLocationLine stockLocationLine) {
    BigDecimal avgPrice;
    if (stockLocationLine != null) {
      avgPrice = stockLocationLine.getAvgPrice();
    } else {
      avgPrice = BigDecimal.ZERO;
    }
    return avgPrice;
  }

  protected void checkMissingStockLocation(Inventory inventory) throws AxelorException {

    List<InventoryLine> inventoryLinesWithMissingStockLocation =
        inventoryLineRepository
            .all()
            .filter("self.inventory = :inventory and self.stockLocation is null")
            .bind("inventory", inventory)
            .fetch(
                INVENTORY_LINE_WITHOUT_STOCK_LOCATION_DISPLAY_LIMIT
                    + 1); // Fetch limit + 1 to check for excess items

    if (CollectionUtils.isEmpty(inventoryLinesWithMissingStockLocation)) {
      return;
    }

    StringHtmlListBuilder stringHTMLListInventoryLine = new StringHtmlListBuilder();
    inventoryLinesWithMissingStockLocation.stream()
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

  protected long getStockMoveLinesCount(StockMove stockMove) {
    return stockMoveLineRepo
        .all()
        .filter("self.stockMove.id = :stockMoveId")
        .bind("stockMoveId", stockMove.getId())
        .count();
  }

  protected StockMove getStockMove(StockMove stockMove) {
    final EntityManager em = JPA.em();

    if (stockMove == null) {
      return null;
    }

    if (em.contains(stockMove)) {
      return stockMove;
    }
    return getStockMove(stockMove.getId());
  }

  protected StockMove getStockMove(Long id) {
    return stockMoveRepo.find(id);
  }

  private final class StockMoveLocationKey {

    private final Long fromStockLocationId;
    private final Long toStockLocationId;

    public StockMoveLocationKey(Long fromStockLocationId, Long toStockLocationId) {
      this.fromStockLocationId = fromStockLocationId;
      this.toStockLocationId = toStockLocationId;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (this == obj) return true;
      if (!(obj instanceof StockMoveLocationKey)) return false;

      final StockMoveLocationKey other = (StockMoveLocationKey) obj;

      if (this.fromStockLocationId != null
          || other.fromStockLocationId != null
          || this.toStockLocationId != null
          || other.toStockLocationId != null) {
        return Objects.equals(this.fromStockLocationId, other.fromStockLocationId)
            && Objects.equals(this.toStockLocationId, other.toStockLocationId);
      }

      return false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(fromStockLocationId, toStockLocationId);
    }

    @Override
    public String toString() {
      return fromStockLocationId + ":" + toStockLocationId;
    }
  }
}
