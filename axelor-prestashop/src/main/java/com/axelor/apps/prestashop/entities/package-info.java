@XmlJavaTypeAdapters({
	@XmlJavaTypeAdapter(type=boolean.class, value=PrestashopBooleanAdapter.class),
	@XmlJavaTypeAdapter(type=LocalDateTime.class, value=PrestashopLocalDateTimeAdapter.class)
})
package com.axelor.apps.prestashop.entities;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;

import com.axelor.apps.prestashop.adapters.PrestashopBooleanAdapter;
import com.axelor.apps.prestashop.adapters.PrestashopLocalDateTimeAdapter;