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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportProductServiceImpl implements ImportProductService {

	PSWebServiceClient ws;
    HashMap<String,Object> opt;
    JSONObject schema;
    private final String shopUrl;
	private final String key;
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ProductRepository productRepo;
	
	/**
	 * Initialization
	 */
	public ImportProductServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}
	
	/**
	 * Import product image prestashop
	 * 
	 * @param productId of perticular product of prestashop
	 * @param imgId of product 
	 * @return metafile object of image
	 * @throws IOException
	 */
	@SuppressWarnings({ "resource", "deprecation" })
	public MetaFile importProductImages(String productId, String imgId) throws IOException {

		String path = AppSettings.get().get("file.upload.dir");
		String imageUrl = shopUrl + "/api/images/products/" + productId + "/" + imgId;
		String destinationFile = path + File.separator + productId + ".jpg";
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(imageUrl);
		httpGet.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials(key, ""), "UTF-8", false));
		HttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity responseEntity = httpResponse.getEntity();
		InputStream is = responseEntity.getContent();
		OutputStream os = new FileOutputStream(destinationFile);
		byte[] b = new byte[2048];
		int length;

		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}
		is.close();
		os.close();

		File image = new File(path + File.separator + productId + ".jpg");
		MetaFile imgUpload = metaFiles.upload(image);
		return imgUpload;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importProduct(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {
		
		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Product");
		String prestashopId = null;
		
		ws = new PSWebServiceClient(shopUrl,key);
		List<Integer> productIds = ws.fetchApiIds("products");
		
		for (Integer id : productIds) {
			
			try {
				
				ws = new PSWebServiceClient(shopUrl,key);
				opt = new HashMap<String, Object>();
				opt.put("resource", "products");
				opt.put("id", id);
				schema = ws.getJson(opt);
				
				Product product = null;
				String name = null;
				String categoryDefaultId = schema.getJSONObject("product").getString("id_category_default");
				prestashopId = String.valueOf(schema.getJSONObject("product").getInt("id"));
				String imgId = schema.getJSONObject("product").getString("id_default_image");
				MetaFile img = this.importProductImages(prestashopId, imgId);
				BigDecimal price = new BigDecimal((schema.getJSONObject("product").getString("price").isEmpty() ? "000.00"
										: schema.getJSONObject("product").getString("price")));
				BigDecimal width = new BigDecimal((schema.getJSONObject("product").getString("width").isEmpty()) ? "000.00"
										: schema.getJSONObject("product").getString("width"));
				Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.parse(schema.getJSONObject("product").getString("date_add"));
				String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
				LocalDate startDate = LocalDate.parse(formattedDate);
				
				JSONArray descriptionArr = schema.getJSONObject("product").getJSONArray("description");
				JSONObject childJSONObject = descriptionArr.getJSONObject(0);
				String description = childJSONObject.getString("value");
				
				JSONArray linkRewriteArr = schema.getJSONObject("product").getJSONArray("link_rewrite");
				childJSONObject = linkRewriteArr.getJSONObject(0);
				String productTypeSelect = childJSONObject.getString("value");
				
				product = Beans.get(ProductRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();
				
				if(product == null) {
					product = new Product();
					product.setPrestaShopId(prestashopId);
				}		

				JSONArray nameArr = schema.getJSONObject("product").getJSONArray("name");
				childJSONObject = nameArr.getJSONObject(0);
				childJSONObject.getString("value");
				
				if (!childJSONObject.getString("value").equals(null)) {
					name = childJSONObject.getString("value");
				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_PRODUCT), IException.NO_VALUE);
				}
				
				if (!name.equals(null)) {
					product.setCode(name);
					product.setName(name);
				}
						
				ProductCategory category = Beans.get(ProductCategoryRepository.class).all().filter("self.prestaShopId = ?", categoryDefaultId).fetchOne();
				product.setPicture(img);
				product.setProductCategory(category);
				product.setSalePrice(price);
				product.setWidth(width);
				product.setStartDate(startDate);
				product.setDescription(description);
				product.setFullName(name);
				product.setSellable(true);
				product.setProductTypeSelect(productTypeSelect);
				productRepo.save(product);
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
