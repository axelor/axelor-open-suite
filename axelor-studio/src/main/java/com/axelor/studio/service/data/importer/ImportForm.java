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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.ViewPanel;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ViewBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.db.repo.ViewPanelRepository;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.builder.FormBuilderService;
import com.axelor.studio.service.data.CommonService;
import com.axelor.studio.service.data.TranslationService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ImportForm extends CommonService {

	private final static Logger log = LoggerFactory
			.getLogger(ImportForm.class);
	
	private final static Pattern DOMAIN_PATTERN = Pattern.compile("(:[\\w\\d\\.]+)");
	
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
	private TranslationService translationService;
	
	@Inject
	private ImportFormula importFormula;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	public void clear() {
		clearedViews = new HashMap<String, ViewBuilder>();
		panelSeqMap = new HashMap<Long, Integer>();
		viewSeqMap = new HashMap<Long, Integer>();
		viewActionMap = new HashMap<String, List<ActionBuilder>>();
		extendViews= new HashMap<String,  Set<String>>();
	}

	public void importForm(MetaModel model, String[] basic, Row row,
			MetaField metaField, boolean replace) throws AxelorException {
		
		String module = getValue(row, MODULE);
		if (model == null || module == null){
			return;
		}
		
		module = module.replace("*", "");
		MetaModule metaModule = configService.getCustomizedModule(module);
		if (metaModule == null) {
			return;
		}
		
		this.row = row;

		String[] name = getViewName(model.getName());
		if (name.length > 1) {
			model = updateModelTitle(model, name[1]);
		}
		
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
			case "dashlet":
				addDashlet(viewBuilder, basic);
				break;
			default:
				addField(viewBuilder, metaField, basic);
		}

		processViewAction(viewBuilder);
	}
	
	@Transactional
	public MetaModel updateModelTitle(MetaModel model, String title) {
		
		model.setTitle(title);

		return metaModelRepo.save(model);
	}

	private ViewBuilder getViewBuilder(MetaModule module, MetaModel model, String[] name) throws AxelorException {
		
		if (clearedViews.containsKey(name[0])) {
			return clearedViews.get(name[0]);
		}
		
		ViewBuilder viewBuilder = viewLoaderService.getViewBuilder(module.getName(), name[0], "form");
		if (viewBuilder == null) {
			viewBuilder = viewLoaderService.getViewBuilderForm(module.getName(), model, name[0], name[1], !replace);
		}
		else if (name[1] != null) {
			viewBuilder.setTitle(name[1]);
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
			if (title.contains(",")) {
				String[] titles = title.split(",");
				title = titles[0];
				translationService.addTranslation(title, titles[1], "fr");
			}
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
		
		if (addAttrs) {
			
			panel.setIfModule(getValue(row, IF_MODULE));
			
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
		
		
		viewItem.setOnClick(FormBuilderService.getUpdatedAction(
				viewItem.getOnClick(), getValue(row, ON_CHANGE)));
		
		viewItem = setCommonAttributes(viewBuilder, viewItem);

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
		
		viewItem = setCommonAttributes(viewBuilder, viewItem);

		viewItemRepo.save(viewItem);
	}
	
	@Transactional
	public ActionBuilder getActionViewBuilder(String name, String title, String viewName, ViewBuilder targetView, MetaModule module, MetaView metaView) {
		
		ActionBuilder builder = null;
		if (module != null) {
			builder = actionBuilderRepo.all()
					.filter("self.name = ?1 and self.metaModule.name = ?2", name, module.getName())
					.fetchOne();
		}
		
		if (builder == null) {
			builder = new ActionBuilder(name);
			builder.setMetaModule(module);
		}

		builder.setEdited(true);
		builder.setTitle(title);
		builder.setTypeSelect(2);
		builder.setViewBuilder(targetView);
		
		if (targetView != null) {
			builder.setViewBuilder(targetView);
			builder.setMetaModel(targetView.getMetaModel());
			actionBuilderRepo.save(builder);
		}
		else if (metaView != null) {
			builder.setMetaView(metaView);
			String model = metaView.getModel();
			if (model != null) {
				builder.setMetaModel(metaModelRepo.all().filter("self.fullName = ?1", model).fetchOne());
			}
			actionBuilderRepo.save(builder);
		}
		else {
			updateViewActionMap(builder, viewName);
		}
		
		return builder;
		
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
		
		String viewName = basic[5];
		if (viewName == null) {
			viewName = ViewLoaderService.getDefaultViewName(modelName, "form");
		}
		
		String actionName = "action-" + inflector.dasherize(basic[2]);
		
		ActionBuilder builder = getActionViewBuilder(actionName, basic[3], viewName, targetView, viewBuilder.getMetaModule(), null);
		builder.setMetaModule(viewBuilder.getMetaModule());
		builder.setPopup(true);

		ViewItem button = new ViewItem(basic[2]);
		button.setTypeSelect(1);
		button.setTitle(basic[3]);
		translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
		button.setOnClick(builder.getName());
		button.setViewPanel(lastPanel);
		button.setSequence(getPanelSeq(lastPanel.getId()));
		button = setEvent(viewBuilder, button);
		button = setCommonAttributes(viewBuilder, button);

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
		else if(!basic[0].equals("empty")) {
			translationService.addTranslation(basic[3], getValue(row, TITLE_FR), "fr");
			viewItem = setDummyField(viewItem, basic);
		}
		
		if (basic[4] != null) {
			setNestedField(lastPanel, basic[4], viewItem);
		}
		else {
			viewItem.setViewPanel(lastPanel);
			viewItem.setSequence(getPanelSeq(lastPanel.getId()));
		}
		
		if (basic[0].equals("html")) {
			viewItem.setWidget("html");
		}
		
		viewItem = setEvent(viewBuilder, viewItem);
		viewItem = setCommonAttributes(viewBuilder, viewItem);
		viewItem.setOnChange(FormBuilderService.getUpdatedAction(
				viewItem.getOnChange(),  getValue(row, ON_CHANGE)));
		viewItem.setPanelLevel(getValue(row, PANEL_LEVEL));
		viewItem.setFormView(basic[5]);
		viewItem.setGridView(basic[6]);
		
		viewItemRepo.save(viewItem);

	}
	
	private ViewItem setDummyField(ViewItem viewItem, String[] basic) {
		
		viewItem.setTitle(basic[3]);
		viewItem.setFieldType(FIELD_TYPES.get(basic[0]));
		if (basic[1] != null) {
			MetaModel model = metaModelRepo.findByName(basic[1]);
			if (model != null) {
				viewItem.setTarget(model.getFullName());
			}
		}
		
		return viewItem;
	}
	
	private ViewItem setNestedField(ViewPanel lastPanel, String parentField, 
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
		
		return viewItem;
		
	}

	private void updateViewActionMap(ActionBuilder builder, String viewName) {
		
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
				builder.setMetaModule(viewBuilder.getMetaModule());
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

	private ViewItem setCommonAttributes(ViewBuilder viewBuilder, ViewItem viewItem) {

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
			addFormula(viewBuilder, viewItem);
		}
		
		String colspan = getValue(row, COLSPAN);
		
		if (StringUtils.isNumeric(colspan)) {
			viewItem.setColSpan(Integer.parseInt(colspan));
		}
		
		viewItem.setWidget(getValue(row, WIDGET));

		return viewItem;
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
				viewItem = setNestedField(lastPanel, basic[4], viewItem);
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
			if (!extendViews.containsKey(module)) {
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
	
	private void createValidation(String type, ViewBuilder viewBuilder,
			MetaModel model) {
		
		List<String> actionEvents = importFormula.importValidation(row, type, viewBuilder, model);
		if (actionEvents.size() > 1) {
			updateEventMap(actionEvents, viewBuilder.getId());
		}
		
	}
	
	private void updateEventMap(List<String> actionEvents, Long viewId) {
		
		String action = actionEvents.get(0);
		for (String event : actionEvents) {
			if (eventMap.containsKey(viewId)) {
				eventMap.put(viewId, new HashMap<String, String>());
			}
			eventMap.put(viewId, addEvents(eventMap.get(viewId), action, event));
		}
	}

	private Map<String, String> addEvents(Map<String, String> fieldMap,
			String action, String event) {
		
		if (fieldMap == null) {
			fieldMap = new HashMap<String, String>();
		}
		
		String oldActions = fieldMap.get(event);
		
		if (oldActions != null) {
			action = FormBuilderService.getUpdatedAction(oldActions, action);
		}
		
		fieldMap.put(event, action);
		
		return fieldMap;
	}
	
	private void addFormula(ViewBuilder viewBuilder, ViewItem viewItem) {
		
		List<String> actionEvents = importFormula.importFormula(row, viewBuilder, viewItem);
		if (actionEvents.size() > 1) {
			updateEventMap(actionEvents, viewBuilder.getId());
		}
		
	}
	
	@Transactional
	public void addDashlet(ViewBuilder viewBuilder, String[] basic) {
		
		log.debug("Add dahshlet: {}", basic[2]);
		if (basic[2] != null) {
			ViewPanel lastPanel = getLastPanel(viewBuilder, true);
			ViewItem viewItem = new ViewItem(basic[2]);
			viewItem.setTypeSelect(4);
			viewItem.setViewPanel(lastPanel);
			viewItem.setSequence(getPanelSeq(lastPanel.getId()));
			log.debug("Dashlet sequence: {}", viewItem.getSequence());
			String colspan = getValue(row, COLSPAN);
			if (colspan != null) {
				viewItem.setColSpan(Integer.parseInt(colspan));
			}
			
			if (basic[1] != null) {
				addDashletAction(basic, viewBuilder.getMetaModule());
			}
			viewItemRepo.save(viewItem);
		}
		
	}
	
	private void addDashletAction(String[] basic, MetaModule metaModule) {
		
		String viewName = basic[5];
		if (viewName == null) {
			viewName = ViewLoaderService.getDefaultViewName(basic[1], "grid");
		}
		
		ViewBuilder targetView = viewBuilderRepo
				.all()
				.filter("self.name = ?1 and self.metaModule = ?2"
						, viewName, metaModule).fetchOne();
		
		MetaView view = metaViewRepo.findByName(viewName);
		
		ActionBuilder builder = getActionViewBuilder(basic[2], basic[3], viewName, targetView, metaModule, view);
		builder.setMetaModule(metaModule);
		String[] domainCtx = getDomainContext(getValue(row, DOMAIN));
		builder.setDomainCondition(domainCtx[0]);
		if (domainCtx.length > 1) {
			builder.setContext(domainCtx[1]);
		}
		
	}

	private String[] getDomainContext(String domain) {
		
		if (domain == null) {
			return new String[]{null};
		}
		
		Matcher macher = DOMAIN_PATTERN.matcher(domain);
		
		StringBuffer sb = new StringBuffer(domain.length());
		List<String> context = new ArrayList<String>();
		int count = 0;
		while(macher.find()) {
			String replacement = ":_param" + count;
			context.add(replacement.substring(1) + ";eval" + macher.group().replace("= ", ""));
			macher.appendReplacement(sb, replacement);
			count++;
		}
		
		macher.appendTail(sb);
		
		if (context.isEmpty()) {
			return new String[]{domain};
		}
		
		return new String[]{sb.toString(), Joiner.on(",").join(context)};
	}
	

}
