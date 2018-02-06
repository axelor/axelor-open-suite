package com.axelor.apps.prestashop.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class ListContainer<T extends PrestashopContainerEntity> extends PrestashopContainerEntity {
	List<T> entities = new LinkedList<T>();

	@XmlElementRef
	public List<T> getEntities() {
		return entities;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("entities", entities)
				.toString();
	}

	@XmlRootElement(name="currencies")
	public static class CurrenciesContainer extends ListContainer<PrestashopCurrency> {
	}

	@XmlRootElement(name="countries")
	public static class CountriesContainer extends ListContainer<PrestashopCountry> {
	}
}
