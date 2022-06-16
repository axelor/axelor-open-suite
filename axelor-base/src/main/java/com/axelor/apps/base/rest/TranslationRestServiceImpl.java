package com.axelor.apps.base.rest;

import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Iterator;
import java.util.List;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class TranslationRestServiceImpl implements TranslationRestService {

  @Inject private MetaTranslationRepository translationRepo;

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Integer createNewTranslation(JSONObject translationFile, String language)
      throws AxelorException {
    try {
      Iterator<String> keys = translationFile.keys();
      int addedTranslation = 0;

      while (keys.hasNext()) {
        String currentKey = keys.next();

        Query<MetaTranslation> query =
            translationRepo.all().filter("self.language = :language " + "AND self.key LIKE :key");
        query.bind("language", language);
        query.bind("key", "mobile_app_" + currentKey);

        List<MetaTranslation> translationList = query.fetch();
        if (translationList.size() == 0) {
          MetaTranslation newTranslation = new MetaTranslation();
          newTranslation.setKey("mobile_app_" + currentKey);
          newTranslation.setMessage((String) translationFile.get(currentKey));
          newTranslation.setLanguage(language);
          translationRepo.save(newTranslation);
          addedTranslation++;
        }
      }
      return addedTranslation;
    } catch (JSONException e) {
      throw new AxelorException();
    }
  }
}
