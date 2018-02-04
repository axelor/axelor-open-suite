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
package com.axelor.apps.prestashop.exports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.db.Order_details;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportOrderDetailServiceImpl implements ExportOrderDetailService {
	private SaleOrderLineRepository saleOrderLineRepo;

	@Inject
	public ExportOrderDetailServiceImpl(SaleOrderLineRepository saleOrderLineRepo) {
		this.saleOrderLineRepo = saleOrderLineRepo;
	}

	/**
	 * Reset/delete/remove order details from prestashop
	 *
	 * @param orderId
	 * @throws PrestaShopWebserviceException
	 */
	public void clearOrderDetails(AppPrestashop appConfig, Integer orderId) throws PrestaShopWebserviceException {
		String orderDetailId = null;
		PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		HashMap<String, String> orderDetailMap = new HashMap<String, String>();
		orderDetailMap.put("id_order", orderId.toString());

		Options options = new Options();
		options.setResourceType(PrestashopResourceType.ORDER_DETAILS);
		options.setFilter(orderDetailMap);
		Document document =  ws.get(options);

		NodeList list = document.getElementsByTagName("order_details");

		options.setFilter(null);
		for(int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
		    NodeList nodeList = element.getElementsByTagName("order_detail");

		    for(int j = 0; j < nodeList.getLength(); j++) {
		    	Node order = nodeList.item(j);

		    	if(nodeList.getLength() > 0) {
					ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

			    	options.setRequestedId(Integer.valueOf(order.getAttributes().getNamedItem("id").getNodeValue()));
			    	ws.delete(options);
			    }
		    }
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public void exportOrderDetail(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport)
			throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException,
			SAXException, PrestaShopWebserviceException, JAXBException, TransformerFactoryConfigurationError {
		int done = 0;
		int anomaly = 0;

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Order detail");

		final StringBuilder filter = new StringBuilder(128);

		// FIXME add parameter in app config for this
		filter.append("self.saleOrder.prestaShopId IS NOT NULL");

		if(appConfig.getIsOrderStatus() == Boolean.TRUE) {
			filter.append("AND (self.saleOrder.statusSelect = 1)");
		}

		List<SaleOrderLine> saleOrderLines = saleOrderLineRepo.all().filter(filter.toString()).fetch();

		for(SaleOrderLine orderLine : saleOrderLines) {
			clearOrderDetails(appConfig, orderLine.getSaleOrder().getPrestaShopId());

			try {
				if(orderLine.getProduct() != null && orderLine.getSaleOrder().getPrestaShopId() != null) {

					Order_details details = new Order_details();
					details.setId_order(orderLine.getSaleOrder().getPrestaShopId().toString()); // Not null thanks to filter
					details.setProduct_id(orderLine.getProduct().getPrestaShopId().toString());
					details.setProduct_name(orderLine.getProduct().getName());
					details.setProduct_quantity(orderLine.getQty().setScale(0, RoundingMode.HALF_UP).toString());
					details.setUnit_price_tax_excl(orderLine.getPrice().setScale(2, RoundingMode.HALF_UP).toString());
					details.setUnit_price_tax_incl(orderLine.getPrice().setScale(2, RoundingMode.HALF_UP).toString());
					details.setProduct_price(orderLine.getPrice().setScale(2, RoundingMode.HALF_UP).toString());
					details.setId_warehouse("0");
					details.setId_shop("1");

					Prestashop prestaShop = new Prestashop();
					prestaShop.setPrestashop(details);

					StringWriter sw = new StringWriter();
					JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
					Marshaller marshallerObj = contextObj.createMarshaller();
					marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshallerObj.marshal(prestaShop, sw);

					PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/order_details?schema=synopsis", appConfig.getPrestaShopKey());
					Options options = new Options();
					options.setResourceType(PrestashopResourceType.ORDER_DETAILS);
					options.setXmlPayload(sw.toString());
					Document document = ws.add(options);

					orderLine.setPrestaShopId(Integer.valueOf(document.getElementsByTagName("id").item(0).getTextContent()));
					saleOrderLineRepo.save(orderLine);

				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_ORDER_LINE),IException.NO_VALUE);
				}

				done++;

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + orderLine.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			} catch (Exception e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + orderLine.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
