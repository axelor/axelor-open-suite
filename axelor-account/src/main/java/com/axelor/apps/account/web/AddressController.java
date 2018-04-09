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
package com.axelor.apps.account.web;

//import java.io.File;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerList;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AppBaseRepository;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class AddressController {

	private static final Logger LOG = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	protected AppAccountService appAccountService;

	@Inject
	private AddressRepository addressRepo;

	@Inject
	private InvoiceRepository invoiceRepo;


	@SuppressWarnings("unchecked")
	@Transactional
	public void viewSalesMap(ActionRequest request, ActionResponse response)  {
	    try {
	        // Only allowed for google maps to prevent overloading OSM
	        if (appAccountService.getAppBase().getMapApiSelect() == AppBaseRepository.MAP_API_GOOGLE) {
	            PartnerList partnerList = request.getContext().asType(PartnerList.class);

//	          File file = new File("/home/axelor/www/HTML/latlng_"+partnerList.getId()+".csv");
	            //file.write("latitude,longitude,fullName,turnover\n");

	            Iterator<Partner> it = partnerList.getPartnerSet().iterator();

	            while(it.hasNext()) {

	                Partner partner = it.next();
	                Address address = Beans.get(PartnerService.class).getInvoicingAddress(partner);
	                if (address != null) {
	                    address = addressRepo.find(address.getId());
	                    if (!(address.getLatit() != null && address.getLongit() != null)) {
	                        String qString = address.getAddressL4()+" ,"+address.getAddressL6();
	                        LOG.debug("qString = {}", qString);

	                        Map<String,Object> googleResponse = Beans.get(MapService.class).geocodeGoogle(qString);
	                        address.setLatit((BigDecimal) googleResponse.get("lat"));
	                        address.setLongit((BigDecimal) googleResponse.get("lng"));
	                        addressRepo.save(address);
	                    }
	                    if (address.getLatit() != null && address.getLongit() != null) {
	                        //def turnover = Invoice.all().filter("self.partner.id = ? AND self.statusSelect = 'val'", partner.id).fetch().sum{ it.inTaxTotal }
	                        List<Invoice> listInvoice = invoiceRepo.all().filter("self.partner.id = ?", partner.getId()).fetch();
	                        BigDecimal turnover = BigDecimal.ZERO;
	                        for(Invoice invoice: listInvoice) {
	                            turnover.add(invoice.getInTaxTotal());
	                        }
	                        /*
	                        file.withWriterAppend('UTF-8') {
	                            it.write("${address.latit},${address?.longit},${partner.fullName},${turnover?:0.0}\n")
	                        }
	                         */
	                    }
	                }
	            }
	            //response.values = [partnerList : partnerList]
	            String url = "";
	            if (partnerList.getIsCluster())
	                url = "http://localhost/HTML/cluster_gmaps_xhr.html?file=latlng_"+partnerList.getId()+".csv";
	            else
	                url = "http://localhost/HTML/gmaps_xhr.html?file=latlng_"+partnerList.getId()+".csv";

	            Map<String,Object> mapView = new HashMap<String,Object>();
	            mapView.put("title", I18n.get(IExceptionMessage.ADDRESS_1));
	            mapView.put("resource", url);
	            mapView.put("viewType", "html");
	            response.setView(mapView);

	        } else {
	            response.setFlash(I18n.get(IExceptionMessage.ADDRESS_2));
            }

	    } catch (Exception e) {
            TraceBackService.trace(response, e);
        }
	}

}
