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
package com.axelor.studio.service.data.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaSequence;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaSequenceRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.ViewPanel;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.db.repo.ViewPanelRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.FilterService;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.builder.FormBuilderService;
import com.axelor.studio.service.data.DataCommon;
import com.axelor.studio.service.data.DataTranslationService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class DataViewService extends DataCommon {

	private final static Logger log = LoggerFactory
			.getLogger(DataViewService.class);
	
	private Map<String, ViewBuilder> clearedViews = new HashMap<String, ViewBuilder>();

	private Map<Long, Integer> panelSeqMap = new HashMap<Long, Integer>();

	private Map<Long, Integer> viewSeqMap = new HashMap<Long, Integer>();

	private Map<Long, Map<String, String>> eventMap = new HashMap<Long, Map<String, String>>();

	private Map<String, List<ActionBuilder>> viewActionMap = new HashMap<String, List<ActionBuilder>>();
	
	private Map<String, Set<String>> extendViews;

	private Row row = null;
	
	private boolean replace = true;
	
	@Inject
	private ViewPanelRepository viewPanelRepo;

	@Inject
	private ActionBuilderRepository actionBuilderRepo;

	@Inject
	private FilterService filterService;
	
	@Inject
	private MetaSequenceRepository metaSequenceRepo;
	
	@Inject
	private ConfigurationService configService;
	
	@Inject
	private ViewLoaderService viewLoaderService;
	
	@Inject
	private ViewItemRepository viewItemRepo;
	
	@Inject
	private MetaModelRepository metaModelRepo;
	
	@Inject
	private ViewBuilderRepository viewBuilderRepo;
	
	@Inject
	private DataTranslationService translationService;
	
	public void clear() {
		clearedViews = new HashMap<String, ViewBuilder>();
		panelSeqMap = new HashMap<Long, Integer>();
		viewSeqMap = new HashMap<Long, Integer>();
		viewActionMap = new HashMap<String, List<ActionBuilder>>();
		extendViews= new HashMap<String,  Set<String>>();
	}

	public void addViewElement(MetaModel model, String[] basic, Row row,
			MetaField metaField, boolean replace) throws AxelorException {
		
		this.row = row;
		
		String module = getValue(row, MODULE);
		module = module.replace("*", "");
		MetaModule metaModule = configService.getCustomizedModule(module);

		if (model == null) {
			throw new AxelorException(I18n.get("No object defind for row : %s sheet: %s"),
					1, row.getRowNum() + 1, row.getSheet().getSheetName());
		}
		
		String[] name = getViewName(model.getName());
		checkReplace(replace, module, name[0]);
		
		ViewBuilder viewBuilder = getViewBuilder(metaModule, model, name);
		
		String panelLevel = getValue(row, PANEL_LEVEL);
		
		switch (basic[0]) {
			case "panelbook":
				panelLevel = getPanelLevel(panelLevel, viewBuilder, false, false);
				createPanelBook(panelLevel, viewBuilder, null);
				break;
			case "paneltab":
				panelLevel = getPanelLevel(panelLevel, viewBuilder, false, true);
				createPanelTab(panelLevel, viewBuilder, basic);
				break;
			case "panelside":
				panelLevel = getPanelLevel(panelLevel, viewBuilder, true, false);
				createPanelSide(panelLevel, viewBuilder, basic);
				break;
			case "panel":
				panelLevel = getPanelLevel(panelLevel, viewBuilder, false, false);
				createPanel(panelLevel, viewBuilder, basic, true);
				break;
			case "button":
				addButton(viewBuilder, basic);
				break;
			case "label":
				addLabel(viewBuilder, basic);
				break;
			case "wizard":
				addWizard(module, viewBuilder, basic);
			case "stream":
				viewBuilder.setAddStream(true);
				break;
			case "error":
				createValidation("error", viewBuilder, model);
				break;
			case "warning":
				createValidation("alert", viewBuilder, model);
				break;
			case "onsave":
				addOnSave(viewBuilder);
				break;
			case "onnew":
				addOnNew(viewBuilder);
				break;
			case "onload":
				addOnLoad(viewBuilder);
				break;	
			case "spacer":
				addSpacer(basic, viewBuilder);
				break;
			case "menubar":
				addMenubar(viewBuilder);
				break;
			case "menubar.item":
				addMenubarItem(viewBuilder);
				break;
			default:
				addField(viewBuilder, metaField, basic);
		}

		processViewAction(viewBuilder);
	}

	private ViewBuilder getViewBuilder(MetaModule module, MetaModel model, String[] name) throws AxelorException {
		
		if (clearedViews.containsKey(name[0])) {
			return clearedViews.get(name[0]);
		}
		
		ViewBuilder viewBuilder = viewLoaderService.getViewBuilder(module.getName(), name[0], "form");
		if (viewBuilder == null) {
			viewBuilder = viewLoaderService.getViewBuilderForm(module.getName(), model, name[0], name[1], !replace);
		}

		viewBuilder = clearView(viewBuilder);

		return viewBuilder;
	}
	
	private String[] getViewName(String model) {
		
		String viewName = getValue(row, VIEW);
		String title = null;
		if (viewName != null && viewName.contains("(")) {
			String[] view = viewName.split("\\(");
			viewName = view[0];
			title = view[1].replace(")", "");
		}
		
		if (viewName == null) {
			viewName = ViewLoaderService.getDefaultViewName(model, "form");
		}
		
		return new String[] {viewName, title};
	}
	
	public Map<String, List<ActionBuilder>> getViewActionMap() {
		return viewActionMap;
	}

	private ViewPanel getLastPanel(ViewBuilder viewBuilder, boolean createPanel) {

		ViewPanel viewPanel = viewPanelRepo
				.all()
				.filter("self.viewBuilderSideBar = ?1 OR self.viewBuilder = ?1",
						viewBuilder).order("-panelLevel").fetchOne();
		
		if (viewPanel == null && createPanel) {
			viewPanel = createPanel("0", viewBuilder, new String[] { "panel",
					null, "main", null }, false);
		}

		return viewPanel;
	}

	private String getPanelLevel(String level, ViewBuilder viewBuilder, boolean side,
			boolean tab) {
		
		if (!Strings.isNullOrEmpty(level)) {
			return level;
		}

		ViewPanel lastPanel = getLastPanel(viewBuilder, false);
		Integer panelLevel = -1;

		if (lastPanel == null) {
			return "0";
		}

		String[] levels = lastPanel.getPanelLevel().split("\\.");
		panelLevel = new Integer(levels[0]);
		if (tab) {
			Integer lastTab = 0;
			if (levels.length > 1) {
				lastTab = new Integer(levels[1]) + 1;
			}
			return panelLevel + "." + lastTab;
		} else {
			panelLevel++;
			return panelLevel.toString();
		}

	}

	@Transactional
	public ViewPanel createPanelCommon(String panelLevel, ViewBuilder viewBuilder,
			String[] basic, boolean addAttrs) {
		
		ViewPanel panel = new ViewPanel();
		panel.setPanelLevel(panelLevel);
		
		if (basic != null) {
			panel.setName(basic[2]);
			panel.setTitle(basic[3]);
			translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		}
		
		panel.setNewPanel(!replace);
		panel.setIfModule(getValue(row, IF_MODULE));
		
		if (addAttrs) {
			panel.setColspan(getValue(row, COLSPAN));
			
			String readonly = getValue(row, READONLY);
			if (readonly != null && readonly.equalsIgnoreCase("x")) {
				panel.setReadonly(true);
			}
			else {
				panel.setReadonlyIf(readonly);
			}
				
			String hidden = getValue(row, HIDDEN);
			if (hidden != null && hidden.equalsIgnoreCase("x")) {
				panel.setHidden(true);
			}
			else {
				panel.setHideIf(hidden);
			}
			
			panel.setShowIf(getValue(row, SHOW_IF));
			
			panel.setIfConfig(getValue(row, IF_CONFIG));
			
		}

		return viewPanelRepo.save(panel);
	}
	
	@Transactional
	public ViewPanel createPanelBook(String panelLevel, ViewBuilder viewBuilder, String[] basic) {
		
		ViewPanel panel = createPanelCommon(panelLevel, viewBuilder, basic, true);
		
		panel.setIsNotebook(true);
		panel.setViewBuilder(viewBuilder);
		
		return viewPanelRepo.save(panel);
	}
	
	@Transactional
	public ViewPanel createPanelTab(String panelLevel, ViewBuilder viewBuilder, String[] basic) {
		
		ViewPanel panel = createPanelCommon(panelLevel, viewBuilder, basic, true);
		
		panel.setIsPanelTab(true);
		panel.setViewBuilder(viewBuilder);
		
		return viewPanelRepo.save(panel);
	}
	
	@Transactional
	public ViewPanel createPanelSide(String panelLevel, ViewBuilder viewBuilder, String[] basic) {
		
		ViewPanel panel = createPanelCommon(panelLevel, viewBuilder, basic, true);
		
		panel.setViewBuilderSideBar(viewBuilder);
		
		return viewPanelRepo.save(panel);
	}
	
	@Transactional
	public ViewPanel createPanel(String panelLevel, ViewBuilder viewBuilder, String[] basic, boolean addAttrs) {
		
		ViewPanel panel = createPanelCommon(panelLevel, viewBuilder, basic, addAttrs);
		
		panel.setViewBuilder(viewBuilder);
		
		return viewPanelRepo.save(panel);
	}

	@Transactional
	public void addButton(ViewBuilder viewBuilder, String[] basic) {

		ViewItem viewItem = new ViewItem(basic[2]);
		viewItem.setTypeSelect(1);
		viewItem.setTitle(basic[3]);
		
		translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");

		if (basic[1] != null && basic[1].startsWith("toolbar")) {
			viewItem.setViewBuilderToolbar(viewBuilder);
			viewItem.setSequence(getViewSeq(viewBuilder.getId()));
		} else {
			ViewPanel lastPanel = getLastPanel(viewBuilder, true);
			viewItem.setSequence(getPanelSeq(lastPanel.getId()));
			viewItem.setPanelTop(basic[1] != null && basic[1].equals("top"));
			viewItem.setViewPanel(lastPanel);
		}
		
		viewItem.setOnClick(getValue(row, ON_CHANGE));
		setAttributes(viewBuilder, viewItem);

		viewItemRepo.save(viewItem);
	}
	
	@Transactional
	public void addLabel(ViewBuilder viewBuilder, String[] basic) throws AxelorException {
		
		ViewPanel lastPanel = getLastPanel(viewBuilder, true);
		ViewItem viewItem = new ViewItem(basic[2]);
		
		if (basic[4] != null) {
			setNestedField(lastPanel, basic[4], viewItem);
		}
		else {
			viewItem.setViewPanel(lastPanel);
			viewItem.setSequence(getPanelSeq(lastPanel.getId()));
		}
		viewItem.setTypeSelect(2);
		viewItem.setTitle(basic[3]);
		translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		
		setAttributes(viewBuilder, viewItem);

		viewItemRepo.save(viewItem);
	}

	@Transactional
	public void addWizard(String module, ViewBuilder viewBuilder, String[] basic) throws AxelorException {

		ViewPanel lastPanel = getLastPanel(viewBuilder, true);
		String modelName = inflector.camelize(basic[1]);
		MetaModel metaModel = metaModelRepo.findByName(modelName);

		ViewBuilder targetView = null;
		if (metaModel != null) {
			targetView = viewLoaderService.getDefaultForm(module, metaModel, null, !replace);
		}

		String actionName = "action-" + inflector.dasherize(basic[2]);

		ActionBuilder builder = actionBuilderRepo.all()
				.filter("self.name  = ?1 and self.metaModule.name = ?2", actionName, viewBuilder.getMetaModule().getName())
				.fetchOne();
		if (builder == null) {
			builder = new ActionBuilder(actionName);
			builder.setMetaModule(viewBuilder.getMetaModule());
		}
		builder.setEdited(true);
		builder.setTypeSelect(2);
		builder.setMetaModel(metaModel);
		builder.setViewBuilder(targetView);
		builder.setPopup(true);

		if (targetView == null) {
			updateViewActionMap(builder, basic[1]);
		} else {
			actionBuilderRepo.save(builder);
		}

		ViewItem button = new ViewItem(basic[2]);
		button.setTypeSelect(1);
		button.setTitle(basic[3]);
		translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		button.setOnClick(builder.getName());
		button.setViewPanel(lastPanel);
		button.setSequence(getPanelSeq(lastPanel.getId()));
		button = setEvent(viewBuilder, button);
		setAttributes(viewBuilder, button);

		viewItemRepo.save(button);
	}

	@Transactional
	public void addField(ViewBuilder viewBuilder, MetaField metaField, String[] basic) throws AxelorException {
		
		ViewPanel lastPanel = getLastPanel(viewBuilder, true);
		
		ViewItem viewItem = new ViewItem(basic[2]);
		
		if (metaField != null) {
			viewItem.setFieldType(viewLoaderService.getFieldType(metaField));
			if (metaField.getTypeName().equals("MetaFile")) {
				viewItem.setWidget("binary-link");
			}
			viewItem.setMetaField(metaField);
		}
		else {
			translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		}
		
		if (basic[4] != null) {
			setNestedField(lastPanel, basic[4], viewItem);
		}
		else {
			viewItem.setViewPanel(lastPanel);
			viewItem.setSequence(getPanelSeq(lastPanel.getId()));
		}
		
		
		viewItem = setEvent(viewBuilder, viewItem);
		
		setAttributes(viewBuilder, viewItem);
		if (basic[0].equals("html")) {
			viewItem.setWidget("html");
		}
		
		viewItem.setOnChange(getValue(row, ON_CHANGE));
		viewItem.setPanelLevel(getValue(row, PANEL_LEVEL));
		viewItem.setFormView(basic[5]);
		viewItem.setGridView(basic[6]);
		
		viewItemRepo.save(viewItem);

	}
	
	private void setNestedField(ViewPanel lastPanel, String parentField, 
			ViewItem viewItem) throws AxelorException {
		
		log.debug("Last panel: {}", lastPanel);
		ViewItem relationalField = viewItemRepo.all()
					.filter("self.viewPanel = ?1 and LOWER(self.name) = ?2", 
					lastPanel, parentField.toLowerCase()).fetchOne();
		
		if (relationalField == null) {
			throw new AxelorException(I18n.get("No parent field found '%s' for nested editor"),
					1, parentField);
		}
		
		viewItem.setNestedField(relationalField);
		Long sequence = viewItemRepo.all().filter("self.nestedField = ?1", relationalField).count();
		viewItem.setSequence(sequence.intValue());
		
	}

	private void updateViewActionMap(ActionBuilder builder, String model) {

		String viewName = ViewLoaderService.getDefaultViewName(model, "form");
		log.debug("Updating view action map view: {} action: {}", viewName,
				builder.getName());

		if (viewActionMap.containsKey(viewName)) {
			viewActionMap.get(viewName).add(builder);
		} else {
			List<ActionBuilder> builders = new ArrayList<ActionBuilder>();
			builders.add(builder);
			viewActionMap.put(viewName, builders);
		}
	}

	@Transactional
	public void processViewAction(ViewBuilder viewBuilder) {

		List<ActionBuilder> builders = viewActionMap.get(viewBuilder.getName());

		if (builders != null) {

			for (ActionBuilder builder : builders) {
				builder.setViewBuilder(viewBuilder);
				builder.setMetaModel(viewBuilder.getMetaModel());
				actionBuilderRepo.save(builder);
			}

			viewActionMap.remove(viewBuilder.getName());

		}
	}

	@Transactional
	public ViewBuilder clearView(ViewBuilder viewBuilder) {
		
		log.debug("Clear view: {}", viewBuilder.getName());
		viewBuilder.clearToolbar();
		viewBuilder.clearViewSidePanelList();
		viewBuilder.clearViewPanelList();
		viewBuilder.clearMenubar();
		viewBuilder.setOnSave(null);
		viewBuilder.setOnNew(null);
		viewBuilder.setOnLoad(null);
		viewBuilder.setEdited(true);
		viewBuilder.setRecorded(false);
		viewBuilder.setAddOnly(!replace);
		
		if (replace) {
			viewBuilder.setMetaView(null);
			viewBuilder.setParent(null);
		}
		
		viewBuilder = viewBuilderRepo.save(viewBuilder);
		
		clearedViews.put(viewBuilder.getName(), viewBuilder);

		return viewBuilder;
	}

	private Integer getPanelSeq(Long panelId) {

		Integer seq = 0;
		if (panelSeqMap.containsKey(panelId)) {
			seq = panelSeqMap.get(panelId) + 1;
		}

		panelSeqMap.put(panelId, seq);

		return seq;
	}

	private Integer getViewSeq(Long viewId) {

		Integer seq = 0;
		if (viewSeqMap.containsKey(viewId)) {
			seq = viewSeqMap.get(viewId) + 1;
		}

		viewSeqMap.put(viewId, seq);

		return seq;
	}

	@Transactional
	public void createValidation(String type, ViewBuilder viewBuilder,
			MetaModel model) {

		String formula = getValue(row, FORMULA);
		String event = getValue(row, EVENT);

		if (Strings.isNullOrEmpty(formula) || Strings.isNullOrEmpty(event)) {
			return;
		}

		String name = "action-" + viewBuilder.getMetaModule().getName() + "-" + viewBuilder.getName() + "-" + type;

		ActionBuilder action = actionBuilderRepo
				.all()
				.filter("self.name = ?1 and self.metaModule.name = ?2", name, viewBuilder.getMetaModule().getName())
				.fetchOne();
		if (action == null) {
			action = new ActionBuilder(name);
			action.setMetaModule(viewBuilder.getMetaModule());
		} else {
			action.clearLines();
		}

		action.setMetaModel(model);
		action.setTypeSelect(5);
		action.setEdited(true);
		
		String[] items = formula.split(",");
		ActionBuilderLine line = new ActionBuilderLine();
		List<String> conditions = new ArrayList<String>();
		for (int i = 0; i < items.length; i++) {
			if (i % 2 == 0) {
				String condition = null;
				if (items[i].equals("else")) {
					condition = "!(" + Joiner.on(" && ").join(conditions) + ")";
				} else {
					condition = filterService.getTagValue(items[i], false);
					conditions.add(condition);
				}
				line.setConditionText(condition);
			} else {
				line.setValidationTypeSelect(type);
				line.setValidationMsg(items[i]);
				action.addLine(line);
				line = new ActionBuilderLine();
			}
		}

		if (action.getLines() != null && !action.getLines().isEmpty()) {
			actionBuilderRepo.save(action);
		}

		addEvents(viewBuilder, event, name);

	}

	private void addEvents(ViewBuilder viewBuilder, String events, String action) {

		for (String event : events.split(",")) {

			if (event.equals("new")) {
				continue;
			}
			if (event.equals("save")) {
				String onSave = viewBuilder.getOnSave();
				viewBuilder.setOnSave(FormBuilderService.getUpdatedAction(
						onSave, action));
			} else {
				ViewItem viewItem = viewItemRepo
						.all()
						.filter("self.name = ?1 "
								+ "and (self.viewPanel.viewBuilder = ?2 "
								+ "OR self.viewPanel.viewBuilderSideBar = ?2 "
								+ "OR self.viewBuilderToolbar = ?2)", event,
								viewBuilder).fetchOne();
				if (viewItem != null) {
					if (viewItem.getTypeSelect() == 0) {
						viewItem.setOnChange(FormBuilderService
								.getUpdatedAction(viewItem.getOnChange(),
										action));
					} else if (viewItem.getTypeSelect() == 1) {
						viewItem.setOnClick(FormBuilderService
								.getUpdatedAction(viewItem.getOnClick(), action));
					}
				} else {
					Long viewId = viewBuilder.getId();
					if (!eventMap.containsKey(viewId)) {
						eventMap.put(viewId, new HashMap<String, String>());
					}

					eventMap.get(viewId).put(event, action);
				}
			}
		}
	}

	private ViewItem setEvent(ViewBuilder builder, ViewItem viewItem) {

		Long viewId = builder.getId();
		if (!eventMap.containsKey(viewId)) {
			return viewItem;
		}

		Map<String, String> actionMap = eventMap.get(viewId);
		String event = actionMap.get(viewItem.getName());
		if (event != null) {
			if (viewItem.getTypeSelect() == 0) {
				String onChange = FormBuilderService.getUpdatedAction(
						viewItem.getOnChange(), event);
				viewItem.setOnChange(onChange);
			} else if (viewItem.getTypeSelect() == 1) {
				String onClick = FormBuilderService.getUpdatedAction(
						viewItem.getOnClick(), event);
				viewItem.setOnClick(onClick);
			}
		}

		return viewItem;
	}

	private ViewItem setAttributes(ViewBuilder viewBuilder, ViewItem viewItem) {

		if (viewItem.getTypeSelect() == 0) {
			viewItem.setRequired(false);
			String required = getValue(row, REQUIRED);
			if (required != null && required.equalsIgnoreCase("x")) {
				viewItem.setRequired(true);
			}
			else {
				viewItem.setRequiredIf(required);
			}
		}
		
		viewItem.setHidden(false);
		String hidden = getValue(row, HIDDEN);
		if (hidden != null && hidden.equalsIgnoreCase("x")) {
			viewItem.setHidden(true);
		}
		else {
			viewItem.setHideIf(hidden);
		}
		
		viewItem.setReadonly(false);
		String readonly = getValue(row, READONLY);
		if (readonly != null && readonly.equalsIgnoreCase("x")) {
			viewItem.setReadonly(true);
		}
		else {
			viewItem.setReadonlyIf(readonly);
		}
		
		viewItem.setShowIf(getValue(row, SHOW_IF));
		viewItem.setIfModule(getValue(row, IF_MODULE));
		viewItem.setIfConfig(getValue(row, IF_CONFIG));
		
		Integer typeSelect = viewItem.getTypeSelect();
		if (typeSelect == 0 && viewItem.getFieldType() != null) {
			if ("many-to-one,many-to-many".contains(viewItem.getFieldType())) {
				viewItem.setDomainCondition(getValue(row, DOMAIN));
			}
			createAction(viewBuilder, viewItem);
		}
		
		String colspan = getValue(row, COLSPAN);
		
		if (StringUtils.isNumeric(colspan)) {
			viewItem.setColSpan(Integer.parseInt(colspan));
		}
		
		viewItem.setWidget(getValue(row, WIDGET));

		return viewItem;
	}

	@Transactional
	public void createAction(ViewBuilder viewBuilder, ViewItem viewItem) {

		String formula = getValue(row, FORMULA);
		String event = getValue(row, EVENT);
		
		if (formula == null) {
			return;
		}
		

		MetaModel metaModel = viewBuilder.getMetaModel();
		
		if (event == null) {
			return;
		}
		
		String name = "action-" + viewBuilder.getName() + "-set-"
				+ viewItem.getName();

		ActionBuilder actionBuilder = actionBuilderRepo
				.all()
				.filter("self.name = ?1 and self.metaModule.name = ?2", name, viewBuilder.getMetaModule().getName())
				.fetchOne();
		if (actionBuilder == null) {
			actionBuilder = new ActionBuilder(name);
			actionBuilder.setMetaModule(viewBuilder.getMetaModule());
		}
		else{
			actionBuilder.clearLines();
		}

		actionBuilder.setTypeSelect(1);
		actionBuilder.setMetaModel(metaModel);

		String[] exprs = formula.split(",");
		ActionBuilderLine line = null;
		for (int i = 0; i < exprs.length; i++) {
			String expr = exprs[i].trim();
			if (i % 2 == 0) {
				line = new ActionBuilderLine();
				line.setMetaField(viewItem.getMetaField());
				line.setTargetField(viewItem.getName());
				if(expr.startsWith("seq(")) {
					String seqName = createSequence(expr, metaModel, viewItem);
					if(seqName != null) {
						line.setValue("com.axelor.studio.service.builder.ActionBuilderService.getSequence('" + seqName + "')"); 
					}
				}
				else if(expr.startsWith("sum(")) {
					String[] sum = expr.split(":");
					if (sum.length > 1) {
						line.setValue(sum[0] + ")");
						line.setFilter(sum[1].substring(0,sum[1].length()-1));
					}
					else {
						line.setValue(expr);
					}
				}
				else {
					line.setValue(filterService.getTagValue(expr, false));
				}
			} else {
				line.setConditionText(expr);
				actionBuilder.addLine(line);
				line = null;
			}
		}

		if (line != null) {
			actionBuilder.addLine(line);
		}

		List<ActionBuilderLine> lines = actionBuilder.getLines();

		if (lines != null && !lines.isEmpty()) {
			if (event.trim().equals("new") && lines.size() == 1
					&& lines.get(0).getConditionText() == null) {
				viewItem.setDefaultValue(lines.get(0).getValue());
			} else {
				actionBuilderRepo.save(actionBuilder);
				addEvents(viewBuilder, event, name);
			}
		}
	}

	@Transactional
	public String createSequence(String formula, MetaModel metaModel,
			ViewItem viewItem) {

		formula = formula.trim().replace("seq(", "");
		if (formula.endsWith(")")) {
			formula = formula.substring(0, formula.length()-1);
		}
		String[] sequence = formula.split(":");
		if (sequence.length > 1) {
			String model = inflector.dasherize(metaModel.getName()).replace(
					"-", ".");
			String field = inflector.dasherize(viewItem.getName()).replace("-",
					".");
			
			String name = "sequence." + model + "." + field;
			MetaSequence metaSequence = metaSequenceRepo.findByName(name);
			if(metaSequence == null){
				metaSequence = new MetaSequence(name);
			}
			metaSequence.setMetaModel(metaModel);
			metaSequence.setPadding(new Integer(sequence[0]));
			if (sequence.length > 1) {
				metaSequence.setPrefix(sequence[1]);
			}
			if (sequence.length > 2) {
				metaSequence.setSuffix(sequence[2]);
			}
			
			metaSequence = metaSequenceRepo.save(metaSequence);
			
			return "sequence." + model + "." + field;
		}

		return null;
	}
	
	@Transactional
	public void addOnSave(ViewBuilder viewBuilder) {
		
		String formula = getValue(row, FORMULA);
		
		if (formula != null) {
			viewBuilder.setOnSave(formula);
			viewBuilderRepo.save(viewBuilder);
		}
	}
	
	@Transactional
	public void addOnNew(ViewBuilder viewBuilder) {
		
		String formula = getValue(row, FORMULA);
		
		if (formula != null) {
			viewBuilder.setOnNew(formula);
			viewBuilderRepo.save(viewBuilder);
		}
	}
	
	@Transactional
	public void addOnLoad(ViewBuilder viewBuilder) {
		
		String formula = getValue(row, FORMULA);
		
		if (formula != null) {
			viewBuilder.setOnLoad(formula);
			viewBuilderRepo.save(viewBuilder);
		}
	}
	
	@Transactional
	public void addSpacer(String[] basic, ViewBuilder viewBuilder) throws AxelorException {
		
		String colSpan = getValue(row, COLSPAN);
		
		if (colSpan != null) {
			
			ViewPanel lastPanel = getLastPanel(viewBuilder, true);
			ViewItem viewItem = new ViewItem();
			if (basic[4] != null) {
				setNestedField(lastPanel, basic[4], viewItem);
			}
			else {
				viewItem.setViewPanel(lastPanel);
				viewItem.setSequence(getPanelSeq(lastPanel.getId()));
			}
			viewItem.setColSpan(Integer.parseInt(colSpan));
			viewItem.setTypeSelect(3);
			
			viewItemRepo.save(viewItem);
		}
	}
	
	@Transactional
	public void addMenubar(ViewBuilder viewBuilder) throws AxelorException {
		
		String title = getValue(row, TITLE);
		String titleFr = getValue(row, TITLE_FR);
		
		if (title == null) {
			title = titleFr;
		}
		translationService.addTranslation(title, titleFr, "fr");
		
		if (title == null) {
			throw new AxelorException(I18n.get("Menubar must have title. Row: %s"), 1, row.getRowNum());
		}
		
		
		ViewItem viewItem = new ViewItem();
		viewItem.setTitle(title);
		viewItem.setMenubarBuilder(viewBuilder);
		
		viewItemRepo.save(viewItem);
			
	}
	
	@Transactional
	public void addMenubarItem(ViewBuilder viewBuilder) throws AxelorException {
		
		String title = getValue(row, TITLE);
		String titleFr = getValue(row, TITLE_FR);
		
		if (title == null) {
			title = titleFr;
		}
		else if (!title.equals(titleFr)) {
			translationService.addTranslation(title, titleFr, "fr");
		}
		
		if (title == null) {
			throw new AxelorException(I18n.get("Menubar item must have title. Row: %s"), 1, row.getRowNum());
		}
		
		List<ViewItem> menubar = viewItemRepo.all().filter("self.menubarBuilder = ?1", viewBuilder).fetch();
		
		if (menubar == null || menubar.isEmpty()) {
			throw new AxelorException(I18n.get("No menubar found for menubar item. Row: %s"), 1, row.getRowNum());
		}
		
		ViewItem viewItem = new ViewItem();
		viewItem.setTitle(title);
		viewItem.setMenubarMenu(menubar.get(menubar.size() - 1));
		viewItem.setOnClick(getValue(row, ON_CHANGE));
		
		viewItemRepo.save(viewItem);
			
	}
	
	private void checkReplace(boolean replace, String module, String view) {
		
		if (!replace) {
			if (extendViews.containsKey(module)) {
				extendViews.put(module, new HashSet<String>());
			}
			extendViews.get(module).add(view);
			this.replace = false;
		}
		else if (extendViews.containsKey(module)) {
			this.replace =  !extendViews.get(module).contains(view);
		}
		else {
			this.replace =  replace;
		}
		
		
	}
	
	

}
