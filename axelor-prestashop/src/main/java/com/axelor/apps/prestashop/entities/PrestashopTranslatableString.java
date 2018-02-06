package com.axelor.apps.prestashop.entities;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

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
	}

}
