package com.axelor.web.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.bind.JAXBException;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.ClassUtils;
import com.axelor.common.Inflector;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.i18n.I18n;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.loader.XMLViews;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.View;
import com.axelor.meta.schema.views.AbstractView;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.meta.schema.views.Button;
import com.axelor.meta.schema.views.FormView;
import com.axelor.meta.schema.views.Label;
import com.axelor.meta.schema.views.Panel;
import com.axelor.meta.schema.views.PanelEditor;
import com.axelor.meta.schema.views.PanelField;
import com.axelor.meta.schema.views.PanelInclude;
import com.axelor.meta.schema.views.PanelRelated;
import com.axelor.meta.schema.views.PanelTabs;
import com.axelor.meta.schema.views.Selection;
import com.axelor.meta.schema.views.Selection.Option;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;


@RequestScoped
@Path("/viewDocExport")
public class ViewDocExport {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	private static final String[] HEADERS = new String[]{
		"Module", 
		"Object", 
		"View", 
		"Field type", 
		"Field name", 
		"Field title(EN)", 
		"Field title(FR)",
		"Selection(EN)",
		"Selection(FR)",
		"Menu(EN)",
		"Menu(FR)"
	};
	
	private Map<String, List<String>> itemCheckList = new HashMap<String, List<String>>();
	
	private XSSFWorkbook workBook;
	
	private XSSFSheet sheet;
	
	private CellStyle style;
	
	private int rowCount;
	
	private String[] menuPath;
	
	private String rootMenu;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private Inflector inflector;
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Inject
	private MetaTranslationRepository translationRepo; 
	
	
	@GET
	public Response export(){
		
		inflector = Inflector.getInstance();
		
		List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null and self.left = true").order("order").fetch();
		
		workBook = new XSSFWorkbook();
		addStyle();
		
		processRootMenu(menus.iterator());
		
		updateColumnWidth();
		
		return createResponse();
	}
	
	private void addStyle(){
		
		style = workBook.createCellStyle();
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		
	}
	
	private void processRootMenu(Iterator<MetaMenu> rootMenuIter){
		
		if(!rootMenuIter.hasNext()){
			return;
		}
		
		MetaMenu menu = rootMenuIter.next();
		String name = menu.getName();
		
		if(!processedMenus.contains(name)){
			String title = menu.getTitle();
			if(workBook.getSheetIndex(title) >= 0){
				title = title + "(" + menu.getId() + ")";
			}
			rootMenu = title;
			sheet = null;
			processMenu(name);
			processedMenus.add(name);
		}
		
		processRootMenu(rootMenuIter);
	}
	
	private void updateColumnWidth(){
		
		Iterator<XSSFSheet> sheets = workBook.iterator();
		
		while(sheets.hasNext()){
			sheet = sheets.next();
			int count = 0;
			while(count < HEADERS.length){
				sheet.autoSizeColumn(count);
				count++;
			}
		}
	}
	
	private void createSheet(){
		
		sheet = workBook.createSheet(I18n.get(rootMenu));
		rowCount = -1;
		menuPath = null;
		writeRow(HEADERS);
		
	}
	
	private void writeRow(String[] values){
		
		rowCount += 1;
		XSSFRow row = sheet.createRow(rowCount);
		
		
		int count = 0;
		for(String value : values){
			XSSFCell cell = row.createCell(count);
			cell.setCellStyle(style);
			cell.setCellValue(value);
			count++;
		}
		
		if(menuPath != null){
			XSSFCell cell = row.createCell(count);
			cell.setCellStyle(style);
			cell.setCellValue(menuPath[0]);
			count++;
			cell = row.createCell(count);
			cell.setCellStyle(style);
			cell.setCellValue(menuPath[1]);
		}
		
		menuPath = new String[]{"",""};
	}
	
	private String translate(String key, String lang){
		
		MetaTranslation translation = translationRepo.findByKey(key, lang);
		
		if(translation != null){
			String msg = translation.getMessage();
			if(!Strings.isNullOrEmpty(msg)){
				return msg;
			}
		}
		
		return key;
	}
	
	private boolean isChecked(String model, String item){
		
		if(!itemCheckList.containsKey(model)){
			itemCheckList.put(model, new ArrayList<String>());
		}
		
		List<String> checkList = itemCheckList.get(model);
		
		if(!checkList.contains(item)){
			checkList.add(item);
			return false;
		}
		else{
			return true;
		}
	}
	
	private void processMenu(String parentMenu){
		
		List<MetaMenu> subMenus = metaMenuRepo.all().filter("self.parent.name = ?", parentMenu).order("order").fetch();
		
		if(subMenus.isEmpty()){
			log.debug("No sub menus for parent : {}", parentMenu);
		}
		
		for(MetaMenu subMenu : subMenus){
			
			MetaAction action = subMenu.getAction();
			
			if(action == null){
				processMenu(subMenu.getName());
				continue;
			}	
			
			String model = action.getModel();
			
			if(model != null){
				if(sheet == null){
					log.debug("Creating sheet: {}, model: {}", rootMenu, model);
					createSheet();
				}
				updateMenuPath(subMenu);
				String form = getForm(action);
				if(form != null){
					processModel(model, form);
				}
				else{
					log.debug("No form view specified for action: {}", action.getName());
					String viewName = model.substring(model.lastIndexOf(".")+1);
					viewName = inflector.underscore(viewName);
					viewName = inflector.dasherize(viewName);
					processModel(model, viewName + "-form");
				}
			}
		}
		
	}
	
	private void updateMenuPath(MetaMenu metaMenu){
		
		List<String> menus = new ArrayList<String>();
		menus.add(metaMenu.getTitle());
		
		addParentMenus(menus, metaMenu);
		
		Collections.reverse(menus);
		
		String menuEN = null;
		String menuFR = null;
		
		for(String menu : menus){
			
			if(menuEN == null){
				menuEN = translate(menu, "en");
				menuFR = translate(menu, "fr");
			}
			else{
				menuEN += "/" + translate(menu, "en");
				menuFR += "/" + translate(menu, "fr");
			}
		}
		
		menuPath = new String[]{menuEN,menuFR};
		
	}
	
	private void addParentMenus(List<String> menus, MetaMenu metaMenu){
		
		MetaMenu parentMenu = metaMenu.getParent();
		
		if(parentMenu != null){
			menus.add(parentMenu.getTitle());
			addParentMenus(menus, parentMenu);
		}
		
	}
	
	private String getForm(MetaAction action){
		
		try {
			ObjectViews objectViews = XMLViews.fromXML(action.getXml());
			ActionView actionView = (ActionView) objectViews.getActions().get(0);
			for(View view : actionView.getViews()){
				if(view.getType().equals("form")){
					return view.getName();
				}
			}
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private void processModel(String model, String form){
		
		try{
			Mapper mapper = Mapper.of(ClassUtils.findClass(model));
			List<MetaView> metaViews =  metaViewRepo.all().filter("self.type = 'form' and self.model = ? and self.name = ?", model, form).fetch();
		
			if(metaViews.isEmpty()){
				log.debug("No view found: {}, model: {}", form, model);
			}
			
			processView(metaViews.iterator(), mapper);
		}
		catch(IllegalArgumentException e){
			log.debug("Model not found: {}", model);
		}
	}
	
	
	private void processView(Iterator<MetaView> viewIter, Mapper mapper){
		
		if(!viewIter.hasNext()){
			return;
		}
		
		MetaView view = viewIter.next();
		
		try {
			ObjectViews views = XMLViews.fromXML(view.getXml());
			
			FormView form = (FormView) views.getViews().get(0);
			
			processForm(form, view.getModule(), mapper);
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
	}
	
	private void processForm(FormView form, String module, Mapper mapper){
		
		String view = form.getName();
		
		List<Button> buttons = form.getToolbar();
		if(buttons != null){
			for(Button button : buttons){
				processButton(button, view, module, mapper);
			}
		}
		
		List<AbstractWidget> items = form.getItems();
		if(items != null){
			processItems(items.iterator(), view, module, mapper);
		}
		
	}
	
	private void processItems(Iterator<AbstractWidget> itemIter, String view, String module, Mapper mapper){
		
		if(!itemIter.hasNext()){
			return;
		}
		
		AbstractWidget item = itemIter.next();

		String moduleName = item.getModuleToCheck();
		if(!Strings.isNullOrEmpty(moduleName)){
			module = moduleName;
		}

		if(item instanceof Panel){
			processPanel((Panel) item, view, module, mapper);
		}
		else if(item instanceof PanelField) {
			processPanelField((PanelField) item, view, module, mapper);
		}
		else if(item instanceof PanelInclude){
			processPanelInclude((PanelInclude)item, module, mapper);
		}
		else if(item instanceof Button){
			processButton((Button) item, view, module, mapper);
		}
		else if(item instanceof PanelRelated){
			processPanelRelated((PanelRelated) item, view, module, mapper);
		}
		else if (item instanceof Label) {
			processLabel((Label) item, view, module, mapper);
		}
		else if (item instanceof PanelTabs) {
			PanelTabs panelTabs = (PanelTabs)item;
			processItems(panelTabs.getItems().iterator(), view, module, mapper);
		}
		
		processItems(itemIter, view, module, mapper);
	}
	
	private void processPanel(Panel panel, String view, String module, Mapper mapper){
		
		String className = mapper.getBeanClass().getName();
		String title = panel.getTitle();
		
		if(title != null && !isChecked(className, title)){
			String[] values = new String[]{
					module, 
					className, 
					view, "Panel", 
					panel.getName(), 
					translate(title, "en"), 
					translate(title, "fr"),
					"",
					""
			};
			writeRow(values);
		}
		
		processItems(panel.getItems().iterator(), view, module, mapper);
	}
	
	private void processPanelField(PanelField panelField, String view, String module, Mapper mapper){
		
		String name = panelField.getName();
		String title = panelField.getTitle();
		String className = mapper.getBeanClass().getName();
		
		if(isChecked(className, name)){
			return;
		}
		
		Property property = mapper.getProperty(name);
		String type = panelField.getServerType();
		List<?> selectionList = panelField.getSelectionList();
		
		if(property != null){
			type = property.getType().name();
			if(title == null){
				title = property.getTitle();
			}
			String selection = property.getSelection();
			if(selection != null && selectionList == null){
				selectionList = MetaStore.getSelectionList(selection);
			}
		}
		
		if(name != null){
			String selectEN = "";
			String selectFR = "";
			if(selectionList != null){
				selectEN = updateSelect(selectionList, "en");
				selectFR = updateSelect(selectionList, "fr");
			}
			
			String[] values = new String[]{module, 
					className, 
					view, 
					type, 
					name, 
					translate(title,"en"), 
					translate(title, "fr"), 
					selectEN, 
					selectFR
			};
			
			writeRow(values);
		}

		processPanelEditor(panelField, view, module, mapper);
	}
	
	private String updateSelect(List<?> selection, String lang){
		
		List<String> titles = new ArrayList<String>();
		for(Object object : selection){
			Selection.Option option = (Option) object;
			titles.add(translate(option.getTitle(), lang));
		}
		
		
		return Joiner.on(":").join(titles);
	}
	
	
	private void processPanelEditor(PanelField panelField, String view, String module, Mapper mapper) {
		
		PanelEditor panelEditor = panelField.getEditor();
		
		if(panelEditor != null){
			String target = panelField.getTarget();
		
			if(target != null){
				try{
					Mapper targetMapper = Mapper.of(ClassUtils.findClass(target));
					processItems(panelEditor.getItems().iterator(), view, module, targetMapper);
				}catch(IllegalArgumentException e){
					log.debug("Model not found: {}", target);
				}
			}
			else{
				processItems(panelEditor.getItems().iterator(), view, module, mapper);
			}
		}
	}
	
	private void processPanelInclude(PanelInclude panelInclude, String module, Mapper mapper){
		
		AbstractView view = panelInclude.getView();
		if(view != null){
			processForm((FormView)view, module, mapper);
		}
		else{
			log.debug("Issue in panel include: {}", panelInclude.getName());
		}
		
	}
	
	private void processButton(Button button, String view, String module, Mapper mapper){
		
		String name = button.getName();
		String className = mapper.getBeanClass().getName();
		
		if(!isChecked(className, name)){
			String title = button.getTitle();
			String[] values = new String[]{module, 
					className, 
					view, 
					"Button", 
					name, 
					translate(title, "en"), 
					translate(title, "fr"),
					"", ""
			};
			writeRow(values);
		}
	}
	
	
	private void processPanelRelated(PanelRelated panelRelated, String view, String module, Mapper mapper){
		
		String name = panelRelated.getName();
		String title = panelRelated.getTitle();
		String className = mapper.getBeanClass().getName();
		
		if(isChecked(className, name)){
			return;
		}
		
		Property property = mapper.getProperty(name);
		String type = panelRelated.getServerType();
		if(property != null){
			type = property.getType().name();
			if(title == null){
				title = property.getTitle();
			}
		}
		else{
			log.debug("No property found: {}, class: {}", name, className);
		}
		
		String[] values = new String[]{module, 
				className, 
				view, 
				type, 
				name, 
				translate(title,"en"), 
				translate(title, "fr"), 
				"", 
				""
		};
		
		writeRow(values);
	}
	
	private void processLabel(Label label, String view, String module, Mapper mapper){
		
		String className = mapper.getBeanClass().getName();
		String title = label.getTitle();
		if(isChecked(className, title)){
			return;
		}
		
		String[] values = new String[]{module, 
				className, 
				view, 
				"Label", 
				label.getName(), 
				translate(title,"en"), 
				translate(title, "fr"), 
				"", 
				""
		};
		
		writeRow(values);
	}
	
	private Response createResponse(){
		
		String date = LocalDateTime.now().toString("ddMMyyyy HH:mm:ss");
		String fileName = "Export " + date + ".xls";
		
		StreamingOutput out = new StreamingOutput() {
				
			@Override
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				workBook.write(output);
			}
		};
		
		ResponseBuilder response = Response.ok(out);
		response.header("Content-Disposition",
				"attachment; filename=" + fileName);
		
		return response.build();
	}
	
}
