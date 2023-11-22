/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.*;
import com.axelor.apps.stock.db.repo.*;
import com.axelor.apps.supplychain.db.*;
import com.axelor.apps.supplychain.db.repo.*;
import com.axelor.apps.supplychain.service.*;
import com.axelor.db.Model;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductReservationServiceProductionImpl extends ProductReservationServiceImpl
    implements ProductReservationServiceProduction {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject
  public ProductReservationServiceProductionImpl(
      ProductReservationRepository productReservationRepository,
      ProductRepository productRepository) {
    super(productReservationRepository, productRepository);
  }

  protected static final Map<String, BiConsumer<ProductReservation, Model>>
      MAP_ORIGIN_SETTER_CONSUMER_BY_CLASS_NAME =
          Map.of(
              "com.axelor.apps.production.db.SaleOrderLine",
              new SetterOriginSaleOrderLineConsumer(),
              "com.axelor.apps.production.db.ManufOrder",
              new SetterOriginManufOrderConsumer());

  protected static final class SetterOriginManufOrderConsumer
      implements BiConsumer<ProductReservation, Model> {
    public void accept(ProductReservation pr, Model mo) {
      pr.setOriginManufOrder(autocast(mo));
    }
  }

  protected static final class SetterOriginSaleOrderLineConsumer
      implements BiConsumer<ProductReservation, Model> {
    public void accept(ProductReservation pr, Model mo) {
      pr.setOriginSaleOrderLine(autocast(mo));
    }
  }

  protected static final Map<String, Function<ProductReservation, Model>>
      MAP_ORIGIN_GETTER_CONSUMER_BY_CLASS_NAME =
          Map.of(
              "com.axelor.apps.production.db.SaleOrderLine",
              new GetterOriginSaleOrderLineFunction(),
              "com.axelor.apps.production.db.ManufOrder",
              new GetterOriginManufOrderFunction());

  protected static final class GetterOriginManufOrderFunction
      implements Function<ProductReservation, Model> {
    public Model apply(ProductReservation pr) {
      return autocast(pr.getOriginManufOrder());
    }
  }

  protected static final class GetterOriginSaleOrderLineFunction
      implements Function<ProductReservation, Model> {
    public Model apply(ProductReservation pr) {
      return autocast(pr.getOriginSaleOrderLine());
    }
  }

  @SuppressWarnings("unchecked")
  protected static <T> T autocast(Model mo) {
    if (mo == null) {
      return null;
    }
    return (T) mo;
  }

  @Override
  public void setOrigin(ProductReservation productReservationToSave, Model originInstanceModel) {
    BiConsumer<ProductReservation, Model> setter =
        MAP_ORIGIN_SETTER_CONSUMER_BY_CLASS_NAME.get(originInstanceModel.getClass().getName());
    setter.accept(productReservationToSave, originInstanceModel);
  }

  @Override
  public Model getOrigin(ProductReservation productReservationToSave, Model originInstanceModel) {
    Function<ProductReservation, Model> getter =
        MAP_ORIGIN_GETTER_CONSUMER_BY_CLASS_NAME.get(originInstanceModel.getClass().getName());
    return getter.apply(productReservationToSave);
  }
}
