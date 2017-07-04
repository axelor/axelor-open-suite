package com.axelor.apps.sale.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.repo.ConfiguratorCreatorRepository;
import com.axelor.apps.sale.db.repo.ConfiguratorRepository;
import com.axelor.meta.db.MetaJsonField;
import com.google.common.base.Strings;
import com.axelor.inject.Beans;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfiguratorCreatorServiceImpl implements ConfiguratorCreatorService {

	private ConfiguratorRepository configuratorRepo;
	private ConfiguratorCreatorRepository configuratorCreatorRepo;

	@Inject
	public ConfiguratorCreatorServiceImpl(ConfiguratorRepository configuratorRepo, ConfiguratorCreatorRepository configuratorCreatorRepo) {
		this.configuratorRepo = configuratorRepo;
		this.configuratorCreatorRepo = configuratorCreatorRepo;
	}


	@Override
	@Transactional
	public Configurator generateConfigurator(ConfiguratorCreator creator) {
		
		if (creator == null) {
			return null;
		}
		
		for (MetaJsonField field : creator.getAttributes()) {
			String condition = "$record.configuratorCreator.id == " + creator.getId();
			String showIf = field.getShowIf();
			if (!Strings.isNullOrEmpty(showIf)) {
				if (!showIf.contains(condition)) {
					field.setShowIf(condition + " && (" + showIf + ")");
				}
			}
			else {
				field.setShowIf(condition);
			}
		}

		Configurator configurator =  configuratorRepo.all().filter("self.configuratorCreator = ?1", creator).fetchOne();
		
		if (configurator == null) {
			configurator = new Configurator();
			configurator.setConfiguratorCreator(creator);
			configuratorRepo.save(configurator);
		}
		
		
		return configurator;
	}

	@Transactional(rollbackOn = {Exception.class})
	public void updateIndicators(ConfiguratorCreator creator) {
		List<ConfiguratorFormula> formulas = creator.getFormulas();
		formulas = formulas.stream()
				.filter(it -> it.getShowOnConfigurator())
				.collect(Collectors.toList());
		List<MetaJsonField> fields = creator.getIndicators();

		//add missing formulas
		if (formulas != null) {
			for (ConfiguratorFormula formula : formulas) {
			    addIfMissing(formula, creator);
			}
		}

		//remove formulas
		List<MetaJsonField> fieldsToRemove = new ArrayList<>();
		for (MetaJsonField field : fields) {
			if (field.getModel().equals(Product.class.getName())
					&& isNotInFormulas(field, formulas)) {
				fieldsToRemove.add(field);
			}
		}
		for (MetaJsonField fieldToRemove : fieldsToRemove ) {
		    creator.removeIndicator(fieldToRemove);
		}

		configuratorCreatorRepo.save(creator);
	}

	/**
	 * Add the {@link ConfiguratorFormula} in {@link ConfiguratorCreator#indicators}
	 * if the formula is not represented by an existing indicator.
	 * @param formula
	 * @param creator
	 */
	protected void addIfMissing(ConfiguratorFormula formula, ConfiguratorCreator creator) {
	    List<MetaJsonField> fields = creator.getIndicators();
		for (MetaJsonField field : fields) {
			if (field.getName().equals(formula.getProductField().getName())) {
				return;
			}
		}
		MetaJsonField newField = new MetaJsonField();
		newField.setModel(Configurator.class.getName());
		newField.setModelField("indicators");
		String typeName = Beans.get(MetaFieldRepository.class).all()
				.filter("self.metaModel.name = 'Product' AND " +
						"self.name = ?", formula.getProductField().getName())
				.fetchOne().getTypeName();
		newField.setType(typeToJsonType(typeName));
		newField.setName(formula.getProductField().getName());
		creator.addIndicator(newField);
	}

	/**
	 *
	 * @param field
	 * @param formulas
	 * @return false if field is represented in formula list
	 *         true if field is missing in the formula list
	 */
	protected boolean isNotInFormulas(MetaJsonField field, List<ConfiguratorFormula> formulas) {
	    for(ConfiguratorFormula formula : formulas) {
	        if (formula.getProductField().getName().equals(field.getName())) {
	        	return false;
			}
		}
		return true;
	}

	/**
	 * Convert the type of a field to a type of a json field.
	 * @param nameType type of a field
	 * @return corresponding type of json field
	 */
	protected String typeToJsonType(String nameType) {
	    if (nameType.equals("BigDecimal")) {
	    	return "decimal";
		}
		else {
	    	return nameType.toLowerCase();
		}
	}

}
