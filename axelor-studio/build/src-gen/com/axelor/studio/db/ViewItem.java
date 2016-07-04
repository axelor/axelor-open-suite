package com.axelor.studio.db;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.axelor.auth.db.AuditableModel;
import com.axelor.db.annotations.Widget;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaSelect;
import com.google.common.base.MoreObjects;

/**
 * This object store custom view items like field, button, label with attributes readonlyIf, widget, colSpan, onChange ..etc.
 */
@Entity
@Cacheable
@Table(name = "STUDIO_VIEW_ITEM")
public class ViewItem extends AuditableModel {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "STUDIO_VIEW_ITEM_SEQ")
	@SequenceGenerator(name = "STUDIO_VIEW_ITEM_SEQ", sequenceName = "STUDIO_VIEW_ITEM_SEQ", allocationSize = 1)
	private Long id;

	@Widget(title = "Type", selection = "studio.view.item.type.select")
	private Integer typeSelect = 0;

	@Widget(title = "Field")
	@Index(name = "STUDIO_VIEW_ITEM_META_FIELD_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaField metaField;

	@Index(name = "STUDIO_VIEW_ITEM_VIEW_PANEL_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewPanel viewPanel;

	@Index(name = "STUDIO_VIEW_ITEM_VIEW_BUILDER_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilder;

	@Widget(title = "Name")
	@Index(name = "STUDIO_VIEW_ITEM_NAME_IDX")
	private String name;

	@Widget(title = "Title")
	private String title;

	@Widget(title = "Sequence")
	private Integer sequence = 0;

	@Widget(title = "Default")
	private String defaultValue;

	@Widget(title = "On Change")
	private String onChange;

	@Widget(title = "Progress Bar ?")
	private Boolean progressBar = Boolean.FALSE;

	@Widget(title = "Html ?")
	private Boolean htmlWidget = Boolean.FALSE;

	@Widget(title = "Widget", selection = "view.item.widget.selection")
	private String widget = "normal";

	@Widget(title = "Doman")
	private String domainCondition;

	@Widget(title = "Readonly ?")
	private Boolean readonly = Boolean.FALSE;

	@Widget(title = "Readonly If")
	private String readonlyIf;

	@Widget(title = "Required ?")
	private Boolean required = Boolean.FALSE;

	@Widget(title = "Required If")
	private String requiredIf;

	@Widget(title = "Hidden ?")
	private Boolean hidden = Boolean.FALSE;

	@Widget(title = "Hide If")
	private String hideIf;

	@Widget(title = "ShowIf")
	private String showIf;

	@Widget(title = "Selection")
	@Index(name = "STUDIO_VIEW_ITEM_META_SELECT_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private MetaSelect metaSelect;

	@Widget(title = "Type")
	private String fieldType;

	@Widget(title = "Colspan")
	private Integer colSpan = 0;

	@Widget(title = "Icon")
	private String icon;

	@Widget(title = "Prompt")
	private String promptMsg;

	@Widget(title = "On click")
	private String onClick;

	@Widget(title = "Is workflow button?")
	private Boolean wkfButton = Boolean.FALSE;

	@Index(name = "STUDIO_VIEW_ITEM_VIEW_BUILDER_TOOLBAR_IDX")
	@ManyToOne(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private ViewBuilder viewBuilderToolbar;

	@Widget(title = "Panel top")
	private Boolean panelTop = Boolean.FALSE;

	public ViewItem() {
	}

	public ViewItem(String name) {
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

	public Integer getTypeSelect() {
		return typeSelect == null ? 0 : typeSelect;
	}

	public void setTypeSelect(Integer typeSelect) {
		this.typeSelect = typeSelect;
	}

	public MetaField getMetaField() {
		return metaField;
	}

	public void setMetaField(MetaField metaField) {
		this.metaField = metaField;
	}

	public ViewPanel getViewPanel() {
		return viewPanel;
	}

	public void setViewPanel(ViewPanel viewPanel) {
		this.viewPanel = viewPanel;
	}

	public ViewBuilder getViewBuilder() {
		return viewBuilder;
	}

	public void setViewBuilder(ViewBuilder viewBuilder) {
		this.viewBuilder = viewBuilder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getSequence() {
		return sequence == null ? 0 : sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getOnChange() {
		return onChange;
	}

	public void setOnChange(String onChange) {
		this.onChange = onChange;
	}

	public Boolean getProgressBar() {
		return progressBar == null ? Boolean.FALSE : progressBar;
	}

	public void setProgressBar(Boolean progressBar) {
		this.progressBar = progressBar;
	}

	public Boolean getHtmlWidget() {
		return htmlWidget == null ? Boolean.FALSE : htmlWidget;
	}

	public void setHtmlWidget(Boolean htmlWidget) {
		this.htmlWidget = htmlWidget;
	}

	public String getWidget() {
		return widget;
	}

	public void setWidget(String widget) {
		this.widget = widget;
	}

	public String getDomainCondition() {
		return domainCondition;
	}

	public void setDomainCondition(String domainCondition) {
		this.domainCondition = domainCondition;
	}

	public Boolean getReadonly() {
		return readonly == null ? Boolean.FALSE : readonly;
	}

	public void setReadonly(Boolean readonly) {
		this.readonly = readonly;
	}

	public String getReadonlyIf() {
		return readonlyIf;
	}

	public void setReadonlyIf(String readonlyIf) {
		this.readonlyIf = readonlyIf;
	}

	public Boolean getRequired() {
		return required == null ? Boolean.FALSE : required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public String getRequiredIf() {
		return requiredIf;
	}

	public void setRequiredIf(String requiredIf) {
		this.requiredIf = requiredIf;
	}

	public Boolean getHidden() {
		return hidden == null ? Boolean.FALSE : hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public String getHideIf() {
		return hideIf;
	}

	public void setHideIf(String hideIf) {
		this.hideIf = hideIf;
	}

	public String getShowIf() {
		return showIf;
	}

	public void setShowIf(String showIf) {
		this.showIf = showIf;
	}

	public MetaSelect getMetaSelect() {
		return metaSelect;
	}

	public void setMetaSelect(MetaSelect metaSelect) {
		this.metaSelect = metaSelect;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public Integer getColSpan() {
		return colSpan == null ? 0 : colSpan;
	}

	public void setColSpan(Integer colSpan) {
		this.colSpan = colSpan;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getPromptMsg() {
		return promptMsg;
	}

	public void setPromptMsg(String promptMsg) {
		this.promptMsg = promptMsg;
	}

	public String getOnClick() {
		return onClick;
	}

	public void setOnClick(String onClick) {
		this.onClick = onClick;
	}

	public Boolean getWkfButton() {
		return wkfButton == null ? Boolean.FALSE : wkfButton;
	}

	public void setWkfButton(Boolean wkfButton) {
		this.wkfButton = wkfButton;
	}

	public ViewBuilder getViewBuilderToolbar() {
		return viewBuilderToolbar;
	}

	public void setViewBuilderToolbar(ViewBuilder viewBuilderToolbar) {
		this.viewBuilderToolbar = viewBuilderToolbar;
	}

	public Boolean getPanelTop() {
		return panelTop == null ? Boolean.FALSE : panelTop;
	}

	public void setPanelTop(Boolean panelTop) {
		this.panelTop = panelTop;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (this == obj) return true;
		if (!(obj instanceof ViewItem)) return false;

		final ViewItem other = (ViewItem) obj;
		if (this.getId() != null || other.getId() != null) {
			return Objects.equals(this.getId(), other.getId());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public String toString() {
		final MoreObjects.ToStringHelper tsh = MoreObjects.toStringHelper(this);

		tsh.add("id", this.getId());
		tsh.add("typeSelect", this.getTypeSelect());
		tsh.add("name", this.getName());
		tsh.add("title", this.getTitle());
		tsh.add("sequence", this.getSequence());
		tsh.add("defaultValue", this.getDefaultValue());
		tsh.add("onChange", this.getOnChange());
		tsh.add("progressBar", this.getProgressBar());
		tsh.add("htmlWidget", this.getHtmlWidget());
		tsh.add("widget", this.getWidget());
		tsh.add("domainCondition", this.getDomainCondition());
		tsh.add("readonly", this.getReadonly());

		return tsh.omitNullValues().toString();
	}
}
