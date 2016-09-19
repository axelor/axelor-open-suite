/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.studio.service.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionRecord;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.RightManagementService;
import com.axelor.studio.service.ViewRemovalService;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * This service class handle all view side processing. It call recording of
 * different types of ViewBuilder. Provides common method to create view and
 * action xml for all types of view. It also call Menu and RightMangement
 * processing.
 * 
 * @author axelor
 *
 */
public class ViewBuilderService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private boolean updateMeta = false;

	private File viewDir;

	private Map<String, List<ViewBuilder>> modelMap = new HashMap<String, List<ViewBuilder>>();

	@Inject
	private ViewBuilderRepository viewBuilderRepo;

	@Inject
	private MetaViewRepository metaViewRepo;

	@Inject
	private MetaActionRepository metaActionRepo;

	@Inject
	private FormBuilderService formBuilderService;

	@Inject
	private GridBuilderService gridBuilderService;

	@Inject
	private RightManagementService rightMgmtService;

	@Inject
	private MenuBuilderService menuBuilderService;

	@Inject
	private ChartBuilderService chartBuilderService;

	@Inject
	private DashboardBuilderService dashboardBuilderService;

	@Inject
	private ViewRemovalService removalService;

	@Inject
	private ActionBuilderService actionBuilderService;

	@Inject
	private ReportBuilderService reportBuilderService;
	
	@Inject
	private ConfigurationService configService;

	private boolean autoCreate = false;
	
	public String build(String module, boolean updateMeta, 
			boolean autoCreate, boolean updateAll) throws AxelorException {
		
		this.autoCreate = autoCreate;
		this.updateMeta = updateMeta;
		modelMap = new HashMap<String, List<ViewBuilder>>(); 
		
		if (!updateMeta) {
			this.viewDir = configService.getViewDir(module, true);
		}

		try {
			removalService.remove(module, viewDir);
			reportBuilderService.processReports();
			
			String error = actionBuilderService.build(module, viewDir, updateMeta);
			if (error != null) {
				return error;
			}
			
			menuBuilderService.build(module, viewDir, updateMeta);
			
			List<ViewBuilder> viewBuilders = getViewBuilders(module, updateAll);

			splitByModel(viewBuilders.iterator());

			for (String model : modelMap.keySet()) {
				processView(model, modelMap.get(model));
			}

			rightMgmtService.updateRights();

			updateEdited(viewBuilders, updateMeta);

			return null;
		} catch (IOException | JAXBException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}
	
	private List<ViewBuilder> getViewBuilders(String module, boolean updateAll) {
		
		if (updateAll) {
			return viewBuilderRepo.all().filter("self.metaModule.name = ?1", module).fetch();
		}
		else {
			String query = "self.edited = true";
			if (!updateMeta) {
				query += " OR self.recorded = false";
			}
			query = "self.metaModule.name = ?1 AND (" + query + ")";
			return viewBuilderRepo.all()
					.filter(query, module).fetch();
		}
		
	}

	/**
	 * Update 'edited' boolean of ViewBuilder to false and 'recorded' to true if
	 * not updateMeta. Method called at the end of ViewBuilder processing.
	 * 
	 * @param viewBuilders
	 *            List of ViewBuilders.
	 * @param updateMeta
	 *            Boolean to check if only to update 'edited' boolean or
	 *            'recorded' too.
	 */
	@Transactional
	public void updateEdited(List<ViewBuilder> viewBuilders, boolean updateMeta) {

		for (ViewBuilder viewBuilder : viewBuilders) {
			if (!updateMeta) {
				viewBuilder.setRecorded(true);
			}
			viewBuilder.setEdited(false);
			viewBuilderRepo.save(viewBuilder);
		}

	}

	/**
	 * Create or Update MetaAction from Action
	 * 
	 * @param actionIterator
	 *            Action iterator.
	 */
	@Transactional
	public void generateMetaAction(String module, List<Action> actions) {

		for (Action action : actions) {
			String name = action.getName();

			MetaAction metaAction = metaActionRepo.all()
					.filter("self.name = ?1 and self.module = ?2", name, module).fetchOne();

			if (metaAction == null) {
				metaAction = new MetaAction();
				metaAction.setModule(module);
				metaAction.setName(name);
				metaAction.setModel(action.getModel());
				Class<?> klass = action.getClass();
				String type = klass.getSimpleName()
						.replaceAll("([a-z\\d])([A-Z]+)", "$1-$2")
						.toLowerCase();
				metaAction.setType(type);
				metaAction.setXmlId(action.getXmlId());
			}

			metaAction.setXml(XMLViews.toXml(action, true));

			metaAction = metaActionRepo.save(metaAction);

		}

	}

	/**
	 * Create or Update metaView from AbstractView.
	 * 
	 * @param viewIterator
	 *            ViewBuilder iterator
	 */
	@Transactional
	public MetaView generateMetaView(String module, AbstractView view) {

		String name = view.getName();
		String xmlId = view.getXmlId();
		String model = view.getModel();
		String viewType = view.getType();

		log.debug("Search view name: {}, xmlId: {}", name, xmlId);

		MetaView metaView;
		if (xmlId != null) {
			metaView = metaViewRepo
					.all()
					.filter("self.name = ?1 and self.xmlId = ?2 and self.type = ?3",
							name, xmlId, viewType).fetchOne();
		} else {
			metaView = metaViewRepo
					.all()
					.filter("self.name = ?1 and self.type = ?2", name, viewType)
					.fetchOne();
		}

		log.debug("Meta view found: {}", metaView);

		if (metaView == null) {
			metaView = metaViewRepo
					.all()
					.filter("self.name = ?1 and self.type = ?2", name, viewType)
					.order("-priority").fetchOne();
			Integer priority = 20;
			if (metaView != null) {
				priority = metaView.getPriority() + 1;
			}
			metaView = new MetaView();
			metaView.setName(name);
			metaView.setModule(module);
			metaView.setXmlId(xmlId);
			metaView.setModel(model);
			metaView.setPriority(priority);
			metaView.setType(viewType);
			metaView.setTitle(view.getTitle());
		}

		String viewXml = XMLViews.toXml(view, true);
		metaView.setXml(viewXml.toString());
		return metaViewRepo.save(metaView);

	}

	/**
	 * Split ViewBuilder according to model and update modelMap.
	 * 
	 * @param iterator
	 *            ViewBuilder iterator
	 */
	private void splitByModel(Iterator<ViewBuilder> iterator) {

		if (!iterator.hasNext()) {
			return;
		}

		ViewBuilder viewBuilder = iterator.next();

		String model = viewBuilder.getModel();
		String viewType = viewBuilder.getViewType();

		if (model == null) {
			if (viewType != null && !viewType.equals("dashboard")) {
				log.debug("Rejected view: {}", viewBuilder.getName());
				splitByModel(iterator);
				return;
			}
			model = "Dashboard";
		} else {
			model = model.substring(model.lastIndexOf(".") + 1);
		}

		if (!modelMap.containsKey(model)) {
			modelMap.put(model, new ArrayList<ViewBuilder>());
		}

		modelMap.get(model).add(viewBuilder);

		splitByModel(iterator);

	}

	/**
	 * Method to process ViewBuilder according to its type. It will call method
	 * to create view xml and generate MetaView.
	 * 
	 * @param model
	 *            Name of model.
	 * @param viewBuilders
	 *            List of ViewBuilders to process.
	 * @throws JAXBException
	 *             Xml processing exception
	 * @throws IOException
	 *             File handling exception
	 */
	@Transactional
	public void processView(String model, List<ViewBuilder> viewBuilders)
			throws JAXBException, IOException {

		for (ViewBuilder viewBuilder : viewBuilders) {
			AbstractView view = null;
			List<Action> actions = new ArrayList<Action>();

			switch (viewBuilder.getViewType()) {
				case "form":
					view = formBuilderService.getView(viewBuilder, autoCreate);
					actions.addAll(formBuilderService.getActionRecords());
					break;
				case "grid":
					view = gridBuilderService.getView(viewBuilder);
					break;
				case "chart":
					view = chartBuilderService.getView(viewBuilder);
					ActionRecord actionRecord = chartBuilderService
							.getOnNewAction();
					if (actionRecord != null) {
						actions.add(actionRecord);
					}
					break;
				case "dashboard":
					view = dashboardBuilderService.getView(viewBuilder);
					actions.addAll(dashboardBuilderService.getActions());
					break;
			}

			if (view != null) {
				String module = viewBuilder.getMetaModule().getName();
				MetaView metaView = generateMetaView(module, view);
				viewBuilder.setMetaViewGenerated(metaView);
				metaViewRepo.save(metaView);
				generateMetaAction(module, actions);
			}

			if (!updateMeta && (view != null || !actions.isEmpty())) {
				writeView(viewDir, model, view, actions);
			}

		}

	}

	/**
	 * Write view and action xml into new viewFile.
	 * 
	 * @param name
	 *            Name of model
	 * @param views
	 *            List of AbstractView of models.
	 * @param actions
	 *            List of Actions of model.
	 * @throws IOException
	 * @throws JAXBException
	 */
	public void writeView(File viewDir, String name, AbstractView view,
			List<Action> actions) throws IOException, JAXBException {

		ObjectViews objectViews = new ObjectViews();

		File viewFile = new File(viewDir, name + ".xml");
		if (viewFile.exists()) {
			String xml = Files.toString(viewFile, Charsets.UTF_8);
			if (!Strings.isNullOrEmpty(xml)) {
				objectViews = XMLViews.fromXML(xml);
			}
		}

		if (objectViews == null) {
			objectViews = new ObjectViews();
		}
		
		if (view != null) {
			List<AbstractView> views = filterOldViews(view,
					objectViews.getViews());
			objectViews.setViews(views);
		}

		if (actions != null && !actions.isEmpty()) {
			actions = filterOldActions(actions, objectViews.getActions());
			objectViews.setActions(actions);
		}
		
		if (view != null || (actions != null && !actions.isEmpty())) {
			XMLViews.marshal(objectViews, new FileWriter(viewFile));
		}

	}

	/**
	 * Write xml file with proper xml header.
	 * 
	 * @param file
	 *            File to write.
	 * @param xmlWriter
	 *            XmlWriter containing view xml.
	 * @throws IOException
	 */
	public void writeFile(File file, StringWriter xmlWriter) throws IOException {

		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(prepareXML(xmlWriter.toString()));
		fileWriter.close();

	}

	/**
	 * Replace old views from extracted views of existing view file with new
	 * AbstractViews.
	 * 
	 * @param views
	 *            List of new AbstractView created.
	 * @param oldViews
	 *            Old List of AbstractView from existing view file.
	 * @return Updated list of old and new AbstractView.
	 */
	private List<AbstractView> filterOldViews(AbstractView view,
			List<AbstractView> oldViews) {

		if (oldViews == null) {
			oldViews = new ArrayList<AbstractView>();
			oldViews.add(view);
			return oldViews;
		}
		Iterator<AbstractView> oldViewIter = oldViews.iterator();
		while (oldViewIter.hasNext()) {
			AbstractView oldView = oldViewIter.next();
			if (oldView.getName().equals(view.getName())) {
				oldViews.remove(oldView);
				break;
			}
		}

		oldViews.add(view);

		return oldViews;
	}

	/**
	 * Replace old Action from ViewFile with New Action.
	 * 
	 * @param actions
	 *            List of new Actions created
	 * @param oldActions
	 *            List of old Actions extracted from file.
	 * @return List of updated list containing both old and new actions.
	 */
	private List<Action> filterOldActions(List<Action> actions,
			List<Action> oldActions) {

		if (oldActions == null) {
			return actions;
		}

		for (Action action : actions) {
			Iterator<Action> oldActionIter = oldActions.iterator();
			while (oldActionIter.hasNext()) {
				Action oldAction = oldActionIter.next();
				if (oldAction.getName().equals(action.getName())) {
					oldActions.remove(oldAction);
					break;
				}
			}
		}

		oldActions.addAll(actions);

		return oldActions;
	}

	/**
	 * Method to format xml string with proper header.
	 * 
	 * @param xml
	 *            Xml string to use.
	 * @return Formatted xml.
	 */
	private String prepareXML(String xml) {

		StringBuilder sb = new StringBuilder(
				"<?xml version='1.0' encoding='UTF-8'?>\n");
		sb.append("<object-views")
				.append(" xmlns='")
				.append(ObjectViews.NAMESPACE)
				.append("'")
				.append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
				.append(" xsi:schemaLocation='")
				.append(ObjectViews.NAMESPACE)
				.append(" ")
				.append(ObjectViews.NAMESPACE + "/" + "object-views_"
						+ ObjectViews.VERSION + ".xsd").append("'")
				.append(">\n").append(xml).append("\n</object-views>");

		return sb.toString();
	}

}
