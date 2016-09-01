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
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.auth.db.Role;
import com.axelor.meta.db.MetaPermission;
import com.axelor.meta.db.MetaPermissionRule;
import com.axelor.meta.db.repo.MetaPermissionRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.actions.ActionGroup;
import com.axelor.meta.schema.actions.ActionGroup.ActionItem;
import com.axelor.meta.schema.actions.ActionRecord;
import com.axelor.meta.schema.actions.ActionValidate;
import com.axelor.meta.schema.actions.ActionRecord.RecordField;
import com.axelor.meta.schema.actions.ActionValidate.Alert;
import com.axelor.meta.schema.actions.ActionValidate.Info;
import com.axelor.meta.schema.actions.ActionValidate.Notify;
import com.axelor.meta.schema.actions.ActionValidate.Validator;
import com.axelor.studio.db.Filter;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.WkfTransition;
import com.axelor.studio.service.FilterService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class handle processing of WkfTransition. It creates buttons and
 * actions from WkfTransition to change the status.
 * 
 * @author axelor
 *
 */
class WkfTransitionService {

	private WkfService wkfService;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private List<String> wkfButtonNames;

	@Inject
	private MetaPermissionRepository metaPermissionRepo;

	@Inject
	private FilterService filterService;

	@Inject
	protected WkfTransitionService(WkfService wkfService) {
		this.wkfService = wkfService;
	}

	/**
	 * Root method to access the service. Method call different method to
	 * process transition. Create wkf action that call on save of model to
	 * update status. It save all changes to ViewBuilder linked with Workflow.
	 */
	protected void process(ActionGroup actionGroup) {

		String action = "action-" + wkfService.dasherizeModel + "-wkf";
		wkfButtonNames = new ArrayList<String>();

		List<RecordField> fields = proccessTransitions();
		ViewBuilder viewBuilder = wkfService.viewBuilder;

		String onSave = viewBuilder.getOnSave();
		if (!fields.isEmpty() || actionGroup != null) {
			if (!fields.isEmpty()) {
				String xml = getActionRecordXML(action, fields);
				this.wkfService.updateMetaAction(action, "action-record", xml);
			} else {
				this.wkfService.removeMetaActions(action);
			}
			if (actionGroup != null) {
				ActionItem actionItem = new ActionItem();
				actionItem.setName(action);
				actionGroup.getActions().add(actionItem);
				String xml = XMLViews.toXml(actionGroup, true);
				action = actionGroup.getName();
				this.wkfService.updateMetaAction(action, "action-group", xml);
			}
			onSave = this.wkfService.getUpdatedActions(onSave, action, false);
		}

		else {
			this.wkfService.removeMetaActions(action);
		}

		log.debug("Wkf onSave : {}", onSave);

		String actionsToRemove = wkfService.clearViewButtons(
				viewBuilder.getToolbar(), null, wkfButtonNames);
		wkfService.removeMetaActions(actionsToRemove);

		viewBuilder.setOnSave(onSave);

	}

	/**
	 * Method process each WkfTransition and create RecordField. RecordField
	 * contains status and condition to assign the status. Based on record
	 * fields onSave action will be created. It also add button for transition
	 * if transition executed based on button.
	 * 
	 * @return List of RecordField
	 */
	private List<RecordField> proccessTransitions() {

		List<RecordField> fields = new ArrayList<RecordField>();

		Integer buttonSeq = 0;
		if (wkfService.viewBuilder.getToolbar() != null) {
			buttonSeq = wkfService.viewBuilder.getToolbar().size();
		}

		for (WkfTransition transition : wkfService.workflow.getTransitions()) {

			String condition = WkfService.WKF_STATUS + " == '"
					+ transition.getSource().getName() + "'";

			if (transition.getIsButton()) {
				buttonSeq++;
				addButton(wkfService.viewBuilder, transition, condition,
						buttonSeq);
				continue;
			}

			log.debug("Conditions : {}", transition.getConditions());
			String filters = filterService.getGroovyFilters(
					transition.getConditions(), null);
			log.debug("Filters : {}", filters);
			if (filters != null) {
				condition += " && (" + filters + ")";
			}

			RecordField field = new RecordField();
			field.setName(WkfService.WKF_STATUS);
			field.setCondition(condition);
			field.setExpression("eval:'" + transition.getTarget().getName()
					+ "'");

			fields.add(field);
		}

		return fields;
	}

	/**
	 * Create toolbar ViewButton in ViewBuilder from WkfTransition. Method
	 * called if WkfTransition is based on button.
	 * 
	 * @param viewBuilder
	 *            ViewBuilder to update.
	 * @param transition
	 *            WkfTransition to process.
	 * @param condition
	 *            Condition to show button
	 * @param sequence
	 *            Sequence of button to add in toolbar.
	 */
	private void addButton(ViewBuilder viewBuilder, WkfTransition transition,
			String condition, Integer sequence) {

		String source = transition.getSource().getName();
		String title = transition.getButtonTitle();
		String name = wkfService.inflector.camelize(source + "-" + title, true);
		if (name.equals("save") || name.equals("cancel") || name.equals("back")) {
			name = "wkf" + name;
		}
		wkfButtonNames.add(name);

		ViewItem viewButton = wkfService.getViewButton(viewBuilder, name);
		viewButton.setTitle(title);
		viewButton.setShowIf(condition);
		viewButton.setSequence(sequence);
		viewButton.setWkfButton(true);

		addButtonActions(viewButton, transition, name);

		String permName = this.wkfService.moduleName + "."
				+ wkfService.dasherizeModel.replace("-", ".") + name;
		clearOldMetaPermissions(permName);
		addButtonPermissions(permName, name, transition.getRoleSet());

	}

	/**
	 * Method create action for button from WkfTransition and related
	 * destination nodes. It set onClick of ViewButton with new action created.
	 * 
	 * @param viewButton
	 *            ViewButton to update for onClick.
	 * @param transition
	 *            WkfTransition from where action created.
	 * @param buttonName
	 *            Name of button used in creation of action name.
	 */
	public void addButtonActions(ViewItem viewButton, WkfTransition transition,
			String buttonName) {

		String actionName = buttonName.toLowerCase().replace(" ", "-");
		actionName = "action-" + wkfService.dasherizeModel + "-" + actionName;
		List<String> actions = new ArrayList<String>();
		String xml = "";
		Integer alterType = transition.getAlertTypeSelect();
		String alertMsg = transition.getAlertMsg();
		if (alterType > 0 && alertMsg != null) {
			String type = "alert";
			if (alterType == 2) {
				type = "info";
			}
			String alertAction = actionName + "-alert";
			xml = getActionValidateXML(alertAction, type, alertMsg,
					transition.getConditions());
			this.wkfService.updateMetaAction(alertAction, "action-validate",
					xml);
			actions.add(alertAction);
		}

		List<RecordField> fields = new ArrayList<RecordField>();
		RecordField field = new RecordField();
		field.setName(WkfService.WKF_STATUS);
		field.setExpression("eval:'" + transition.getTarget().getName() + "'");
		fields.add(field);
		actions.add(actionName);
		xml = getActionRecordXML(actionName, fields);
		this.wkfService.updateMetaAction(actionName, "action-record", xml);
		// actions.add("save");
		actions.add(WkfTrackingService.ACTION_TRACK);

		String successMsg = transition.getSuccessMsg();
		if (successMsg != null) {
			String sucessAction = actionName + "-success";
			xml = getActionValidateXML(sucessAction, "notify", successMsg, null);
			this.wkfService.updateMetaAction(sucessAction, "action-validate",
					xml);
			actions.add(sucessAction);
		}

		if (!actions.isEmpty()) {
			viewButton.setOnClick(Joiner.on(",").join(actions));
		}

	}

	/**
	 * Method create ActionRecord from given name and list of RecordField. It
	 * generate xml from ActionRecord.
	 * 
	 * @param name
	 *            Name of action.
	 * @param fields
	 *            List of ActionRecord.
	 * @return Xml of ActionRecord created.
	 */
	private String getActionRecordXML(String name, List<RecordField> fields) {

		ActionRecord action = new ActionRecord();
		action.setModel(wkfService.workflow.getMetaModel().getFullName());
		action.setName(name);
		action.setFields(fields);

		return XMLViews.toXml(action, true);

	}

	/**
	 * Method create ActionValidate from given name,type and validation message.
	 * 
	 * @param name
	 *            Name of action
	 * @param type
	 *            Type of validate action ('notify','info' or 'alert').
	 * @param message
	 *            Message to display for action.
	 * @return Xml generated from ActionValidate.
	 */
	private String getActionValidateXML(String name, String type,
			String message, List<Filter> conditions) {

		ActionValidate actionValidate = new ActionValidate();
		actionValidate.setName(name);
		List<Validator> validators = new ArrayList<ActionValidate.Validator>();
		String condition = filterService.getGroovyFilters(conditions, null);
		switch (type) {
		case "notify":
			Notify notify = new Notify();
			notify.setMessage(message);
			notify.setCondition(condition);
			validators.add(notify);
			break;
		case "info":
			Info info = new Info();
			info.setMessage(message);
			info.setCondition(condition);
			validators.add(info);
			break;
		default:
			Alert alert = new Alert();
			alert.setMessage(message);
			alert.setCondition(condition);
			validators.add(alert);
			break;
		}

		actionValidate.setValidators(validators);

		return XMLViews.toXml(actionValidate, true);

	}

	/**
	 * Create/Update MetaPermission for button and set this permission in roles.
	 * 
	 * @param name
	 *            Name of permission to create/update.
	 * @param buttonName
	 *            Name of button to add permission.
	 * @param roles
	 *            Roles to update with permission.
	 */
	@Transactional
	public void addButtonPermissions(String name, String buttonName,
			Set<Role> roles) {

		if (roles == null || roles.isEmpty()) {
			return;
		}

		MetaPermission permission = metaPermissionRepo.findByName(name);
		if (permission == null) {
			permission = new MetaPermission(name);
			permission.setObject(wkfService.workflow.getMetaModel().getFullName());
			MetaPermissionRule rule = new MetaPermissionRule();
			rule.setCanRead(false);
			rule.setField(buttonName);
			permission.addRule(rule);
			permission = metaPermissionRepo.save(permission);
		}

		for (Role role : roles) {
			role.addMetaPermission(permission);
			wkfService.roleRepo.save(role);
		}

	}

	/**
	 * Clear old button permission from all roles having it.
	 * 
	 * @param name
	 *            Name of permission to clear.
	 */
	@Transactional
	public void clearOldMetaPermissions(String name) {

		MetaPermission permission = metaPermissionRepo.findByName(name);

		if (permission != null) {
			List<Role> oldRoleList = wkfService.roleRepo.all()
					.filter("self.metaPermissions.id = ?1", permission.getId())
					.fetch();
			for (Role role : oldRoleList) {
				role.removeMetaPermission(permission);
				wkfService.roleRepo.save(role);
			}
		}

	}

}
