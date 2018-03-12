/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorder;

import java.lang.invoke.MethodHandles;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.sale.db.ISaleOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.IExceptionMessage;
import com.axelor.apps.sale.report.IReport;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class SaleOrderServiceImpl implements SaleOrderService {

	protected final Logger logger = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public String getFileName(SaleOrder saleOrder)  {
		
		return I18n.get("Sale order") + " " + saleOrder.getSaleOrderSeq() + ((saleOrder.getVersionNumber() > 1) ? "-V" + saleOrder.getVersionNumber() : "");
	}
	

	@Override
	public SaleOrder computeEndOfValidityDate(SaleOrder saleOrder)  {
		if (saleOrder.getDuration() != null && saleOrder.getCreationDate() != null) {
			saleOrder.setEndOfValidityDate(
					Beans.get(DurationService.class).computeDuration(saleOrder.getDuration(), saleOrder.getCreationDate()));
		}
		return saleOrder;
	}
	
	@Override
	public String getReportLink(SaleOrder saleOrder, String name, String language, boolean proforma, String format) throws AxelorException{
        return ReportFactory.createReport(IReport.SALES_ORDER, name+"-${date}")
                .addParam("Locale", language)
                .addParam("SaleOrderId", saleOrder.getId())
                .addParam("ProformaInvoice", proforma)
                .addFormat(format)
                .generate()
                .getFileLink();
	}
	

	@Override
	public void computeAddressStr(SaleOrder saleOrder) {
		AddressService addressService = Beans.get(AddressService.class);
		saleOrder.setMainInvoicingAddressStr(
				addressService.computeAddressStr(
						saleOrder.getMainInvoicingAddress()
				)
		);
		saleOrder.setDeliveryAddressStr(
				addressService.computeAddressStr(
						saleOrder.getDeliveryAddress()
				)
		);
	}


	@Override
    @Transactional(rollbackOn = { Exception.class, AxelorException.class })
	public void enableEditOrder(SaleOrder saleOrder) throws AxelorException {
	    if (saleOrder.getStatusSelect() == ISaleOrder.STATUS_FINISHED) {
	        throw new AxelorException(saleOrder, IException.INCONSISTENCY, I18n.get(IExceptionMessage.SALES_ORDER_FINISHED));
	    }

		saleOrder.setOrderBeingEdited(true);
	}

    @Override
    public void validateChanges(SaleOrder saleOrder, SaleOrder saleOrderView) throws AxelorException {
    }

    @Override
    public void sortSaleOrderLineList(SaleOrder saleOrder) {
        if (saleOrder.getSaleOrderLineList() != null) {
            saleOrder.getSaleOrderLineList().sort(Comparator.comparing(SaleOrderLine::getSequence));
        }
    }

}
