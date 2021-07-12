package com.axelor.apps.sale.xml.models;

import com.axelor.apps.base.xml.adapters.GroupXmlAdapter;
import com.axelor.apps.base.xml.adapters.UserXmlAdapter;
import com.axelor.apps.base.xml.models.ExportedModel;
import com.axelor.apps.sale.db.ConfiguratorProductFormula;
import com.axelor.apps.sale.db.ConfiguratorSOLineFormula;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaJsonField;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "configurator-creator")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExportedConfiguratorCreator extends ExportedModel {

  private String name;
  
  @XmlElement(name = "attribute")
  @XmlElementWrapper(name = "attributes")
  private List<MetaJsonField> attributes;

  @XmlTransient private List<ConfiguratorProductFormula> configuratorProductFormulaList;

  @XmlTransient private List<ConfiguratorSOLineFormula> configuratorSOLineFormulaList;

  @XmlElement(name = "authorizedUser")
  @XmlElementWrapper(name = "authorizedUserSet")
  @XmlJavaTypeAdapter(value = UserXmlAdapter.class)
  private Set<User> authorizedUserSet;

  @XmlElement(name = "authorizedGroup")
  @XmlElementWrapper(name = "authorizedGroupSet")
  @XmlJavaTypeAdapter(value = GroupXmlAdapter.class)
  private Set<Group> authorizedGroupSet;

  private Boolean generateProduct = Boolean.TRUE;

  private String qtyFormula;

  private Boolean isActive = Boolean.FALSE;

  private Boolean copyNeedingUpdate = Boolean.FALSE;

  private String attrs;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MetaJsonField> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<MetaJsonField> attributes) {
    this.attributes = attributes;
  }

  public List<ConfiguratorProductFormula> getConfiguratorProductFormulaList() {
    return configuratorProductFormulaList;
  }

  public void setConfiguratorProductFormulaList(
      List<ConfiguratorProductFormula> configuratorProductFormulaList) {
    this.configuratorProductFormulaList = configuratorProductFormulaList;
  }

  public List<ConfiguratorSOLineFormula> getConfiguratorSOLineFormulaList() {
    return configuratorSOLineFormulaList;
  }

  public void setConfiguratorSOLineFormulaList(
      List<ConfiguratorSOLineFormula> configuratorSOLineFormulaList) {
    this.configuratorSOLineFormulaList = configuratorSOLineFormulaList;
  }

  public Set<User> getAuthorizedUserSet() {
    return authorizedUserSet;
  }

  public void setAuthorizedUserSet(Set<User> authorizedUserSet) {
    this.authorizedUserSet = authorizedUserSet;
  }

  public Set<Group> getAuthorizedGroupSet() {
    return authorizedGroupSet;
  }

  public void setAuthorizedGroupSet(Set<Group> authorizedGroupSet) {
    this.authorizedGroupSet = authorizedGroupSet;
  }

  public Boolean getGenerateProduct() {
    return generateProduct;
  }

  public void setGenerateProduct(Boolean generateProduct) {
    this.generateProduct = generateProduct;
  }

  public String getQtyFormula() {
    return qtyFormula;
  }

  public void setQtyFormula(String qtyFormula) {
    this.qtyFormula = qtyFormula;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public Boolean getCopyNeedingUpdate() {
    return copyNeedingUpdate;
  }

  public void setCopyNeedingUpdate(Boolean copyNeedingUpdate) {
    this.copyNeedingUpdate = copyNeedingUpdate;
  }

  public String getAttrs() {
    return attrs;
  }

  public void setAttrs(String attrs) {
    this.attrs = attrs;
  }
}
