/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.app.production.db.IWorkCenter;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.UnitRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class BillOfMaterialServiceBusinessImpl extends BillOfMaterialServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	protected GeneralService generalService;


	@Override
	protected BigDecimal _computeProcess(ProdProcess prodProcess) throws AxelorException  {

		BigDecimal costPrice = BigDecimal.ZERO;

		if(prodProcess != null && prodProcess.getProdProcessLineList() != null)  {

			for(ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList())  {

				WorkCenter workCenter = prodProcessLine.getWorkCenter();

				if(workCenter != null)  {

					int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();

					if(workCenterTypeSelect == IWorkCenter.WORK_CENTER_HUMAN || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH)  {

						costPrice = costPrice.add(this._computeHumanResourceCost(workCenter));

					}
					if(workCenterTypeSelect == IWorkCenter.WORK_CENTER_MACHINE || workCenterTypeSelect == IWorkCenter.WORK_CENTER_BOTH)  {

						costPrice = costPrice.add(this._computeMachineCost(workCenter));

					}

				}
			}
		}

		return costPrice;
	}



	private BigDecimal _computeHumanResourceCost(WorkCenter workCenter) throws AxelorException  {

		BigDecimal costPrice = BigDecimal.ZERO;

		Beans.get(UnitRepository.class);

		if(workCenter.getProdHumanResourceList() != null)  {

			for(ProdHumanResource prodHumanResource : workCenter.getProdHumanResourceList())  {

				if(prodHumanResource.getEmployee() != null)  {

//					BigDecimal costPerMin = unitConversionService.convert(unitRepository.findByCode(UNIT_MIN_CODE), unitRepository.findByCode(UNIT_DAY_CODE), prodHumanResource.getEmployee().getDailySalaryCost());

//					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));

				}
				else if(prodHumanResource.getProduct() != null)  {

					Product product = prodHumanResource.getProduct();

					BigDecimal costPerMin = unitConversionService.convert(generalService.getGeneral().getUnitMinutes(), product.getUnit(), product.getCostPrice());

					costPrice = costPrice.add((costPerMin).multiply(new BigDecimal(prodHumanResource.getDuration()/60)));

				}
			}

		}

		logger.debug("Human resource cost : {} (Resource : {})",costPrice, workCenter.getName());

		return costPrice;
	}




}
