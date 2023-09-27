package com.axelor.apps.base.service;

import com.axelor.apps.base.db.Print;

public interface PrintHtmlGenerationService {
  String generateHtml(Print print, String attachmentPath);
}
