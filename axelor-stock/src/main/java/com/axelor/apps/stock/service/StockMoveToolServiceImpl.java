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

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockLocationRepository;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.exception.IExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StockMoveToolServiceImpl implements StockMoveToolService {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected StockMoveLineService stockMoveLineService;
  protected AppBaseService appBaseService;
  protected StockMoveRepository stockMoveRepo;
  protected PartnerProductQualityRatingService partnerProductQualityRatingService;
  private SequenceService sequenceService;
  private StockMoveLineRepository stockMoveLineRepo;

  @Inject
  public StockMoveToolServiceImpl(
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

  /**
   * @param customerPartner
   * @param toAddress
   * @return default value for {@link StockMove#isIspmRequired}
   */
  public boolean getDefaultISPM(Partner customerPartner, Address toAddress) {
    if (customerPartner != null && customerPartner.getIsIspmRequired()) {
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
  public void computeAddressStr(StockMove stockMove) {
    AddressService addressService = Beans.get(AddressService.class);
    stockMove.setFromAddressStr(addressService.computeAddressStr(stockMove.getFromAddress()));
    stockMove.setToAddressStr(addressService.computeAddressStr(stockMove.getToAddress()));
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

  @Override
  public Address getPartnerAddress(StockMove stockMove) throws AxelorException {
    Address address;

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
      address = getToAddress(stockMove);
    } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
      address = getFromAddress(stockMove);
    } else {
      throw new AxelorException(
          stockMove, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Bad stock move type"));
    }

    if (address.getAddressL7Country() == null) {
      throw new AxelorException(address, TraceBackRepository.CATEGORY_NO_VALUE, "Missing country");
    }

    return address;
  }

  @Override
  public Address getCompanyAddress(StockMove stockMove) throws AxelorException {
    Address address;

    if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_OUTGOING) {
      address = getFromAddress(stockMove);
    } else if (stockMove.getTypeSelect() == StockMoveRepository.TYPE_INCOMING) {
      address = getToAddress(stockMove);
    } else {
      throw new AxelorException(
          stockMove, TraceBackRepository.CATEGORY_INCONSISTENCY, I18n.get("Bad stock move type"));
    }

    if (address.getAddressL7Country() == null) {
      throw new AxelorException(address, TraceBackRepository.CATEGORY_NO_VALUE, "Missing country");
    }

    if (address.getCity() == null
        || address.getCity().getDepartment() == null
        || StringUtils.isBlank(address.getCity().getDepartment().getCode())) {
      throw new AxelorException(
          address, TraceBackRepository.CATEGORY_NO_VALUE, "Missing department");
    }

    return address;
  }
}
