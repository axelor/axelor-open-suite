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
package com.axelor.studio.service.wkf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaSelectRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionGroup;
import com.axelor.meta.schema.actions.ActionGroup.ActionItem;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.ViewPanel;
import com.axelor.studio.db.Wkf;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.db.repo.WkfRepository;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class handle workflow processing. It updated related models/views
 * according to update in workflow. Also remove effects of workflow it some
 * workflow deleted. Call node and transition service for further processing.
 * 
 * @author axelor
 *
 */
public class WkfService {

	private final Logger log = LoggerFactory.getLogger(getClass());

	protected Wkf workflow = null;

	protected String dasherizeModel = null;

	protected String moduleName;

	protected Inflector inflector;

	protected ViewBuilder viewBuilder;

	@Inject
	protected RoleRepository roleRepo;

	@Inject
	private ViewBuilderRepository viewBuilderRepo;

	@Inject
	private WkfNodeService nodeService;

	@Inject
	private WkfTransitionService transitionService;

	@Inject
	private MetaActionRepository metaActionRepo;

	@Inject
	private MetaFieldRepository metaFieldRepo;

	@Inject
	private ViewItemRepository viewItemRepo;

	@Inject
	private WkfTrackingService trackingService;

	@Inject
	private MetaSelectRepository metaSelectRepo;

	@Inject
	private WkfRepository wkfRepo;

	/**
	 * Method to process workflow. It call node and transition service for nodes
	 * and transitions linked with workflow.
	 * 
	 * @param wkf
	 *            Worklfow to process.
	 * @return Exception string if any issue in processing else null.
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public String process(Wkf wkf) {

		try {
			workflow = wkf;
			inflector = Inflector.getInstance();
			moduleName = wkf.getMetaModule().getName();
			dasherizeModel = inflector.dasherize(workflow.getMetaModel().getName());
			viewBuilder = wkf.getViewBuilder();
			ActionGroup actionGroup = nodeService.process();

			viewBuilder.setEdited(true);
			addWkfStatusView(viewBuilder, workflow.getDisplayTypeSelect());

			transitionService.process(actionGroup);

			trackingService.addTracking(viewBuilder);

			viewBuilderRepo.save(viewBuilder);

			workflow.setEdited(false);
			wkfRepo.save(workflow);

		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}

		return null;
	}
	
	public String getSelectName() {
		
		if (workflow != null && viewBuilder != null) {
			MetaField wkfField = workflow.getWkfField();
			String selectName = "wkf." + inflector.dasherize(viewBuilder.getName()).replace("_", ".");
			selectName += "." + inflector.dasherize(wkfField.getName()).replace("_", ".") + ".select";
			
			return selectName;
		}
		
		return null;
	}

	/**
	 * Method add 'wkfStatus' field in ViewBuilder linked with Workflow.
	 * 
	 * @param viewBuilder
	 *            ViewBuilder linked with workflow.
	 * @param navSelect
	 *            Selection of widget type for 'wkfStatus' field.
	 */
	private void addWkfStatusView(ViewBuilder viewBuilder, Integer navSelect) {

		ViewPanel viewPanel = viewBuilder.getViewPanelList().get(0);

		String selectName = getSelectName();
		
		MetaField statusField = workflow.getWkfField();
		List<ViewItem> viewItemList = viewPanel.getViewItemList();
		if (viewItemList != null) {
			for (ViewItem item : viewItemList) {
				if (item.getMetaField() != null && item.getMetaField().equals(statusField)) {
					if (navSelect > 0) {
						item.setWidget("NavSelect");
					} else {
						item.setWidget("normal");
					}
					item.setMetaSelect(metaSelectRepo.findByName(selectName));
					item.setDefaultValue("'" + statusField.getDefaultString() + "'");
					item.setColSpan(12);
					return;
				}
			}
		}

		ViewItem viewField = new ViewItem(statusField.getName());
		viewField.setTypeSelect(0);
		viewField.setFieldType("string");
		viewField.setMetaField(statusField);
		viewField.setColSpan(12);
		viewField.setSequence(0);
		viewField.setReadonly(true);
		viewField.setMetaSelect(metaSelectRepo.findByName(selectName));
		viewField.setDefaultValue("'" + statusField.getDefaultString() + "'");

		if (navSelect > 0) {
			viewField.setWidget("NavSelect");
		}

		viewPanel.addViewItemListItem(viewField);

	}

	/**
	 * Method set 'clearWkf' boolean in ViewBuilder. Also call methods to remove
	 * wkf related buttons, actions and status. Method call when related
	 * workflow deleted.
	 * 
	 * @param viewBuilder
	 *            ViewBuilder linked with deleted workflow.
	 * @return Error string if issue in setting boolean, else return null.
	 */
	@Transactional
	public String clearViewBuilder(ViewBuilder viewBuilder) {

		try {
			MetaModel metaModel = viewBuilder.getMetaModel();
			String modelName = "";
			if (metaModel != null) {
				inflector = Inflector.getInstance();
				modelName = inflector.dasherize(metaModel.getName());
			}

			String actions = clearViewButtons(viewBuilder.getToolbar(),
					"action-" + modelName + "-wkf", new ArrayList<String>());
			removeMetaActions(actions);
			viewBuilder.setClearWkf(false);
			String onSave = viewBuilder.getOnSave();
			if (onSave != null) {
				onSave = onSave.replace("action-group-" + modelName + "-wkf",
						"");
				onSave = onSave.replace("action-" + modelName + "-wkf", "");
				onSave = onSave.replace("save,"
						+ WkfTrackingService.ACTION_TRACK, "");
				onSave = onSave.replace(",,", ",");
			}
			viewBuilder.setOnSave(onSave);
			viewBuilder = viewBuilderRepo.save(viewBuilder);
			log.debug("viewBuilder onSave : {}", viewBuilder.getOnSave());
			removeWkfStatus(viewBuilder);
		} catch (Exception e) {
			return e.toString();
		}

		return null;

	}

	/**
	 * Method delete ViewButtons of deleted workflow.
	 * 
	 * @param viewButtons
	 *            List of ViewButton to remove.
	 * @param actionNames
	 *            Comma separated names of actions related with buttons.
	 * @param skipList
	 *            Buttons names to skip from deleting
	 * @return Comma separated names of actions related with button.
	 */
	@Transactional
	public String clearViewButtons(List<ViewItem> viewButtons,
			String actionNames, List<String> skipList) {

		if (viewButtons == null) {
			return null;
		}

		Iterator<ViewItem> buttonIter = viewButtons.iterator();

		while (buttonIter.hasNext()) {

			ViewItem viewButton = buttonIter.next();
			log.debug("Button : {}, onClick: {}", viewButton.getName(),
					viewButton.getOnClick());
			String buttonName = viewButton.getName();

			if (viewButton.getWkfButton() && !skipList.contains(buttonName)) {

				String onClick = viewButton.getOnClick();

				if (onClick != null
						&& !onClick
								.equals(WkfTrackingService.ACTION_OPEN_TRACK)
						&& !onClick.equals(WkfTrackingService.ACTION_TRACK)) {
					if (actionNames == null) {
						actionNames = onClick;
					} else {
						actionNames += "," + onClick;

					}
				}

				buttonIter.remove();
				viewItemRepo.remove(viewButton);

			}

		}

		return actionNames;

	}

	/**
	 * Remove MetaActions from comma separated names in string.
	 * 
	 * @param actionNames
	 *            Comma separated string of action names.
	 */
	@Transactional
	public void removeMetaActions(String actionNames) {

		if (actionNames != null) {
			List<MetaAction> metaActions = metaActionRepo
					.all()
					.filter("self.name in ?1",
							Arrays.asList(actionNames.split(","))).fetch();

			for (MetaAction action : metaActions) {
				metaActionRepo.remove(action);
			}
		}
	}

	/**
	 * Remove wkfStatus field from ViewBuilder
	 * 
	 * @param viewBuilder
	 *            ViewBuilder having 'wkfStatus' field.
	 */
	@Transactional
	public void removeWkfStatus(ViewBuilder viewBuilder) {
		
		final String WKF_STATUS = "wkfStatus";
		MetaField metaField = metaFieldRepo
				.all()
				.filter("self.name = '" + WKF_STATUS
						+ "' AND self.metaModel = ?1",
						viewBuilder.getMetaModel()).fetchOne();

		if (metaField != null) {
			List<ViewItem> viewFields = viewItemRepo
					.all()
					.filter("self.name = '" + WKF_STATUS
							+ "' AND self.metaField = ?1", metaField).fetch();

			for (ViewItem viewItem : viewFields) {
				viewItemRepo.remove(viewItem);
			}
			// metaField.getMetaModel().setEdited(true);
			metaField = metaFieldRepo.save(metaField);
			MetaSelect metaSelect = metaField.getMetaSelect();
			log.debug("Meta select to remove: {}", metaSelect);
			if (metaSelect != null) {
				metaSelectRepo.remove(metaSelect);
			}
		}

	}

	/**
	 * It update comma separated string of action with new action.
	 * 
	 * @param actions
	 *            String of comma separated action names.
	 * @param action
	 *            Name of action to add in actions string.
	 * @param save
	 *            Boolean to check if needs to add 'save' in actions string.
	 * @return Updated actions with new action.
	 */
	protected String getUpdatedActions(String actions, String action,
			boolean save) {

		if (!Strings.isNullOrEmpty(actions)) {
			List<String> actionList = Arrays.asList(actions.split(","));
			if (actionList.contains(action)) {
				return actions;
			}
			if (save && !actions.endsWith(",save")) {
				actions += ",save";
			}
			return actions += "," + action;
		}

		if (save) {
			return "save," + action;
		}

		return action;
	}

	/**
	 * Method to find/create ViewButton by button name from ViewBuilder.
	 * 
	 * @param viewBuilder
	 *            ViewBuilder to check for button.
	 * @param buttonName
	 *            Name of button to search.
	 * @return Button searched or created.
	 */
	protected ViewItem getViewButton(ViewBuilder viewBuilder, String buttonName) {

		ViewItem viewButton = null;

		if (viewBuilder.getToolbar() != null) {
			for (ViewItem button : viewBuilder.getToolbar()) {
				if (button.getName().equals(buttonName)) {
					viewButton = button;
					break;
				}
			}
		}

		if (viewButton == null) {
			viewButton = new ViewItem(buttonName);
			viewButton.setTypeSelect(1);
			viewBuilder.addToolbar(viewButton);
		}

		return viewButton;

	}

	/**
	 * Method to find all edited workflow and process it. Also it clear all
	 * ViewBuilders having 'clearWkf' boolean true.
	 * 
	 * @return Result string in clearing views or processing workflow.
	 */
	public String processWkfs() {

		List<ViewBuilder> clearWkfList = viewBuilderRepo.all()
				.filter("self.clearWkf = true").fetch();
		String result = null;

		for (ViewBuilder viewBuilder : clearWkfList) {
			result = clearViewBuilder(viewBuilder);
			if (result != null) {
				return result;
			}
		}

		List<Wkf> processWkfs = wkfRepo.all().filter("self.edited = true")
				.fetch();

		for (Wkf wkf : processWkfs) {
			result = process(wkf);
			if (result != null) {
				return result;
			}
		}

		return result;
	}

	/**
	 * Update xml of MetaAction with xml string passed. It creates new
	 * MetaAction if no MetaAction not found.
	 * 
	 * @param actionName
	 *            Name of MetaAction to update.
	 * @param actionType
	 *            Type of MetaAction.
	 * @param xml
	 *            Xml to update in MetaAction.
	 */
	@Transactional
	public void updateMetaAction(String actionName, String actionType,
			String xml) {

		MetaAction action = metaActionRepo.findByName(actionName);

		if (action == null) {
			action = new MetaAction(actionName);
			action.setModel(workflow.getMetaModel().getFullName());
			action.setModule(moduleName);
			action.setType(actionType);
		}
		action.setXml(xml);
		action = metaActionRepo.save(action);

	}

	protected ActionGroup createActionGroup(String name, List<String> actions,
			String condition) {

		ActionGroup actionGroup = new ActionGroup();
		actionGroup.setName(name);

		List<ActionItem> actionItems = new ArrayList<ActionGroup.ActionItem>();

		for (String action : actions) {
			ActionItem actionItem = new ActionItem();
			actionItem.setCondition(condition);
			actionItem.setName(action);
			actionItems.add(actionItem);
		}

		actionGroup.setActions(actionItems);

		String xml = XMLViews.toXml(actionGroup, true);

		updateMetaAction(name, "action-group", xml);

		return actionGroup;
	}

}
