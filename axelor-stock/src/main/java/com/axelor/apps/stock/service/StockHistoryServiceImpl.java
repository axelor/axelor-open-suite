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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockHistoryLine;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockHistoryLineManagementRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.utils.file.CsvTool;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StockHistoryServiceImpl implements StockHistoryService {

  protected StockMoveLineRepository stockMoveLineRepository;
  protected UnitConversionService unitConversionService;
  protected StockLocationRepository stockLocationRepository;
  protected StockHistoryLineManagementRepository stockHistoryLineRepository;

  @Inject
  public StockHistoryServiceImpl(
      StockMoveLineRepository stockMoveLineRepository,
      UnitConversionService unitConversionService,
      StockLocationRepository stockLocationRepository,
      StockHistoryLineManagementRepository stockHistoryLineRepository) {
    this.stockMoveLineRepository = stockMoveLineRepository;
    this.unitConversionService = unitConversionService;
    this.stockLocationRepository = stockLocationRepository;
    this.stockHistoryLineRepository = stockHistoryLineRepository;
  }

  @Override
  @Transactional(rollbackOn = Exception.class)
  public List<StockHistoryLine> computeAndSaveStockHistoryLineList(
      Long productId, Long companyId, Long stockLocationId, LocalDate beginDate, LocalDate endDate)
      throws AxelorException {

    return stockHistoryLineRepository.save(
        this.computeStockHistoryLineList(
            productId, companyId, stockLocationId, beginDate, endDate));
  }

  public List<StockHistoryLine> computeStockHistoryLineList(
      Long productId, Long companyId, Long stockLocationId, LocalDate beginDate, LocalDate endDate)
      throws AxelorException {
    List<StockHistoryLine> stockHistoryLineList = new ArrayList<>();
    List<Long> stockLocationIdList = new ArrayList<>();
    if (stockLocationId == null) {
      stockLocationIdList.addAll(
          stockLocationRepository.all()
              .filter(
                  "self.typeSelect != :typeSelect AND self.company.id = :company "
                      + "AND self.stockLocationLineList.product = :product")
              .bind("typeSelect", StockLocationRepository.TYPE_VIRTUAL).bind("company", companyId)
              .bind("product", productId).select("id").fetch(0, 0).stream()
              .map(m -> (Long) m.get("id"))
              .collect(Collectors.toList()));
    } else {
      stockLocationIdList.add(stockLocationId);
    }

    CompanyRepository companyRepo = Beans.get(CompanyRepository.class);
    ProductRepository productRepo = Beans.get(ProductRepository.class);

    // one line per month
    for (LocalDate periodBeginDate = beginDate.withDayOfMonth(1);
        periodBeginDate.isBefore(endDate);
        periodBeginDate = periodBeginDate.plusMonths(1)) {
      LocalDate periodEndDate = periodBeginDate.plusMonths(1);
      StockHistoryLine stockHistoryLine = new StockHistoryLine();
      Company company = companyRepo.find(companyId);
      Product product = productRepo.find(productId);
      stockHistoryLine.setProduct(product);
      stockHistoryLine.setCompany(company);
      stockHistoryLine.setLabel(periodBeginDate.toString());
      stockHistoryLine.setPeriod(
          Beans.get(PeriodService.class)
              .getActivePeriod(periodBeginDate, company, YearRepository.TYPE_CIVIL));
      if (!stockLocationIdList.isEmpty()) {
        fetchAndFillResultForStockHistoryQuery(
            stockHistoryLine,
            productId,
            companyId,
            stockLocationIdList,
            periodBeginDate,
            periodEndDate,
            true);
        fetchAndFillResultForStockHistoryQuery(
            stockHistoryLine,
            productId,
            companyId,
            stockLocationIdList,
            periodBeginDate,
            periodEndDate,
            false);
      }
      stockHistoryLineList.add(stockHistoryLine);
    }
    StockHistoryLine totalStockHistoryLine = createStockHistoryTotalLine(stockHistoryLineList);
    StockHistoryLine avgStockHistoryLine =
        createStockHistoryAvgLine(stockHistoryLineList, totalStockHistoryLine);
    stockHistoryLineList.add(totalStockHistoryLine);
    stockHistoryLineList.add(avgStockHistoryLine);

    // result lines

    return stockHistoryLineList;
  }

  public String getStockHistoryLineExportName(String productName) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm");
    return I18n.get("Stock History")
        + " - "
        + productName
        + " - "
        + Beans.get(AppBaseService.class).getTodayDateTime().toLocalDateTime().format(dtf);
  }

  private String[] getStockHistoryLineExportHeader() {

    String[] headers = new String[7];
    headers[0] = I18n.get("Label");
    headers[1] = I18n.get("Nbr of incoming moves");
    headers[2] = I18n.get("Incoming quantity");
    headers[3] = I18n.get("Incoming amount");
    headers[4] = I18n.get("Nbr of outgoing moves");
    headers[5] = I18n.get("Outgoing quantity");
    headers[6] = I18n.get("Outgoing amount");
    return headers;
  }

  public MetaFile exportStockHistoryLineList(
      List<StockHistoryLine> stockHistoryLineList, String fileName) throws IOException {
    List<String[]> list = new ArrayList<>();

    for (StockHistoryLine stockHistoryLine : stockHistoryLineList) {
      String[] item = new String[7];
      item[0] = stockHistoryLine.getLabel();
      item[1] = stockHistoryLine.getCountIncMvtStockPeriod().toString();
      item[2] = stockHistoryLine.getSumIncQtyPeriod().toString();
      item[3] = stockHistoryLine.getPriceIncStockMovePeriod().toString();
      item[4] = stockHistoryLine.getCountOutMvtStockPeriod().toString();
      item[5] = stockHistoryLine.getSumOutQtyPeriod().toString();
      item[6] = stockHistoryLine.getPriceOutStockMovePeriod().toString();
      list.add(item);
    }

    File file = MetaFiles.createTempFile(fileName, ".csv").toFile();
    fileName = fileName + ".csv";

    CsvTool.csvWriter(
        file.getParent(), file.getName(), ';', getStockHistoryLineExportHeader(), list);

    FileInputStream inStream = new FileInputStream(file);
    MetaFile metaFile = Beans.get(MetaFiles.class).upload(inStream, fileName);

    return metaFile;
  }

  protected void computeAvgOutQtyOn12PastMonth(
      StockHistoryLine stockHistoryLine,
      Long productId,
      Long companyId,
      List<Long> stockLocationIdList,
      LocalDate periodBeginDate)
      throws AxelorException {
    String filter =
        "self.product.id = :productId "
            + "AND self.stockMove.statusSelect = :realized "
            + "AND self.stockMove.company.id = :companyId "
            + "AND self.stockMove.realDate >= :beginDate "
            + "AND self.stockMove.realDate < :endDate "
            + "AND self.stockMove.fromStockLocation.id IN :stockLocationIdList ";

    List<StockMoveLine> stockMoveLineList =
        stockMoveLineRepository
            .all()
            .filter(filter)
            .bind("productId", productId)
            .bind("companyId", companyId)
            .bind("stockLocationIdList", stockLocationIdList)
            .bind("realized", StockMoveRepository.STATUS_REALIZED)
            .bind("beginDate", periodBeginDate.minusMonths(12))
            .bind("endDate", periodBeginDate)
            .fetch();

    BigDecimal avgOutQtyOn12PastMonth = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      // quantity in product unit
      BigDecimal qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      avgOutQtyOn12PastMonth = avgOutQtyOn12PastMonth.add(qtyConverted);
    }
    avgOutQtyOn12PastMonth =
        avgOutQtyOn12PastMonth.divide(
            new BigDecimal(12),
            Beans.get(AppBaseService.class).getNbDecimalDigitForQty(),
            RoundingMode.HALF_EVEN);
    stockHistoryLine.setAvgOutQtyOn12PastMonth(avgOutQtyOn12PastMonth);
  }

  protected void fetchAndFillResultForStockHistoryQuery(
      StockHistoryLine stockHistoryLine,
      Long productId,
      Long companyId,
      List<Long> stockLocationIdList,
      LocalDate periodBeginDate,
      LocalDate periodEndDate,
      boolean incoming)
      throws AxelorException {
    String filter =
        "self.product.id = :productId "
            + "AND self.stockMove.statusSelect = :realized "
            + "AND self.stockMove.company.id = :companyId "
            + "AND self.stockMove.realDate >= :beginDate "
            + "AND self.stockMove.realDate < :endDate ";

    if (incoming) {
      filter += "AND self.stockMove.toStockLocation.id IN :stockLocationIdList ";
    } else {
      filter += "AND self.stockMove.fromStockLocation.id IN :stockLocationIdList ";
    }

    List<StockMoveLine> stockMoveLineList =
        stockMoveLineRepository
            .all()
            .filter(filter)
            .bind("productId", productId)
            .bind("companyId", companyId)
            .bind("stockLocationIdList", stockLocationIdList)
            .bind("realized", StockMoveRepository.STATUS_REALIZED)
            .bind("beginDate", periodBeginDate)
            .bind("endDate", periodEndDate)
            .fetch();
    if (incoming) {
      fillIncomingStockHistoryLineFields(stockHistoryLine, stockMoveLineList);
    } else {
      fillOutgoingStockHistoryLineFields(stockHistoryLine, stockMoveLineList);
      computeAvgOutQtyOn12PastMonth(
          stockHistoryLine, productId, companyId, stockLocationIdList, periodBeginDate);
    }
  }

  protected void fillIncomingStockHistoryLineFields(
      StockHistoryLine stockHistoryLine, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    stockHistoryLine.setCountIncMvtStockPeriod(
        Math.toIntExact(
            stockMoveLineList.stream().map(StockMoveLine::getStockMove).distinct().count()));
    BigDecimal sumIncQtyPeriod = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      // quantity in product unit
      BigDecimal qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      sumIncQtyPeriod = sumIncQtyPeriod.add(qtyConverted);
    }
    stockHistoryLine.setSumIncQtyPeriod(sumIncQtyPeriod);
    stockHistoryLine.setPriceIncStockMovePeriod(
        stockMoveLineList.stream()
            .map(StockMoveLine::getCompanyUnitPriceUntaxed)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  protected void fillOutgoingStockHistoryLineFields(
      StockHistoryLine stockHistoryLine, List<StockMoveLine> stockMoveLineList)
      throws AxelorException {

    stockHistoryLine.setCountOutMvtStockPeriod(
        Math.toIntExact(
            stockMoveLineList.stream().map(StockMoveLine::getStockMove).distinct().count()));
    BigDecimal sumOutQtyPeriod = BigDecimal.ZERO;
    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      // quantity in product unit
      BigDecimal qtyConverted =
          unitConversionService.convert(
              stockMoveLine.getUnit(),
              stockMoveLine.getProduct().getUnit(),
              stockMoveLine.getRealQty(),
              stockMoveLine.getRealQty().scale(),
              stockMoveLine.getProduct());
      sumOutQtyPeriod = sumOutQtyPeriod.add(qtyConverted);
    }
    stockHistoryLine.setSumOutQtyPeriod(sumOutQtyPeriod);
    stockHistoryLine.setPriceOutStockMovePeriod(
        stockMoveLineList.stream()
            .map(StockMoveLine::getCompanyUnitPriceUntaxed)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
  }

  /**
   * Create a line labelled "Total", summing each field in the table.
   *
   * @param stockHistoryLineList
   * @return the created line.
   */
  protected StockHistoryLine createStockHistoryTotalLine(
      List<StockHistoryLine> stockHistoryLineList) {
    StockHistoryLine stockHistoryLine = new StockHistoryLine();
    stockHistoryLine.setLabel(I18n.get("Total"));

    Integer countIncMvtStock =
        stockHistoryLineList.stream()
            .map(StockHistoryLine::getCountIncMvtStockPeriod)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
    stockHistoryLine.setCountIncMvtStockPeriod(countIncMvtStock);

    BigDecimal sumIncQtyPeriod =
        stockHistoryLineList.stream()
            .map(StockHistoryLine::getSumIncQtyPeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setSumIncQtyPeriod(sumIncQtyPeriod);

    BigDecimal priceIncStockMove =
        stockHistoryLineList.stream()
            .map(StockHistoryLine::getPriceIncStockMovePeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setPriceIncStockMovePeriod(priceIncStockMove);

    Integer countOutMvtStock =
        stockHistoryLineList.stream()
            .map(StockHistoryLine::getCountOutMvtStockPeriod)
            .filter(Objects::nonNull)
            .mapToInt(Integer::intValue)
            .sum();
    stockHistoryLine.setCountOutMvtStockPeriod(countOutMvtStock);

    BigDecimal sumOutQtyPeriod =
        stockHistoryLineList.stream()
            .map(StockHistoryLine::getSumOutQtyPeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setSumOutQtyPeriod(sumOutQtyPeriod);

    BigDecimal priceOutStockMove =
        stockHistoryLineList.stream()
            .map(StockHistoryLine::getPriceOutStockMovePeriod)
            .filter(Objects::nonNull)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    stockHistoryLine.setPriceOutStockMovePeriod(priceOutStockMove);

    return stockHistoryLine;
  }

  /**
   * Create a line labelled "Average", using a list of stockHistory and a line containing totals.
   *
   * @param stockHistoryLineList
   * @param totalStockHistoryLine
   * @return the created line.
   */
  protected StockHistoryLine createStockHistoryAvgLine(
      List<StockHistoryLine> stockHistoryLineList, StockHistoryLine totalStockHistoryLine) {
    StockHistoryLine stockHistoryLine = new StockHistoryLine();
    stockHistoryLine.setLabel(I18n.get("Average"));

    int qtyScale = Beans.get(AppBaseService.class).getAppBase().getNbDecimalDigitForQty();

    int sizeOfList = stockHistoryLineList.size();
    if (sizeOfList == 0) {
      return stockHistoryLine;
    }

    stockHistoryLine.setCountIncMvtStockPeriod(
        totalStockHistoryLine.getCountIncMvtStockPeriod() / sizeOfList);

    stockHistoryLine.setSumIncQtyPeriod(
        totalStockHistoryLine
            .getSumIncQtyPeriod()
            .divide(BigDecimal.valueOf(sizeOfList), qtyScale, RoundingMode.HALF_UP));

    stockHistoryLine.setPriceIncStockMovePeriod(
        totalStockHistoryLine
            .getPriceIncStockMovePeriod()
            .divide(BigDecimal.valueOf(sizeOfList), 2, RoundingMode.HALF_UP));

    stockHistoryLine.setCountOutMvtStockPeriod(
        totalStockHistoryLine.getCountOutMvtStockPeriod() / sizeOfList);

    stockHistoryLine.setSumOutQtyPeriod(
        totalStockHistoryLine
            .getSumOutQtyPeriod()
            .divide(BigDecimal.valueOf(sizeOfList), qtyScale, RoundingMode.HALF_UP));

    stockHistoryLine.setPriceOutStockMovePeriod(
        totalStockHistoryLine
            .getPriceOutStockMovePeriod()
            .divide(BigDecimal.valueOf(sizeOfList), 2, RoundingMode.HALF_UP));

    return stockHistoryLine;
  }
}
