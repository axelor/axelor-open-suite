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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.axelor.apps.prestashop.entities.ListContainer;
import com.axelor.apps.prestashop.entities.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopContainerEntity;
import com.axelor.apps.prestashop.entities.PrestashopIdentifiableEntity;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class PSWebServiceClient {
	// HttpClient default content types are ISO-8859-1 encoded (except for JSON)
	private final static ContentType XML_CONTENT_TYPE = ContentType.create("text/xml", Consts.UTF_8);

	private final Logger log = LoggerFactory.getLogger(getClass());
	private JAXBContext jaxbContext;

	/** @var string Shop URL */
	protected String url;
	/** @var string Authentification key */
	protected String key;

	private final CloseableHttpClient httpclient;

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
	 */
	public PSWebServiceClient(String url, String key) {
		this.url = url;
		this.key = key;

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(key, ""));

		this.httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
		try {
			jaxbContext = JAXBContext.newInstance("com.axelor.apps.prestashop.entities:com.axelor.apps.prestashop.entities.xlink");
		} catch(JAXBException e) {
			log.error("Unable to create jaxb context", e);
			throw new RuntimeException("Unable to create JAXB context", e);
		}
	}

	/**
	 * Take the status code and throw an exception if the server didn't return 200
	 * or 201 code
	 *
	 * @param status_code
	 *            Status code of an HTTP return
	 * @throws pswebservice.PrestaShopWebserviceException
	 */
	protected void checkStatusCode(CloseableHttpResponse response) throws PrestaShopWebserviceException {
		final int statusCode = response.getStatusLine().getStatusCode();
		if(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) return;
		throw new PrestashopHttpException(
				statusCode,
				String.format("An underlying call to the Prestashop API failed with status code %d: %s (%s)", statusCode, response.getStatusLine().getReasonPhrase(), HttpStatus.getStatusText(statusCode)));
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
	protected RequestResult executeRequest(HttpUriRequest request) throws PrestaShopWebserviceException {
		final RequestResult result = new RequestResult();
		try {
			result.response = httpclient.execute(request);
			checkStatusCode(result.response);
			result.headers = Arrays.asList(result.response.getAllHeaders());
			result.content = result.response.getEntity().getContent();

			return result;
		} catch (UnsupportedOperationException | IOException e) {
			IOUtils.closeQuietly(result.response);
			throw new PrestaShopWebserviceException("Error while processing request", e);
		} catch(PrestaShopWebserviceException e) {
			IOUtils.closeQuietly(result.response);
			throw e;
		}
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
	public Document add(Options options) throws PrestaShopWebserviceException {
		if(options.resourceType == null && StringUtils.isEmpty(options.fullUrl)) throw new IllegalArgumentException("You have to provide an URL or a resource type");
		if(StringUtils.isEmpty(options.xmlPayload)) throw new IllegalArgumentException("You have to provide the XML payload to send");

		HttpPost post = new HttpPost(buildUri(options));
		post.setEntity(new StringEntity(options.xmlPayload, ContentType.create("text/xml", Consts.UTF_8)));

		RequestResult result = null;
		try {
			result = this.executeRequest(post);
			// FIXME we should unmarshall
			return parseXML(result.content);
		} catch (Exception e) {
			throw new PrestaShopWebserviceException("An error occured while processing add response", e);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
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
	public Document get(Options options) throws PrestaShopWebserviceException {
		if(options.resourceType == null && StringUtils.isEmpty(options.fullUrl)) throw new IllegalArgumentException("You have to provide an URL or a resource type");
		if(options.offset != null && options.limit == null) throw new IllegalArgumentException("Offset is only allowed with limit");


		HttpGet httpget = new HttpGet(buildUri(options));
		RequestResult result = null;

		try {
			result = executeRequest(httpget);
			// FIXME we should unmarshall
			return parseXML(result.content);
		} catch (Exception e) {
			throw new PrestaShopWebserviceException("An error occured while processing get response", e);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
		}
	}

	/**
	 * Fetches a single resource by its ID.
	 * @param resourceType Type of resource
	 * @param id Id of the resource to fetch
	 * @return The requested resource or null if such resource does not exists.
	 * @throws PrestaShopWebserviceException
	 */
	public <T extends PrestashopContainerEntity> T fetch(final PrestashopResourceType resourceType, final int id) throws PrestaShopWebserviceException {
		Options options = new Options();
		options.setResourceType(resourceType);
		options.setRequestedId(id);

		HttpGet httpget = new HttpGet(buildUri(options));
		RequestResult result = null;

		try {
			result = executeRequest(httpget);
			return ((Prestashop)jaxbContext
					.createUnmarshaller()
					.unmarshal(result.content))
					.getContent();
		} catch(PrestashopHttpException e) {
			if(e.getStatusCode() == HttpStatus.SC_NOT_FOUND) return null;
			throw e;
		} catch (JAXBException e) {
			throw new PrestaShopWebserviceException("Error while unmarshalling respoinse from fetch", e);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
		}
	}

	/**
	 * Fetches a list of entities based on the given filter. Entities will have all their
	 * attributes set.
	 * @param resourceType Type of resource to fetch.
	 * @param filter Filter to apply (depends on entity)
	 * @return A (possibly empty) list of entities
	 * @throws PrestaShopWebserviceException
	 */
	@SuppressWarnings("unchecked")
	public <T extends PrestashopContainerEntity> List<T> fetch(final PrestashopResourceType resourceType, final Map<String, String> filter) throws PrestaShopWebserviceException {
		Options options = new Options();
		options.setResourceType(resourceType);
		options.setFilter(filter);
		options.setDisplay(Collections.singletonList("full"));

		HttpGet httpget = new HttpGet(buildUri(options));
		RequestResult result = null;

		try {
			result = executeRequest(httpget);
			return ((ListContainer<T>)((Prestashop)jaxbContext
					.createUnmarshaller()
					.unmarshal(result.content)).getContent())
					.getEntities();
		} catch (JAXBException e) {
			throw new PrestaShopWebserviceException("Error while unmarshalling response from fetch", e);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends PrestashopIdentifiableEntity> T save(final PrestashopResourceType resourceType, final T entity) throws PrestaShopWebserviceException {
		Options options = new Options();
		options.setResourceType(resourceType);
		options.setRequestedId(entity.getId());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try {
			Prestashop envelop = new Prestashop();
			envelop.setContent(entity);
			jaxbContext
				.createMarshaller()
				.marshal(envelop, bos);
		} catch(JAXBException e) {
			throw new PrestaShopWebserviceException("Error while marshalling class " + entity.getClass(), e);
		}

		HttpEntityEnclosingRequestBase request;
		if(entity.getId() == null) {
			request = new HttpPost(buildUri(options));
		} else {
			request = new HttpPut(buildUri(options));
		}

		request.setEntity(new ByteArrayEntity(bos.toByteArray(), XML_CONTENT_TYPE));
		RequestResult result = null;

		try {
			result = executeRequest(request);
			return (T)((Prestashop)jaxbContext
					.createUnmarshaller()
					.unmarshal(result.content)).getContent();
		} catch (JAXBException e) {
			throw new PrestaShopWebserviceException("Error while unmarshalling response from save", e);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
		}
	}

	@Deprecated
	public JSONObject getJson(Options options) throws PrestaShopWebserviceException, JSONException {
		if(options.resourceType == null && StringUtils.isEmpty(options.fullUrl)) throw new IllegalArgumentException("You have to provide an URL or a resource type");
		if(options.offset != null && options.limit == null) throw new IllegalArgumentException("Offset is only allowed with limit");

		StringBuilder sb = new StringBuilder(buildUri(options));
		sb.append(sb.indexOf("?") == -1 ? '?' : '&');
		sb.append("output_format=JSON");

		HttpGet httpget = new HttpGet(sb.toString());
		RequestResult result = null;

		try {
			result = executeRequest(httpget);
			String json = IOUtils.toString(result.content, Consts.UTF_8);
			return ("[]".equals(json) ? null : new JSONObject(json));
		} catch (IOException e) {
			throw new PrestaShopWebserviceException("An error occured while processing get response", e);
		} finally {
			log.debug("Closing connection");
			IOUtils.closeQuietly(result.response);
		}
	}

	/**
	 * Head method (HEAD) a resource
	 *
	 * @param opt
	 *            Map representing resource for head request.
	 * @return XMLElement status_code, response
	 */
	public Map<String, String> head(Options options) throws PrestaShopWebserviceException {
		HttpHead httphead = new HttpHead(buildUri(options));
		RequestResult result = executeRequest(httphead);

		if(result.headers == null) return null;
		return result.headers.stream().collect(Collectors.toMap(Header::getName, Header::getValue));
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
	public Document edit(Options options) throws PrestaShopWebserviceException, TransformerException {
		// Existing checks were completely inconsistent
		if((options.resourceType == null || options.entityId == null) && StringUtils.isEmpty(options.fullUrl)) throw new IllegalArgumentException("You have to provide an URL or a resource type and ID");
		if(StringUtils.isEmpty(options.xmlPayload)) throw new IllegalArgumentException("You have to provide the XML payload to send");

		HttpPut httpput = new HttpPut(buildUri(options));
		httpput.setEntity(new StringEntity(options.xmlPayload, ContentType.create("text/xml", Consts.UTF_8)));
		RequestResult result = null;

		try {
			result = executeRequest(httpput);
			// FIXME we should unmarshall
			return parseXML(result.content);
		} catch (Exception e) {
			throw new PrestaShopWebserviceException("An error occured while processing add response", e);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
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
	public boolean delete(Options options) throws PrestaShopWebserviceException {
		if((options.resourceType == null || options.entityId == null) && StringUtils.isEmpty(options.fullUrl)) throw new IllegalArgumentException("You have to provide an URL or a resource type and ID");

		HttpDelete httpdelete = new HttpDelete(buildUri(options));
		RequestResult result = null;

		try {
			result = executeRequest(httpdelete);
		} finally {
			log.debug("Closing connection");
			if(result != null) IOUtils.closeQuietly(result.response);
		}

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
	public Document addImg(final Path imagePath, final PrestashopResourceType resourceType, final int resourceId)
			throws PrestaShopWebserviceException, MalformedURLException, IOException {

		byte[] imageData = IOUtils.toByteArray(new FileInputStream(imagePath.toFile()));
		String requestUrl = String.format("%s/api/images/%s/%s", this.url, resourceType.getLabel(), Integer.toString(resourceId));

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		builder.addPart("image", new ByteArrayBody(imageData, "upload.jpg"));

		HttpPost httppost = new HttpPost(requestUrl);
		httppost.setEntity(builder.build());

		RequestResult result = null;

		try {
			result = executeRequest(httppost);
			// FIXME we should unmarshall (and factorize)
			return parseXML(result.content);
		} catch (Exception e) {
			throw new PrestaShopWebserviceException("An error occured while processing add response", e);
		} finally {
			IOUtils.closeQuietly(result.response);
		}
	}

	private String buildUri(Options options) throws PrestaShopWebserviceException {
		final String url;
		if(StringUtils.isEmpty(options.fullUrl)) {
			if(options.entityId == null) {
				url = String.format("%s/api/%s", this.url, options.resourceType.getLabel());
			} else {
				url = String.format("%s/api/%s/%d", this.url, options.resourceType.getLabel(), options.entityId);
			}
		} else {
			url = options.fullUrl;
		}
		URIBuilder uriBuilder;
		try {
			uriBuilder = new URIBuilder(url);
		} catch (URISyntaxException e) {
			throw new PrestaShopWebserviceException(String.format("Invalid URI %s provided to webservices", url), e);
		}
		if(MapUtils.isNotEmpty(options.filter)) {
			for(Map.Entry<String, String> e : options.filter.entrySet()) {
				uriBuilder.addParameter(String.format("filter[%s]", e.getKey()), e.getValue());
			}
		}
		if(CollectionUtils.isNotEmpty(options.display)) {
			// you've to use display=full or display=[fields,…], display=[full] or display=field wont work
			if(options.display.size() == 1 && options.display.get(0).equals("full")) uriBuilder.addParameter("display", "full");
			else uriBuilder.addParameter("display", "[" + StringUtils.join(options.display, ",") + "]");
		}
		if(CollectionUtils.isNotEmpty(options.sort)) {
			uriBuilder.addParameter("sort", "[" + StringUtils.join(options.sort, ",") + "]");
		}
		if(options.limit != null) {
			uriBuilder.addParameter("limit", (options.offset != null ? options.offset.toString() + "," : "") + options.limit.toString());
		}
		if(options.shopId != null) uriBuilder.addParameter("id_shop", options.shopId.toString());
		if(options.shopGroupId != null) uriBuilder.addParameter("id_group_shop", options.shopGroupId.toString());

		return uriBuilder.toString();
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

	public List<Integer> fetchApiIds(PrestashopResourceType resourceType) throws PrestaShopWebserviceException, JSONException {
		Options options = new Options();
		options.resourceType = resourceType;
		JSONObject schema = this.getJson(options);
		if(schema == null) return Collections.emptyList();

		JSONArray jsonMainArr = schema.getJSONArray(resourceType.getLabel());
		List<Integer> ids = new ArrayList<Integer>(jsonMainArr.length());

		for (int i = 0; i < jsonMainArr.length(); i++) {
			JSONObject childJSONObject = jsonMainArr.getJSONObject(i);
			ids.add(childJSONObject.getInt("id"));
		}

		return ids;
	}

	public static class Options {
		private PrestashopResourceType resourceType;
		private String xmlPayload; // TODO Check if we could marshal in WS
		private Integer entityId;
		private String fullUrl;
		private Integer shopId;
		private Integer shopGroupId;
		private Integer limit;
		private Integer offset;
		private List<String> display;
		private Map<String, String> filter;
		private List<String> sort;

		public void setResourceType(PrestashopResourceType resourceType) {
			this.resourceType = resourceType;
		}

		public void setXmlPayload(String xmlPayload) {
			this.xmlPayload = xmlPayload;
		}

		public void setRequestedId(Integer requestedId) {
			this.entityId = requestedId;
		}

		public void setFullUrl(String fullUrl) {
			this.fullUrl = fullUrl;
		}

		public void setShopId(int shopId) {
			this.shopId = shopId;
		}

		public void setShopGroupId(int shopGroupId) {
			this.shopGroupId = shopGroupId;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}

		public void setOffset(int offset) {
			this.offset = offset;
		}

		public void setDisplay(List<String> display) {
			this.display = display;
		}

		public void setFilter(Map<String, String> filter) {
			this.filter = filter;
		}

		public void setSort(List<String> sort) {
			this.sort = sort;
		}

		public void clear() {
			resourceType = null;
			xmlPayload = null;
			entityId = null;
			fullUrl = null;
			shopId = null;
			shopGroupId = null;
			limit = null;
			offset = null;
			display = null;
			filter = null;
			sort = null;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("resource", resourceType)
					.append("xmlPayload", xmlPayload)
					.append("requestedId", entityId)
					.append("fullUrl", fullUrl)
					.append("shopId", shopId)
					.append("shopGroupId", shopGroupId)
					.append("limit", limit)
					.append("offset", offset)
					.append("display", display)
					.append("filter", filter)
					.append("sort", sort)
					.toString();
		}
	}

	private static class RequestResult {
		CloseableHttpResponse response;
		List<Header> headers;
		InputStream content;
	}
}
