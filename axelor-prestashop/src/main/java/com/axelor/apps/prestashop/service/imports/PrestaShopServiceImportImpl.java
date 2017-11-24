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

package com.axelor.apps.prestashop.service.imports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.axelor.app.AppSettings;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ProductCategory;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.CurrencyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductCategoryRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.prestashop.db.SaleOrderStatus;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.PSWebServiceClient;
import com.axelor.apps.prestashop.service.PrestaShopWebserviceException;
import com.axelor.apps.sale.db.CancelReason;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

@SuppressWarnings("deprecation")
public class PrestaShopServiceImportImpl implements PrestaShopServiceImport {
	
	@Inject
	private SaleOrderService saleOrderService;
	
	@Inject
	private PartnerRepository partnerRepo;
	
	@Inject
	private ProductRepository productRepo;
	
	@Inject
	private ProductCategoryRepository productCategoryRepo;
	
	@Inject
	private CompanyRepository companyRepo;
	
	@Inject
	private CityRepository cityRepo;
	
	@Inject
	private CountryRepository countryRepo;
	
	@Inject
	private SaleOrderRepository saleOrderRepo;
	
	@Inject
	private PaymentConditionRepository paymentConditionRepo;

	@Inject
	private CurrencyRepository currencyRepo;
	
	@Inject
	private MetaFiles metaFiles;
	
	File importFile = File.createTempFile("Import Log", ".txt");
	FileWriter fwImport = null;
	BufferedWriter bwImport = null;
	
	private final String shopUrl;
	private final String key;
	private final boolean isStatus;
	private final PaymentMode paymentMode;
	private final List<SaleOrderStatus> saleOrderStatus;
	
    PSWebServiceClient ws;
    HashMap<String,Object> opt;
    Document schema;
    Integer totalDone = 0;
    Integer totalAnomaly = 0;
	
   /**
    * Initialize constructor. 
    * 
    * @throws IOException
    */
    public PrestaShopServiceImportImpl() throws IOException {

		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
		isStatus = prestaShopObj.getIsOrderStatus();
		saleOrderStatus = prestaShopObj.getSaleOrderStatusList();
		paymentMode = prestaShopObj.getPaymentMode();
		ws = new PSWebServiceClient(shopUrl, key);
		fwImport = new FileWriter(importFile, true);
		bwImport = new BufferedWriter(fwImport);
	}
    
    /**
	 * Import Axelor base module (Currency, Country, Partners/Contact, Product Category, Product).  
	*/
    public void importAxelorBase() throws IOException, PrestaShopWebserviceException {
		
		importAxelorCurrencies();
		importAxelorCountries();
		importAxelorPartners();
		importAxelorPartnerAddresses();
		importAxelorProductCategories();
		importAxelorProducts();
	}
	
    /**
     * Import Axelor Base, SaleOrder, SaleOrderLine module.
     */
    @Override
	public Batch importPrestShop(Batch batch) throws IOException, PrestaShopWebserviceException {
    	
		this.importAxelorBase();
		importAxelorSaleOrders();
		importAxelorSaleOrderLines();
		File importFile = closeLog();
		MetaFile importMetaFile = metaFiles.upload(importFile);
		batch.setPrestaShopBatchLog(importMetaFile);
		batch.setDone(totalDone);
		batch.setAnomaly(totalAnomaly);
		return batch;
	}
    
	/**
	 * Initialize title/header for import log file. 
	 * 
	 * @param objectName an Object name as title in log file
	 * @throws IOException
	 */
	public void importLogObjectHeder(String objectName) throws IOException {
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write(objectName);
	}
	
	/**
	 * 
	 * @param done number of succeed 
	 * @param anomaly number of error
	 * @throws IOException
	 */
	public void importLogObjectHederDetails(Integer done, Integer anomaly) throws IOException {
		totalDone += done;
		totalAnomaly += anomaly;
		bwImport.newLine();
		bwImport.newLine();
		bwImport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
	
	/**
	 *  Log error/bug in import log file. 
	 *  
	 * @param id print id of current object
	 * @param msg display message/log which are executed
	 * @throws IOException
	 */
	public void importLog(String id, String msg) throws IOException {
		bwImport.newLine();
		bwImport.newLine();
		bwImport.write("Id - " + id + " " + msg);
	}
	
	/**
	 * Close export log file.
	 * 
	 * @return import log file object 
	 * @throws IOException
	 */
	public File closeLog() throws IOException {
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.close();
		fwImport.close();
		return importFile;
	}
	
	/**
	 * Initialize new connection with prestashop for perticular object.
	 * 
	 * @param resource particular resource/object of prestashop
	 * @param id particular id of resource/object
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 */
	public void importConnection(String resource, String id) throws PrestaShopWebserviceException {
		ws = new PSWebServiceClient(shopUrl + "/api/"+ resource + "/"+ id,key);
		opt = new HashMap<String, Object>();
		opt.put("resource", resource);
		schema = ws.get(opt);
	}
	
	/**
	 * Fetch prestashop API ids for perticular object/resource.
	 * 
	 * @param resources resource particular resource/object of prestashop
	 * @param node resource child node
	 * @return list of ids of particular resources
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 */
	public List<String> fetchApiIds(String resources, String node) throws PrestaShopWebserviceException {

		ws = new PSWebServiceClient(shopUrl, key);
		opt = new HashMap<String, Object>();
		opt.put("resource", resources);
		schema = ws.get(opt);
		NodeList nodeList = schema.getElementsByTagName(node);
		List<String> ids = new ArrayList<String>();

		for (int x = 0, size = nodeList.getLength(); x < size; x++) {
			ids.add(nodeList.item(x).getAttributes().getNamedItem("id").getNodeValue());
		}
		return ids;
	}
	
	/**
	 * Fetch only draft status prestashop API ids for perticular object/resource.
	 * 
	 * @return list of order ids which are in draft state
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 */
	public List<String> getDraftOrderIds() throws PrestaShopWebserviceException {
		
		List<String> orderIds = new ArrayList<String>();
		PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
		HashMap<String, String> orderMap = new HashMap<String, String>();
		List<Integer> currentStatus = new ArrayList<Integer>();
		
		
		for(SaleOrderStatus orderStatus : saleOrderStatus) {
			
			if(orderStatus.getAbsStatus() == 1) {
				currentStatus.add(orderStatus.getPrestaShopStatus());
			}
		}
		
		for(Integer id : currentStatus) {
			
			orderMap.put("current_state", id.toString());
			opt = new HashMap<String, Object>();
			opt.put("resource", "orders");
			opt.put("filter", orderMap);
			Document str =  ws.get(opt);
			
			NodeList list = str.getElementsByTagName("orders");
			for(int i = 0; i < list.getLength(); i++) {
			    Element element = (Element) list.item(i);
			    NodeList nodeList = element.getElementsByTagName("order");
			    for(int j = 0; j < nodeList.getLength(); j++) {
			    	Node order = nodeList.item(j);
			    	
			    	if(nodeList.getLength() > 0) {
			    		orderIds.add(order.getAttributes().getNamedItem("id").getNodeValue());
				    }
			    }
			}
		}
		
		return orderIds;
	}
	
	/**
	 * Fetch only OrderDetail/SaleOrderLine prestashop API ids for perticular object/resource.
	 * 
	 * @param orderIds list of order ids
	 * @return return order details of particular order id
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 */
	public List<String> getOrderLineIds(List<String> orderIds) throws PrestaShopWebserviceException {
		
		List<String> orderDetailIds = new ArrayList<String>();
			
		for(String id : orderIds) {
			
			PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
			HashMap<String, String> orderDetailMap = new HashMap<String, String>();
			orderDetailMap.put("id_order", id);
			opt = new HashMap<String, Object>();
			opt.put("resource", "order_details");
			opt.put("filter", orderDetailMap);
			Document str =  ws.get(opt);
			
			NodeList list = str.getElementsByTagName("order_details");
			for(int i = 0; i < list.getLength(); i++) {
			    Element element = (Element) list.item(i);
			    NodeList nodeList = element.getElementsByTagName("order_detail");
			    for(int j = 0; j < nodeList.getLength(); j++) {
			    	Node orderDetail = nodeList.item(j);
			    	
			    	if(nodeList.getLength() > 0) {
			    		orderDetailIds.add(orderDetail.getAttributes().getNamedItem("id").getNodeValue());
				    }
			    	
			    }
			}
		}
		
		return orderDetailIds;
	}
	
	/**
	 * Fetch only OrderDetail/SaleOrderLine prestashop API ids for perticular object/resource
	 * 
	 * @param id of category
	 * @return category details
	 * @throws PrestaShopWebserviceException
	 */
	public String[] getParentCategoryName(String id) throws PrestaShopWebserviceException {
		
		PSWebServiceClient ws = new PSWebServiceClient(shopUrl + "/api/categories/" + id,key);
		HashMap<String, Object> opt = new HashMap<String, Object>();
		opt.put("resource", "categories");
		Document schema = ws.get(opt);
		NodeList list = schema.getChildNodes();
		String[] category = new String[2];
		
		for (int i = 0; i < list.getLength(); i++) {

			if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) list.item(i);
				category[0] = element.getElementsByTagName("name").item(0).getFirstChild().getTextContent();
				category[1] = element.getElementsByTagName("link_rewrite").item(0).getFirstChild().getTextContent();
			}
		}
		return category;
	}
	
	/**
	 * Import product image to ABS
	 * 
	 * @param productId on which product image should be add
	 * @param imgId of images
	 * @return object of metafile
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
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
	
	/**
	 * Update particular order by status
	 * 
	 * @param order object of SaleOrder
	 * @param status like draft, finalize, order confirm, finished, cancel
	 * @return complete saleOrder with updated staus
	 * @throws Exception
	 */
	public SaleOrder updateOrderStatus(SaleOrder order, int status) throws Exception {
		
		for(SaleOrderStatus saleOrderStatus : saleOrderStatus) {
			if(status == saleOrderStatus.getPrestaShopStatus()) {
				if(saleOrderStatus.getAbsStatus() == 1) {
					order.setStatusSelect(saleOrderStatus.getAbsStatus());
						
				} else if (saleOrderStatus.getAbsStatus() == 2) {
					order.setManualUnblock(true);
					saleOrderService.finalizeSaleOrder(order);
					order.setStatusSelect(saleOrderStatus.getAbsStatus());
					
				} else if (saleOrderStatus.getAbsStatus() == 3) {
					order.setManualUnblock(true);
					saleOrderService.finalizeSaleOrder(order);
					saleOrderService.confirmSaleOrder(order);
					order.setStatusSelect(saleOrderStatus.getAbsStatus());
					
				} else if (saleOrderStatus.getAbsStatus() == 4) {
					order.setManualUnblock(true);
					saleOrderService.finalizeSaleOrder(order);
					saleOrderService.confirmSaleOrder(order);
					order.setStatusSelect(saleOrderStatus.getAbsStatus());
					
				} else if (saleOrderStatus.getAbsStatus() == 5) {
					CancelReason cancelReason = new CancelReason();
					cancelReason.setName("From prestashop");
					order.setCancelReason(cancelReason);
					order.setCancelReasonStr("From prestashop");
					saleOrderService.cancelSaleOrder(order, order.getCancelReason(), order.getCancelReasonStr());
					order.setStatusSelect(saleOrderStatus.getAbsStatus());
					
				} else {
					order.setStatusSelect(1);
				}
			}
		}
		return order;
	}
	
	/**
	 * Set delivery and invoice address of saleOrder
	 * 
	 * @param deliveryAddressId contains id of prestashop's address
	 * @param invoiceAddressId contains id of prestashop's address
	 * @param order object required for add address
	 * @return order with add/updated new address
	 */
	public SaleOrder manageAddresses(String deliveryAddressId, String invoiceAddressId, SaleOrder order) {

		Address deliveryAddress = null;
		Address invoiceAddress = null;
		
		if (deliveryAddressId != null) {
			deliveryAddress = Beans.get(AddressRepository.class).all().filter("self.prestaShopId = ?" , deliveryAddressId).fetchOne(); 
			order.setDeliveryAddress(deliveryAddress);
			order.setDeliveryAddressStr(deliveryAddress.getAddressL4() + "\n" + deliveryAddress.getAddressL5() 
				+ "\n" + deliveryAddress.getAddressL6() + "\n" + deliveryAddress.getAddressL7Country().getName());
		}
		
		if(invoiceAddressId != null) {
			invoiceAddress = Beans.get(AddressRepository.class).all().filter("self.prestaShopId = ?" , invoiceAddressId).fetchOne();
			order.setMainInvoicingAddress(invoiceAddress);
			order.setMainInvoicingAddressStr(invoiceAddress.getAddressL4() + "\n" + invoiceAddress.getAddressL5() 
				+ "\n" + invoiceAddress.getAddressL6() + "\n" + invoiceAddress.getAddressL7Country().getName());
		}
		
		return order;
	}
	
	/**
	 *	Add/update currency to ABS	
	 * 
	 * @throws IOException
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 */
	@Transactional
	public void importAxelorCurrencies() throws IOException, PrestaShopWebserviceException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("Currency");
		List<String> currencyIds = this.fetchApiIds("currencies", "currency");
		
		for (String id : currencyIds) {
			
			this.importConnection("currencies", id);
			NodeList list = schema.getChildNodes();
			Currency currency = null;
			String code = null;
			String name = null;
			String prestashopId = null;
			
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					try {
						Element element = (Element) list.item(i);
						prestashopId = element.getElementsByTagName("id").item(0).getTextContent();
						currency = Beans.get(CurrencyRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();
						
						if(currency == null) {
							currency = currencyRepo.findByCode(element.getElementsByTagName("iso_code").item(0).getTextContent());
							if(currency == null) {
								currency = new Currency();
								currency.setPrestaShopId(prestashopId);
							} else {
								currency.setPrestaShopId(prestashopId);
							}
						}
						
						if(!element.getElementsByTagName("iso_code").item(0).getTextContent().equals(null) &&
								!element.getElementsByTagName("name").item(0).getTextContent().equals(null)) {
							
							code = element.getElementsByTagName("iso_code").item(0).getTextContent();
							name = element.getElementsByTagName("name").item(0).getTextContent();
							currency.setCode(code);
							currency.setName(name);
						} else {
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CURRENCY), IException.NO_VALUE);
						}
						currencyRepo.save(currency);
						done++;
						
					} catch (AxelorException e) {
						
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					} catch (Exception e) {

						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					}
				}
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}
	
	/**
	 * Add/Update Country to ABS. 
	 * 
	 * @throws IOException
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 */
	public void importAxelorCountries() throws IOException, PrestaShopWebserviceException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("Country");
		List<String> countryIds = this.fetchApiIds("countries", "country");

		for (String id : countryIds) {
			this.importConnection("countries", id);
			NodeList list = schema.getChildNodes();
			String name = null;
			String alpha2Code = null;
			
			for (int i = 0; i < list.getLength(); i++) {

				if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					try {
						Element element = (Element) list.item(i);
						name = element.getElementsByTagName("name").item(i).getFirstChild().getTextContent();
						alpha2Code = element.getElementsByTagName("iso_code").item(i).getTextContent();
						
						if(name.isEmpty()) {
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COUNTRY), IException.NO_VALUE);
						}
						
						Country country = Beans.get(CountryRepository.class).all().filter("self.alpha2Code = ?", alpha2Code).fetchOne();
						if(country == null) {
							country = new Country();
						}
						country.setName(name);
						country.setAlpha2Code(alpha2Code);
						country.setPrestaShopId(id);
						countryRepo.save(country);
						done++;
						
					} catch (AxelorException e) {
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					} catch (Exception e) {
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					}
				}
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}
	
	/**
	 * Add/Update Partner/Customer to prestashop
	 * 
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 * @throws IOException
	 */
	@Transactional
	public void importAxelorPartners() throws PrestaShopWebserviceException, IOException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("Partner(Customer)");
		List<String> customerIds = this.fetchApiIds("customers", "customer");
		
		for (String id : customerIds) {
			
			this.importConnection("customers", id);
			NodeList list = schema.getChildNodes();
			Integer titleSelect;
			String emailName = null;
			String firstName = null;
			String name = null;
			String email = null;
			String website = null;
			String company = null;
			boolean flag = false; 
			Partner partner = null;
			Partner contactPartner = null;

			for (int i = 0; i < list.getLength(); i++) {

				if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					try {
						Element element = (Element) list.item(i);
						String prestashop_id = element.getElementsByTagName("id").item(i).getTextContent();
						partner = Beans.get(PartnerRepository.class).all().filter("self.prestaShopId = ?", prestashop_id).fetchOne();
						
						if(partner == null) {
							flag = true;
							partner = new Partner();
							partner.setPrestaShopId(prestashop_id);
						}
						
						if (!element.getElementsByTagName("firstname").item(0).getTextContent().isEmpty() && 
								!element.getElementsByTagName("lastname").item(0).getTextContent().isEmpty()) {
							
							titleSelect = Integer.parseInt(element.getElementsByTagName("id_gender").item(0).getTextContent());
							firstName = element.getElementsByTagName("firstname").item(0).getTextContent();
							name = element.getElementsByTagName("lastname").item(0).getTextContent();
							email = element.getElementsByTagName("email").item(0).getTextContent();
							website = element.getElementsByTagName("website").item(0).getTextContent();
							company = element.getElementsByTagName("company").item(0).getTextContent();
							
							if(!element.getElementsByTagName("company").item(0).getTextContent().isEmpty()) {
								
								partner.setPartnerTypeSelect(1);
								partner.setName(company);
								
								if(flag) {
									contactPartner = new Partner();
								} else {
									contactPartner = partner.getContactPartnerSet().iterator().next();
								}
									
								partner.setFullName(company);
								contactPartner.setName(name);
								contactPartner.setFirstName(firstName);
								contactPartner.setIsContact(true);
								contactPartner.setMainPartner(partner);
								
								if(name != null && firstName != null) {
									contactPartner.setFullName(name + " " + firstName);
								} else if (name != null && firstName == null) {
									contactPartner.setFullName(name);
								} else if (name == null && firstName != null) {
									contactPartner.setFullName(firstName);
								}
									
								if(flag) {
									partner.addContactPartnerSetItem(contactPartner);
								}
									
							} else {
								
								partner.setPartnerTypeSelect(2);
								partner.setFirstName(firstName);
								partner.setName(name);

								if(name != null && firstName != null) {
									partner.setFullName(name + " " + firstName);
								} else if (name != null && firstName == null) {
									partner.setFullName(name);
								} else if (name == null && firstName != null) {
									partner.setFullName(firstName);
								}
							}
						} else {
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COMPANY), IException.NO_VALUE);
						}

						partner.setTitleSelect(titleSelect);
						EmailAddress emailAddress = new EmailAddress();
						emailAddress.setAddress(email);

						if (partner != null)
							emailName = partner.getFullName();
						
						if (emailAddress.getAddress() != null)
							emailName = emailAddress.getAddress();
						
						if(flag) {
							partner.addCompanySetItem(AuthUtils.getUser().getActiveCompany());
							flag = false;
						}

						emailAddress.setName(emailName);
						partner.setEmailAddress(emailAddress);
						partner.setWebSite(website);
						partner.setIsCustomer(true);
						partnerRepo.persist(partner);
						partnerRepo.save(partner);
						done++;

					} catch (AxelorException e) {
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					} catch (Exception e) {
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					}
				}
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}
	
	/**
	 * Add/Update Address to ABS.
	 * 
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 * @throws IOException
	 */
	@Transactional
	public void importAxelorPartnerAddresses() throws PrestaShopWebserviceException, IOException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("Partner(Customer) Address");
		List<String> addressesIds = this.fetchApiIds("addresses", "address");
		String partnerId = null;
		String deletedId = null;
		String addressId = null;
		String addressL4 = null;
		String addressL5 = null;
		String postcode = null;
		String cityName = null;
		String countryId = null;
		Partner partner = null;
		Address address = null;
		PartnerAddress partnerAddress = null;
		City city = null;
		
		
		for (String id : addressesIds) {
			ws = new PSWebServiceClient(shopUrl + "/api/addresses/" + id, key);
			opt.put("resource", "addresses");
			schema = ws.get(opt);
			NodeList nodeList = schema.getChildNodes();

			for (int i = 0; i < nodeList.getLength(); i++) {

				if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) nodeList.item(i);
					deletedId = element.getElementsByTagName("deleted").item(i).getTextContent();
					partnerId = element.getElementsByTagName("id_customer").item(i).getTextContent();
						
					if(deletedId.equals("1"))
						continue;
					
					try {
						if(partnerId == null || partnerId.equals("0"))
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_ADDRESS), IException.NO_VALUE);
						
						addressId = element.getElementsByTagName("id").item(i).getTextContent();
						addressL4 = element.getElementsByTagName("address1").item(i).getTextContent();
						addressL5 = element.getElementsByTagName("address2").item(i).getTextContent();
						cityName = element.getElementsByTagName("city").item(i).getTextContent();
						postcode = element.getElementsByTagName("postcode").item(i).getTextContent();
						countryId = element.getElementsByTagName("id_country").item(i).getTextContent();
						partner = Beans.get(PartnerRepository.class).all().filter("self.prestaShopId = ?", partnerId).fetchOne();
						address = Beans.get(AddressRepository.class).all().filter("self.prestaShopId = ?", id).fetchOne(); 
						city = cityRepo.findByName(cityName);

						if(city == null) {
							city = new City();
						}
						
						Country country = Beans.get(CountryRepository.class).all().filter("self.prestaShopId = ?", countryId).fetchOne();
						if(country == null)
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COUNTRY), IException.NO_VALUE);
							
						if(address == null) {
							address = new Address();
							address.setAddressL4(addressL4);
							address.setAddressL5(addressL5);
							city.setName(cityName);
							city.setHasZipOnRight(false);
							address.setAddressL6(cityName + " " + postcode);
							address.setFullName(address.getAddressL4().toString() + " " + address.getAddressL6().toString());
							address.setCity(city);
							address.setAddressL7Country(country);
							partnerAddress = new PartnerAddress();
							partnerAddress.setIsDeliveryAddr(true);
							partnerAddress.setIsInvoicingAddr(true);
							partnerAddress.setIsDefaultAddr(true);
							partnerAddress.setAddress(address);
							partnerAddress.setPartner(partner);
							address.setPrestaShopId(addressId);
							partner.addPartnerAddressListItem(partnerAddress);
							
						} else {
							address.setAddressL4(addressL4);
							address.setAddressL5(addressL5);
							city.setName(cityName);
							city.setHasZipOnRight(false);
							address.setAddressL6(cityName + " " + postcode);
							address.setFullName(address.getAddressL4().toString() + " " + address.getAddressL6().toString());
							address.setAddressL7Country(country);
							address.setPrestaShopId(addressId);
							address.setCity(city);
						}
						partnerRepo.save(partner);
						done++;
						
					} catch (AxelorException e) {
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					} catch (Exception e) {
						this.importLog(id, e.getMessage());
						anomaly++;
						continue;
					}
				}
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}
	
	/**
	 * Add/Update ProductCategory to ABS
	 * 
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 * @throws DOMException
	 * @throws IOException
	 */
	@Transactional
	public void importAxelorProductCategories() throws PrestaShopWebserviceException, DOMException, IOException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("Product Category");
		List<String> categoryIds = this.fetchApiIds("categories", "category");
		String prestashopId = null;
		
		for (String id : categoryIds) {
			this.importConnection("categories", id);
			NodeList list = schema.getChildNodes();
			
			for (int i = 0; i < list.getLength(); i++) {
				
				if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) list.item(i);
					try {
						ProductCategory productCategory = null;
						String name = element.getElementsByTagName("name").item(i).getFirstChild().getTextContent();
						String code = element.getElementsByTagName("link_rewrite").item(i).getFirstChild().getTextContent();
						String parentId = element.getElementsByTagName("id_parent").item(i).getTextContent();
						ProductCategory categoryObj = productCategoryRepo.findByName(name);
						ProductCategory parentProductCategory = null;
						String[] parentCategoryData = new String[2];
						
						if(categoryObj != null) {
							categoryObj.setPrestaShopId(id);
							parentProductCategory = Beans.get(ProductCategoryRepository.class).all().filter("self.prestaShopId = ?", parentId).fetchOne();
							categoryObj.setParentProductCategory(parentProductCategory);
							productCategoryRepo.save(categoryObj);
							done++;
							continue;
						}
						
						prestashopId = element.getElementsByTagName("id").item(i).getTextContent();
						productCategory = Beans.get(ProductCategoryRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();
						
						if(productCategory == null) {
							productCategory = productCategoryRepo.findByCode(element.getElementsByTagName("link_rewrite").item(0).getTextContent());
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
						this.importLog(prestashopId, e.getMessage());
						anomaly++;
						continue;
					} catch (Exception e) {
						this.importLog(prestashopId, e.getMessage());
						anomaly++;
						continue;
					}
				}
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}

	/**
	 * Add/Update Product to ABS. 
	 * 
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 * @throws IOException
	 */
	@Transactional
	public void importAxelorProducts() throws PrestaShopWebserviceException, IOException {

		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("Product");
		String prestashopId = null;
		List<String> productsIds = this.fetchApiIds("products", "product");

		for (String id : productsIds) {
		
			try {
				this.importConnection("products", id);
				NodeList list = schema.getChildNodes();

				for (int i = 0; i < list.getLength(); i++) {
					
					if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) list.item(i);
						Product product = null;
						String name = null;
						String categoryDefaultId = element.getElementsByTagName("id_category_default").item(0).getTextContent();
						prestashopId = element.getElementsByTagName("id").item(0).getTextContent();
						String imgId = element.getElementsByTagName("id_default_image").item(0).getTextContent();
						MetaFile img = this.importProductImages(prestashopId, imgId);
						BigDecimal price = new BigDecimal((element.getElementsByTagName("price").item(0).getTextContent().isEmpty() ? "000.00"
										: element.getElementsByTagName("price").item(0).getTextContent()));
						BigDecimal width = new BigDecimal((element.getElementsByTagName("width").item(0).getTextContent().isEmpty()) ? "000.00"
										: element.getElementsByTagName("width").item(0).getTextContent());
						Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.parse(element.getElementsByTagName("date_add").item(0).getTextContent());
						String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
						LocalDate startDate = LocalDate.parse(formattedDate);
						String description = element.getElementsByTagName("description").item(0).getTextContent();
						String productTypeSelect = element.getElementsByTagName("link_rewrite").item(0).getTextContent();
						product = Beans.get(ProductRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();
						
						if(product == null) {
							product = new Product();
							product.setPrestaShopId(prestashopId);
						}

						if (!element.getElementsByTagName("name").item(0).getFirstChild().getTextContent().equals(null)) {
							name = element.getElementsByTagName("name").item(0).getFirstChild().getTextContent();
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
						product.setProductTypeSelect(productTypeSelect);
						productRepo.save(product);
						done++;
					}
				}
			} catch (AxelorException e) {
				this.importLog(prestashopId, e.getMessage());
				anomaly++;
				continue;
			} catch (Exception e) {
				this.importLog(prestashopId, e.getMessage());
				anomaly++;
				continue;
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}

	/**
	 * Add/Update SaleOrder to ABS. 
	 * 
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 * @throws IOException
	 */
	@Transactional
	public void importAxelorSaleOrders() throws PrestaShopWebserviceException, IOException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("SaleOrder");
		List<String> orderIds = null;
		Long clientPartner = 0l;
		String prestashopId = null;
		
		if(isStatus == true) {
			orderIds = this.fetchApiIds("orders", "order");
		} else {
			orderIds = this.getDraftOrderIds();
		}

		for (String id : orderIds) {
			try {
				ws = new PSWebServiceClient(shopUrl + "/api/orders/" + id, key);
				opt = new HashMap<String, Object>();
				opt.put("resource", "orders");
				schema = ws.get(opt);
				NodeList list = schema.getChildNodes();

				for (int i = 0; i < list.getLength(); i++) {
					if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element) list.item(i);
						prestashopId = element.getElementsByTagName("id").item(i).getTextContent();
						SaleOrder order = null;
						Partner partner = null;
						Integer status = null;
						Currency currency = null;
						order = Beans.get(SaleOrderRepository.class).all().filter("self.prestaShopId = ?", prestashopId).fetchOne();

						if(order == null) {
							order = new SaleOrder();
							order.setPrestaShopId(prestashopId);
						}
						
						if (!element.getElementsByTagName("id_customer").item(0).getTextContent().isEmpty()) {
							clientPartner = Long.parseLong(element.getElementsByTagName("id_customer").item(0).getTextContent());
						} else {
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CUSTOMER), IException.NO_VALUE);
						}

						partner = Beans.get(PartnerRepository.class).all().filter("self.prestaShopId = ?", clientPartner).fetchOne();
						status = Integer.parseInt(element.getElementsByTagName("current_state").item(0).getTextContent());
						currency = Beans.get(CurrencyRepository.class).all().filter("self.prestaShopId = ?" , element.getElementsByTagName("id_currency").item(0).getTextContent()).fetchOne();
						
						if(partner == null)
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CUSTOMER), IException.NO_VALUE);
						
						if(currency == null)
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CURRENCY), IException.NO_VALUE);
						
						String deliveryAddressId = element.getElementsByTagName("id_address_delivery").item(0).getTextContent();
						String invoiceAddressId = element.getElementsByTagName("id_address_invoice").item(0).getTextContent();
						String paymentCondition = element.getElementsByTagName("payment").item(0).getTextContent();
						Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(element.getElementsByTagName("date_add").item(0).getTextContent());
						String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
						PaymentCondition paymentConditionObj = paymentConditionRepo.findByName(paymentCondition);
						
						if(paymentConditionObj == null) {
							paymentConditionObj = new PaymentCondition();
							paymentConditionObj.setCode(paymentCondition);
							paymentConditionObj.setName(paymentCondition);
						}
						
						order.setClientPartner(partner);
						order = this.manageAddresses(deliveryAddressId, invoiceAddressId, order);
						order.setExTaxTotal(new BigDecimal(element.getElementsByTagName("total_paid_tax_excl").item(0).getTextContent()).setScale(2, RoundingMode.HALF_UP));
						order.setTaxTotal(new BigDecimal(element.getElementsByTagName("total_wrapping_tax_incl").item(0).getTextContent()).setScale(2, RoundingMode.HALF_UP));
						order.setInTaxTotal(new BigDecimal(element.getElementsByTagName("total_paid").item(0).getTextContent()).setScale(2, RoundingMode.HALF_UP));
						order.setCreationDate(LocalDate.parse(formattedDate));
						order.setOrderDate(LocalDate.parse(formattedDate));
						order.setExternalReference(element.getElementsByTagName("reference").item(0).getTextContent());
						order.setPaymentMode(paymentMode);
						order.setPaymentCondition(paymentConditionObj);
						Company company = companyRepo.find(1L);
						order.setCompany(company);
						order.setCurrency(currency);
						order = this.updateOrderStatus(order, status);
						saleOrderRepo.persist(order);
						saleOrderRepo.save(order);
						done++;
					}
				}
			} catch (AxelorException e) {
				this.importLog(prestashopId, e.getMessage());
				anomaly++;
				continue;
			} catch (Exception e) {
				this.importLog(prestashopId, e.getMessage());
				anomaly++;
				continue;
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}
	
	/**
	 * Add/Update SaleOrderLine to ABS.
	 * 
	 * @throws PrestaShopWebserviceException display exception which are handled by prestashop
	 * @throws IOException
	 */
	@Transactional
	public void importAxelorSaleOrderLines() throws PrestaShopWebserviceException, IOException {
		
		Integer done = 0;
		Integer anomaly = 0;
		this.importLogObjectHeder("OrderLine");
		List<String> orderIds = null;
		List<String> orderLineIds = null;
		boolean isNewSaleOrderLine = false;
		
		if(isStatus == true) {
			orderIds = this.fetchApiIds("orders", "order");
		} else {
			orderIds = this.getDraftOrderIds();
		}
		orderLineIds = this.getOrderLineIds(orderIds);
		
		for(String id : orderLineIds) {
			try {
				ws = new PSWebServiceClient(shopUrl + "/api/order_details/" + id, key);
				opt = new HashMap<String, Object>();
				opt.put("resource", "order_details");
				schema = ws.get(opt);
				NodeList list = schema.getChildNodes();
				
				for (int i = 0; i < list.getLength(); i++) {
					if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
						
						Element element = (Element) list.item(i);
						SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).all().filter("self.prestaShopId = ?", element.getElementsByTagName("id_order").item(i).getTextContent()).fetchOne();
						Product product = Beans.get(ProductRepository.class).all().filter("self.prestaShopId = ?", element.getElementsByTagName("product_id").item(i).getTextContent()).fetchOne();
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
						saleOrderLine.setProductName(element.getElementsByTagName("product_name").item(i).getTextContent());
						saleOrderLine.setQty(new BigDecimal(element.getElementsByTagName("product_quantity").item(i).getTextContent()));
						saleOrderLine.setPrice(new BigDecimal(element.getElementsByTagName("product_price").item(i).getTextContent()));
						saleOrderLine.setExTaxTotal(new BigDecimal(element.getElementsByTagName("total_price_tax_incl").item(i).getTextContent()));
						saleOrderLine.setSaleOrder(saleOrder);
						saleOrderLine.setPrestaShopId(id);
						
						if(isNewSaleOrderLine) {
							saleOrder.addSaleOrderLineListItem(saleOrderLine);
							isNewSaleOrderLine = false;
						}
						saleOrderRepo.persist(saleOrder);
						saleOrderRepo.save(saleOrder);
						done++;
					}
				}
			} catch (AxelorException e) {
				this.importLog(id, e.getMessage());
				anomaly++;
				continue;
			} catch (Exception e) {
				this.importLog(id, e.getMessage());
				anomaly++;
				continue;
			}
		}
		this.importLogObjectHederDetails(done, anomaly);
	}
}
