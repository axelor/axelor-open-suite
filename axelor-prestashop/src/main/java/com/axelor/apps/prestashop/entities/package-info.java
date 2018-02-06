/**
 * PrestaShop isn't able to handle boolean if not serialized as 0/1…
 */

@XmlJavaTypeAdapter(type=boolean.class, value=PrestashopBooleanAdapter.class)
package com.axelor.apps.prestashop.entities;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.axelor.apps.prestashop.adapters.PrestashopBooleanAdapter;
