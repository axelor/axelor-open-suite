package com.axelor.apps.base.service.translation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.Localization;
import com.axelor.apps.base.service.language.LanguageCheckerService;
import com.axelor.apps.base.service.localization.LocalizationService;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.repo.MetaTranslationRepository;
import com.axelor.utils.service.TranslationService;
import com.axelor.utils.service.translation.TranslationBaseService;
import com.axelor.utils.service.translation.TranslationBaseServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.axelor.apps.base.service.translation.TranslationTestTool.generateMockTranslations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestTranslationBaseService {

    protected static TranslationBaseService translationBaseService;
    private static MetaTranslationRepository metaTranslationRepository;
    private static List<MetaTranslation> metaTranslationList;
    private static List<Localization> localizationList;

    @BeforeAll
    public static void prepare() throws AxelorException {
        metaTranslationList = generateMockTranslations();
        localizationList =
        UserService userService = mock(UserService.class);
        TranslationService translationService = mock(TranslationService.class);
        metaTranslationRepository = mock(MetaTranslationRepository.class);
        LocalizationService localizationService = mock(LocalizationService.class);
        LanguageCheckerService languageCheckerService = mock(LanguageCheckerService.class);
        translationBaseService = new TranslationBaseServiceImpl(userService, translationService, metaTranslationRepository, localizationService, languageCheckerService);

        when(localizationService.getLocalization(any())).then((Answer< Localization >) invocation -> localizationList.stream().filter(loc -> loc.getCode().equals((String) invocation.getArguments()[0])).findFirst().orElse(null));

    }

    @Test
    void testGetCountryTranslations() throws AxelorException {
        List<MetaTranslation> result = translationBaseService.getLocalizationTranslations("fr", "mobile_app_%");

        Assertions.assertEquals(metaTranslationList, result);
    }


    static List<Localization> generateMockLocalizations() {
        List<Localization> localizations = new ArrayList<>();
        Language languageFR = new Language("FR", "fr");
        Language languageEN = new Language("EN", "en");

        // FR localizations
        localizations.add(createLocalization("fr_FR", languageFR));
        localizations.add(createLocalization("fr_CA", languageFR));
        // EN localizations
        localizations.add(createLocalization("en_GB", languageEN));
        localizations.add(createLocalization("en_US", languageEN));
        localizations.add(createLocalization("en_CA", languageEN));

        return localizations;
    }

    static Localization createLocalization(String code, Language language) {
        Localization localization = new Localization();
        localization.setCode(code);
        localization.setLanguage(language);

        return localization;
    }
}
