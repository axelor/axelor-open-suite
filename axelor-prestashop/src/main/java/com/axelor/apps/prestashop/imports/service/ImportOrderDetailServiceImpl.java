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
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
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

		PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> filter = new HashMap<>();
		Options options = new Options();
		options.setResourceType(PrestashopResourceType.ORDERS);
		options.setFilter(filter);

		for(Integer id : currentStatus) {
			filter.put("current_state", id.toString());
			JSONObject schema =  ws.getJson(options);

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

			PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
			HashMap<String, String> filter = new HashMap<String, String>();
			filter.put("id_order", id.toString());
			Options options = new Options();
			options.setResourceType(PrestashopResourceType.ORDER_DETAILS);
			options.setFilter(filter);
			JSONObject schema =  ws.getJson(options);

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

		PSWebServiceClient ws = new PSWebServiceClient(shopUrl,key);

		if(isStatus == true) {
			orderIds = ws.fetchApiIds(PrestashopResourceType.ORDERS);
		} else {
			orderIds = this.getDraftOrderIds();
		}

		orderLineIds = this.getOrderLineIds(orderIds);
		Options options = new Options();
		options.setResourceType(PrestashopResourceType.ORDER_DETAILS);

		for(Integer id : orderLineIds) {
			try {
				options.setRequestedId(id);
				JSONObject schema = ws.getJson(options);

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
