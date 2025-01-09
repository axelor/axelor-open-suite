/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.stock.db.TrackingNumber;
import com.axelor.apps.stock.db.TrackingNumberConfiguration;
import java.time.LocalDate;
import java.util.Set;

public interface TrackingNumberService {
  TrackingNumber getTrackingNumber(
      Product product, Company company, LocalDate date, String origin, Partner supplier)
      throws AxelorException;

  String getOrderMethod(TrackingNumberConfiguration trackingNumberConfiguration);

  TrackingNumber createTrackingNumber(
      Product product, Company company, LocalDate date, String origin, Partner supplier)
      throws AxelorException;

  TrackingNumber generateTrackingNumber(
      Product product,
      Company company,
      LocalDate date,
      String origin,
      Partner supplier,
      String notes)
      throws AxelorException;

  void calculateDimension(TrackingNumber trackingNumber) throws AxelorException;

  Set<TrackingNumber> getOriginParents(TrackingNumber trackingNumber) throws AxelorException;
}
