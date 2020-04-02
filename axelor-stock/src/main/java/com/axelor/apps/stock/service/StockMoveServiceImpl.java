/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.FreightCarrierMode;
import com.axelor.apps.stock.db.Incoterm;
import com.axelor.apps.stock.db.InventoryLine;
import com.axelor.apps.stock.db.PartnerStockSettings;
import com.axelor.apps.stock.db.ShipmentMode;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.InventoryLineRepository;
import com.axelor.apps.stock.db.repo.InventoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.apps.stock.report.IReport;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveServiceImpl implements StockMoveService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected StockMoveLineService stockMoveLineService;
  protected AppBaseService appBaseService;
  protected StockMoveRepository stockMoveRepo;
  protected PartnerProductQualityRatingService partnerProductQualityRatingService;
  private SequenceService sequenceService;
  private StockMoveLineRepository stockMoveLineRepo;

  @Inject
  public StockMoveServiceImpl(
      StockMoveLineService stockMoveLineService,
      SequenceService sequenceService,
      StockMoveLineRepository stockMoveLineRepository,
      AppBaseService appBaseService,
      StockMoveRepository stockMoveRepository,
      PartnerProductQualityRatingService partnerProductQualityRatingService) {
    this.stockMoveLineService = stockMoveLineService;
    this.sequenceService = sequenceService;
    this.stockMoveLineRepo = stockMoveLineRepository;
    this.appBaseService = appBaseService;
    this.stockMoveRepo = stockMoveRepository;
    this.partnerProductQualityRatingService = partnerProductQualityRatingService;
  }

  @Override
  public BigDecimal compute(StockMove stockMove) {
    BigDecimal exTaxTotal = BigDecimal.ZERO;
    if (stockMove.getStockMoveLineList() != null && !stockMove.getStockMoveLineList().isEmpty()) {
      for (StockMoveLine stockMoveLine : stockMove.getStockMoveLineList()) {
        exTaxTotal =
            exTaxTotal.add(
                stockMoveLine.getRealQty().multiply(stockMoveLine.getUnitPriceUntaxed()));
      }
    }
    return exTaxTotal.setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Méthode permettant d'obtenir la séquence du StockMove.
   *
   * @param stockMoveType Type de mouvement de stock
   * @param company la société
   * @return la chaine contenant la séquence du StockMove
   * @throws AxelorException Aucune séquence de StockMove n'a été configurée
   */
  @Override
  public String getSequenceStockMove(int stockMoveType, Company company) throws AxelorException {

    String ref = "";

    switch (stockMoveType) {
      case StockMoveRepository.TYPE_INTERNAL:
        ref = sequenceService.getSequenceNumber(SequenceRepository.INTERNAL, company);
        if (ref == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.STOCK_MOVE_1),
              company.getName());
        }
        break;

      case StockMoveRepository.TYPE_INCOMING:
        ref = sequenceService.getSequenceNumber(SequenceRepository.INCOMING, company);
        if (ref == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.STOCK_MOVE_2),
              company.getName());
        }
        break;

      case StockMoveRepository.TYPE_OUTGOING:
        ref = sequenceService.getSequenceNumber(SequenceRepository.OUTGOING, company);
        if (ref == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.STOCK_MOVE_3),
              company.getName());
        }
        break;

      default:
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.STOCK_MOVE_4),
            company.getName());
    }

    return ref;
  }

  @Override
  /**
   * Generic method to create any stock move
   *
   * @param fromAddress
   * @param toAddress
   * @param company
   * @param clientPartner
   * @param fromStockLocation
   * @param toStockLocation
   * @param realDate
   * @param estimatedDate
   * @param description
   * @param shipmentMode
   * @param freightCarrierMode
   * @param carrierPartner
   * @param forwarderPartner
   * @param incoterm
   * @return
   * @throws AxelorException No Stock move sequence defined
   */
  public StockMove createStockMove(
      Address fromAddress,
      Address toAddress,
      Company company,
      Partner clientPartner,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      LocalDate realDate,
      LocalDate estimatedDate,
      String description,
      ShipmentMode shipmentMode,
      FreightCarrierMode freightCarrierMode,
      Partner carrierPartner,
      Partner forwarderPartner,
      Incoterm incoterm,
      int typeSelect)
      throws AxelorException {

    StockMove stockMove =
        this.createStockMove(
            fromAddress,
            toAddress,
            company,
            fromStockLocation,
            toStockLocation,
            realDate,
            estimatedDate,
            description,
            typeSelect);
    stockMove.setPartner(clientPartner);
    stockMove.setShipmentMode(shipmentMode);
    stockMove.setFreightCarrierMode(freightCarrierMode);
    stockMove.setCarrierPartner(carrierPartner);
    stockMove.setForwarderPartner(forwarderPartner);
    stockMove.setIncoterm(incoterm);
    stockMove.setIsIspmRequired(this.getDefaultISPM(clientPartner, toAddress));

    return stockMove;
  }

  /**
   * Generic method to create any stock move for internal stock move (without partner information)
   *
   * @param fromAddress
   * @param toAddress
   * @param company
   * @param fromStockLocation
   * @param toStockLocation
   * @param realDate
   * @param estimatedDate
   * @param description
   * @param typeSelect
   * @return
   * @throws AxelorException No Stock move sequence defined
   */
  @Override
  public StockMove createStockMove(
      Address fromAddress,
      Address toAddress,
      Company company,
      StockLocation fromStockLocation,
      StockLocation toStockLocation,
      LocalDate realDate,
      LocalDate estimatedDate,
      String description,
      int typeSelect)
      throws AxelorException {

    StockMove stockMove = new StockMove();

    if (stockMove.getStockMoveLineList() == null) {
      stockMove.setStockMoveLineList(new ArrayList<>());
    }

    stockMove.setFromAddress(fromAddress);
    stockMove.setToAddress(toAddress);
    this.computeAddressStr(stockMove);
    stockMove.setCompany(company);
    stockMove.setStatusSelect(StockMoveRepository.STATUS_DRAFT);
    stockMove.setRealDate(realDate);
    stockMove.setEstimatedDate(estimatedDate);
    stockMove.setFromStockLocation(fromStockLocation);
    stockMove.setToStockLocation(toStockLocation);
    stockMove.setDescription(description);

    stockMove.setPrintingSettings(
        Beans.get(TradingNameService.class).getDefaultPrintingSettings(null, company));

    stockMove.setTypeSelect(typeSelect);
    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
        && stockMove.getPartner() != null) {
      setDefaultAutoMailSettings(stockMove);
    }

    return stockMove;
  }

  /**
   * Set automatic mail configuration from the partner.
   *
   * @param stockMove
   */
  protected void setDefaultAutoMailSettings(StockMove stockMove) throws AxelorException {
    Partner partner = stockMove.getPartner();
    Company company = stockMove.getCompany();

    PartnerStockSettings mailSettings =
        Beans.get(PartnerStockSettingsService.class).getOrCreateMailSettings(partner, company);
    boolean stockMoveAutomaticMail = mailSettings.getStockMoveAutomaticMail();
    Template stockMoveMessageTemplate = mailSettings.getStockMoveMessageTemplate();

    stockMove.setStockMoveAutomaticMail(stockMoveAutomaticMail);
    stockMove.setStockMoveMessageTemplate(stockMoveMessageTemplate);
  }

  /**
   * @param clientPartner
   * @param toAddress
   * @return default value for {@link StockMove#isIspmRequired}
   */
  protected boolean getDefaultISPM(Partner clientPartner, Address toAddress) {
    if (clientPartner != null && clientPartner.getIsIspmRequired()) {
      return true;
    } else {
      return toAddress != null
          && toAddress.getAddressL7Country() != null
          && toAddress.getAddressL7Country().getIsIspmRequired();
    }
  }

  @Override
  public int getStockMoveType(StockLocation fromStockLocation, StockLocation toStockLocation) {

    if (fromStockLocation.getTypeSelect() == StockLocationRepository.TYPE_INTERNAL
        && toStockLocation.getTypeSelect() == StockLocationRepository.TYPE_INTERNAL) {
      return StockMoveRepository.TYPE_INTERNAL;
    } else if (fromStockLocation.getTypeSelect() != StockLocationRepository.TYPE_INTERNAL
        && toStockLocation.getTypeSelect() == StockLocationRepository.TYPE_INTERNAL) {
      return StockMoveRepository.TYPE_INCOMING;
    } else if (fromStockLocation.getTypeSelect() == StockLocationRepository.TYPE_INTERNAL
        && toStockLocation.getTypeSelect() != StockLocationRepository.TYPE_INTERNAL) {
      return StockMoveRepository.TYPE_OUTGOING;
    }
    return 0;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void validate(StockMove stockMove) throws AxelorException {

    this.plan(stockMove);
    this.realize(stockMove);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void goBackToDraft(StockMove stockMove) throws AxelorException {
    if (stockMove.getStatusSelect() != StockMoveRepository.STATUS_CANCELED) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.STOCK_MOVE_CANNOT_GO_BACK_TO_DRAFT));
    }

    stockMove.setStatusSelect(StockMoveRepository.STATUS_DRAFT);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void plan(StockMove stockMove) throws AxelorException {

    LOG.debug(
        "Planification du mouvement de stock : {} ", new Object[] {stockMove.getStockMoveSeq()});

    if (stockMove.getExTaxTotal().compareTo(BigDecimal.ZERO) == 0) {
      stockMove.setExTaxTotal(compute(stockMove));
    }

    StockLocation fromStockLocation = stockMove.getFromStockLocation();
    StockLocation toStockLocation = stockMove.getToStockLocation();

    if (fromStockLocation == null) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STOCK_MOVE_5),
          stockMove.getName());
    }
    if (toStockLocation == null) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STOCK_MOVE_6),
          stockMove.getName());
    }

    // Set the type select
    if (stockMove.getTypeSelect() == null || stockMove.getTypeSelect() == 0) {
      stockMove.setTypeSelect(this.getStockMoveType(fromStockLocation, toStockLocation));
    }

    String draftSeq;

    // Set the sequence.
    if (sequenceService.isEmptyOrDraftSequenceNumber(stockMove.getStockMoveSeq())) {
      draftSeq = stockMove.getStockMoveSeq();
      stockMove.setStockMoveSeq(
          getSequenceStockMove(stockMove.getTypeSelect(), stockMove.getCompany()));
    } else {
      draftSeq = null;
    }

    if (Strings.isNullOrEmpty(stockMove.getName())
        || draftSeq != null && stockMove.getName().startsWith(draftSeq)) {
      stockMove.setName(computeName(stockMove));
    }

    if (stockMove.getEstimatedDate() == null) {
      stockMove.setEstimatedDate(appBaseService.getTodayDate());
    }

    copyPlannedStockMovLines(stockMove);

    stockMoveLineService.updateLocations(
        fromStockLocation,
        toStockLocation,
        stockMove.getStatusSelect(),
        StockMoveRepository.STATUS_PLANNED,
        stockMove.getPlannedStockMoveLineList(),
        stockMove.getEstimatedDate(),
        false);

    stockMove.setStatusSelect(StockMoveRepository.STATUS_PLANNED);

    stockMoveRepo.save(stockMove);
  }

  protected void copyPlannedStockMovLines(StockMove stockMove) {
    List<StockMoveLine> stockMoveLineList =
        MoreObjects.firstNonNull(stockMove.getStockMoveLineList(), Collections.emptyList());
    stockMove.clearPlannedStockMoveLineList();

    stockMoveLineList.forEach(
        stockMoveLine -> {
          StockMoveLine copy = stockMoveLineRepo.copy(stockMoveLine, false);
          copy.setArchived(true);
          stockMove.addPlannedStockMoveLineListItem(copy);
        });
  }

  @Override
  public String realize(StockMove stockMove) throws AxelorException {
    return realize(stockMove, true);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public String realize(StockMove stockMove, boolean checkOngoingInventoryFlag)
      throws AxelorException {
    LOG.debug(
        "Réalisation du mouvement de stock : {} ", new Object[] {stockMove.getStockMoveSeq()});

    if (checkOngoingInventoryFlag) {
      checkOngoingInventory(stockMove);
    }

    String newStockSeq = null;
    stockMoveLineService.checkConformitySelection(stockMove);
    checkExpirationDates(stockMove);

    stockMoveLineService.updateLocations(
        stockMove.getFromStockLocation(),
        stockMove.getToStockLocation(),
        stockMove.getStatusSelect(),
        StockMoveRepository.STATUS_CANCELED,
        stockMove.getPlannedStockMoveLineList(),
        stockMove.getEstimatedDate(),
        false);

    stockMoveLineService.updateLocations(
        stockMove.getFromStockLocation(),
        stockMove.getToStockLocation(),
        StockMoveRepository.STATUS_DRAFT,
        StockMoveRepository.STATUS_REALIZED,
        stockMove.getStockMoveLineList(),
        stockMove.getEstimatedDate(),
        true);

    updatePlannedRealQties(stockMove);

    stockMoveLineService.storeCustomsCodes(stockMove.getStockMoveLineList());

    stockMove.setStatusSelect(StockMoveRepository.STATUS_REALIZED);
    stockMove.setRealDate(appBaseService.getTodayDate());
    resetWeights(stockMove);

    try {
      if (stockMove.getIsWithBackorder() && mustBeSplit(stockMove.getPlannedStockMoveLineList())) {
        Optional<StockMove> newStockMove =
            copyAndSplitStockMove(stockMove, stockMove.getPlannedStockMoveLineList());
        if (newStockMove.isPresent()) {
          newStockSeq = newStockMove.get().getStockMoveSeq();
        }
      }

      if (stockMove.getIsWithReturnSurplus()
          && mustBeSplit(stockMove.getPlannedStockMoveLineList())) {
        Optional<StockMove> newStockMove =
            copyAndSplitStockMoveReverse(stockMove, stockMove.getPlannedStockMoveLineList(), true);
        if (newStockMove.isPresent()) {
          if (newStockSeq != null) {
            newStockSeq = newStockSeq + " " + newStockMove.get().getStockMoveSeq();
          } else {
            newStockSeq = newStockMove.get().getStockMoveSeq();
          }
        }
      }
    } finally {
      computeWeights(stockMove);
      stockMoveRepo.save(stockMove);
    }

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
      partnerProductQualityRatingService.calculate(stockMove);
    } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING
        && stockMove.getStockMoveAutomaticMail()) {
      Template template = stockMove.getStockMoveMessageTemplate();
      if (template == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.STOCK_MOVE_MISSING_TEMPLATE),
            stockMove);
      }
      try {
        Beans.get(TemplateMessageService.class).generateAndSendMessage(stockMove, template);
      } catch (Exception e) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage(), stockMove);
      }
    }

    return newStockSeq;
  }

  private void updatePlannedRealQties(StockMove stockMove) {
    List<StockMoveLine> stockMoveLineList = new ArrayList<>();
    stockMoveLineList.addAll(
        MoreObjects.firstNonNull(stockMove.getStockMoveLineList(), Collections.emptyList()));
    List<StockMoveLine> plannedStockMoveLineList =
        MoreObjects.firstNonNull(stockMove.getPlannedStockMoveLineList(), Collections.emptyList());

    for (StockMoveLine plannedStockMoveLine : plannedStockMoveLineList) {
      plannedStockMoveLine.setRealQty(BigDecimal.ZERO);
      Iterator<StockMoveLine> it = stockMoveLineList.iterator();

      while (it.hasNext()) {
        StockMoveLine stockMoveLine = it.next();
        Optional<Product> productOpt = Optional.ofNullable(stockMoveLine.getProduct());
        BigDecimal unitPriceUntaxed = stockMoveLine.getUnitPriceUntaxed();
        BigDecimal unitPriceTaxed = stockMoveLine.getUnitPriceTaxed();

        if (productOpt.isPresent()
            && productOpt.get().equals(plannedStockMoveLine.getProduct())
            && unitPriceUntaxed.compareTo(plannedStockMoveLine.getUnitPriceUntaxed()) == 0
            && unitPriceTaxed.compareTo(plannedStockMoveLine.getUnitPriceTaxed()) == 0) {
          plannedStockMoveLine.setRealQty(
              plannedStockMoveLine.getRealQty().add(stockMoveLine.getRealQty()));
          it.remove();
        }
      }
    }
  }

  /**
   * Check and raise an exception if the provided stock move is involved in an ongoing inventory.
   *
   * @param stockMove
   * @throws AxelorException
   */
  private void checkOngoingInventory(StockMove stockMove) throws AxelorException {
    List<StockLocation> stockLocationList = new ArrayList<>();

    if (stockMove.getFromStockLocation().getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
      stockLocationList.add(stockMove.getFromStockLocation());
    }

    if (stockMove.getToStockLocation().getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
      stockLocationList.add(stockMove.getToStockLocation());
    }

    if (stockLocationList.isEmpty()) {
      return;
    }

    List<Product> productList =
        stockMove
            .getStockMoveLineList()
            .stream()
            .map(StockMoveLine::getProduct)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (productList.isEmpty()) {
      return;
    }

    InventoryLineRepository inventoryLineRepo = Beans.get(InventoryLineRepository.class);

    InventoryLine inventoryLine =
        inventoryLineRepo
            .all()
            .filter(
                "self.inventory.statusSelect BETWEEN :startStatus AND :endStatus\n"
                    + "AND self.inventory.stockLocation IN (:stockLocationList)\n"
                    + "AND self.product IN (:productList)")
            .bind("startStatus", InventoryRepository.STATUS_IN_PROGRESS)
            .bind("endStatus", InventoryRepository.STATUS_COMPLETED)
            .bind("stockLocationList", stockLocationList)
            .bind("productList", productList)
            .fetchOne();

    if (inventoryLine != null) {
      throw new AxelorException(
          inventoryLine,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.STOCK_MOVE_19),
          inventoryLine.getInventory().getInventorySeq());
    }
  }

  private void resetWeights(StockMove stockMove) {
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();

    if (stockMoveLineList == null) {
      return;
    }

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      stockMoveLine.setTotalNetWeight(null);
    }
  }

  private void computeWeights(StockMove stockMove) throws AxelorException {
    boolean weightsRequired = checkWeightsRequired(stockMove);
    StockConfig stockConfig = stockMove.getCompany().getStockConfig();
    Unit endUnit = stockConfig != null ? stockConfig.getCustomsWeightUnit() : null;

    if (weightsRequired && endUnit == null) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(IExceptionMessage.STOCK_MOVE_17));
    }

    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();

    if (stockMoveLineList == null) {
      return;
    }

    UnitConversionService unitConversionService = Beans.get(UnitConversionService.class);

    for (StockMoveLine stockMoveLine : stockMoveLineList) {
      Product product = stockMoveLine.getProduct();

      if (product == null
          || !ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())) {
        continue;
      }

      BigDecimal netWeight = stockMoveLine.getNetWeight();

      if (netWeight.signum() == 0) {
        Unit startUnit = product.getWeightUnit();

        if (startUnit != null) {
          netWeight =
              unitConversionService.convert(
                  startUnit,
                  endUnit,
                  product.getNetWeight(),
                  product.getNetWeight().scale(),
                  product);
          stockMoveLine.setNetWeight(netWeight);
        }
      }

      if (netWeight.signum() != 0) {
        BigDecimal totalNetWeight = netWeight.multiply(stockMoveLine.getRealQty());
        stockMoveLine.setTotalNetWeight(totalNetWeight);
      } else if (weightsRequired) {
        throw new AxelorException(
            stockMove,
            TraceBackRepository.CATEGORY_NO_VALUE,
            I18n.get(IExceptionMessage.STOCK_MOVE_18));
      }
    }
  }

  @Override
  public boolean checkWeightsRequired(StockMove stockMove) {
    Address fromAddress = getFromAddress(stockMove);
    Address toAddress = getToAddress(stockMove);

    Country fromCountry = fromAddress != null ? fromAddress.getAddressL7Country() : null;
    Country toCountry = toAddress != null ? toAddress.getAddressL7Country() : null;

    return (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING)
        && fromCountry != null
        && toCountry != null
        && !fromCountry.equals(toCountry);
  }

  @Override
  public Address getFromAddress(StockMove stockMove) {
    Address fromAddress = stockMove.getFromAddress();
    if (fromAddress == null && stockMove.getFromStockLocation() != null) {
      fromAddress = stockMove.getFromStockLocation().getAddress();
    }
    return fromAddress;
  }

  @Override
  public Address getToAddress(StockMove stockMove) {
    Address toAddress = stockMove.getToAddress();
    if (toAddress == null && stockMove.getToStockLocation() != null) {
      toAddress = stockMove.getToStockLocation().getAddress();
    }
    return toAddress;
  }

  @Override
  public boolean mustBeSplit(List<StockMoveLine> stockMoveLineList) {

    for (StockMoveLine stockMoveLine : stockMoveLineList) {

      if (stockMoveLine.getRealQty().compareTo(stockMoveLine.getQty()) != 0) {

        return true;
      }
    }

    return false;
  }

  @Override
  public Optional<StockMove> copyAndSplitStockMove(StockMove stockMove) throws AxelorException {
    return copyAndSplitStockMove(stockMove, stockMove.getStockMoveLineList());
  }

  @Override
  public Optional<StockMove> copyAndSplitStockMove(
      StockMove stockMove, List<StockMoveLine> stockMoveLines) throws AxelorException {

    stockMoveLines = MoreObjects.firstNonNull(stockMoveLines, Collections.emptyList());
    StockMove newStockMove = stockMoveRepo.copy(stockMove, false);

    for (StockMoveLine stockMoveLine : stockMoveLines) {

      if (stockMoveLine.getQty().compareTo(stockMoveLine.getRealQty()) > 0) {
        StockMoveLine newStockMoveLine = stockMoveLineRepo.copy(stockMoveLine, false);
        newStockMoveLine.setArchived(false);

        newStockMoveLine.setQty(stockMoveLine.getQty().subtract(stockMoveLine.getRealQty()));
        newStockMoveLine.setRealQty(newStockMoveLine.getQty());

        newStockMove.addStockMoveLineListItem(newStockMoveLine);
      }
    }

    if (ObjectUtils.isEmpty(newStockMove.getStockMoveLineList())) {
      return Optional.empty();
    }

    newStockMove.setRealDate(null);
    newStockMove.setStockMoveSeq(
        this.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany()));
    newStockMove.setName(
        computeName(
            newStockMove,
            newStockMove.getStockMoveSeq()
                + " "
                + I18n.get(IExceptionMessage.STOCK_MOVE_7)
                + " "
                + stockMove.getStockMoveSeq()
                + " )"));
    newStockMove.setExTaxTotal(compute(newStockMove));

    plan(newStockMove);
    return Optional.of(stockMoveRepo.save(newStockMove));
  }

  @Override
  public Optional<StockMove> copyAndSplitStockMoveReverse(StockMove stockMove, boolean split)
      throws AxelorException {
    return copyAndSplitStockMoveReverse(stockMove, stockMove.getStockMoveLineList(), split);
  }

  @Override
  public Optional<StockMove> copyAndSplitStockMoveReverse(
      StockMove stockMove, List<StockMoveLine> stockMoveLines, boolean split)
      throws AxelorException {

    stockMoveLines = MoreObjects.firstNonNull(stockMoveLines, Collections.emptyList());
    StockMove newStockMove = new StockMove();

    newStockMove.setCompany(stockMove.getCompany());
    newStockMove.setPartner(stockMove.getPartner());
    newStockMove.setFromStockLocation(stockMove.getToStockLocation());
    newStockMove.setToStockLocation(stockMove.getFromStockLocation());
    newStockMove.setEstimatedDate(stockMove.getEstimatedDate());
    newStockMove.setFromAddress(stockMove.getFromAddress());
    if (stockMove.getToAddress() != null) newStockMove.setFromAddress(stockMove.getToAddress());
    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING)
      newStockMove.setTypeSelect(StockMoveRepository.TYPE_OUTGOING);
    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING)
      newStockMove.setTypeSelect(StockMoveRepository.TYPE_INCOMING);
    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INTERNAL)
      newStockMove.setTypeSelect(StockMoveRepository.TYPE_INTERNAL);
    newStockMove.setStockMoveSeq(
        getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany()));

    for (StockMoveLine stockMoveLine : stockMoveLines) {

      if (!split || stockMoveLine.getRealQty().compareTo(stockMoveLine.getQty()) > 0) {
        StockMoveLine newStockMoveLine = stockMoveLineRepo.copy(stockMoveLine, false);

        if (split) {
          newStockMoveLine.setQty(stockMoveLine.getRealQty().subtract(stockMoveLine.getQty()));
          newStockMoveLine.setRealQty(newStockMoveLine.getQty());
        } else {
          newStockMoveLine.setQty(stockMoveLine.getRealQty());
          newStockMoveLine.setRealQty(stockMoveLine.getRealQty());
        }

        newStockMove.addStockMoveLineListItem(newStockMoveLine);
      }
    }

    if (ObjectUtils.isEmpty(newStockMove.getStockMoveLineList())) {
      return Optional.empty();
    }

    newStockMove.setRealDate(null);
    newStockMove.setStockMoveSeq(
        this.getSequenceStockMove(newStockMove.getTypeSelect(), newStockMove.getCompany()));
    newStockMove.setName(
        computeName(
            newStockMove,
            newStockMove.getStockMoveSeq()
                + " "
                + I18n.get(IExceptionMessage.STOCK_MOVE_8)
                + " "
                + stockMove.getStockMoveSeq()
                + " )"));
    newStockMove.setExTaxTotal(compute(newStockMove));
    newStockMove.setIsReversion(true);

    plan(newStockMove);
    return Optional.of(stockMoveRepo.save(newStockMove));
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void cancel(StockMove stockMove, CancelReason cancelReason) throws AxelorException {
    applyCancelReason(stockMove, cancelReason);
    cancel(stockMove);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void cancel(StockMove stockMove) throws AxelorException {
    LOG.debug("Annulation du mouvement de stock : {} ", new Object[] {stockMove.getStockMoveSeq()});

    if (stockMove.getStatusSelect() == StockMoveRepository.STATUS_PLANNED) {
      stockMoveLineService.updateLocations(
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation(),
          stockMove.getStatusSelect(),
          StockMoveRepository.STATUS_CANCELED,
          stockMove.getPlannedStockMoveLineList(),
          stockMove.getEstimatedDate(),
          false);
    } else {
      stockMoveLineService.updateLocations(
          stockMove.getFromStockLocation(),
          stockMove.getToStockLocation(),
          stockMove.getStatusSelect(),
          StockMoveRepository.STATUS_CANCELED,
          stockMove.getStockMoveLineList(),
          stockMove.getEstimatedDate(),
          true);
    }

    stockMove.setRealDate(appBaseService.getTodayDate());

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING
        && stockMove.getStatusSelect() == StockMoveRepository.STATUS_REALIZED) {
      partnerProductQualityRatingService.undoCalculation(stockMove);
    }

    stockMove.setStatusSelect(StockMoveRepository.STATUS_CANCELED);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Boolean splitStockMoveLinesUnit(List<StockMoveLine> stockMoveLines, BigDecimal splitQty) {

    Boolean selected = false;

    for (StockMoveLine moveLine : stockMoveLines) {
      if (moveLine.isSelected()) {
        selected = true;
        StockMoveLine line = stockMoveLineRepo.find(moveLine.getId());
        BigDecimal totalQty = line.getQty();
        LOG.debug("Move Line selected: {}, Qty: {}", line, totalQty);
        while (splitQty.compareTo(totalQty) < 0) {
          totalQty = totalQty.subtract(splitQty);
          StockMoveLine newLine = stockMoveLineRepo.copy(line, false);
          newLine.setQty(splitQty);
          newLine.setRealQty(splitQty);
          newLine.setStockMove(line.getStockMove());
          stockMoveLineRepo.save(newLine);
        }
        LOG.debug("Qty remains: {}", totalQty);
        if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
          StockMoveLine newLine = stockMoveLineRepo.copy(line, false);
          newLine.setQty(totalQty);
          newLine.setRealQty(totalQty);
          stockMoveLineRepo.save(newLine);
          LOG.debug("New line created: {}", newLine);
        }
        stockMoveLineRepo.remove(line);
      }
    }

    return selected;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void splitStockMoveLinesSpecial(
      StockMove stockMove, List<StockMoveLine> stockMoveLines, BigDecimal splitQty) {

    LOG.debug("SplitQty: {}", splitQty);

    for (StockMoveLine moveLine : stockMoveLines) {
      LOG.debug("Move line: {}", moveLine);
      BigDecimal totalQty = moveLine.getQty();
      while (splitQty.compareTo(totalQty) < 0) {
        totalQty = totalQty.subtract(splitQty);
        StockMoveLine newLine = stockMoveLineRepo.copy(moveLine, false);
        newLine.setQty(splitQty);
        newLine.setRealQty(splitQty);
        stockMove.addStockMoveLineListItem(newLine);
      }
      LOG.debug("Qty remains: {}", totalQty);
      if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
        StockMoveLine newLine = stockMoveLineRepo.copy(moveLine, false);
        newLine.setQty(totalQty);
        newLine.setRealQty(totalQty);
        stockMove.addStockMoveLineListItem(newLine);
        LOG.debug("New line created: {}", newLine);
      }
      stockMove.removeStockMoveLineListItem(moveLine);
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public StockMove splitInto2(
      StockMove originalStockMove, List<StockMoveLine> modifiedStockMoveLines)
      throws AxelorException {

    // Copy this stock move
    StockMove newStockMove = stockMoveRepo.copy(originalStockMove, false);
    newStockMove.setStockMoveLineList(new ArrayList<>());

    modifiedStockMoveLines =
        modifiedStockMoveLines
            .stream()
            .filter(stockMoveLine -> stockMoveLine.getQty().compareTo(BigDecimal.ZERO) != 0)
            .collect(Collectors.toList());
    for (StockMoveLine moveLine : modifiedStockMoveLines) {
      StockMoveLine newStockMoveLine;

      // Set quantity in new stock move line
      newStockMoveLine = stockMoveLineRepo.copy(moveLine, false);
      newStockMoveLine.setQty(moveLine.getQty());
      newStockMoveLine.setRealQty(moveLine.getQty());

      // add stock move line
      newStockMove.addStockMoveLineListItem(newStockMoveLine);

      // find the original move line to update it
      Optional<StockMoveLine> correspondingMoveLine =
          originalStockMove
              .getStockMoveLineList()
              .stream()
              .filter(stockMoveLine -> stockMoveLine.getId().equals(moveLine.getId()))
              .findFirst();
      if (BigDecimal.ZERO.compareTo(moveLine.getQty()) > 0
          || (correspondingMoveLine.isPresent()
              && moveLine.getQty().compareTo(correspondingMoveLine.get().getRealQty()) > 0)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            I18n.get(IExceptionMessage.STOCK_MOVE_16),
            originalStockMove);
      }

      if (correspondingMoveLine.isPresent()) {
        // Update quantity in original stock move.
        // If the remaining quantity is 0, remove the stock move line
        BigDecimal remainingQty = correspondingMoveLine.get().getQty().subtract(moveLine.getQty());
        if (BigDecimal.ZERO.compareTo(remainingQty) == 0) {
          // Remove the stock move line
          originalStockMove.removeStockMoveLineListItem(correspondingMoveLine.get());
        } else {
          correspondingMoveLine.get().setQty(remainingQty);
          correspondingMoveLine.get().setRealQty(remainingQty);
        }
      }
    }

    if (!newStockMove.getStockMoveLineList().isEmpty()) {
      newStockMove.setExTaxTotal(compute(newStockMove));
      originalStockMove.setExTaxTotal(compute(originalStockMove));
      return stockMoveRepo.save(newStockMove);
    } else {
      return null;
    }
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void copyQtyToRealQty(StockMove stockMove) {
    for (StockMoveLine line : stockMove.getStockMoveLineList()) line.setRealQty(line.getQty());
    stockMoveRepo.save(stockMove);
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public Optional<StockMove> generateReversion(StockMove stockMove) throws AxelorException {

    LOG.debug(
        "Creation d'un mouvement de stock inverse pour le mouvement de stock: {} ",
        new Object[] {stockMove.getStockMoveSeq()});

    return copyAndSplitStockMoveReverse(stockMove, false);
  }

  @Override
  public List<Map<String, Object>> getStockPerDate(
      Long locationId, Long productId, LocalDate fromDate, LocalDate toDate) {

    List<Map<String, Object>> stock = new ArrayList<>();

    while (!fromDate.isAfter(toDate)) {
      Double qty = getStock(locationId, productId, fromDate);
      Map<String, Object> dateStock = new HashMap<>();
      dateStock.put("$date", fromDate);
      dateStock.put("$qty", new BigDecimal(qty));
      stock.add(dateStock);
      fromDate = fromDate.plusDays(1);
    }

    return stock;
  }

  private Double getStock(Long locationId, Long productId, LocalDate date) {

    List<StockMoveLine> inLines =
        stockMoveLineRepo
            .all()
            .filter(
                "self.product.id = ?1 AND self.stockMove.toStockLocation.id = ?2 AND self.stockMove.statusSelect != ?3 AND (self.stockMove.estimatedDate <= ?4 OR self.stockMove.realDate <= ?4)",
                productId,
                locationId,
                StockMoveRepository.STATUS_CANCELED,
                date)
            .fetch();

    List<StockMoveLine> outLines =
        stockMoveLineRepo
            .all()
            .filter(
                "self.product.id = ?1 AND self.stockMove.fromStockLocation.id = ?2 AND self.stockMove.statusSelect != ?3 AND (self.stockMove.estimatedDate <= ?4 OR self.stockMove.realDate <= ?4)",
                productId,
                locationId,
                StockMoveRepository.STATUS_CANCELED,
                date)
            .fetch();

    Double inQty =
        inLines.stream().mapToDouble(inl -> Double.parseDouble(inl.getQty().toString())).sum();

    Double outQty =
        outLines.stream().mapToDouble(out -> Double.parseDouble(out.getQty().toString())).sum();

    Double qty = inQty - outQty;

    return qty;
  }

  @Override
  public List<StockMoveLine> changeConformityStockMove(StockMove stockMove) {
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();

    if (stockMoveLineList != null) {
      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        stockMoveLine.setConformitySelect(stockMove.getConformitySelect());
      }
    }

    return stockMoveLineList;
  }

  @Override
  public Integer changeConformityStockMoveLine(StockMove stockMove) {
    Integer stockMoveConformitySelect;
    List<StockMoveLine> stockMoveLineList = stockMove.getStockMoveLineList();

    if (stockMoveLineList != null) {
      stockMoveConformitySelect = StockMoveRepository.CONFORMITY_COMPLIANT;

      for (StockMoveLine stockMoveLine : stockMoveLineList) {
        Integer conformitySelect = stockMoveLine.getConformitySelect();

        if (!conformitySelect.equals(StockMoveRepository.CONFORMITY_COMPLIANT)) {
          stockMoveConformitySelect = conformitySelect;
          if (conformitySelect.equals(StockMoveRepository.CONFORMITY_NON_COMPLIANT)) {
            break;
          }
        }
      }
    } else {
      stockMoveConformitySelect = StockMoveRepository.CONFORMITY_NONE;
    }

    stockMove.setConformitySelect(stockMoveConformitySelect);
    return stockMoveConformitySelect;
  }

  @Override
  public void computeAddressStr(StockMove stockMove) {
    AddressService addressService = Beans.get(AddressService.class);
    stockMove.setFromAddressStr(addressService.computeAddressStr(stockMove.getFromAddress()));
    stockMove.setToAddressStr(addressService.computeAddressStr(stockMove.getToAddress()));
  }

  @Override
  public Map<String, Object> viewDirection(StockMove stockMove) throws AxelorException {

    String fromAddressStr = stockMove.getFromAddressStr();
    String toAddressStr = stockMove.getToAddressStr();

    String dString;
    String aString;
    BigDecimal dLat = BigDecimal.ZERO;
    BigDecimal dLon = BigDecimal.ZERO;
    BigDecimal aLat = BigDecimal.ZERO;
    BigDecimal aLon = BigDecimal.ZERO;
    if (Strings.isNullOrEmpty(fromAddressStr)) {
      Address fromAddress = stockMove.getCompany().getAddress();
      dString = fromAddress.getAddressL4() + " ," + fromAddress.getAddressL6();
      dLat = fromAddress.getLatit();
      dLon = fromAddress.getLongit();
    } else {
      dString = fromAddressStr.replace('\n', ' ');
    }
    if (toAddressStr == null) {
      Address toAddress = stockMove.getCompany().getAddress();
      aString = toAddress.getAddressL4() + " ," + toAddress.getAddressL6();
      aLat = toAddress.getLatit();
      aLon = toAddress.getLongit();
    } else {
      aString = toAddressStr.replace('\n', ' ');
    }
    if (Strings.isNullOrEmpty(dString) || Strings.isNullOrEmpty(aString)) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.STOCK_MOVE_11));
    }
    if (appBaseService.getAppBase().getMapApiSelect()
        == AppBaseRepository.MAP_API_OPEN_STREET_MAP) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.STOCK_MOVE_12));
    }
    Map<String, Object> result =
        Beans.get(MapService.class).getDirectionMapGoogle(dString, dLat, dLon, aString, aLat, aLon);
    if (result == null) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.TYPE_FUNCTIONNAL,
          I18n.get(IExceptionMessage.STOCK_MOVE_13),
          dString,
          aString);
    }
    return result;
  }

  @Override
  public String printStockMove(
      StockMove stockMove, List<Integer> lstSelectedMove, boolean isPicking)
      throws AxelorException {
    List<Long> selectedStockMoveListId;
    if (lstSelectedMove != null && !lstSelectedMove.isEmpty()) {
      selectedStockMoveListId =
          lstSelectedMove
              .stream()
              .map(integer -> Long.parseLong(integer.toString()))
              .collect(Collectors.toList());
      stockMove = stockMoveRepo.find(selectedStockMoveListId.get(0));
    } else if (stockMove != null && stockMove.getId() != null) {
      selectedStockMoveListId = new ArrayList<>();
      selectedStockMoveListId.add(stockMove.getId());
    } else {
      throw new AxelorException(
          StockMove.class,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.STOCK_MOVE_10));
    }

    List<StockMove> stockMoveList =
        stockMoveRepo
            .all()
            .filter(
                "self.id IN ("
                    + selectedStockMoveListId
                        .stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","))
                    + ") AND self.printingSettings IS NULL")
            .fetch();
    if (!stockMoveList.isEmpty()) {
      String exceptionMessage =
          String.format(
              I18n.get(IExceptionMessage.STOCK_MOVES_MISSING_PRINTING_SETTINGS),
              "<ul>"
                  + stockMoveList
                      .stream()
                      .map(StockMove::getStockMoveSeq)
                      .collect(Collectors.joining("</li><li>", "<li>", "</li>"))
                  + "<ul>");
      throw new AxelorException(TraceBackRepository.CATEGORY_MISSING_FIELD, exceptionMessage);
    }

    String stockMoveIds =
        selectedStockMoveListId.stream().map(Object::toString).collect(Collectors.joining(","));

    String title = I18n.get("Stock move");
    if (stockMove.getStockMoveSeq() != null) {
      title =
          selectedStockMoveListId.size() == 1
              ? I18n.get("StockMove") + " " + stockMove.getStockMoveSeq()
              : I18n.get("StockMove(s)");
    }

    String report = isPicking ? IReport.PICKING_STOCK_MOVE : IReport.STOCK_MOVE;
    String locale =
        isPicking
            ? Beans.get(UserService.class).getLanguage()
            : ReportSettings.getPrintingLocale(stockMove.getPartner());

    return ReportFactory.createReport(report, title + "-${date}")
        .addParam("StockMoveId", stockMoveIds)
        .addParam("Locale", locale)
        .generate()
        .getFileLink();
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void updateFullySpreadOverLogisticalFormsFlag(StockMove stockMove) {
    stockMove.setFullySpreadOverLogisticalFormsFlag(
        computeFullySpreadOverLogisticalFormsFlag(stockMove));
  }

  protected boolean computeFullySpreadOverLogisticalFormsFlag(StockMove stockMove) {
    return stockMove.getStockMoveLineList() != null
        ? stockMove
            .getStockMoveLineList()
            .stream()
            .allMatch(
                stockMoveLine ->
                    stockMoveLineService.computeFullySpreadOverLogisticalFormLinesFlag(
                        stockMoveLine))
        : true;
  }

  @Override
  public String computeName(StockMove stockMove) {
    return computeName(stockMove, null);
  }

  @Override
  public String computeName(StockMove stockMove, String name) {
    Objects.requireNonNull(stockMove);
    StringBuilder nameBuilder = new StringBuilder();

    if (Strings.isNullOrEmpty(name)) {
      if (!Strings.isNullOrEmpty(stockMove.getStockMoveSeq())) {
        nameBuilder.append(stockMove.getStockMoveSeq());
      }
    } else {
      nameBuilder.append(name);
    }

    if (stockMove.getPartner() != null
        && !Strings.isNullOrEmpty(stockMove.getPartner().getFullName())) {
      if (nameBuilder.length() > 0) {
        nameBuilder.append(" - ");
      }

      nameBuilder.append(stockMove.getPartner().getFullName());
    }

    return nameBuilder.toString();
  }

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  protected void applyCancelReason(StockMove stockMove, CancelReason cancelReason)
      throws AxelorException {
    if (cancelReason == null) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.CANCEL_REASON_MISSING));
    }
    if (!StockMove.class.getCanonicalName().equals(cancelReason.getApplicationType())) {
      throw new AxelorException(
          stockMove,
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(IExceptionMessage.CANCEL_REASON_BAD_TYPE));
    }
    stockMove.setCancelReason(cancelReason);
  }

  @Override
  public void checkExpirationDates(StockMove stockMove) throws AxelorException {
    if (stockMove.getToStockLocation().getTypeSelect() != StockLocationRepository.TYPE_VIRTUAL) {
      stockMoveLineService.checkExpirationDates(stockMove);
    }
  }
}
