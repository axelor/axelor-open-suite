package com.axelor.studio.service.builder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.common.Inflector;
import com.axelor.common.VersionUtils;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.MetaSequence;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.meta.schema.ObjectViews;
import com.axelor.studio.utils.Namming;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

/**
 * Service class generate domain xml files from MetaModels. It process MetaModel
 * to create model and MetaField to create field xml.
 * 
 * @author axelor
 *
 */
public class ModelBuilderService {
	
	public final static List<String> reserveFields;
	
	static {
		List<String> fields = new ArrayList<String>();
		fields.add("id");
		fields.add("version");
		fields.add("createdBy");
		fields.add("updatedBy");
		fields.add("createdOn");
		fields.add("updatedOn");
		reserveFields = Collections.unmodifiableList(fields);
	}
	
	private static final String NAMESPACE = "http://axelor.com/xml/ns/domain-models";

	private static final String VERSION = VersionUtils.getVersion().feature;

	private final Logger log = LoggerFactory.getLogger(getClass());

	private StringBuilder fieldXml;

	private StringBuilder sequenceXml;

	private StringBuilder selectionBuilder = new StringBuilder();

	private File domainDir;

	private List<String> trackFields;

	@Inject
	private MetaModelRepository metaModelRepo;

	@Inject
	private MetaTranslationRepository translationRepo;

	@Inject
	private MetaFieldRepository metaFieldRepo;
	
	/**
	 * Root method to accesss the service. It will find all edited and
	 * customised MetaModels. Call other methods to process MetaModel founds.
	 * @throws AxelorException 
	 */
	public boolean build(File domainDir) throws AxelorException {

		if (domainDir == null) {
			throw new AxelorException(I18n.get("Model directory not found please check the configuration"), 4);
		}
		this.domainDir = domainDir;

		List<MetaModel> customizedModels = metaModelRepo.all()
				.filter("self.customised = true").fetch();
		List<MetaModel> models = new ArrayList<MetaModel>();
		for (MetaModel model : customizedModels) {
			if (model.getEdited()) {
				models.add(model);
			}
		}

		if (models.isEmpty()) {
			log.error("No edited models found");
			return true;
		}

		log.debug("Total edited models: {}", models.size());

		try {
			removeDeleted(customizedModels);
			recordModel(models.iterator());
			String selectionXml = selectionBuilder.toString();

			if (!Strings.isNullOrEmpty(selectionXml)) {
				writeSelectionXml(selectionXml);
			}

			updateEdited(models);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			throw new AxelorException(I18n.get("Error in model recording: %s"), 4, e.getMessage());
		}

	}

	/**
	 * Method to reset edited boolean to false when processing of model
	 * completed.
	 * 
	 * @param models
	 *            List of MetaModel to process.
	 */
	@Transactional
	public void updateEdited(List<MetaModel> models) {

		for (MetaModel model : models) {
			model.setEdited(false);
			metaModelRepo.save(model);
		}

	}

	/**
	 * Method create domain xml file from MetaModel. It create one single domain
	 * xml string for domain and write it to file. Also call method to process
	 * fields.
	 * 
	 * @param modelIterator
	 *            MetaModel iterator
	 * @throws IOException
	 *             Exception thrown by file handling of domain xml file.
	 * @throws AxelorException 
	 */
	private void recordModel(Iterator<MetaModel> modelIterator)
			throws IOException, AxelorException {

		if (!modelIterator.hasNext()) {
			return;
		}

		MetaModel metaModel = modelIterator.next();
		String packageName = metaModel.getPackageName();
		String modelName = metaModel.getName();
		trackFields = new ArrayList<String>();

		sequenceXml = new StringBuilder();
		updateSequenceXml(metaModel.getMetaSequencList().iterator());

		fieldXml = new StringBuilder("");
		List<MetaField> customFields = getCustomisedFields(metaModel, true);

		if (customFields.isEmpty()) {
			recordModel(modelIterator);
			return;
		}

		sortFieldList(customFields);
		writeFields(customFields.iterator());

		StringBuilder sb = new StringBuilder(
				"<?xml version='1.0' encoding='UTF-8'?>\n")
				.append("<domain-models")
				.append(" xmlns='")
				.append(NAMESPACE)
				.append("'")
				.append(" xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'")
				.append(" xsi:schemaLocation='")
				.append(NAMESPACE)
				.append(" ")
				.append(NAMESPACE + "/" + "domain-models_" + VERSION + ".xsd")
				.append("'")
				.append(">\n\n")
				.append("\t<module name=\"custom\" package=\"" + packageName
						+ "\" />\n\n").append(sequenceXml.toString() + "\n")
				.append("\t<entity name=\"" + modelName + "\">\n")
				.append(fieldXml.toString()).append(getTrackFields())
				.append("\t</entity>\n\n").append("</domain-models>\n");

		File domainFile = new File(domainDir, modelName + ".xml");
		writeFile(domainFile, sb.toString());

		recordModel(modelIterator);

	}

	/**
	 * This method process MetaField and create field xml from it. The generated
	 * field xml will be appended to model xml.
	 * 
	 * @param fieldIterator
	 *            MetaField iterator
	 * @throws AxelorException 
	 */
	private void writeFields(Iterator<MetaField> fieldIterator) throws AxelorException {

		if (!fieldIterator.hasNext()) {
			return;
		}

		MetaField field = fieldIterator.next();

		String fieldType = field.getFieldType();

		fieldXml.append("\t\t<" + fieldType + " ");

		String name = field.getName();
		
		if (reserveFields.contains(name)) {
			throw new AxelorException(I18n.get("In model '%s' field name '%s' is reserve word. "
					+ "Please use different one"),
					5, field.getMetaModel().getName(), name);
		}
		
		if (field.getTrack()) {
			trackFields.add(name);
		}

		fieldXml.append("name=\"" + name + "\" ");

		if (Namming.isKeyword(name)) {
			fieldXml.append("column=\""
					+ Inflector.getInstance().underscore(name) + "_val\" ");
		}

		String title = field.getLabel();
		if (title != null) {
			title = title.replace("\\","\\\\");
			title = title.replace("\"", "");
			title = StringEscapeUtils.escapeXml(title);
			fieldXml.append("title=\"" + title + "\" ");
		}

		switch (fieldType) {
		case "integer":
			writeInteger(field);
			break;
		case "decimal":
			writeDecimal(field);
			break;
		case "string":
			writeString(field);
			break;
		case "boolean":
			writeBoolean(field);
			break;
		case "many-to-one":
			writeRelational(field);
			break;
		case "one-to-many":
			writeRelational(field);
			break;
		case "many-to-many":
			writeRelational(field);
			break;
		}

		if (field.getMetaSelect() != null) {
			writeSelection(field);
			fieldXml.append("selection=\"" + field.getMetaSelect().getName()
					+ "\"  ");
		}

		if (field.getRequired()) {
			fieldXml.append("required=\"true\"  ");
		}

		if (field.getReadonly()) {
			fieldXml.append("readonly=\"true\" ");
		}

		if (field.getHidden()) {
			fieldXml.append("hidden=\"true\" ");
		}

		if (field.getHelpText() != null) {
			String help = field.getHelpText();
			help = StringEscapeUtils.escapeJava(help);
			help = StringEscapeUtils.escapeXml(help);
			fieldXml.append("help=\"" + help + "\" ");
//			String key = "help:" + field.getMetaModel().getName() + ":"
//					+ field.getName();
//			String lang = AuthUtils.getUser().getLanguage();
//			if (lang == null) {
//				lang = "en";
//			}
//			updateTranslation(key, field.getHelpText(), lang);
		}

		if (field.getNameColumn()) {
			fieldXml.append("namecolumn=\"true\" ");
		}

		fieldXml.append("/>\n");

		writeFields(fieldIterator);
	}

	/**
	 * Special method to append min/max attributes to field xml for string and
	 * integer type of fields.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	private void writeMinMax(MetaField field) {

		Integer min = field.getIntegerMin();
		if (min != null && min != 0) {
			fieldXml.append("min=\"" + min + "\" ");
		}

		Integer max = field.getIntegerMax();
		if (max != null && max != 0) {
			fieldXml.append("max=\"" + max + "\" ");
		}

	}

	/**
	 * Method to set default value and min/max for integer type of field.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	private void writeInteger(MetaField field) {

		Integer defaultInt = field.getDefaultInteger();
		if (defaultInt != null && defaultInt != 0) {
			fieldXml.append("default=\"" + defaultInt + "\" ");
		}

		writeMinMax(field);
	}

	/**
	 * Method to process decimal type of field for default,min,max attributes
	 * and append it to field xml.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	private void writeDecimal(MetaField field) {

		BigDecimal max = field.getDecimalMax();
		if (max != null && max.compareTo(BigDecimal.ZERO) != 0) {
			fieldXml.append("max=\"" + max + "\" ");
		}

		BigDecimal min = field.getDecimalMin();
		if (min != null && min.compareTo(BigDecimal.ZERO) != 0) {
			fieldXml.append("min=\"" + min + "\" ");
		}

		BigDecimal defaultDecimal = field.getDefaultDecimal();
		if (defaultDecimal != null
				&& defaultDecimal.compareTo(BigDecimal.ZERO) != 0) {
			fieldXml.append("default=\"" + defaultDecimal + "\" ");
		}

	}

	/**
	 * Method to process string type of field for default,large,min,max
	 * attributes and append it to field xml.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	private void writeString(MetaField field) {

		Boolean large = field.getLarge();
		if (large != null && large) {
			fieldXml.append("large=\"true\" ");
		}

		String defaultString = field.getDefaultString();
		if (defaultString != null) {
			fieldXml.append("default=\"" + defaultString + "\" ");
		}
		
		String sequence = field.getMetaSequence();
		if (sequence != null) {
			fieldXml.append("sequence=\"" + sequence + "\" ");
		}
		
		writeMinMax(field);
	}

	/**
	 * Method to process boolean field for default attributes and append it to
	 * field xml.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	private void writeBoolean(MetaField field) {

		Boolean defaultBoolean = field.getDefaultBoolean();
		if (defaultBoolean != null && defaultBoolean) {
			fieldXml.append("default=\"true\" ");
		}

	}

	/**
	 * Method to add 'ref' attribute in field xml for relational field. It
	 * search typeName of MetaField for reference model and set fullName as
	 * reference.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	private void writeRelational(MetaField field) {

		log.debug("Type name for relational field : {}, typeName: {}",
				field.getName(), field.getTypeName());

		MetaModel metaModel = metaModelRepo.findByName(field.getTypeName());

		if (metaModel != null) {
			log.debug("Meta model found: {}", metaModel.getName());
			fieldXml.append("ref=\"" + metaModel.getFullName() + "\" ");
		}

		String mappedBy = field.getMappedBy();
		if (!Strings.isNullOrEmpty(mappedBy)) {
			fieldXml.append("mappedBy=\"" + mappedBy + "\" ");
		}

	}

	/**
	 * Method create selection xml from MetaSelect of MetaField. It append
	 * selection xml string to selectionBuilder.
	 * 
	 * @param field
	 *            MetaField to process
	 */
	@Transactional
	public void writeSelection(MetaField field) {

		MetaSelect metaSelect = field.getMetaSelect();
		List<MetaSelectItem> selectionItemList = field.getMetaSelect()
				.getItems();
		StringBuilder selectXml = new StringBuilder("\t<selection name=\""
				+ metaSelect.getName() + "\">\n");

		for (MetaSelectItem item : selectionItemList) {
			String title = item.getTitle();
			String value = item.getValue();
			selectXml.append("\t\t<option value=\"" + value + "\">" + title
					+ "</option>\n");
		}
		selectXml.append("\t</selection>\n");

		log.debug("Saving meta select: {}", metaSelect.getName());
		
		selectionBuilder.append(selectXml);
	}

	/**
	 * Method to write domain xml file.
	 * 
	 * @param file
	 *            File to write.
	 * @param content
	 *            Content to write in file.
	 * @throws IOException
	 *             Exception thrown by file handling.
	 */
	private void writeFile(File file, String content) throws IOException {

		log.debug("Writing file: {}", file.getPath());

		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(content);
		fileWriter.close();

	}

	/**
	 * Special method for writing separate file 'Selection.xml' for selections
	 * xml string generated during model recording.
	 * 
	 * @param selectionXml
	 *            Xml string containing selections.
	 * @throws IOException
	 *             Exception in file writing.
	 */
	private void writeSelectionXml(String selectionXml) throws IOException {

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
				.append(">\n\n").append(selectionXml)
				.append("\n</object-views>");

		File file = new File(domainDir, "Selection.xml");
		writeFile(file, sb.toString());

	}

	/**
	 * Sort list of MetaFields according to sequence.
	 * 
	 * @param fieldList
	 */
	public void sortFieldList(List<MetaField> fieldList) {

		Comparator<MetaField> comparator = new Comparator<MetaField>() {

			@Override
			public int compare(MetaField field1, MetaField field2) {

				if (field1.getSequence() < field2.getSequence()) {
					return -1;
				}

				return 0;
			}

		};

		Collections.sort(fieldList, comparator);
	}

	/**
	 * Method filter customised field from list MetaField. Only customised field
	 * will be processed and added in domain xml file.
	 * 
	 * @param fields
	 *            List of MetaField to filter.
	 * @return List of customised field.
	 */
	public List<MetaField> getCustomisedFields(MetaModel metaModel,
			boolean getWkf) {

		String query = "self.metaModel = ?1 and self.customised = true";

		if (!getWkf) {
			query = "self.metaModel = ?1 and self.customised = true and self.name != 'wkfStatus'";
		}

		List<MetaField> customFields = metaFieldRepo.all()
				.filter(query, metaModel).order("sequence").fetch();

		return customFields;
	}

	/**
	 * Method create/update MetaTranslation for help text of a field.
	 * 
	 * @param key
	 *            Key for translation.
	 * @param value
	 *            Value for translation.
	 * @param language
	 *            Translation language to update.
	 */
	@Transactional
	public void updateTranslation(String key, String value, String language) {
		
		if (key != null && language != null) {
			MetaTranslation translation = translationRepo.all()
					.filter("self.key = ? AND self.language = ?", key, language)
					.fetchOne();
	
			if (translation == null) {
				translation = new MetaTranslation();
				translation.setKey(key);
				translation.setLanguage(language);
			}
	
			translation.setMessage(value);
	
			translationRepo.save(translation);
		}
	}

	/**
	 * Method convert simple type name of field into adk supported 'typeName'
	 * (which is type class name) for MetaField.
	 * 
	 * @param metaField
	 *            MetaField to get typeName.
	 * @return Adk supported typeName.
	 */
	public String getFieldTypeName(String type) {

		switch (type) {
		case "string":
			return "String";
		case "integer":
			return "Integer";
		case "boolean":
			return "Boolean";
		case "decimal":
			return "BigDecimal";
		case "long":
			return "Long";
		case "binary":
			return "byte[]";
		case "date":
			return "LocalDate";
		case "datetime":
			return "LocalDateTime";
		default:
			return null;

		}
	}

	private void removeDeleted(List<MetaModel> customizedModels) {

		List<String> fileNames = new ArrayList<String>();
		for (MetaModel model : customizedModels) {
			fileNames.add(model.getName() + ".xml");
		}

		for (File file : domainDir.listFiles()) {
			if (!fileNames.contains(file.getName())) {
				file.delete();
			}
		}
	}

	private String getTrackFields() {

		String track = "";

		if (trackFields.isEmpty()) {
			return track;
		}

		track = "\n\t\t<track>";

		for (String field : trackFields) {
			track += "\n\t\t\t<field name=\"" + field + "\"/>";
		}

		track += "\n\t\t</track>\n";

		return track;
	}
	
	@Transactional
	public void updateSequenceXml(Iterator<MetaSequence> seqIter) {

		if (!seqIter.hasNext()) {
			return;
		}
		
		MetaSequence sequence = seqIter.next();
		
		String name = sequence.getName();
		String seqXml = "\t<sequence name=\"" + name + "\" ";
		
		String prefix = sequence.getPrefix();
		if (prefix != null) {
			seqXml += "prefix=\"" + prefix + "\" ";
		}
		
		String suffix = sequence.getSuffix();
		if (suffix != null) {
			seqXml += "suffix=\"" + suffix + "\" ";
		}
		
		Integer padding = sequence.getPadding();
		if (padding != null) {
			seqXml += "padding=\"" + padding + "\" ";
		}
		
		seqXml += "/>\n";

		sequenceXml.append(seqXml);

		updateSequenceXml(seqIter);
	}

}
