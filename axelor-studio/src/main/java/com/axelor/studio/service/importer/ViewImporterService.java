package com.axelor.studio.service.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSequence;
import com.axelor.meta.db.repo.MetaSequenceRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderLine;
import com.axelor.studio.db.MenuBuilder;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.ViewPanel;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.MenuBuilderRepository;
import com.axelor.studio.db.repo.ViewPanelRepository;
import com.axelor.studio.service.FilterService;
import com.axelor.studio.service.builder.FormBuilderService;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ViewImporterService extends ModelImporterService {

	private final static Logger log = LoggerFactory
			.getLogger(ViewImporterService.class);

	@Inject
	private ViewPanelRepository viewPanelRepo;

	@Inject
	private ActionBuilderRepository actionBuilderRepo;

	@Inject
	private MenuBuilderRepository menuBuilderRepo;

	@Inject
	private FilterService filterService;
	
	@Inject
	private MetaSequenceRepository metaSequenceRepo;

	private List<String> clearedViews = new ArrayList<String>();

	private Map<Long, Integer> panelSeqMap = new HashMap<Long, Integer>();

	private Map<Long, Integer> viewSeqMap = new HashMap<Long, Integer>();

	private Map<Long, Map<String, String>> eventMap = new HashMap<Long, Map<String, String>>();

	private Integer parentMenuSeq = 0;

	private Map<Long, Integer> menuSeqMap = new HashMap<Long, Integer>();

	private Map<String, List<ActionBuilder>> viewActionMap = new HashMap<String, List<ActionBuilder>>();

	private Row row = null;

	public void clear() {
		clearedViews = new ArrayList<String>();
		panelSeqMap = new HashMap<Long, Integer>();
		viewSeqMap = new HashMap<Long, Integer>();
		menuSeqMap = new HashMap<Long, Integer>();
		viewActionMap = new HashMap<String, List<ActionBuilder>>();
		parentMenuSeq = 0;
	}

	public void addViewElement(MetaModel model, String[] basic, Row row,
			MetaField metaField) {
		
		if (basic[0].equals("menu")) {
			addMenu(model, basic);
			return;
		}

		if (model == null) {
			throw new ValidationException(String.format(
					I18n.get("No object defind for row : %s sheet: %s"),
					row.getRowNum() + 1, row.getSheet().getSheetName()));
		}

		this.row = row;

		ViewBuilder viewBuilder = getViewBuilder(model, getString(row.getCell(VIEW)));

		String panelLevel = null;
		switch (basic[0]) {
		case "panelbook":
			panelLevel = getPanelLevel(viewBuilder, false, false);
			createPanel(panelLevel, viewBuilder, null, true, false, false);
			break;
		case "paneltab":
			panelLevel = getPanelLevel(viewBuilder, false, true);
			createPanel(panelLevel, viewBuilder, basic, false, true, false);
			break;
		case "panelside":
			panelLevel = getPanelLevel(viewBuilder, true, false);
			createPanel(panelLevel, viewBuilder, basic, false, false, true);
			break;
		case "panel":
			panelLevel = getPanelLevel(viewBuilder, false, false);
			createPanel(panelLevel, viewBuilder, basic, false, false, false);
			break;
		case "button":
			addButton(viewBuilder, basic);
			break;
		case "label":
			addLabel(viewBuilder, basic);
			break;
		case "wizard":
			addWizard(viewBuilder, basic);
		case "stream":
			viewBuilder.setAddStream(true);
			break;
		case "error":
			createValidation("error", viewBuilder, model);
			break;
		case "warning":
			createValidation("alert", viewBuilder, model);
			break;
		default:
			if (metaField != null) {
				addField(viewBuilder, metaField, basic);
			}
		}

		processViewAction(viewBuilder);
	}

	private ViewBuilder getViewBuilder(MetaModel model, String viewName) {

		ViewBuilder viewBuilder = null;
		if (viewName != null) {
			viewBuilder = viewLoaderService.getViewBuilderForm(model, viewName, false);
		} else {
			viewBuilder = viewLoaderService.getDefaultForm(model, false);
		}

		viewBuilder.setEdited(true);
		viewBuilder.setRecorded(false);

		viewName = viewBuilder.getName();
		if (!clearedViews.contains(viewName)) {
			viewBuilder = clearView(viewBuilder);
		}

		return viewBuilder;
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
					null, "main", null }, false, false, false);
		}

		return viewPanel;
	}

	private String getPanelLevel(ViewBuilder viewBuilder, boolean side,
			boolean tab) {

		ViewPanel lastPanel = getLastPanel(viewBuilder, false);
		Integer panelLevel = -1;

		if (lastPanel == null) {
			return "0";
		}

		String[] level = lastPanel.getPanelLevel().split("\\.");
		panelLevel = new Integer(level[0]);
		if (tab) {
			Integer lastTab = 0;
			if (level.length > 1) {
				lastTab = new Integer(level[1]) + 1;
			}
			return panelLevel + "." + lastTab;
		} else {
			panelLevel++;
			return panelLevel.toString();
		}

	}

	@Transactional
	public ViewPanel createPanel(String level, ViewBuilder viewBuilder,
			String[] basic, boolean book, boolean tab, boolean side) {

		ViewPanel panel = new ViewPanel();
		if (side) {
			panel.setViewBuilderSideBar(viewBuilder);
		} else {
			panel.setViewBuilder(viewBuilder);
		}

		panel.setPanelLevel(level);
		panel.setIsNotebook(book);
		panel.setIsPanelTab(tab);

		if (basic != null) {
			panel.setName(basic[2]);
			panel.setTitle(basic[3]);
		}

		return viewPanelRepo.save(panel);
	}

	@Transactional
	public void addButton(ViewBuilder viewBuilder, String[] basic) {

		ViewItem viewItem = new ViewItem(basic[2]);
		viewItem.setTypeSelect(1);
		viewItem.setTitle(basic[3]);

		if (basic[1] != null && basic[1].startsWith("toolbar")) {
			viewItem.setViewBuilderToolbar(viewBuilder);
			viewItem.setSequence(getViewSeq(viewBuilder.getId()));
		} else {
			ViewPanel lastPanel = getLastPanel(viewBuilder, true);
			viewItem.setSequence(getPanelSeq(lastPanel.getId()));
			viewItem.setPanelTop(basic[1] != null && basic[1].equals("top"));
			viewItem.setViewPanel(lastPanel);
		}

		viewItem = setEvent(viewBuilder, viewItem);
		setAttributes(viewBuilder, viewItem);

		viewItemRepo.save(viewItem);
	}
	
	@Transactional
	public void addLabel(ViewBuilder viewBuilder, String[] basic) {

		ViewItem viewItem = new ViewItem(basic[2]);
		viewItem.setTypeSelect(2);
		viewItem.setTitle(basic[3]);
		
		viewItem.setHideIf(getString(row.getCell(HIDE_IF)));

		viewItemRepo.save(viewItem);
	}

	@Transactional
	public void addWizard(ViewBuilder viewBuilder, String[] basic) {

		ViewPanel lastPanel = getLastPanel(viewBuilder, true);
		String modelName = inflector.camelize(basic[1]);
		MetaModel metaModel = metaModelRepo.findByName(modelName);

		ViewBuilder targetView = null;
		if (metaModel != null) {
			targetView = viewLoaderService.getDefaultForm(metaModel, true);
		}

		String actionName = "action-" + inflector.dasherize(basic[2]);

		ActionBuilder builder = actionBuilderRepo.findByName(actionName);
		if (builder == null) {
			builder = new ActionBuilder(actionName);
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
		button.setOnClick(builder.getName());
		button.setViewPanel(lastPanel);
		button.setSequence(getPanelSeq(lastPanel.getId()));
		button = setEvent(viewBuilder, button);
		setAttributes(viewBuilder, button);

		viewItemRepo.save(button);
	}

	@Transactional
	public void addField(ViewBuilder viewBuilder, MetaField metaField, String[] basic) {

		ViewPanel lastPanel = getLastPanel(viewBuilder, true);

		ViewItem viewItem = new ViewItem(metaField.getName());
		viewItem.setFieldType(viewLoaderService.getFieldType(metaField));
		viewItem.setViewPanel(lastPanel);
		viewItem.setMetaField(metaField);
		viewItem.setSequence(getPanelSeq(lastPanel.getId()));
		viewItem = setEvent(viewBuilder, viewItem);
		setAttributes(viewBuilder, viewItem);
		if (basic[0].equals("html")) {
			viewItem.setWidget("html");
		}
		if (metaField != null && metaField.getTypeName().equals("MetaFile")) {
			viewItem.setWidget("binary-link");
		}
		viewItemRepo.save(viewItem);

	}

	@Transactional
	public void addMenu(MetaModel model, String[] basic) {

		String name = "menu-" + inflector.dasherize(basic[2]);

		MenuBuilder menuBuilder = menuBuilderRepo.findByName(name);
		if (menuBuilder == null) {
			menuBuilder = new MenuBuilder(name);
		}
		menuBuilder.setTitle(basic[3]);
		if (model != null) {
			menuBuilder.setMetaModel(model);
			menuBuilder.setModel(model.getFullName());
			menuBuilder.setIsParent(false);
		} else {
			menuBuilder.setIsParent(true);
		}

		MenuBuilder parent = null;
		if (!Strings.isNullOrEmpty(basic[1])) {
			parent = menuBuilderRepo.findByName("menu-"
					+ inflector.dasherize(basic[1]));
			if (parent != null) {
				menuBuilder.setParent(parent.getName());
				menuBuilder.setMenuBuilder(parent);
			} else {
				throw new ValidationException(String.format(
						I18n.get("No parent menu found %s for menu %s"),
						basic[1], basic[3]));
			}
		}

		menuBuilder.setEdited(true);
		menuBuilder.setOrder(getMenuOrder(parent));

		menuBuilderRepo.save(menuBuilder);

	}

	private void updateViewActionMap(ActionBuilder builder, String model) {

		String viewName = viewLoaderService.getDefaultViewName(model, "form");
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

	private int getMenuOrder(MenuBuilder parent) {

		Integer seq = 0;
		if (parent == null) {
			seq = parentMenuSeq;
			parentMenuSeq++;
		} else {
			Long menuId = parent.getId();
			if (menuSeqMap.containsKey(menuId)) {
				seq = menuSeqMap.get(menuId);
			}
			menuSeqMap.put(menuId, seq + 1);
		}

		return seq;

	}

	@Transactional
	public ViewBuilder clearView(ViewBuilder viewBuilder) {

		viewBuilder.clearToolbar();
		viewBuilder.clearViewSidePanelList();
		viewBuilder.clearViewPanelList();

		clearedViews.add(viewBuilder.getName());

		return viewBuilderRepo.save(viewBuilder);
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

		String formula = getString(row.getCell(FORMULA));
		String event = getString(row.getCell(EVENT));

		if (Strings.isNullOrEmpty(formula) || Strings.isNullOrEmpty(event)) {
			return;
		}

		String name = "action-" + viewBuilder.getName() + "-" + type;

		ActionBuilder action = actionBuilderRepo.findByName(name);
		if (action == null) {
			action = new ActionBuilder(name);
		} else {
			action.clearLines();
		}

		action.setMetaModel(model);
		action.setTypeSelect(5);

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
						viewItem.setOnChange(FormBuilderService
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

		viewItem.setHideIf(getString(row.getCell(HIDE_IF)));
		viewItem.setReadonlyIf(getString(row.getCell(READONLY_IF)));
		viewItem.setRequiredIf(getString(row.getCell(REQUIRED_IF)));
		
		Integer typeSelect = viewItem.getTypeSelect();
		if (typeSelect == 0) {
			if ("many-to-one,many-to-many".contains(viewItem.getFieldType())) {
				viewItem.setDomainCondition(getString(row.getCell(DOMAIN)));
			}
			createAction(viewBuilder, viewItem);
		}

		return viewItem;
	}

	@Transactional
	public void createAction(ViewBuilder viewBuilder, ViewItem viewItem) {

		String formula = getString(row.getCell(FORMULA));
		String event = getString(row.getCell(EVENT));
		
		if (formula == null) {
			return;
		}
		

		MetaModel metaModel = viewBuilder.getMetaModel();
		
		if (event == null) {
			return;
		}
		
		String name = "action-" + viewBuilder.getName() + "-set-"
				+ viewItem.getName();

		ActionBuilder actionBuilder = actionBuilderRepo.findByName(name);
		if (actionBuilder == null) {
			actionBuilder = new ActionBuilder(name);
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
					if (sum.length > 0) {
						line.setValue(sum[0] + ")");
						if (sum.length > 1) {
							line.setFilter(sum[1].substring(0,sum[1].length()-1));
						}
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

}
