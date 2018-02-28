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
package com.axelor.studio.service.template;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.Filter;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.ViewPanel;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.WkfNode;
import com.axelor.studio.db.WkfTransition;
import com.google.inject.Inject;

public class TemplateXmlCreator {

	private final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Inject
	private MetaModelRepository metaModelRepo;

	private Document doc;

	private static final List<String> logFields = Arrays
			.asList(new String[] { "id", "version", "createdOn", "updatedOn",
					"createdBy", "updatedBy" });

	public void createXml(List<String> modelNames, File file) {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("studio-data");
			doc.appendChild(rootElement);

			Iterator<String> modelIter = modelNames.iterator();

			processModel(modelIter, rootElement);

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(file);
			transformer.transform(source, result);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void processModel(Iterator<String> modelIter, Element rootElement)
			throws ClassNotFoundException {

		if (!modelIter.hasNext()) {
			return;
		}

		String modelName[] = modelIter.next().split(",");

		MetaModel metaModel = metaModelRepo.findByName(modelName[0]);

		if (metaModel == null) {
			log.debug("No meta model found: {}", modelName[0]);
			processModel(modelIter, rootElement);
			return;
		}

		List<Model> models = getModels(modelName, metaModel);

		createModelElement(models.iterator(), modelName[0], rootElement, false);

		processModel(modelIter, rootElement);
	}

	private void createModelElement(Iterator<Model> modelIter, String element,
			Element root, boolean fetch) {

		if (!modelIter.hasNext()) {
			return;
		}

		Model model = modelIter.next();
		Class<? extends Model> klass = EntityHelper.getEntityClass(model);
		if (fetch) {
			model = (Model) JPA.find(klass, model.getId());
		}
		Mapper modelMapper = Mapper.of(klass);

		Element modelElement = doc.createElement(element);
		root.appendChild(modelElement);

		Property[] properties = modelMapper.getProperties();

		for (int i = 0; i < properties.length; i++) {
			Property field = properties[i];
			String name = field.getName();
			if (logFields.contains(name)) {
				continue;
			}
			Object obj = field.get(model);
			if (field.getTarget() != null) {
				processRelational(name, obj, field.getType().name(),
						modelElement);
				continue;
			}

			createElement(name, obj, modelElement);
		}

		if (klass.equals(ViewItem.class)) {
			processViewPanel(modelElement, model, modelMapper);
		} else if (klass.equals(Filter.class)) {
			addViewBuilderModel(modelElement, model, modelMapper);
			addWkfModel(modelElement, model, modelMapper);
			addActionBuilderModel(modelElement, model, modelMapper);
		} else if (klass.equals(ActionBuilderLine.class)) {
			addActionBuilderModel(modelElement, model, modelMapper);
		} else if (klass.equals(WkfNode.class)) {
			addWkfNodeModel(modelElement, model, modelMapper);
		}

		createModelElement(modelIter, element, root, fetch);
	}

	@SuppressWarnings("unchecked")
	private List<Model> getModels(String[] modelName, MetaModel metaModel)
			throws ClassNotFoundException {

		String fullName = metaModel.getFullName();
		Class<?> klass = Class.forName(fullName);
		List<Model> models = null;
		if (modelName.length > 1 && modelName[1] != null) {
			models = JPA.all((Class<Model>) klass).filter(modelName[1]).fetch();
		} else {
			models = JPA.all((Class<Model>) klass).fetch();
		}

		return models;
	}

	private void processRelational(String name, Object object, String relation,
			Element modelElement) {

		if (object == null) {
			return;
		}

		if (relation.equals("MANY_TO_ONE")) {
			String nameColumn = getNameColumn(object.getClass());
			Map<String, Object> mapper = Mapper.toMap(object);
			Object obj = mapper.get(nameColumn);
			createElement(name, obj, modelElement);
		} else if (relation.equals("MANY_TO_MANY")) {
			String nameColumn = null;
			@SuppressWarnings("unchecked")
			Set<Object> objects = (Set<Object>) object;
			if (objects == null || objects.isEmpty()) {
				return;
			}
			Element fieldElement = doc.createElement(name);
			modelElement.appendChild(fieldElement);
			for (Object obj : objects) {
				if (nameColumn == null) {
					nameColumn = getNameColumn(object.getClass());
				}
				Map<String, Object> mapper = Mapper.toMap(obj);
				createElement(nameColumn, mapper.get(nameColumn), fieldElement);
			}
		} else {
			@SuppressWarnings("unchecked")
			List<Model> objects = (List<Model>) object;
			if (objects == null || objects.isEmpty()) {
				return;
			}
			if (name.equals("metaFields")) {
				Iterator<Model> fieldIter = objects.iterator();
				while (fieldIter.hasNext()) {
					MetaField field = (MetaField) fieldIter.next();
					if (!field.getCustomised()
							&& !logFields.contains(field.getName())) {
						fieldIter.remove();
					}
				}
			}
			Element fieldElement = doc.createElement(name);
			modelElement.appendChild(fieldElement);
			createModelElement(objects.iterator(), "item", fieldElement, true);
		}
	}

	private String getNameColumn(Class<? extends Object> class1) {

		Mapper mapper = Mapper.of(class1);

		Property[] properties = mapper.getProperties();

		for (int i = 0; i < properties.length; i++) {
			if (properties[i].isNameColumn()) {
				return properties[i].getName();
			}
		}

		return "name";
	}

	private void createElement(String name, Object value, Element parent) {

		if (value != null) {
			Element fieldElement = doc.createElement(name);
			fieldElement.setTextContent(value.toString());
			parent.appendChild(fieldElement);
		}
	}

	private void processViewPanel(Element parent, Model model,
			Mapper modelMapper) {

		Property property = modelMapper.getProperty("viewPanel");
		ViewPanel viewPanel = (ViewPanel) property.get(model);

		if (viewPanel != null) {
			ViewBuilder viewBuilder = viewPanel.getViewBuilder();
			if (viewBuilder == null) {
				viewBuilder = viewPanel.getViewBuilderSideBar();
			}
			createElement("viewBuilder", viewBuilder.getName(), parent);
			createElement("model", viewBuilder.getModel(), parent);
		} else {
			addViewBuilderModel(parent, model, modelMapper);
		}

	}

	private void addViewBuilderModel(Element parent, Model model,
			Mapper modelMapper) {

		Property property = modelMapper.getProperty("viewBuilder");
		ViewBuilder viewBuilder = (ViewBuilder) property.get(model);

		if (viewBuilder != null) {
			createElement("model", viewBuilder.getModel(), parent);
		}
	}

	private void addWkfModel(Element parent, Model model, Mapper modelMapper) {

		Property property = modelMapper.getProperty("wkfTransition");
		WkfTransition wkfTransition = (WkfTransition) property.get(model);

		if (wkfTransition != null) {
			Wkf wkf = wkfTransition.getWkf();
			if (wkf != null) {
				createElement("wkf", wkf.getName(), parent);
				ViewBuilder viewBuilder = wkf.getViewBuilder();
				if (viewBuilder != null) {
					createElement("model", viewBuilder.getModel(), parent);
				}
			}

		}
	}

	private void addActionBuilderModel(Element parent, Model model,
			Mapper modelMapper) {

		Property property = modelMapper.getProperty("actionBuilder");
		ActionBuilder actionBuilder = (ActionBuilder) property.get(model);

		if (actionBuilder != null) {
			MetaModel metaModel = null;
			if (actionBuilder.getTypeSelect() == 0) {
				metaModel = actionBuilder.getTargetModel();
			} else {
				metaModel = actionBuilder.getMetaModel();
			}

			if (metaModel != null) {
				createElement("model", metaModel.getFullName(), parent);
			}
		}
	}

	private void addWkfNodeModel(Element parent, Model model, Mapper modelMapper) {

		Property property = modelMapper.getProperty("wkf");
		Wkf wkf = (Wkf) property.get(model);

		if (wkf != null) {
			createElement("metaModel", wkf.getMetaModel().getName(), parent);
		}
	}
}
