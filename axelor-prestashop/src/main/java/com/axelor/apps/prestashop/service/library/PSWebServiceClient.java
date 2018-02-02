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
package com.axelor.apps.prestashop.service.library;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class PSWebServiceClient {

	/** @var string Shop URL */
	protected String url;
	/** @var string Authentification key */
	protected String key;

	private final CloseableHttpClient httpclient;
	private CloseableHttpResponse response;
	private HashMap<String, Object> responseReturns;

	/**
	 * PrestaShopWebservice constructor. <code>
	 *
	 * try
	 * {
	 * 	PSWebServiceClient ws = new PSWebServiceClient('http://mystore.com/', 'ZQ88PRJX5VWQHCWE4EE7SQ7HPNX00RAJ', false);
	 * 	// Now we have a webservice object to play with
	 * }
	 * catch (PrestaShopWebserviceException ex)
	 * {
	 * 	// Handle exception
	 * }
	 *
	 * </code>
	 *
	 * @param url
	 *            Root URL for the shop
	 * @param key
	 *            Authentification key
	 * @param debug
	 *            Debug mode Activated (true) or deactivated (false)
	 */
	public PSWebServiceClient(String url, String key) {
		this.url = url;
		this.key = key;

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(key, ""));

		this.httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
	}

	/**
	 * Take the status code and throw an exception if the server didn't return 200
	 * or 201 code
	 *
	 * @param status_code
	 *            Status code of an HTTP return
	 * @throws pswebservice.PrestaShopWebserviceException
	 */
	protected void checkStatusCode(int status_code) throws PrestaShopWebserviceException {

		String error_label = "This call to PrestaShop Web Services failed and returned an HTTP status of %d. That means: %s.";
		switch (status_code) {
		case 200:
		case 201:
			break;
		case 204:
			throw new PrestaShopWebserviceException(String.format(error_label, status_code, "No content"), this);
		case 400:
			throw new PrestaShopWebserviceException(String.format(error_label, status_code, "Bad Request"), this);
		case 401:
			throw new PrestaShopWebserviceException(String.format(error_label, status_code, "Unauthorized"), this);
		case 404:
			throw new PrestaShopWebserviceException(String.format(error_label, status_code, "Not Found"), this);
		case 405:
			throw new PrestaShopWebserviceException(String.format(error_label, status_code, "Method Not Allowed"),
					this);
		case 500:
			throw new PrestaShopWebserviceException(String.format(error_label, status_code, "Internal Server Error"),
					this);
		default:
			throw new PrestaShopWebserviceException(
					"This call to PrestaShop Web Services returned an unexpected HTTP status of:" + status_code);
		}
	}

	protected String getResponseContent() {
		try {
			return readInputStreamAsString((InputStream) this.responseReturns.get("response"));
		} catch (IOException ex) {
			return "";
		}
	}

	/**
	 * Handles request to PrestaShop Webservice. Can throw exception.
	 *
	 * @param url
	 *            Resource name
	 * @param request
	 * @return array status_code, response
	 * @throws pswebservice.PrestaShopWebserviceException
	 */
	protected HashMap<String, Object> executeRequest(HttpUriRequest request) throws PrestaShopWebserviceException {

		HashMap<String, Object> returns = new HashMap<>();

		try {
			response = httpclient.execute(request);
			Header[] headers = response.getAllHeaders();
			HttpEntity entity = response.getEntity();

			returns.put("status_code", response.getStatusLine().getStatusCode());
			returns.put("response", entity.getContent());
			returns.put("header", headers);

			this.responseReturns = returns;

		} catch (IOException ex) {
			throw new PrestaShopWebserviceException("Bad HTTP response : " + ex.toString());
		}

		return returns;
	}

	/**
	 * Load XML from string. Can throw exception
	 *
	 * @param responseBody
	 * @return parsedXml
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws org.xml.sax.SAXException
	 * @throws java.io.IOException
	 */
	protected Document parseXML(InputStream responseBody)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		// System.out.println(responseBody);
		return docBuilder.parse(responseBody);
	}

	/**
	 * Add (POST) a resource
	 * <p>
	 * Unique parameter must take : <br>
	 * <br>
	 * 'resource' => Resource name<br>
	 * 'postXml' => Full XML string to add resource<br>
	 * <br>
	 *
	 * @param opt
	 * @return xml response
	 * @throws pswebservice.PrestaShopWebserviceException
	 * @throws TransformerException
	 */
	@SuppressWarnings("deprecation")
	public Document add(Map<String, Object> opt) throws PrestaShopWebserviceException, TransformerException {
		if ((opt.containsKey("resource") && opt.containsKey("postXml"))
				|| (opt.containsKey("url") && opt.containsKey("postXml"))) {
			String completeUrl;
			completeUrl = (opt.containsKey("resource") ? this.url + "/api/" + (String) opt.get("resource")
					: (String) opt.get("url"));
			String xml = (String) opt.get("postXml");
			if (opt.containsKey("id_shop"))
				completeUrl += "&id_shop=" + (String) opt.get("id_shop");
			if (opt.containsKey("id_group_shop"))
				completeUrl += "&id_group_shop=" + (String) opt.get("id_group_shop");

			StringEntity entity = new StringEntity(xml, ContentType.create("text/xml", Consts.UTF_8));
			// entity.setChunked(true);

			HttpPost httppost = new HttpPost(completeUrl);
			httppost.setEntity(entity);

			HashMap<String, Object> resoult = this.executeRequest(httppost);
			this.checkStatusCode((Integer) resoult.get("status_code"));

			try {
				String obj = IOUtils.toString((InputStream) resoult.get("response"));
				InputStream is = new ByteArrayInputStream(obj.trim().getBytes());
				Document doc = this.parseXML(is);
				response.close();
				return doc;
			} catch (ParserConfigurationException | SAXException | IOException ex) {
				ex.printStackTrace();
				throw new PrestaShopWebserviceException("Response XML Parse exception");
			}

		} else {
			throw new PrestaShopWebserviceException("Bad parameters given");
		}

	}

	/**
	 * Retrieve (GET) a resource
	 * <p>
	 * Unique parameter must take : <br>
	 * <br>
	 * 'url' => Full URL for a GET request of Webservice (ex:
	 * http://mystore.com/api/customers/1/)<br>
	 * OR<br>
	 * 'resource' => Resource name,<br>
	 * 'id' => ID of a resource you want to get<br>
	 * <br>
	 * </p>
	 * <code>
	 *
	 * try
	 * {
	 *  PSWebServiceClient ws = new PrestaShopWebservice('http://mystore.com/', 'ZQ88PRJX5VWQHCWE4EE7SQ7HPNX00RAJ', false);
	 *  HashMap<String,Object> opt = new HashMap();
	 *  opt.put("resouce","orders");
	 *  opt.put("id",1);
	 *  Document xml = ws->get(opt);
	 *	// Here in xml, a XMLElement object you can parse
	 * catch (PrestaShopWebserviceException ex)
	 * {
	 *  Handle exception
	 * }
	 *
	 * </code>
	 *
	 * @param opt
	 *            Map representing resource to get.
	 * @return Document response
	 * @throws pswebservice.PrestaShopWebserviceException
	 */
	@SuppressWarnings("rawtypes")
	public Document get(Map<String, Object> opt) throws PrestaShopWebserviceException {
		String completeUrl;
		if (opt.containsKey("url")) {
			completeUrl = (String) opt.get("url");
		} else if (opt.containsKey("resource")) {
			completeUrl = this.url + "/api/" + opt.get("resource");
			if (opt.containsKey("id"))
				completeUrl += "/" + opt.get("id");

			String[] params = new String[] { "filter", "display", "sort", "limit", "id_shop", "id_group_shop" };
			for (String p : params)
				if (opt.containsKey(p))
					try {
						Object param = opt.get(p);

						if (param instanceof HashMap) {
							Map xparams = (HashMap) param;
							Iterator it = xparams.entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry pair = (Map.Entry) it.next();
								completeUrl += "?" + p + "[" + pair.getKey() + "]="
										+ URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&";
								it.remove(); // avoids a ConcurrentModificationException
							}
						} else {
							completeUrl += "?" + p + "=" + URLEncoder.encode((String) opt.get(p), "UTF-8") + "&";
						}
					} catch (UnsupportedEncodingException ex) {
						throw new PrestaShopWebserviceException("URI encodin excepton: " + ex.toString());
					}

		} else {
			throw new PrestaShopWebserviceException("Bad parameters given");
		}

		HttpGet httpget = new HttpGet(completeUrl);
		HashMap<String, Object> resoult = this.executeRequest(httpget);
		this.checkStatusCode((int) resoult.get("status_code"));// check the response validity

		try {
			Document doc = this.parseXML((InputStream) resoult.get("response"));
			response.close();
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			throw new PrestaShopWebserviceException("Response XML Parse exception: " + ex.toString());
		}

	}

	@SuppressWarnings({ "rawtypes", "deprecation" })
	public JSONObject getJson(Map<String, Object> opt) throws PrestaShopWebserviceException, JSONException {
		String completeUrl;
		boolean flag = false;

		if (opt.containsKey("url")) {
			completeUrl = (String) opt.get("url");
		} else if (opt.containsKey("resource")) {
			completeUrl = this.url + "/api/" + opt.get("resource");
			if (opt.containsKey("id"))
				completeUrl += "/" + opt.get("id");

			String[] params = new String[] { "filter", "display", "sort", "limit", "id_shop", "id_group_shop" };
			for (String p : params)
				if (opt.containsKey(p))
					try {
						flag = true;
						Object param = opt.get(p);

						if (param instanceof HashMap) {
							Map xparams = (HashMap) param;
							Iterator it = xparams.entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry pair = (Map.Entry) it.next();
								completeUrl += "?" + p + "[" + pair.getKey() + "]="
										+ URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&";
								it.remove(); // avoids a ConcurrentModificationException
							}
						} else {
							completeUrl += "?" + p + "=" + URLEncoder.encode((String) opt.get(p), "UTF-8") + "&";
						}
					} catch (UnsupportedEncodingException ex) {
						throw new PrestaShopWebserviceException("URI encodin excepton: " + ex.toString());
					}

		} else {
			throw new PrestaShopWebserviceException("Bad parameters given");
		}

		if (flag) {
			completeUrl += "output_format=JSON";
		} else {
			completeUrl += "?output_format=JSON";
		}

		HttpGet httpget = new HttpGet(completeUrl);
		HashMap<String, Object> resoult = this.executeRequest(httpget);
		this.checkStatusCode((int) resoult.get("status_code"));// check the response validity

		try {
			String jsonStr = IOUtils.toString((InputStream) resoult.get("response"));
			JSONObject json = null;

			if (!jsonStr.equals("[]")) {
				json = new JSONObject(jsonStr);
			}
			response.close();
			return json;

		} catch (IOException ex) {
			throw new PrestaShopWebserviceException("Response JSON Parse exception: " + ex.toString());
		}

	}

	/**
	 * Head method (HEAD) a resource
	 *
	 * @param opt
	 *            Map representing resource for head request.
	 * @return XMLElement status_code, response
	 */
	public Map<String, String> head(Map<String, Object> opt) throws PrestaShopWebserviceException {
		String completeUrl;
		if (opt.containsKey("url")) {
			completeUrl = (String) opt.get("url");
		} else if (opt.containsKey("resource")) {
			completeUrl = this.url + "/api/" + opt.get("resource");
			if (opt.containsKey("id"))
				completeUrl += "/" + opt.get("id");

			String[] params = new String[] { "filter", "display", "sort", "limit" };
			for (String p : params)
				if (opt.containsKey("p"))
					try {
						completeUrl += "?" + p + "=" + URLEncoder.encode((String) opt.get(p), "UTF-8") + "&";
					} catch (UnsupportedEncodingException ex) {
						throw new PrestaShopWebserviceException("URI encodin excepton: " + ex.toString());
					}

		} else
			throw new PrestaShopWebserviceException("Bad parameters given");

		HttpHead httphead = new HttpHead(completeUrl);
		HashMap<String, Object> resoult = this.executeRequest(httphead);
		this.checkStatusCode((int) resoult.get("status_code"));// check the response validity

		HashMap<String, String> headers = new HashMap<String, String>();
		for (Header h : (Header[]) resoult.get("header")) {
			headers.put(h.getName(), h.getValue());
		}
		return headers;
	}

	/**
	 * Edit (PUT) a resource
	 * <p>
	 * Unique parameter must take : <br>
	 * <br>
	 * 'resource' => Resource name ,<br>
	 * 'id' => ID of a resource you want to edit,<br>
	 * 'putXml' => Modified XML string of a resource<br>
	 * <br>
	 *
	 * @param opt
	 *            representing resource to edit.
	 * @return
	 * @throws TransformerException
	 */
	public Document edit(Map<String, Object> opt) throws PrestaShopWebserviceException, TransformerException {

		String xml = "";
		String completeUrl;
		if (opt.containsKey("url"))
			completeUrl = (String) opt.get("url");
		else if (((opt.containsKey("resource") && opt.containsKey("id")) || opt.containsKey("url"))
				&& opt.containsKey("postXml")) {
			completeUrl = (opt.containsKey("url")) ? (String) opt.get("url")
					: this.url + "/api/" + opt.get("resource") + "/" + opt.get("id");
			xml = (String) opt.get("postXml");
			if (opt.containsKey("id_shop"))
				completeUrl += "&id_shop=" + opt.get("id_shop");
			if (opt.containsKey("id_group_shop"))
				completeUrl += "&id_group_shop=" + opt.get("id_group_shop");
		} else
			throw new PrestaShopWebserviceException("Bad parameters given");

		StringEntity entity = new StringEntity(xml, ContentType.create("text/xml", Consts.UTF_8));
		// entity.setChunked(true);

		HttpPut httpput = new HttpPut(completeUrl);
		httpput.setEntity(entity);
		HashMap<String, Object> resoult = this.executeRequest(httpput);
		this.checkStatusCode((int) resoult.get("status_code"));// check the response validity

		try {
			Document doc = this.parseXML((InputStream) resoult.get("response"));
			response.close();
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			throw new PrestaShopWebserviceException("Response XML Parse exception: " + ex.toString());
		}
	}

	/**
	 * Delete (DELETE) a resource. Unique parameter must take : <br>
	 * <br>
	 * 'resource' => Resource name<br>
	 * 'id' => ID or array which contains IDs of a resource(s) you want to
	 * delete<br>
	 * <br>
	 *
	 * @param opt
	 *            representing resource to delete.
	 * @return
	 * @throws pswebservice.PrestaShopWebserviceException
	 */
	public boolean delete(Map<String, Object> opt) throws PrestaShopWebserviceException {
		String completeUrl = "";
		if (opt.containsKey("url"))
			completeUrl = (String) opt.get("url");
		else if (opt.containsKey("resource") && opt.containsKey("id"))
			// if (opt.get("id"))
			// completeUrl = this.url+"/api/"+opt.get("resource")+"/?id=[".implode(',',
			// $options['id'])+"]";
			// else
			completeUrl = this.url + "/api/" + opt.get("resource") + "/" + opt.get("id");

		if (opt.containsKey("id_shop"))
			completeUrl += "&id_shop=" + opt.get("id_shop");
		if (opt.containsKey("id_group_shop"))
			completeUrl += "&id_group_shop=" + opt.get("id_group_shop");

		HttpDelete httpdelete = new HttpDelete(completeUrl);
		HashMap<String, Object> resoult = this.executeRequest(httpdelete);

		this.checkStatusCode((int) resoult.get("status_code"));// check the response validity

		return true;
	}

	/**
	 *
	 * @param imgURL
	 * @param productId
	 * @return xml response
	 * @throws pswebservice.PrestaShopWebserviceException
	 * @throws java.net.MalformedURLException
	 */
	public Document addImg(String imgURL, Integer productId)
			throws PrestaShopWebserviceException, MalformedURLException, IOException {

		URL imgUrl = new URL(imgURL);
		InputStream is = imgUrl.openStream();

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}
		buffer.flush();

		String completeUrl = this.url + "/api/images/products/" + String.valueOf(productId);
		HttpPost httppost = new HttpPost(completeUrl);

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("image", new ByteArrayBody(buffer.toByteArray(), "upload.jpg"));

		HttpEntity entity = builder.build();
		httppost.setEntity(entity);

		HashMap<String, Object> resoult = this.executeRequest(httppost);
		this.checkStatusCode((Integer) resoult.get("status_code"));

		try {
			Document doc = this.parseXML((InputStream) resoult.get("response"));
			response.close();
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException ex) {
			throw new PrestaShopWebserviceException("Response XML Parse exception");
		}

	}

	private String readInputStreamAsString(InputStream in) throws IOException {

		BufferedInputStream bis = new BufferedInputStream(in);
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		int result = bis.read();
		while (result != -1) {
			byte b = (byte) result;
			buf.write(b);
			result = bis.read();
		}

		String returns = buf.toString();
		return returns;
	}

	public String DocumentToString(Document doc) throws TransformerException {
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer trans = transfac.newTransformer();
		trans.setOutputProperty(OutputKeys.METHOD, "xml");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");
		trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));

		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		DOMSource source = new DOMSource(doc.getDocumentElement());

		trans.transform(source, result);
		String xmlString = sw.toString();

		return xmlString;
	}

	public List<Integer> fetchApiIds(String resources) throws PrestaShopWebserviceException, JSONException {

		new PSWebServiceClient(this.url, this.key);
		HashMap<String, Object> opt = new HashMap<String, Object>();
		opt.put("resource", resources);
		JSONObject schema = this.getJson(opt);
		List<Integer> ids = new ArrayList<Integer>();

		JSONArray jsonMainArr = schema.getJSONArray(resources);

		for (int i = 0; i < jsonMainArr.length(); i++) {
			JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
			ids.add(childJSONObject.getInt("id"));
		}

		return ids;
	}
}
