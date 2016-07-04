package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Basic;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.HashKey;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaModel;
import com.google.common.base.MoreObjects;

/**
 * This class use to generate template from selected view and also add button in view.

 * It will store template and header/footer in html format.

 * It will be used to generate html report for a record.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_REPORT_BUILDER")
public class ReportBuilder extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_REPORT_BUILDER_SEQ")
	@SequenceGenerator(name = "STUDIO_REPORT_BUILDER_SEQ", sequenceName = "STUDIO_REPORT_BUILDER_SEQ", allocationSize = 1)
	private Long id;

	@HashKey
	@Widget(title = "Name")
	@NotNull
	@Column(unique = true)
	private String name;

	@Widget(title = "Model")
	@NotNull
	@Index(name = "STUDIO_REPORT_BUILDER_META_MODEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaModel metaModel;

	@Widget(title = "View Builder")
	@Index(name = "STUDIO_REPORT_BUILDER_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "Add button to", help = "true")
	@Index(name = "STUDIO_REPORT_BUILDER_BUTTON_VIEW_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder buttonView;

	@Widget(title = "Header")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String header;

	@Widget(title = "Footer")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String footer;

	@Widget(title = "Language", selection = "select.language")
	private String language;

	@Widget(title = "Html Template")
	@Lob
	@Basic(fetch = FetchType.LAZY)
	@Type(type = "org.hibernate.type.TextType")
	private String htmlTemplate;

	private Boolean edited = Boolean.FALSE;

	@Widget(title = "Print page number ?")
	private Boolean printPageNo = Boolean.FALSE;

	@Widget(title = "Edit HTML?")
	private Boolean editHtml = Boolean.FALSE;

	@Widget(title = "File name")
	private String fileName;

	public ReportBuilder() {
	}

	public ReportBuilder(String name) {
		this.name = name;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public void setMetaModel(MetaModel metaModel) {
		this.metaModel = metaModel;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	/**
	 * true
	 *
	 * @return the property value
	 */
	public ViewBuilder getButtonView() {
		return buttonView;
	}

	public void setButtonView(ViewBuilder buttonView) {
		this.buttonView = buttonView;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getHtmlTemplate() {
		return htmlTemplate;
	}

	public void setHtmlTemplate(String htmlTemplate) {
		this.htmlTemplate = htmlTemplate;
	}

	public Boolean getEdited() {
		return edited == null ? Boolean.FALSE : edited;
	}

	public void setEdited(Boolean edited) {
		this.edited = edited;
	}

	public Boolean getPrintPageNo() {
		return printPageNo == null ? Boolean.FALSE : printPageNo;
	}

	public void setPrintPageNo(Boolean printPageNo) {
		this.printPageNo = printPageNo;
	}

	public Boolean getEditHtml() {
		return editHtml == null ? Boolean.FALSE : editHtml;
	}

	public void setEditHtml(Boolean editHtml) {
		this.editHtml = editHtml;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ReportBuilder)) return false;

		final ReportBuilder other = (ReportBuilder) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		if (!Objects.equals(getName(), other.getName())) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(141746087, this.getName());
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("name", this.getName());
		tsh.add("language", this.getLanguage());
		tsh.add("edited", this.getEdited());
		tsh.add("printPageNo", this.getPrintPageNo());
		tsh.add("editHtml", this.getEditHtml());
		tsh.add("fileName", this.getFileName());

		return tsh.omitNullValues().toString();
	}
}
