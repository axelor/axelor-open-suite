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
package com.axelor.apps.prestashop.imports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.prestashop.db.SaleOrderStatus;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportOrderDetailServiceImpl implements ImportOrderDetailService {
	
	PSWebServiceClient ws;
    HashMap<String,Object> opt;
    JSONObject schema;
    private final String shopUrl;
	private final String key;
	private final boolean isStatus;
	private final List<SaleOrderStatus> saleOrderStatus;

	@Inject
	private SaleOrderRepository saleOrderRepo;

	/**
	 * Initialization
	 */
	public ImportOrderDetailServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
		isStatus = prestaShopObj.getIsOrderStatus();
		saleOrderStatus = prestaShopObj.getSaleOrderStatusList();
	}

	/**
	 * Get order ids which is in draft state
	 *
	 * @return sale order ids
	 * @throws PrestaShopWebserviceException
	 * @throws JSONException
	 */
	public List<Integer> getDraftOrderIds() throws PrestaShopWebserviceException, JSONException {

		List<Integer> orderIds = new ArrayList<Integer>();
		List<Integer> currentStatus = new ArrayList<Integer>();

		for(SaleOrderStatus orderStatus : saleOrderStatus) {
			if(orderStatus.getAbsStatus() == 1) {
				currentStatus.add(orderStatus.getPrestaShopStatus());
			}
		}
		
		ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> orderMap = null;
		
		for(Integer id : currentStatus) {
			orderMap = new HashMap<String, String>();
			orderMap.put("current_state", id.toString());
			opt = new HashMap<String, Object>();
			opt.put("resource", "orders");
			opt.put("filter", orderMap);
			JSONObject schema =  ws.getJson(opt);
			
			if(schema != null) {
				JSONArray jsonMainArr = schema.getJSONArray("orders");

				for (int i = 0; i < jsonMainArr.length(); i++) {
				     JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
				     orderIds.add(childJSONObject.getInt("id"));
				}
			}
		}
		return orderIds;
	}

	/**
	 * Get all order line/ order details ids
	 *
	 * @param orderIds
	 * @return
	 * @throws PrestaShopWebserviceException
	 * @throws JSONException
	 */
	public List<Integer> getOrderLineIds(List<Integer> orderIds) throws PrestaShopWebserviceException, JSONException {

		List<Integer> orderDetailIds = new ArrayList<Integer>();

		for(Integer id : orderIds) {
			
			ws = new PSWebServiceClient(shopUrl, key);
			HashMap<String, String> orderDetailMap = new HashMap<String, String>();
			orderDetailMap.put("id_order", id.toString());
			opt = new HashMap<String, Object>();
			opt.put("resource", "order_details");
			opt.put("filter", orderDetailMap);
			JSONObject schema =  ws.getJson(opt);
			
			JSONArray jsonMainArr = schema.getJSONArray("order_details"); 

			for (int i = 0; i < jsonMainArr.length(); i++) { 
			     JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
			     orderDetailIds.add(childJSONObject.getInt("id"));
			}
		}
		return orderDetailIds;
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importOrderDetail(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {

		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Order Detail");

		List<Integer> orderIds = null;
		List<Integer> orderLineIds = null;
		boolean isNewSaleOrderLine = false;
		
		ws = new PSWebServiceClient(shopUrl,key);
		
		if(isStatus == true) {
			orderIds = ws.fetchApiIds("orders");
		} else {
			orderIds = this.getDraftOrderIds();
		}

		orderLineIds = this.getOrderLineIds(orderIds);

		for(Integer id : orderLineIds) {
			try {
				ws = new PSWebServiceClient(shopUrl,key);
				opt = new HashMap<String, Object>();
				opt.put("resource", "order_details");
				opt.put("id", id);
				schema = ws.getJson(opt);
				
				SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).all().filter("self.prestaShopId = ?", schema.getJSONObject("order_detail").getString("id_order")).fetchOne();
				Product product = Beans.get(ProductRepository.class).all().filter("self.prestaShopId = ?", schema.getJSONObject("order_detail").getString("product_id")).fetchOne();
				SaleOrderLine saleOrderLine = Beans.get(SaleOrderLineRepository.class).all().filter("self.prestaShopId = ?", id).fetchOne();

				if(saleOrder == null)
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_ORDER), IException.NO_VALUE);

				if(saleOrderLine == null) {
					isNewSaleOrderLine = true;
					saleOrderLine = new SaleOrderLine();
				}

				if(product == null)
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_PRODUCT), IException.NO_VALUE);

				saleOrderLine.setProduct(product);
				saleOrderLine.setProductName(schema.getJSONObject("order_detail").getString("product_name"));
				saleOrderLine.setQty(new BigDecimal(schema.getJSONObject("order_detail").getString("product_quantity")));
				saleOrderLine.setPrice(new BigDecimal(schema.getJSONObject("order_detail").getString("product_price")));
				saleOrderLine.setExTaxTotal(new BigDecimal(schema.getJSONObject("order_detail").getString("total_price_tax_incl")));
				saleOrderLine.setSaleOrder(saleOrder);
				saleOrderLine.setPrestaShopId(id);

				if(isNewSaleOrderLine) {
					saleOrder.addSaleOrderLineListItem(saleOrderLine);
					isNewSaleOrderLine = false;
				}
				saleOrderRepo.persist(saleOrder);
				saleOrderRepo.save(saleOrder);
				done++;
			} catch (AxelorException e) {
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwImport.newLine();
		bwImport.newLine();
		bwImport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
		return bwImport;
	}
}
