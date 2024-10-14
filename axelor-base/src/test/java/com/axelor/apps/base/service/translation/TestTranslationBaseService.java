package com.axelor.apps.base.service.translation;

import static com.axelor.apps.base.service.LanguageTestTools.createLanguages;
import static com.axelor.apps.base.service.LocalizationTestTools.createLocalizations;
import static com.axelor.apps.base.service.translation.TranslationTestTool.assertTranslationsEquals;
import static com.axelor.apps.base.service.translation.TranslationTestTool.createMetaTranslation;
import static com.axelor.apps.base.service.translation.TranslationTestTool.createMetaTranslations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.apps.base.service.language.LanguageCheckerService;
import com.axelor.apps.base.service.language.LanguageCheckerServiceImpl;
import com.axelor.apps.base.service.localization.LocalizationService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.utils.service.TranslationService;
import com.axelor.utils.service.translation.TranslationBaseServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

public class TestTranslationBaseService {

  private static TranslationBaseServiceImpl translationBaseService;
  private static List<Localization> localizationList = new ArrayList<>();
  private static List<Language> languageList = new ArrayList<>();
  private static List<MetaTranslation> metaTranslationList = new ArrayList<>();

  @BeforeAll
  public static void prepare() throws AxelorException {
    createMetaTranslations("", metaTranslationList);
    createMetaTranslations("mobile_app_", metaTranslationList);
    createLanguages(languageList);
    createLocalizations(localizationList);

    UserService userService = mock(UserService.class);
    TranslationService translationService = mock(TranslationService.class);
    MetaTranslationRepository metaTranslationRepository = mock(MetaTranslationRepository.class);
    LocalizationService localizationService = mock(LocalizationService.class);
    when(localizationService.getLocalization(any()))
        .then(
            (Answer<Localization>)
                invocation ->
                    localizationList.stream()
                        .filter(loc -> Objects.equals(loc.getCode(), invocation.getArguments()[0]))
                        .findFirst()
                        .orElse(null));
    LanguageCheckerService languageCheckerService = new LanguageCheckerServiceImpl();
    LanguageRepository languageRepository = mock(LanguageRepository.class);
    when(languageRepository.findByCode(any()))
        .then(
            (Answer<Language>)
                invocation ->
                    languageList.stream()
                        .filter(l -> l.getCode().equals(invocation.getArguments()[0]))
                        .findFirst()
                        .orElse(null));

    translationBaseService =
        new TranslationBaseServiceImpl(
            userService,
            translationService,
            metaTranslationRepository,
            localizationService,
            languageCheckerService,
            languageRepository) {
          @Override
          protected List<MetaTranslation> getTranslations(String language, String key) {
            return metaTranslationList.stream()
                .filter(
                    mt -> Objects.equals(mt.getLanguage(), language) && mt.getKey().contains(key))
                .collect(Collectors.toList());
          }
        };
  }

  @Test
  void testGetLocalizationTranslationsFR() throws AxelorException {
    List<MetaTranslation> expectedList = new ArrayList<>();
    expectedList.add(createMetaTranslation("fr_1", "fr", "fr_1"));
    expectedList.add(createMetaTranslation("fr_2", "fr", "fr_2"));
    expectedList.add(createMetaTranslation("fr_3", "fr", "fr_3"));
    expectedList.add(createMetaTranslation("mobile_app_fr_1", "fr", "mobile_app_fr_1"));
    expectedList.add(createMetaTranslation("mobile_app_fr_2", "fr", "mobile_app_fr_2"));
    expectedList.add(createMetaTranslation("mobile_app_fr_3", "fr", "mobile_app_fr_3"));

    List<MetaTranslation> result = translationBaseService.getLocalizationTranslations("fr", "");
    assertTranslationsEquals(expectedList, result);
  }

  @Test
  void testGetLocalizationTranslationsEN() throws AxelorException {
    List<MetaTranslation> expectedList = new ArrayList<>();
    expectedList.add(createMetaTranslation("en_1", "en", "en_1"));
    expectedList.add(createMetaTranslation("en_2", "en", "en_2"));
    expectedList.add(createMetaTranslation("en_3", "en", "en_3"));
    expectedList.add(createMetaTranslation("mobile_app_en_1", "en", "mobile_app_en_1"));
    expectedList.add(createMetaTranslation("mobile_app_en_2", "en", "mobile_app_en_2"));
    expectedList.add(createMetaTranslation("mobile_app_en_3", "en", "mobile_app_en_3"));

    List<MetaTranslation> result = translationBaseService.getLocalizationTranslations("en", "");
    assertTranslationsEquals(expectedList, result);
  }

  @Test
  void testGetLocalizationTranslationsTest() {
    Assertions.assertThrows(
        NotFoundException.class,
        () -> translationBaseService.getLocalizationTranslations("test", ""));
  }

  @Test
  void testGetMobileAppLocalizationTranslationsFR_CA() throws AxelorException {
    List<MetaTranslation> expectedList = new ArrayList<>();
    expectedList.add(createMetaTranslation("mobile_app_fr_1", "fr", "mobile_app_fr_1"));
    expectedList.add(createMetaTranslation("mobile_app_fr_3", "fr", "mobile_app_fr_3"));
    expectedList.add(createMetaTranslation("mobile_app_fr_2", "fr-CA", "mobile_app_fr_CA_2"));

    List<MetaTranslation> result =
        translationBaseService.getLocalizationTranslations("fr-CA", "mobile_app_");
    assertTranslationsEquals(expectedList, result);
  }

  @Test
  void testGetMobileAppLocalizationTranslationsFR_CA2() throws AxelorException {
    List<MetaTranslation> expectedList = new ArrayList<>();
    expectedList.add(createMetaTranslation("mobile_app_fr_1", "fr", "mobile_app_fr_1"));
    expectedList.add(createMetaTranslation("mobile_app_fr_3", "fr", "mobile_app_fr_3"));
    expectedList.add(createMetaTranslation("mobile_app_fr_2", "fr-CA", "mobile_app_fr_CA_2"));

    List<MetaTranslation> result =
        translationBaseService.getLocalizationTranslations("fr_CA", "mobile_app_");
    assertTranslationsEquals(expectedList, result);
  }

  @Test
  void testGetMobileAppLanguageTranslationsFR() throws AxelorException {
    List<MetaTranslation> expectedList = new ArrayList<>();
    expectedList.add(createMetaTranslation("mobile_app_fr_1", "fr", "mobile_app_fr_1"));
    expectedList.add(createMetaTranslation("mobile_app_fr_2", "fr", "mobile_app_fr_2"));
    expectedList.add(createMetaTranslation("mobile_app_fr_3", "fr", "mobile_app_fr_3"));

    List<MetaTranslation> result =
        translationBaseService.getLocalizationTranslations("fr", "mobile_app_");
    assertTranslationsEquals(expectedList, result);
  }

  @Test
  void testGetMobileAppLanguageTranslationsEN() throws AxelorException {
    List<MetaTranslation> expectedList = new ArrayList<>();
    expectedList.add(createMetaTranslation("mobile_app_en_1", "fr", "mobile_app_en_1"));
    expectedList.add(createMetaTranslation("mobile_app_en_2", "fr", "mobile_app_en_2"));
    expectedList.add(createMetaTranslation("mobile_app_en_3", "fr", "mobile_app_en_3"));

    List<MetaTranslation> result =
        translationBaseService.getLocalizationTranslations("en", "mobile_app_");
    assertTranslationsEquals(expectedList, result);
  }

  @Test
  void testGetMobileAppLocalizationTranslationsTest() {
    Assertions.assertThrows(
        NotFoundException.class,
        () -> translationBaseService.getLocalizationTranslations("test", "mobile_app_"));
  }
}
