/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.prestashop.db.Order_details;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExportOrderDetailServiceImpl implements ExportOrderDetailService {

	Integer done = 0;
	Integer anomaly = 0;
	private final String shopUrl;
	private final String key;
	private final boolean isStatus;
	
	String schema = null;
	Document document;
	PSWebServiceClient ws = null;
	HashMap<String, Object> opt = null;
	
	@Inject
	private SaleOrderLineRepository saleOrderLineRepo;
	
	/**
	 * Initialization	
	 */
	public ExportOrderDetailServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
		isStatus = prestaShopObj.getIsOrderStatus();
	}
	
	/**
	 * Reset/delete/remove order details from prestashop
	 * 
	 * @param orderId
	 * @throws PrestaShopWebserviceException
	 */
	public void exportResetOrderDetails(String orderId) throws PrestaShopWebserviceException {
		
		String orderDetailId = null;
		ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> orderDetailMap = new HashMap<String, String>();
		orderDetailMap.put("id_order", orderId);
		
		opt = new HashMap<String, Object>();
		opt.put("resource", "order_details");
		opt.put("filter", orderDetailMap);
		document =  ws.get(opt);
		
		NodeList list = document.getElementsByTagName("order_details");
		
		for(int i = 0; i < list.getLength(); i++) {
			Element element = (Element) list.item(i);
		    NodeList nodeList = element.getElementsByTagName("order_detail");
		    
		    for(int j = 0; j < nodeList.getLength(); j++) {
		    	Node order = nodeList.item(j);
		    	
		    	if(nodeList.getLength() > 0) {
		    		orderDetailId =  order.getAttributes().getNamedItem("id").getNodeValue();
		    		ws = new PSWebServiceClient(shopUrl, key);
			    	opt  = new HashMap<String, Object>();
			    	opt.put("resource", "order_details");
			    	opt.put("id", orderDetailId);
			    	ws.delete(opt);
			    }
		    }
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter exportOrderDetail(ZonedDateTime endDate, BufferedWriter bwExport)
			throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException,
			SAXException, PrestaShopWebserviceException, JAXBException, TransformerFactoryConfigurationError {
		
		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Order detail");
		List<SaleOrderLine> saleOrderLines = null;
		
		if(isStatus == true) {
			saleOrderLines = Beans.get(SaleOrderLineRepository.class).all().filter("self.saleOrder.prestaShopId != null").fetch();
		} else {
			saleOrderLines = Beans.get(SaleOrderLineRepository.class).all().filter("self.saleOrder.statusSelect = 1 AND self.saleOrder.prestaShopId != null").fetch();
		}
		
		for(SaleOrderLine orderLine : saleOrderLines) {
			this.exportResetOrderDetails(orderLine.getSaleOrder().getPrestaShopId());
		}
		
		for(SaleOrderLine orderLine : saleOrderLines) {
		
			try {
				
				if(orderLine.getProduct() != null && orderLine.getSaleOrder().getPrestaShopId() != null) {
					
					Order_details details = new Order_details();
					details.setId_order(orderLine.getSaleOrder().getPrestaShopId());
					details.setProduct_id(orderLine.getProduct().getPrestaShopId());
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
					schema = sw.toString();
					
					ws = new PSWebServiceClient(shopUrl + "/api/" + "order_details" + "?schema=synopsis", key);
					opt = new HashMap<String, Object>();
					opt.put("resource", "order_details");
					opt.put("postXml", schema);
					document = ws.add(opt);
					
					orderLine.setPrestaShopId(document.getElementsByTagName("id").item(0).getTextContent());
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
		return bwExport;
	}
}
