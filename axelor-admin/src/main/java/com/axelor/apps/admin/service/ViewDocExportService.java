package com.axelor.apps.admin.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
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
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaStore;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
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
import com.axelor.meta.schema.views.Dashlet;
import com.axelor.meta.schema.views.Field;
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

public class ViewDocExportService {
	
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
		"Menu(FR)",
	};
	
	private Map<String, Map<String,List<String>>> itemCheckMap = new HashMap<String, Map<String, List<String>>>();
	
	private List<String> viewProcessed = new  ArrayList<String>(); 
	
	private Map<String, List<String[]>> o2mViewMap = new HashMap<String, List<String[]>>();
	
	private XSSFWorkbook workBook;
	
	private XSSFSheet sheet;
	
	private CellStyle style;
	
	private int rowCount;
	
	private String[] menuPath;
	
	private String rootMenu;
	
	private List<String> processedMenus = new ArrayList<String>();
	
	private Inflector inflector;
	
	private Map<String, String[]> docMap = new HashMap<String, String[]>();
	
	@Inject
	private MetaMenuRepository metaMenuRepo;
	
	@Inject
	private MetaViewRepository metaViewRepo;
	
	@Inject
	private MetaTranslationRepository translationRepo; 
	
	@Inject
	private MetaFiles metaFiles;
	
	public MetaFile export(MetaFile docFile){
		
		inflector = Inflector.getInstance();
		
		List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null and self.left = true").order("order").fetch();
		
		workBook = new XSSFWorkbook();
		addStyle();
		
		if(docFile != null){
			updateDocMap(docFile);
		}
		
		processRootMenu(menus.iterator());
		
		updateColumnWidth();

		return createExportFile(docFile);
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

		writeRow(HEADERS);
		
	}
	
	private void writeRow(String[] values){
		
		rowCount += 1;
		XSSFRow row = sheet.createRow(rowCount);
		
		int count = 0;
		count = writeCell(row, values, count, true);
		
		if(rowCount > 0){
			count = writeCell(row, menuPath, count, true);
		}
		
		addDoc(row, values, count);
		
		menuPath = new String[]{"",""};
		
	}
	
	private int writeCell(XSSFRow row, String[] values,  int count, boolean addStyle){
		
		for(String value : values){
			XSSFCell cell = row.createCell(count);
			if(addStyle){
				cell.setCellStyle(style);
			}
			cell.setCellValue(value);
			count++;
		}
		
		return count;
	}
	
	private void addDoc(XSSFRow row, String[] values, int count){
		
		 String name = values[4];
		 if(name == null){
			 name = values[5];
		 }
		 
		 String[] obj = values[1].split("\\.");
		 String key = obj[obj.length - 1] + "," + values[3] + "," + name;
		 if(row.getRowNum() == 0){
			 key = sheet.getSheetName();
		 }
		 
		 String[] docs = docMap.get(key);
		 if(docs != null){
			 writeCell(row, docs, count, false);
		 }
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
	
	private boolean isChecked(String model, String type, String item){
		
		if(!itemCheckMap.containsKey(model)){
			Map<String, List<String>> map = new HashMap<String, List<String>>();
			map.put(type, new ArrayList<String>());
			itemCheckMap.put(model, map);
		}
		
		Map<String, List<String>> map = itemCheckMap.get(model);
		if(!map.containsKey(type)){
			map.put(type, new ArrayList<String>());
		}
		
		List<String> checkList = map.get(type);
		
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
			
			log.debug("Processing sub menu: {}", subMenu.getName());
			
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
					processModel(model, getFormName(model));
				}
			}
		}
		
	}
	
	private String getFormName(String model){
		
		String viewName = model.substring(model.lastIndexOf(".")+1);
		viewName = inflector.underscore(viewName);
		viewName = inflector.dasherize(viewName);
		
		return viewName + "-form";
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

			if(viewProcessed.contains(form)){
				return;
			}
			
			Mapper mapper = Mapper.of(ClassUtils.findClass(model));
			List<MetaView> metaViews =  metaViewRepo.all().filter(
					"self.type = 'form' and self.model = ? and self.name = ?", 
					model, form).fetch();
			
			if(!itemCheckMap.containsKey(model)){
				Map<String, List<String>> map = new HashMap<String, List<String>>();
				itemCheckMap.put(model, map);
			}
			
			if(metaViews.isEmpty()){
				log.debug("No view found: {}, model: {}", form, model);
			}
			else{
				processView(metaViews.iterator(), mapper);
				
				addO2MViews(model);
			}
			
			o2mViewMap = new HashMap<String, List<String[]>>();
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
		String name = view.getName();
		
		if(!viewProcessed.contains(name)){
			
			try {
				ObjectViews views = XMLViews.fromXML(view.getXml());
				
				FormView form = (FormView) views.getViews().get(0);
				
				processForm(form, view.getModule(), mapper, false);
				
			} catch (JAXBException e) {
				e.printStackTrace();
			}
		}
		
		processView(viewIter, mapper);
	}
	
	private void addO2MViews(String model){
		
		o2mViewMap.remove(model);
		
		Set<Entry<String, List<String[]>>> entrySet = new HashSet<Map.Entry<String,List<String[]>>>();
		entrySet.addAll(o2mViewMap.entrySet());
		
		for(Entry<String, List<String[]>> entry : entrySet){
			List<String[]> views = entry.getValue();
			String key = entry.getKey();
			
			for(String[] view : views){
				if(view[0] != null){
					menuPath = view[0].split(",");
				}
				if(view[1] == null){
					view[1] = getFormName(key);
				}
				
				processModel(key, view[1]);
				if(menuPath != null){
					menuPath = new String[]{"",""};
				}
			}
		}
		
	}
	
	private void processForm(FormView form, String module, Mapper mapper, boolean addPanel){
		
		String name = form.getName();
		log.debug("Processing form: {}", name);
		
		List<Button> buttons = form.getToolbar();
		if(buttons != null){
			for(Button button : buttons){
				processButton(button, name, module, mapper);
			}
		}
		
		List<AbstractWidget> items = form.getItems();
		if(items != null){
			processItems(items.iterator(), name, module, mapper, addPanel);
		}
		
		viewProcessed.add(name);
	}
	
	private String getModuleName(AbstractWidget item, String module){
		
		String moduleName = item.getModuleToCheck();
		
		if(Strings.isNullOrEmpty(moduleName)){
			moduleName = module;
		}
		
		return moduleName;
	}
	
	private void processItems(Iterator<AbstractWidget> itemIter, String view, String module, Mapper mapper, boolean addPanel){
		
		if(!itemIter.hasNext()){
			return;
		}
		
		AbstractWidget item = itemIter.next();
		
		if(item instanceof Panel){
			processPanel((Panel) item, view, module, mapper, addPanel);
		}
		else if(item instanceof PanelField) {
			processField((Field) item, view, module, mapper);
			processPanelEditor((PanelField) item, view, module, mapper);
		}
		else if(item instanceof PanelInclude){
			processPanelInclude((PanelInclude)item, module, mapper, addPanel);
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
			processItems(panelTabs.getItems().iterator(), view, getModuleName(panelTabs, module), mapper, true);
		}
		else if (item instanceof Field){
			processField((Field)item, view, module, mapper);
		}
		else if (item instanceof Dashlet){
			processDashlet((Dashlet)item, view, module, mapper);
		}
		
		processItems(itemIter, view, module, mapper, addPanel);
	}
	
	private void processPanel(Panel panel, String view, String module, Mapper mapper, boolean addPanel){
		
		if(addPanel){
			String className = mapper.getBeanClass().getName();
			String title = panel.getTitle();
	
			if(title != null && !isChecked(className, "panel", title)){
				
				String[] values = new String[]{
						getModuleName(panel, module), 
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
		}
		
		processItems(panel.getItems().iterator(), view, module, mapper, false);
	}
	
	private void processField(Field field, String view, String module, Mapper mapper){
		
		String name = field.getName();
		if(name.contains(".")){
			return;
		}
		String title = field.getTitle();
		String className = mapper.getBeanClass().getName();
		
		if(isChecked(className, "field", name)){
			return;
		}
		
		Property property = mapper.getProperty(name);
		String type = field.getServerType();
		String target = field.getTarget();
		List<?> selectionList = field.getSelectionList();
		
		if(property != null){
			type = property.getType().name();
			if(title == null){
				title = property.getTitle();
			}
			String selection = property.getSelection();
			if(selection != null && selectionList == null){
				selectionList = MetaStore.getSelectionList(selection);
			}
			
			Class<?> targetClass = property.getTarget();
			if(targetClass != null){
				target = targetClass.getName();
			}
		}
		
		if(target != null && type != null){
			if(type.equals("ONE_TO_MANY") || type.equals("one-to-many")){
				String parentView = view + "(" + translate(title, "en") + "),"
						  			+ view + "(" + translate(title, "fr") + ")"; 
				updateO2MViewMap(target, field.getFormView(), parentView);
			}
			String[] targets = target.split("\\.");
			type = type + "(" + targets[targets.length - 1] + ")";
		}
		
		String moduleName = getModuleName(field, module);
		if(name != null){
			String selectEN = "";
			String selectFR = "";
			if(selectionList != null){
				selectEN = updateSelect(selectionList, "en");
				selectFR = updateSelect(selectionList, "fr");
			}
			
			String[] values = new String[]{moduleName, 
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

	}
	
	private void updateO2MViewMap(String className, String o2mView, String parentView){
		
		List<String[]> views = o2mViewMap.get(className);
		if(views == null){
			views = new ArrayList<String[]>();
			o2mViewMap.put(className, views);
		}
		if(!Strings.isNullOrEmpty(o2mView) 
				&& !views.contains(o2mView)
				&& !viewProcessed.contains(o2mView)){
			views.add(new String[]{parentView, o2mView});
		}
		else{
			views.add(new String[]{parentView, null});
		}
		
		o2mViewMap.put(className, views);
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
					processItems(panelEditor.getItems().iterator(), view, getModuleName(panelField, module), targetMapper, false);
				}catch(IllegalArgumentException e){
					log.debug("Model not found: {}", target);
				}
			}
			else{
				processItems(panelEditor.getItems().iterator(), view, getModuleName(panelField, module), mapper, false);
			}
		}
	}
	
	private void processPanelInclude(PanelInclude panelInclude, String module, Mapper mapper, boolean addPanel){
		
		AbstractView view = panelInclude.getView();
		if(view != null){
			String name = view.getName();
			if(!viewProcessed.contains(name)){
				processForm((FormView)view, getModuleName(panelInclude, module), mapper, addPanel);
			}
		}
		else{
			log.debug("Issue in panel include: {}", panelInclude.getName());
		}
	}
	
	private void processButton(Button button, String view, String module, Mapper mapper){
		
		String name = button.getName();
		String className = mapper.getBeanClass().getName();
		
		if(!isChecked(className, "button", name)){
			String title = button.getTitle();
			String[] values = new String[]{getModuleName(button, module), 
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
		
		if(isChecked(className, "field", name)){
			return;
		}
		
		Property property = mapper.getProperty(name);
		String type = panelRelated.getServerType();
		String target = panelRelated.getTarget();
		if(property != null){
			type = property.getType().name();
			if(title == null){
				title = property.getTitle();
			}
			Class<?> targetClass = property.getTarget();
			if(targetClass != null){
				target = targetClass.getName();
			}
		}
		else{
			log.debug("No property found: {}, class: {}", name, className);
		}
		
		if(target != null && type != null){
			if(type.equals("ONE_TO_MANY") || type.equals("one-to-many")){
				String parentView = view + "(" + translate(title, "en") + "),"
								  + view + "(" + translate(title, "fr") + ")"; 
				updateO2MViewMap(target, panelRelated.getFormView(), parentView);
			}
			String[] targets = target.split("\\.");
			type = type + "(" + targets[targets.length - 1] + ")";
		}
		
		String[] values = new String[]{getModuleName(panelRelated, module), 
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
		if(isChecked(className, "label", title)){
			return;
		}
		
		String[] values = new String[]{getModuleName(label, module), 
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
	
	
	private void processDashlet(Dashlet dashlet, String view, String module, Mapper mapper){
		
		String className = mapper.getBeanClass().getName();
		String title = dashlet.getTitle();

		if(title != null && !isChecked(className, "dashlet", title)){
			
			String[] values = new String[]{
					getModuleName(dashlet, module), 
					className, 
					view, "Dashlet", 
					dashlet.getName(), 
					translate(title, "en"), 
					translate(title, "fr"),
					"",
					""
			};

			writeRow(values);
		}
		
	}

	private void updateDocMap(MetaFile docFile){
		
		try {
			File doc = MetaFiles.getPath(docFile).toFile();
			FileInputStream inSteam = new FileInputStream(doc);
			XSSFWorkbook book = new XSSFWorkbook(inSteam);
			
			for(XSSFSheet sheet : book){

				for(Row row : sheet){
					
					String key = null;
					if(row.getRowNum() == 0){
						key = sheet.getSheetName();
					}
					else{
						String name = getCellValue(row.getCell(4));
						if(Strings.isNullOrEmpty(name)){
							name =  getCellValue(row.getCell(5));
						}
						String[] obj = getCellValue(row.getCell(1)).split("\\.");
						key =  obj[obj.length-1] + "," + getCellValue(row.getCell(3)) +  "," +  name;
					}
					
					List<String> values = new ArrayList<String>();
					int count = 11;
					short lastCellNo = row.getLastCellNum();
					while(count < lastCellNo){
						values.add(getCellValue(row.getCell(count)));
						count++;
					};

					String[] vals = new String[]{};
					docMap.put(key, values.toArray(vals));
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getCellValue(Cell cell){
		
		if(cell != null){
			return cell.getStringCellValue();
		}
		
		return null;
	}
	
	private MetaFile createExportFile(MetaFile metaFile){
		
		String date = LocalDateTime.now().toString("ddMMyyyy HH:mm:ss");
		String fileName = "Export " + date + ".xlsx";
		
		try {
			File file = File.createTempFile("Export", ".xlsx");
			FileOutputStream  outStream = new FileOutputStream(file);
			workBook.write(outStream);
			outStream.close();
			
			FileInputStream inStream = new FileInputStream(file);
			if(metaFile != null){
				metaFile.setFileName(fileName);
				metaFile = metaFiles.upload(inStream, metaFile);
			}
			else{
				metaFile = metaFiles.upload(inStream, fileName);
			}
			
			inStream.close();
			
			file.delete();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return metaFile;
	}
	
	
}
