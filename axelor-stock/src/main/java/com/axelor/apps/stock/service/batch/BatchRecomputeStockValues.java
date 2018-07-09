package com.axelor.apps.stock.service.batch;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.batch.BatchStrategy;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationLineService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs recalculation of values for all StockLocationLine. Cannot be @{@link
 * com.google.inject.Singleton} because of AbstractBatch in inheritance chain.
 */
public class BatchRecomputeStockValues extends BatchStrategy {
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int STOCK_PRODUCT_ID_IDX = 0;
  private static final int STOCK_PRODUCT_CODE_IDX = 1;
  private static final int STOCK_LOCATION_IDX = 2;
  private static final int STOCK_CURRENT_QUANTITY_IDX = 3;
  private static final int STOCK_FUTURE_QUANTITY_IDX = 4;
  private static final int STOCK_LAST_FUTURE_QUANTITY_IDX = 5;
  private static final int STOCK_AVG_PRICE_IDX = 6;
  private static final int STOCK_RESERVED_QUANTITY_IDX = 7;

  private static final int INVENTORY_PRODUCT_ID_IDX = 0;
  private static final int INVENTORY_PRODUCT_CODE_IDX = 1;
  private static final int INVENTORY_LOCATION_IDX = 2;
  private static final int INVENTORY_DATE_IDX = 3;
  private static final int INVENTORY_RACK_IDX = 4;
  private static final int INVENTORY_QUANTITY_IDX = 5;

  private StockLocationLineService locationLineService;
  private StockLocationRepository locationRepository;
  private ProductRepository productRepository;

  @Inject
  public BatchRecomputeStockValues(
      StockLocationLineService locationLineService,
      StockLocationRepository locationRepository,
      ProductRepository productRepository) {
    this.locationLineService = locationLineService;
    this.locationRepository = locationRepository;
    this.productRepository = productRepository;
  }

  @Override
  protected void process() {
    recomputeStandardLines();
    checkPoint();
    recomputeDetailsLines();
    checkPoint();
  }

  @SuppressWarnings("WeakerAccess") // protected needed for @Transactional
  @Transactional
  protected void recomputeStandardLines() {
    // Cleanup every existing entries, allowing us to also update lines that shouldn't be there
    JPA.em()
        .createQuery(
            "UPDATE StockLocationLine SET avgPrice = 0, currentQty = 0, lastInventoryDateT = NULL, lastInventoryRealQty = 0, rack = NULL, reservedQty = 0, version = version + 1 WHERE detailsStockLocation is null")
        .executeUpdate();

    // FIXME Need to handle the moveline units
    // Get all information about stock, by product and location. Real quantity is defined as the
    // delta input - output on REALIZED stock moves, futureQuantity takes PLANNED and REALIZED moves
    // into account. Reserved quantity is the delta of the reserved quantities of output - input of
    // PLANNED stock moves (see
    // com.axelor.apps.supplychain.service.StockLocationLineServiceSupplychainImpl.updateLocation,
    // planned is current:false, future:true, REALIZED is current:true,future:true).
    @SuppressWarnings("unchecked")
    final List<Object[]> stocks =
        JPA.em()
            .createNativeQuery(
                "SELECT line.product as productId, product.code as code, location.id as location, "
                    // inputs - outputs
                    + "COALESCE(SUM(CASE WHEN move.status_select <> :realizedStatus THEN 0 WHEN move.from_stock_location = location.id THEN -line.real_qty ELSE line.real_qty END), 0) as realQuantity,"
                    + "COALESCE(SUM(CASE WHEN move.status_select <> :realizedStatus AND move.status_select <> :plannedStatus THEN 0 WHEN move.from_stock_location = location.id THEN -line.qty ELSE line.qty END), 0) as futureQuantity,"
                    + "MAX(CASE WHEN move.status_select <> :plannedStatus THEN NULL ELSE move.estimated_date END) as lastFutureQuantity, "
                    // sum(inputValues)/sum(inputQuantities)
                    + "CASE WHEN COALESCE(SUM(CASE WHEN move.status_select <> :realizedStatus OR move.from_stock_location = location.id THEN 0 ELSE line.real_qty END), 0) = 0 THEN 0 ELSE COALESCE("
                    + "SUM(line.real_qty * CASE WHEN move.status_select <> :realizedStatus OR move.from_stock_location = location.id THEN 0 ELSE line.unit_price_untaxed END)"
                    + " / "
                    + "SUM(CASE WHEN move.status_select <> :realizedStatus OR move.from_stock_location = location.id THEN 0 ELSE line.real_qty END)"
                    + ", 0) END as avgPrice, "
                    + "SUM(CASE WHEN move.status_select <> :plannedStatus THEN 0 WHEN move.from_stock_location = location.id THEN line.reserved_qty ELSE -line.reserved_qty END) as reservedQuantity "
                    + "FROM stock_stock_location location "
                    + "INNER JOIN stock_stock_move move ON (move.from_stock_location = location.id OR move.to_stock_location = location.id) "
                    + "INNER JOIN stock_stock_move_line line on (move.id = line.stock_move) "
                    + "INNER JOIN base_product product ON (product.id = line.product) "
                    + "WHERE move.from_stock_location <> move.to_stock_location "
                    + "AND location.type_select != :virtualLocation "
                    + "AND move.status_select != :canceledStatus "
                    + "AND move.real_date <= :today "
                    + "AND product.product_type_select = :storableType "
                    + "GROUP BY line.product, product.code, location.id "
                    + "ORDER BY code, location")
            .setParameter("realizedStatus", StockMoveRepository.STATUS_REALIZED)
            .setParameter("plannedStatus", StockMoveRepository.STATUS_PLANNED)
            .setParameter("canceledStatus", StockMoveRepository.STATUS_CANCELED)
            .setParameter("virtualLocation", StockLocationRepository.TYPE_VIRTUAL)
            .setParameter("today", LocalDate.now())
            .setParameter("storableType", ProductRepository.PRODUCT_TYPE_STORABLE)
            .getResultList();

    for (Object[] row : stocks) {
      log.info(
          "Stock for product #{} ({}) at location #{}: {} (avg price: {}), future quantity: {}, last future quantity: {}",
          row[STOCK_PRODUCT_ID_IDX],
          row[STOCK_PRODUCT_CODE_IDX],
          row[STOCK_LOCATION_IDX],
          row[STOCK_CURRENT_QUANTITY_IDX],
          row[STOCK_AVG_PRICE_IDX],
          row[STOCK_FUTURE_QUANTITY_IDX],
          row[STOCK_LAST_FUTURE_QUANTITY_IDX]);

      final StockLocation location =
          locationRepository.find(((BigInteger) row[STOCK_LOCATION_IDX]).longValue());
      final Product product =
          productRepository.find(((BigInteger) row[STOCK_PRODUCT_ID_IDX]).longValue());
      StockLocationLine line = locationLineService.getOrCreateStockLocationLine(location, product);
      line.setCurrentQty(
          ((BigDecimal) row[STOCK_CURRENT_QUANTITY_IDX]).setScale(10, RoundingMode.HALF_EVEN));
      line.setAvgPrice(
          ((BigDecimal) row[STOCK_AVG_PRICE_IDX]).setScale(10, RoundingMode.HALF_EVEN));
      line.setFutureQty(
          ((BigDecimal) row[STOCK_FUTURE_QUANTITY_IDX]).setScale(10, RoundingMode.HALF_EVEN));
      line.setLastFutureStockMoveDate(
          row[STOCK_LAST_FUTURE_QUANTITY_IDX] == null
              ? null
              : ((Timestamp) row[STOCK_LAST_FUTURE_QUANTITY_IDX]).toLocalDateTime().toLocalDate());
      //      line.setReservedQty(
      //          ((BigDecimal) row[STOCK_RESERVED_QUANTITY_IDX]).setScale(10,
      // RoundingMode.HALF_EVEN));
    }

    // Get information from last inventory on each product. A product can be present several times
    // on the same inventory but only the last recorded line is taken into account (to be consistent
    // with inventory processing)
    @SuppressWarnings("unchecked")
    final List<Object[]> inventories =
        JPA.em()
            .createNativeQuery(
                "SELECT line.product as product, product.code as code, inventory.stock_location as stockLocation, inventory.datet as lastInventoryDate, line.rack as lastRack, line.real_qty lastRealQuantity "
                    + "FROM stock_inventory_line line "
                    + "INNER JOIN base_product product on (product.id = line.product) "
                    + "INNER JOIN stock_inventory inventory ON (inventory.id = line.inventory) "
                    + "LEFT JOIN stock_inventory_line  newerLine ON (newerLine.inventory = inventory.id AND newerLine.product = line.product AND newerLine.real_qty IS NOT NULL AND newerLine.id > line.id) "
                    + "WHERE line.real_qty IS NOT NULL AND newerLine.id IS NULL AND inventory.id = ("
                    + "SELECT innerInventory.id "
                    + "FROM stock_inventory_line innerLine "
                    + "INNER JOIN stock_inventory innerInventory ON (innerInventory.id = innerLine.inventory)"
                    + "WHERE innerLine.product = line.product AND innerInventory.datet <= :today AND innerLine.real_qty IS NOT NULL "
                    + "ORDER BY innerInventory.datet DESC LIMIT 1"
                    + ") "
                    + "ORDER BY code, stockLocation")
            .setParameter("today", LocalDateTime.now())
            .getResultList();

    for (Object[] row : inventories) {
      log.info(
          "Last inventory info for product #{} ({}) on stock location #{}: date: {}, rack: {}, quantity: {}",
          row[INVENTORY_PRODUCT_ID_IDX],
          row[INVENTORY_PRODUCT_CODE_IDX],
          row[INVENTORY_LOCATION_IDX],
          row[INVENTORY_DATE_IDX],
          row[INVENTORY_RACK_IDX],
          row[INVENTORY_QUANTITY_IDX]);
      final StockLocation location =
          locationRepository.find(((BigInteger) row[INVENTORY_LOCATION_IDX]).longValue());
      final Product product =
          productRepository.find(((BigInteger) row[INVENTORY_PRODUCT_ID_IDX]).longValue());
      StockLocationLine line = locationLineService.getOrCreateStockLocationLine(location, product);
      line.setLastInventoryDateT(
          ZonedDateTime.ofInstant(
              ((java.sql.Timestamp) row[INVENTORY_DATE_IDX]).toInstant(),
              ZoneOffset.systemDefault()));
      line.setRack((String) row[INVENTORY_RACK_IDX]);
      line.setLastInventoryRealQty(
          ((BigDecimal) row[INVENTORY_QUANTITY_IDX]).setScale(10, RoundingMode.HALF_EVEN));
    }

    // Cleanup inconsistent records
    JPA.em()
        .createQuery(
            "DELETE StockLocationLine outerLine WHERE outerLine IN (SELECT line FROM StockLocationLine line JOIN line.product product WHERE product.productTypeSelect <> :storableType)")
        .setParameter("storableType", ProductRepository.PRODUCT_TYPE_STORABLE)
        .executeUpdate();
  }

  @Transactional
  protected void recomputeDetailsLines() {}
}
