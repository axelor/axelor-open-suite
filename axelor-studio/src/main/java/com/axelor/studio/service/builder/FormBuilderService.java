package com.axelor.studio.service.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.Inflector;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.Action;
import com.axelor.meta.schema.actions.ActionRecord;
import com.axelor.meta.schema.actions.ActionRecord.RecordField;
import com.axelor.meta.schema.views.AbstractPanel;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Label;
import com.axelor.meta.schema.views.Menu;
import com.axelor.meta.schema.views.Menu.Item;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelInclude;
import com.axelor.meta.schema.views.PanelMail;
import com.axelor.meta.schema.views.PanelMail.MailFollowers;
import com.axelor.meta.schema.views.PanelMail.MailMessages;
import com.axelor.meta.schema.views.PanelRelated;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.ViewPanel;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.FilterService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;

/**
 * This class generate form view from ViewBuilder of type 'form'. It use
 * ViewPanels to generate panels of formView. Also generate onNew action from
 * default value given in view field.
 * 
 * @author axelor
 */
public class FormBuilderService {

	protected Logger log = LoggerFactory.getLogger(getClass());

	// List of RecordFields generated for default values.
	private List<RecordField> defaultFields;

	private Map<String, AbstractPanel> panelMap;

	private List<ActionRecord> actionRecords;

	private List<AbstractWidget> formViewItems;

	private ViewBuilder viewBuilder;

	@Inject
	private ConfigurationService configService;

	@Inject
	private FilterService filterService;
	
	private boolean autoCreate = false;

	/**
	 * Root method to access the service. It will generate FormView from
	 * ViewBuilder
	 * 
	 * @param viewBuilder
	 *            ViewBuilder record of type form.
	 * @return FormView
	 * @throws JAXBException
	 */
	public FormView getView(ViewBuilder viewBuilder, boolean autoCreate) throws JAXBException {
		
		this.autoCreate = autoCreate;
		log.debug("View builder toolbar: {}", viewBuilder.getToolbar().size());

		this.viewBuilder = viewBuilder;
		defaultFields = new ArrayList<RecordField>();
		panelMap = new HashMap<String, AbstractPanel>();
		actionRecords = new ArrayList<ActionRecord>();

		FormView formView = getFormView();
		processFormView(formView);

		processPanels(false);
		processPanels(true);

		if (viewBuilder.getAddStream()) {
			addStream();
		}

		if (!defaultFields.isEmpty()) {
			addOnNewAction(formView);
		}

		return formView;
	}

	/**
	 * Method update formView record by adding panels, toolbar buttons and onNew
	 * action into it.
	 * 
	 * @param formView
	 *            FormView generated from parent view.
	 */
	private void processFormView(FormView formView) {

		List<Button> toolbar = formView.getToolbar();
		toolbar = processToolBar(toolbar);
		if (!toolbar.isEmpty()) {
			formView.setToolbar(toolbar);
		}

		String onSave = getUpdatedAction(viewBuilder.getOnSave(),
				formView.getOnSave());
		log.debug("OnSave actions final: {}", onSave);
		formView.setOnSave(onSave);

		String onNew = getUpdatedAction(viewBuilder.getOnNew(),
				formView.getOnNew());
		formView.setOnNew(onNew);

		formView.setWidthSpec("large");
		log.debug("Process form view: {}", formView.getName());

		formViewItems = formView.getItems();

		if (formViewItems == null) {
			formViewItems = new ArrayList<AbstractWidget>();
		}

		List<AbstractWidget> items = new ArrayList<AbstractWidget>();
		items.addAll(formViewItems);

		updatePanelInclude(items.iterator(), 0);

		mapPanels(formViewItems.iterator(), null, 0);

		formView.setItems(formViewItems);
	}

	/**
	 * Method generate final list of buttons to keep in formview. It removes old
	 * button and add new button as per ViewBuilder toolbar.
	 * 
	 * @param toolbar
	 *            List of buttons of Parent form view.
	 * @return List of button to keep in formView.
	 */
	private List<Button> processToolBar(List<Button> toolbar) {

		List<Button> retainButtons = new ArrayList<Button>();

		if (toolbar != null) {
			log.debug("Form view toolbar size: {}", toolbar.size());
		}
		log.debug("View builder toolbar size: {}", viewBuilder.getToolbar()
				.size());

		for (ViewItem viewButton : viewBuilder.getToolbar()) {
			log.debug("Button: {} onClick:{}", viewButton.getName(),
					viewButton.getOnClick());
			String name = viewButton.getName();
			Button button = null;
			if (toolbar != null) {
				for (Button toolButton : toolbar) {
					if (toolButton.getName().equals(name)) {
						retainButtons.add(toolButton);
						button = toolButton;
						break;
					}
				}
			}

			if (button == null) {
				button = new Button();
				button.setName(name);
				retainButtons.add(button);
			}
			button.setTitle(viewButton.getTitle());
			button.setColSpan(viewButton.getColSpan() == 0 ? null : viewButton
					.getColSpan());
			button.setShowIf(viewButton.getShowIf());
			button.setOnClick(viewButton.getOnClick());

		}

		log.debug("Retain buttons size: {}", retainButtons.size());
		return retainButtons;
	}

	/**
	 * Method to update onSave string with new actions.
	 * 
	 * @param actions
	 *            String of actions separated by comma.
	 * @return String of updated actions with onSave.
	 */
	public static String getUpdatedAction(String oldAction, String action) {

		if (Strings.isNullOrEmpty(oldAction)) {
			return action;
		}
		if (Strings.isNullOrEmpty(action)) {
			return oldAction;
		}

		List<String> oldActions = new ArrayList<String>();
		oldActions.addAll(Arrays.asList(oldAction.split(",")));

		List<String> newActions = new ArrayList<String>();
		newActions.addAll(Arrays.asList(action.split(",")));
		newActions.removeAll(oldActions);

		oldActions.addAll(newActions);

		return Joiner.on(",").join(oldActions);
	}

	/**
	 * Method to add items of form view refer by panel include in place of panel
	 * include in formViewItems.
	 * 
	 * @param itemIterator
	 *            FormView item(AbstractWidget) iterator.
	 * @param index
	 *            Index of panel include in formViewItems.
	 * @return updated index.
	 */
	private Integer updatePanelInclude(Iterator<AbstractWidget> itemIterator,
			Integer index) {

		if (!itemIterator.hasNext()) {
			return index;
		}

		AbstractWidget widget = itemIterator.next();
		if (widget instanceof PanelInclude) {
			PanelInclude panelInclude = (PanelInclude) widget;
			FormView formView = (FormView) panelInclude.getView();
			if (formView != null && formView.getItems() != null) {
				formViewItems.remove(widget);
				formViewItems.addAll(index, formView.getItems());
				index = updatePanelInclude(formView.getItems().iterator(),
						index);
			}
		}

		index++;

		log.debug("Panel include Index: {}", index);

		return updatePanelInclude(itemIterator, index);
	}

	/**
	 * Method to get generated ActionRecords, after generating form view.
	 * 
	 * @return
	 */
	public List<ActionRecord> getActionRecords() {

		return actionRecords;
	}

	/**
	 * Method to get FormView from parent form view or create new if no parent
	 * form view.
	 * 
	 * @return FormView
	 * @throws JAXBException
	 *             Exception throws by xml parsing.
	 */
	private FormView getFormView() throws JAXBException {

		FormView formView = null;

		MetaView metaView = viewBuilder.getMetaView();

		if (metaView == null) {
			formView = new FormView();
			formView.setName(viewBuilder.getName());
			formView.setTitle(viewBuilder.getTitle());
			formView.setModel(viewBuilder.getModel());
			return formView;
		}

		ObjectViews objectViews = XMLViews.fromXML(metaView.getXml());
		List<AbstractView> views = objectViews.getViews();
		if (!views.isEmpty()) {
			formView = (FormView) views.get(0);
			String xmlId = formView.getXmlId();
			String module = configService.getModuleName() + "-";
			if (xmlId == null || !xmlId.startsWith(module)) {
				xmlId = module + formView.getName();
			}
			formView.setXmlId(xmlId);
		}

		return formView;
	}

	/**
	 * Method process viewPanels of viewBuilder to create new AbstractPanel and
	 * update AbstractPanel.
	 * 
	 * @param sideBar
	 *            boolean to check if panel is side panel or not
	 */
	private void processPanels(boolean sideBar) {

		List<ViewPanel> viewPanels;

		if (sideBar) {
			viewPanels = viewBuilder.getViewSidePanelList();
		} else {
			viewPanels = viewBuilder.getViewPanelList();
		}

		for (ViewPanel viewPanel : viewPanels) {

			String panelLevel = viewPanel.getPanelLevel();

			log.debug("Panel level to process: {}", panelLevel);
			AbstractPanel panel = null;

			if (panelMap.containsKey(panelLevel)) {
				panel = panelMap.get(panelLevel);
			} else {
				panel = addNewPanel(panelLevel, viewPanel.getIsNotebook());
			}

			if (sideBar) {
				panel.setSidebar(sideBar);
			}

			panel = updatePanel(panel, viewPanel);

			panelMap.put(panelLevel, panel);

		}
	}

	/**
	 * Method to create new Panel with specified panel level
	 * 
	 * @param panelLevel
	 *            Level of panel in formView.
	 * @param isNotebook
	 *            Boolean to check if panel is notebook.
	 * @return AbstractPanel
	 */
	private AbstractPanel addNewPanel(String panelLevel, Boolean isNotebook) {

		AbstractPanel abstractPanel = new Panel();
		if (isNotebook) {
			abstractPanel = new PanelTabs();
		}
		abstractPanel.setColSpan(12);
		log.debug("Panel map:{}", panelMap.keySet());

		if (!panelLevel.contains(".")) {
			Integer panelIndex = Integer.parseInt(panelLevel);
			if (panelIndex < formViewItems.size()) {
				formViewItems.add(panelIndex + 1, abstractPanel);
			} else {
				formViewItems.add(abstractPanel);
			}
			panelMap.put(panelLevel, abstractPanel);

			return abstractPanel;
		}

		Integer lastIndex = panelLevel.lastIndexOf(".");
		String parentLevel = panelLevel.substring(0, lastIndex);
		if (!panelMap.containsKey(parentLevel)) {
			log.debug("No parent panel level: {}", panelLevel);
			return abstractPanel;
		}

		Integer childLevel = Integer.parseInt(panelLevel
				.substring(lastIndex + 1));
		List<AbstractWidget> panelItems = new ArrayList<AbstractWidget>();

		if (panelMap.get(parentLevel) instanceof Panel) {
			Panel panel = (Panel) panelMap.get(parentLevel);
			if (panel.getItems() != null) {
				panelItems = panel.getItems();
			}
			if (childLevel < panelItems.size()) {
				panelItems.add(childLevel, abstractPanel);
			} else {
				panelItems.add(abstractPanel);
			}
			panel.setItems(panelItems);
			panelMap.put(parentLevel, panel);
			panelMap.put(panelLevel, abstractPanel);
		} else if (panelMap.get(parentLevel) instanceof PanelTabs) {
			PanelTabs panelTabs = (PanelTabs) panelMap.get(parentLevel);
			log.debug("Panel tabs: {}", panelTabs);
			if (panelTabs.getItems() != null) {
				panelItems = panelTabs.getItems();
			}
			if (childLevel < panelItems.size()) {
				panelItems.add(childLevel, abstractPanel);
			} else {
				panelItems.add(abstractPanel);
			}
			panelTabs.setItems(panelItems);
			panelMap.put(parentLevel, panelTabs);
			panelMap.put(panelLevel, abstractPanel);
			log.debug("Panel tabs: {}", panelTabs);
		}

		return abstractPanel;

	}

	/**
	 * Method to update panelMap with generated paneLevel as key and Panel as
	 * value.
	 * 
	 * @param widgetIterator
	 *            Iterator of formView items.
	 * @param level
	 *            Parent panel level
	 * @param levelCounter
	 *            Current level counter.
	 */
	private void mapPanels(Iterator<AbstractWidget> widgetIterator,
			String level, Integer levelCounter) {

		if (widgetIterator.hasNext()) {

			AbstractWidget widget = widgetIterator.next();
			String currentLevel = levelCounter.toString();
			if (level != null) {
				currentLevel = level + "." + currentLevel;
			}

			if (widget instanceof Panel) {
				Panel panel = (Panel) widget;
				panelMap.put(currentLevel, panel);
				mapPanels(panel.getItems().iterator(), currentLevel, 0);
				levelCounter += 1;
			} else if (widget instanceof PanelTabs) {
				PanelTabs panelTabs = (PanelTabs) widget;
				panelMap.put(currentLevel, panelTabs);
				mapPanels(panelTabs.getItems().iterator(), currentLevel, 0);
				levelCounter += 1;
			}

			mapPanels(widgetIterator, level, levelCounter);
		}

	}

	/**
	 * Method to update AbstractPanel from ViewPanel of formView .
	 * 
	 * @param abstractPanel
	 *            Destination panel to be updated.
	 * @param viewPanel
	 *            Source panel.
	 */
	private AbstractPanel updatePanel(AbstractPanel abstractPanel,
			ViewPanel viewPanel) {

		// if(!viewPanel.getNoTitle()){
		abstractPanel.setTitle(viewPanel.getTitle());
		// }

		abstractPanel.setName(viewPanel.getName());

		if (abstractPanel instanceof Panel) {
			List<AbstractWidget> panelItems = new ArrayList<AbstractWidget>();
			Panel panel = (Panel) abstractPanel;
			if (panel.getItems() != null) {
				panelItems = panel.getItems();
			}

			List<ViewItem> itemList = viewPanel.getViewItemList();
			List<AbstractWidget> items = new ArrayList<AbstractWidget>();

			for (ViewItem viewItem : itemList) {

				Integer type = viewItem.getTypeSelect();

				switch (type) {
				case 0:
					String fieldType = viewItem.getFieldType();
					if ("one-to-many,many-to-many".contains(fieldType)) {
						setPanelRelated(viewItem, items);
					} else {
						setField(viewItem, items);
					}
					checkDefaultValues(fieldType, viewItem);
					break;
				case 1:
					if (viewItem.getPanelTop()) {
						setMenuItem(viewItem, panel);
					} else {
						setButton(viewItem, items);
					}
					break;
				case 2:
					setLabel(viewItem, items);
				}
				

			}

			if (viewPanel.getPlace() == 0) {
				panelItems.addAll(0, items);
			} else {
				panelItems.addAll(items);
			}

			panel.setItems(panelItems);

			return panel;
		}

		return abstractPanel;
	}

	/**
	 * Method check if default value is there for ViewField. If defaultValue is
	 * there it will update defaultFields list that will be used in onNew action
	 * generation.
	 * 
	 * @param fieldType
	 *            Type of field
	 * @param fieldName
	 *            Name of field
	 * @param viewField
	 *            ViewField to check for default vale.
	 */
	private void checkDefaultValues(String fieldType, ViewItem viewItem) {

		String defaultVal = viewItem.getDefaultValue();
		String name = viewItem.getName();
		log.debug("Default field: {} value: {}", name, defaultVal);

		if (defaultVal != null) {

			// if(fieldType.equals("string")){
			// defaultVal = "'" + defaultVal + "'";
			// }

			RecordField recordField = new RecordField();
			recordField.setName(name);
			recordField.setExpression("eval:"
					+ filterService.getTagValue(defaultVal, false));

			defaultFields.add(recordField);

		}

	}

	/**
	 * Method update PanelItems list with new item created from viewField.
	 * 
	 * @param fieldName
	 *            Name of field
	 * @param viewField
	 *            Source field.
	 * @param panelItems
	 *            Destination list to update.
	 */
	private void setField(ViewItem viewItem, List<AbstractWidget> panelItems) {

		PanelField field = new PanelField();
		field.setName(viewItem.getName());
		field.setOnChange(viewItem.getOnChange());
		field.setDomain(viewItem.getDomainCondition());
		field.setReadonlyIf(viewItem.getReadonlyIf());
		field.setHideIf(viewItem.getHideIf());
		field.setRequiredIf(viewItem.getRequiredIf());
		field.setColSpan(null);
		String selectWidget = viewItem.getWidget();
		String widget = null;
		MetaField metaField = viewItem.getMetaField();

		if (viewItem.getColSpan() > 0) {
			field.setColSpan(viewItem.getColSpan());
		}

		if (viewItem.getHidden()) {
			field.setHidden(true);
		} else {
			field.setHidden(null);
		}

		if (viewItem.getRequired()) {
			field.setRequired(true);
		} else {
			field.setRequired(null);
		}

		if (viewItem.getReadonly()) {
			field.setReadonly(true);
		} else {
			field.setReadonly(null);
		}

		if (viewItem.getProgressBar()) {
			widget = "SelectProgress";
		} else if (viewItem.getHtmlWidget()) {
			field.setColSpan(12);
			widget = "html";
		} else if (selectWidget != null && !selectWidget.equals("normal")) {
			widget = selectWidget;
		}

		if (metaField != null) {
			if (metaField.getIsDuration()) {
				widget = "duration";
			} else if (metaField.getIsUrl()) {
				widget = "url";
			} else if (metaField.getLarge()) {
				field.setColSpan(12);
			}
			String relationship = metaField.getRelationship();
			if (autoCreate 
					&& relationship != null 
					&& "ManyToOne,ManyToMany".contains(relationship)) {
				field.setCanNew("true");
			}
		}

		field.setWidget(widget);

		MetaSelect metaSelect = viewItem.getMetaSelect();
		if (metaSelect != null) {
			field.setSelection(metaSelect.getName());
		} else {
			field.setSelection(null);
		}

		panelItems.add(field);
	}

	private void setButton(ViewItem viewItem, List<AbstractWidget> panelItems) {

		Button button = new Button();
		button.setName(viewItem.getName());
		button.setColSpan(viewItem.getColSpan());
		button.setTitle(viewItem.getTitle());
		button.setOnClick(viewItem.getOnClick());
		button.setPrompt(viewItem.getPromptMsg());

		panelItems.add(button);

	}

	private Panel setMenuItem(ViewItem viewItem, Panel panel) {

		Menu menu = panel.getMenu();
		if (menu == null) {
			menu = new Menu();
		}

		List<AbstractWidget> items = menu.getItems();
		if (items == null) {
			items = new ArrayList<AbstractWidget>();
		}

		Integer oldIndex = null;
		for (AbstractWidget menuItem : items) {
			Item item = (Item) menuItem;
			String name = item.getName();
			if (name != null && name.equals(viewItem.getName())) {
				oldIndex = items.indexOf(item);
				break;
			}
		}

		Item item = new Item();
		// item.setName(viewItem.getName());
		item.setTitle(viewItem.getTitle());
		String onClick = viewItem.getOnClick();
		if (Strings.isNullOrEmpty(onClick)) {
			onClick = "";
		}
		item.setAction(onClick);

		if (oldIndex != null) {
			items.remove(oldIndex);
			items.add(oldIndex, item);
		} else {
			items.add(item);
		}

		menu.setItems(items);
		panel.setMenu(menu);

		return panel;
	}

	/**
	 * Method to add panel related for o2m/m2m types of relational field. It
	 * will add PanelRelated object created from viewField into list of
	 * panelItems of parent panel.
	 * 
	 * @param fieldName
	 *            Name of relational field.
	 * @param viewField
	 *            ViewField of relational type.
	 * @param panelItems
	 *            List of items of parent panel.
	 */
	private void setPanelRelated(ViewItem viewItem,
			List<AbstractWidget> panelItems) {

		PanelRelated panelRelated = new PanelRelated();
		panelRelated.setName(viewItem.getName());
		panelRelated.setColSpan(12);
		panelRelated.setOnChange(viewItem.getOnChange());
		panelRelated.setDomain(viewItem.getDomainCondition());
		panelRelated.setReadonlyIf(viewItem.getReadonlyIf());
		panelRelated.setHideIf(viewItem.getHideIf());

		if (viewItem.getReadonly()) {
			panelRelated.setReadonly(true);
		} else {
			panelRelated.setReadonly(null);
		}

		if (viewItem.getHidden()) {
			panelRelated.setHidden(true);
		} else {
			panelRelated.setHidden(null);
		}

		panelItems.add(panelRelated);

	}

	/**
	 * Method generate ActionRecord from defaultFields and using form view
	 * details. Also it set onNew of formView.
	 * 
	 * @param formView
	 *            Updated form view with onNew action.
	 */
	private void addOnNewAction(FormView formView) {

		String model = viewBuilder.getModel();
		String actionName = getOnNewActionName(model, formView.getName(),
				formView.getXmlId());
		Action action = XMLViews.findAction(actionName);
		ActionRecord actionRecord;

		if (action instanceof ActionRecord) {
			actionRecord = (ActionRecord) action;
			removeOldActionFields(actionRecord);
		} else {
			actionRecord = new ActionRecord();
			actionRecord.setName(actionName);
			actionRecord.setModel(model);
		}

		actionRecord.setFields(defaultFields);

		String onNew = getUpdatedAction(formView.getOnNew(), actionName);
		formView.setOnNew(onNew);

		actionRecords.add(actionRecord);
	}

	/**
	 * Method create onNew action name.
	 * 
	 * @param model
	 *            Full name of model.
	 * @param formName
	 *            Name of form view where onNew will be added.
	 * @param xmlId
	 *            of form view
	 * @return Name of onNew action.
	 */
	private String getOnNewActionName(String model, String formName,
			String xmlId) {

		final Inflector inflector = Inflector.getInstance();

		String klassName = inflector.dasherize(model.substring(model
				.lastIndexOf(".") + 1));

		String actionName = "custom-" + klassName + "-" + formName;
		if (xmlId != null && !xmlId.startsWith("custom")) {
			actionName += "-" + xmlId;
		}
		actionName = actionName + "-default";

		return actionName;
	}

	/**
	 * Method remove old defaultFields and update new default field of
	 * ActionRecord.
	 * 
	 * @param actionRecord
	 *            ActionRecord to update.
	 */

	private void removeOldActionFields(ActionRecord actionRecord) {

		List<RecordField> oldFields = actionRecord.getFields();

		for (RecordField recordField : defaultFields) {

			Iterator<RecordField> toRemove = oldFields.iterator();

			while (toRemove.hasNext()) {

				RecordField oldField = toRemove.next();

				if (oldField.getName().equals(recordField.getName())) {
					log.debug("Old default field: {}", oldField.getName());
					toRemove.remove();
					break;
				}

			}
		}

		defaultFields.addAll(oldFields);
	}

	private void addStream() {

		PanelMail panelMail = new PanelMail();

		List<AbstractWidget> items = new ArrayList<AbstractWidget>();

		MailMessages mailMessages = new MailMessages();
		mailMessages.setLimit(4);
		items.add(mailMessages);

		MailFollowers mailFollowers = new MailFollowers();
		items.add(mailFollowers);

		panelMail.setItems(items);

		formViewItems.add(panelMail);

	}
	
	private void setLabel(ViewItem item, List<AbstractWidget> items){
		
		Label label = new Label();
		label.setTitle(item.getTitle());
		label.setName(item.getName());
		label.setHideIf(item.getHideIf());
		
		items.add(label);
	}

}
