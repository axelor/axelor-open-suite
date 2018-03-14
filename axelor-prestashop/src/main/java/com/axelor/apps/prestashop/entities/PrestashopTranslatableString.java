package com.axelor.apps.prestashop.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PrestashopTranslatableString implements Cloneable {
	private List<PrestashopTranslationEntry> translations = new LinkedList<>();

	public PrestashopTranslatableString() {
	}

	private PrestashopTranslatableString(final PrestashopTranslatableString other) {
		for(PrestashopTranslationEntry entry : other.translations) {
			translations.add(new PrestashopTranslationEntry(entry));
		}
	}

	@XmlElement(name="language")
	public List<PrestashopTranslationEntry> getTranslations() {
		return translations;
	}

	@Override
	public PrestashopTranslatableString clone() {
		return new PrestashopTranslatableString(this);
	}

	/**
	 * Reset all translation entries to provided value
	 * @param newValue Value to set to all translations
	 */
	public void clearTranslations(final String newValue) {
		for(PrestashopTranslationEntry e : translations) {
			e.setTranslation(newValue);
		}
	}

	public String getTranslation(final int language) {
		// If ever heavily used, consider using a Map
		for(PrestashopTranslationEntry e : translations) {
			if(e.getLanguageId() == language) return e.getTranslation();
		}
		return null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("translations", translations)
				.toString();
	}

	public static class PrestashopTranslationEntry {
		private int languageId;
		private String translation;

		public PrestashopTranslationEntry() {
		}

		public PrestashopTranslationEntry(final PrestashopTranslationEntry other) {
			this.languageId = other.languageId;
			this.translation = other.translation;
		}

		@XmlAttribute(name="id")
		public int getLanguageId() {
			return languageId;
		}

		public void setLanguageId(int languageId) {
			this.languageId = languageId;
		}

		@XmlValue
		public String getTranslation() {
			return translation;
		}

		public void setTranslation(String translation) {
			this.translation = translation;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("languageId", languageId)
					.append("translation", translation)
					.toString();
		}
	}

}
