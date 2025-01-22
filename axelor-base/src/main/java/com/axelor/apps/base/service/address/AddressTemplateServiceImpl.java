/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service.address;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AddressTemplate;
import com.axelor.apps.base.db.AddressTemplateLine;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.repo.AddressTemplateRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.EntityHelper;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.rpc.Context;
import com.axelor.text.GroovyTemplates;
import com.axelor.text.StringTemplates;
import com.axelor.text.Templates;
import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddressTemplateServiceImpl implements AddressTemplateService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected final String EMPTY_LINE_REMOVAL_REGEX = "(?m)^\\s*$(\\n|\\r\\n)";
  private static final char TEMPLATE_DELIMITER = '$';
  protected GroovyTemplates groovyTemplates;

  @Inject
  public AddressTemplateServiceImpl(GroovyTemplates groovyTemplates) {
    this.groovyTemplates = groovyTemplates;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void setFormattedFullName(Address address) throws AxelorException {
    Country country = address.getCountry();
    if (country == null) {
      return;
    }

    AddressTemplate addressTemplate = country.getAddressTemplate();
    setFormattedAddressField(addressTemplate.getAddressL2Str(), address, address::setAddressL2);
    setFormattedAddressField(addressTemplate.getAddressL3Str(), address, address::setAddressL3);
    setFormattedAddressField(addressTemplate.getAddressL4Str(), address, address::setAddressL4);
    setFormattedAddressField(addressTemplate.getAddressL5Str(), address, address::setAddressL5);
    setFormattedAddressField(addressTemplate.getAddressL6Str(), address, address::setAddressL6);
    setFormattedAddressField(
        addressTemplate.getTemplateStr(), address, address::setFormattedFullName);
  }

  protected void setFormattedAddressField(
      String contentTemplateStr, Address address, Consumer<String> setter) throws AxelorException {
    String formattedString = computeTemplateStr(contentTemplateStr, address);
    setter.accept(formattedString);
  }

  protected String computeTemplateStr(String content, Address address) throws AxelorException {
    AddressTemplate addressTemplate = address.getCountry().getAddressTemplate();
    try {
      Templates templates;
      if (addressTemplate.getEngineSelect() == AddressTemplateRepository.GROOVY_TEMPLATE) {
        templates = this.groovyTemplates;
      } else {
        templates = new StringTemplates(TEMPLATE_DELIMITER, TEMPLATE_DELIMITER);
      }

      Map<String, Object> templatesContext = Maps.newHashMap();
      Class<?> klass = EntityHelper.getEntityClass(address);
      Context context = new Context(Mapper.toMap(address), klass);
      templatesContext.put(klass.getSimpleName(), context.asType(klass));
      String computedString = templates.fromText(content).make(templatesContext).render();
      if (computedString == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                I18n.get(BaseExceptionMessage.ADDRESS_FIELD_TEMPLATE_ERROR),
                addressTemplate.getName(),
                content));
      }
      computedString = computedString.trim();
      computedString = computedString.replaceAll(EMPTY_LINE_REMOVAL_REGEX, "");
      return computedString;

    } catch (Exception e) {
      log.error("Runtime Exception Address: {} - {}", addressTemplate.getName(), content);
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }
  }

  @Override
  public void checkRequiredAddressFields(Address address) throws AxelorException {
    if (address.getCountry() != null) {
      AddressTemplate addressTemplate = address.getCountry().getAddressTemplate();
      for (AddressTemplateLine addressTemplateLine : addressTemplate.getAddressTemplateLineList()) {
        if (addressTemplateLine.getIsRequired()) {
          // Assuming field name is stored in addressTemplateLine.getFieldName()
          String fieldName = addressTemplateLine.getMetaField().getName();
          Object fieldValue = null;
          try {
            fieldValue = Mapper.of(address.getClass()).getGetter(fieldName).invoke(address);
          } catch (InvocationTargetException | IllegalAccessException e) {
            throw new AxelorException(
                addressTemplateLine,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                String.format(
                    I18n.get(BaseExceptionMessage.MISSING_ADDRESS_FIELD),
                    addressTemplateLine.getMetaField().getName()));
          }
          if (fieldValue == null) {
            throw new AxelorException(
                addressTemplateLine,
                TraceBackRepository.CATEGORY_MISSING_FIELD,
                String.format(
                    I18n.get(BaseExceptionMessage.MISSING_ADDRESS_FIELD),
                    addressTemplateLine.getMetaField().getName()));
          }
        }
      }
    }
  }
}
