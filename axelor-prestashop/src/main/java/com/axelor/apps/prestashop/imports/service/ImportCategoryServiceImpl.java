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
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportCategoryServiceImpl implements ImportCategoryService  {

	PSWebServiceClient ws;
    HashMap<String,Object> opt;
    JSONObject schema;
    private final String shopUrl;
	private final String key;
	
	@Inject
	private ProductCategoryRepository productCategoryRepo;
	
	/**
	 * Initialization
	 */
	public ImportCategoryServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}
	
	/**
	 * Get details of parent category
	 * 
	 * @param id of parent category
	 * @return array of category
	 * @throws PrestaShopWebserviceException
	 * @throws JSONException
	 */
	public String[] getParentCategoryName(String id) throws PrestaShopWebserviceException, JSONException {
		
		ws = new PSWebServiceClient(shopUrl,key);
		opt = new HashMap<String, Object>();
		opt.put("resource", "categories");
		opt.put("id", id);
		schema = ws.getJson(opt);
		String[] category = new String[2];
		
		JSONArray namesArr = schema.getJSONObject("category").getJSONArray("name");
		JSONObject names = namesArr.getJSONObject(0);
		String name = names.getString("value");
		JSONArray linkRewriteArr = schema.getJSONObject("category").getJSONArray("link_rewrite");
		JSONObject linkRewrites = linkRewriteArr.getJSONObject(0);
		String code = linkRewrites.getString("value");
		
		category[0] = name;
		category[1] = code;
		return category;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importCategory(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {
		
		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Category");
		String prestashopId = null;
		
		ws = new PSWebServiceClient(shopUrl,key);
		List<Integer> categoryIds = ws.fetchApiIds("categories");
		
		for (Integer id : categoryIds) {
			
			ws = new PSWebServiceClient(shopUrl,key);
			opt = new HashMap<String, Object>();
			opt.put("resource", "categories");
			opt.put("id", id);
			schema = ws.getJson(opt);
			
			try {
				ProductCategory productCategory = null;
				JSONArray namesArr = schema.getJSONObject("category").getJSONArray("name");
				JSONObject names = namesArr.getJSONObject(0);
				String name = names.getString("value");
				
				JSONArray linkRewriteArr = schema.getJSONObject("category").getJSONArray("link_rewrite");
				JSONObject linkRewrites = linkRewriteArr.getJSONObject(0);
				String code = linkRewrites.getString("value");
						
				String parentId = schema.getJSONObject("category").getString("id_parent");
				ProductCategory categoryObj = productCategoryRepo.findByName(name);
				ProductCategory parentProductCategory = null;
				String[] parentCategoryData = new String[2];
				
				if(categoryObj != null) {
					categoryObj.setPrestaShopId(String.valueOf(schema.getJSONObject("category").getInt("id")));
					parentProductCategory = Beans.get(ProductCategoryRepository.class).all().filter("self.prestaShopId = ?", parentId).fetchOne();
					categoryObj.setParentProductCategory(parentProductCategory);
					productCategoryRepo.save(categoryObj);
					done++;
					continue;
				}
				
				prestashopId = String.valueOf(schema.getJSONObject("category").getInt("id"));
				productCategory = Beans.get(ProductCategoryRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();
				
				if(productCategory == null) {
					productCategory = productCategoryRepo.findByCode(code);
					if(productCategory == null) {
						productCategory = new ProductCategory();
						productCategory.setPrestaShopId(prestashopId);
					} else {
						productCategory.setPrestaShopId(prestashopId);
					}
				}

				ProductCategory parentCategory = null;
				
				if(!parentId.equals("0")) {
					parentCategoryData = this.getParentCategoryName(parentId);
					parentCategory = productCategoryRepo.findByName(parentCategoryData[0]);
					
					if(parentCategory == null) {
						parentCategory = new ProductCategory();
						parentCategory.setName(parentCategoryData[0]);
						parentCategory.setCode(parentCategoryData[1]);
					}
				}			

				if (name.equals(null) || code.equals(null)) {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_PRODUCT_CATEGORY), IException.NO_VALUE);
				}	

				productCategory.setCode(code);
				productCategory.setName(name);
				if(!parentId.equals("0")) {
					productCategory.setParentProductCategory(parentCategory);
				}
				
				productCategoryRepo.save(productCategory);
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
