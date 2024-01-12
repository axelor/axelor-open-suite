/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.module;

import com.axelor.app.AppSettings;
import com.axelor.app.AxelorModule;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.ABCAnalysisBaseRepository;
import com.axelor.apps.base.db.repo.ABCAnalysisRepository;
import com.axelor.apps.base.db.repo.AddressBaseRepository;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AdvancedImportBaseRepository;
import com.axelor.apps.base.db.repo.AdvancedImportRepository;
import com.axelor.apps.base.db.repo.AlarmEngineBatchBaseRepository;
import com.axelor.apps.base.db.repo.AlarmEngineBatchRepository;
import com.axelor.apps.base.db.repo.BankAddressBaseRepository;
import com.axelor.apps.base.db.repo.BankAddressRepository;
import com.axelor.apps.base.db.repo.BankBaseRepository;
import com.axelor.apps.base.db.repo.BankRepository;
import com.axelor.apps.base.db.repo.BaseBatchBaseRepository;
import com.axelor.apps.base.db.repo.BaseBatchRepository;
import com.axelor.apps.base.db.repo.DataBackupManagementRepository;
import com.axelor.apps.base.db.repo.DataBackupRepository;
import com.axelor.apps.base.db.repo.DurationBaseRepository;
import com.axelor.apps.base.db.repo.DurationRepository;
import com.axelor.apps.base.db.repo.ICalendarEventManagementRepository;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.base.db.repo.ImportConfigurationBaseRepository;
import com.axelor.apps.base.db.repo.ImportConfigurationRepository;
import com.axelor.apps.base.db.repo.MailBatchBaseRepository;
import com.axelor.apps.base.db.repo.MailBatchRepository;
import com.axelor.apps.base.db.repo.MailingListMessageBaseRepository;
import com.axelor.apps.base.db.repo.MailingListMessageRepository;
import com.axelor.apps.base.db.repo.ObjectDataConfigExportManagementRepository;
import com.axelor.apps.base.db.repo.ObjectDataConfigExportRepository;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerBaseRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductBaseRepository;
import com.axelor.apps.base.db.repo.ProductCompanyBaseRepository;
import com.axelor.apps.base.db.repo.ProductCompanyRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.SequenceBaseRepository;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TaxBaseRepository;
import com.axelor.apps.base.db.repo.TeamTaskBaseRepository;
import com.axelor.apps.base.db.repo.UserBaseRepository;
import com.axelor.apps.base.db.repo.YearBaseRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.listener.BaseServerStartListener;
import com.axelor.apps.base.openapi.AosSwagger;
import com.axelor.apps.base.rest.TranslationRestService;
import com.axelor.apps.base.rest.TranslationRestServiceImpl;
import com.axelor.apps.base.service.ABCAnalysisService;
import com.axelor.apps.base.service.ABCAnalysisServiceImpl;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.base.service.AddressServiceImpl;
import com.axelor.apps.base.service.AnonymizeService;
import com.axelor.apps.base.service.AnonymizeServiceImpl;
import com.axelor.apps.base.service.BankDetailsFullNameComputeService;
import com.axelor.apps.base.service.BankDetailsFullNameComputeServiceImpl;
import com.axelor.apps.base.service.BankDetailsService;
import com.axelor.apps.base.service.BankDetailsServiceImpl;
import com.axelor.apps.base.service.BankService;
import com.axelor.apps.base.service.BankServiceImpl;
import com.axelor.apps.base.service.BarcodeGeneratorService;
import com.axelor.apps.base.service.BarcodeGeneratorServiceImpl;
import com.axelor.apps.base.service.BaseReportGenerator;
import com.axelor.apps.base.service.CompanyService;
import com.axelor.apps.base.service.CompanyServiceImpl;
import com.axelor.apps.base.service.DMSImportWizardService;
import com.axelor.apps.base.service.DMSImportWizardServiceImpl;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.DMSServiceImpl;
import com.axelor.apps.base.service.DataBackupAnonymizeService;
import com.axelor.apps.base.service.DataBackupAnonymizeServiceImpl;
import com.axelor.apps.base.service.DataBackupService;
import com.axelor.apps.base.service.DataBackupServiceImpl;
import com.axelor.apps.base.service.DurationService;
import com.axelor.apps.base.service.DurationServiceImpl;
import com.axelor.apps.base.service.FakerService;
import com.axelor.apps.base.service.FakerServiceImpl;
import com.axelor.apps.base.service.FrequencyService;
import com.axelor.apps.base.service.FrequencyServiceImpl;
import com.axelor.apps.base.service.InternationalService;
import com.axelor.apps.base.service.InternationalServiceImpl;
import com.axelor.apps.base.service.MailServiceBaseImpl;
import com.axelor.apps.base.service.MapRestService;
import com.axelor.apps.base.service.MapRestServiceImpl;
import com.axelor.apps.base.service.ModelEmailLinkService;
import com.axelor.apps.base.service.ModelEmailLinkServiceImpl;
import com.axelor.apps.base.service.ObjectDataAnonymizeService;
import com.axelor.apps.base.service.ObjectDataAnonymizeServiceImpl;
import com.axelor.apps.base.service.ObjectDataExportService;
import com.axelor.apps.base.service.ObjectDataExportServiceImpl;
import com.axelor.apps.base.service.PartnerPriceListService;
import com.axelor.apps.base.service.PartnerPriceListServiceImpl;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.PartnerServiceImpl;
import com.axelor.apps.base.service.PaymentModeService;
import com.axelor.apps.base.service.PaymentModeServiceImpl;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.PeriodServiceImpl;
import com.axelor.apps.base.service.PricedOrderDomainService;
import com.axelor.apps.base.service.PricedOrderDomainServiceImpl;
import com.axelor.apps.base.service.ProductCategoryDomainCreatorService;
import com.axelor.apps.base.service.ProductCategoryDomainCreatorServiceImpl;
import com.axelor.apps.base.service.ProductCategoryService;
import com.axelor.apps.base.service.ProductCategoryServiceImpl;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.ProductCompanyServiceImpl;
import com.axelor.apps.base.service.ProductConversionService;
import com.axelor.apps.base.service.ProductConversionServiceImpl;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.ProductMultipleQtyServiceImpl;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.ProductServiceImpl;
import com.axelor.apps.base.service.ProductUpdateService;
import com.axelor.apps.base.service.ProductUpdateServiceImpl;
import com.axelor.apps.base.service.ProductVariantService;
import com.axelor.apps.base.service.ProductVariantServiceImpl;
import com.axelor.apps.base.service.TeamTaskService;
import com.axelor.apps.base.service.TeamTaskServiceImpl;
import com.axelor.apps.base.service.TradingNameService;
import com.axelor.apps.base.service.TradingNameServiceImpl;
import com.axelor.apps.base.service.YearService;
import com.axelor.apps.base.service.YearServiceImpl;
import com.axelor.apps.base.service.administration.SequenceVersionGeneratorQueryService;
import com.axelor.apps.base.service.administration.SequenceVersionGeneratorQueryServiceImpl;
import com.axelor.apps.base.service.administration.SequenceVersionGeneratorService;
import com.axelor.apps.base.service.administration.SequenceVersionGeneratorServiceImpl;
import com.axelor.apps.base.service.advanced.imports.ActionService;
import com.axelor.apps.base.service.advanced.imports.ActionServiceImpl;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportService;
import com.axelor.apps.base.service.advanced.imports.AdvancedImportServiceImpl;
import com.axelor.apps.base.service.advanced.imports.DataImportService;
import com.axelor.apps.base.service.advanced.imports.DataImportServiceImpl;
import com.axelor.apps.base.service.advanced.imports.FileFieldService;
import com.axelor.apps.base.service.advanced.imports.FileFieldServiceImpl;
import com.axelor.apps.base.service.advanced.imports.FileTabService;
import com.axelor.apps.base.service.advanced.imports.FileTabServiceImpl;
import com.axelor.apps.base.service.advanced.imports.SearchCallService;
import com.axelor.apps.base.service.advanced.imports.SearchCallServiceImpl;
import com.axelor.apps.base.service.advancedExport.AdvancedExportService;
import com.axelor.apps.base.service.advancedExport.AdvancedExportServiceImpl;
import com.axelor.apps.base.service.api.ResponseComputeService;
import com.axelor.apps.base.service.api.ResponseComputeServiceImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.axelor.apps.base.service.exception.HandleExceptionResponse;
import com.axelor.apps.base.service.exception.HandleExceptionResponseImpl;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorService;
import com.axelor.apps.base.service.filesourceconnector.FileSourceConnectorServiceImpl;
import com.axelor.apps.base.service.imports.ConvertDemoDataFileService;
import com.axelor.apps.base.service.imports.ConvertDemoDataFileServiceImpl;
import com.axelor.apps.base.service.imports.ImportCityService;
import com.axelor.apps.base.service.imports.ImportCityServiceImpl;
import com.axelor.apps.base.service.imports.ImportDemoDataService;
import com.axelor.apps.base.service.imports.ImportDemoDataServiceImpl;
import com.axelor.apps.base.service.message.MailAccountServiceBaseImpl;
import com.axelor.apps.base.service.message.MessageBaseService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.base.service.pac4j.BaseAuthPac4jUserService;
import com.axelor.apps.base.service.pricing.PricingService;
import com.axelor.apps.base.service.pricing.PricingServiceImpl;
import com.axelor.apps.base.service.print.PrintHtmlGenerationService;
import com.axelor.apps.base.service.print.PrintHtmlGenerationServiceImpl;
import com.axelor.apps.base.service.print.PrintPdfGenerationService;
import com.axelor.apps.base.service.print.PrintPdfGenerationServiceImpl;
import com.axelor.apps.base.service.print.PrintService;
import com.axelor.apps.base.service.print.PrintServiceImpl;
import com.axelor.apps.base.service.print.PrintTemplateLineService;
import com.axelor.apps.base.service.print.PrintTemplateLineServiceImpl;
import com.axelor.apps.base.service.print.PrintTemplateService;
import com.axelor.apps.base.service.print.PrintTemplateServiceImpl;
import com.axelor.apps.base.service.tax.AccountManagementService;
import com.axelor.apps.base.service.tax.AccountManagementServiceImpl;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.FiscalPositionServiceImpl;
import com.axelor.apps.base.service.tax.TaxEquivService;
import com.axelor.apps.base.service.tax.TaxEquivServiceImpl;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningService;
import com.axelor.apps.base.service.weeklyplanning.WeeklyPlanningServiceImp;
import com.axelor.apps.base.tracking.ExportObserver;
import com.axelor.apps.base.tracking.GlobalAuditInterceptor;
import com.axelor.apps.base.tracking.GlobalTrackingLogService;
import com.axelor.apps.base.tracking.GlobalTrackingLogServiceImpl;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.auth.pac4j.AuthPac4jUserService;
import com.axelor.base.service.ical.ICalendarEventService;
import com.axelor.base.service.ical.ICalendarEventServiceImpl;
import com.axelor.message.service.MailAccountServiceImpl;
import com.axelor.message.service.MailServiceMessageImpl;
import com.axelor.message.service.MessageServiceImpl;
import com.axelor.message.service.TemplateMessageServiceImpl;
import com.axelor.report.ReportGenerator;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.app.service.AppService;
import com.axelor.studio.app.service.AppServiceImpl;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.matcher.Matchers;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

public class BaseModule extends AxelorModule {

  @Override
  protected void configure() {
    bindInterceptor(
        Matchers.any(),
        Matchers.annotatedWith(HandleExceptionResponse.class),
        new HandleExceptionResponseImpl());

    bindInterceptor(
        new AbstractMatcher<>() {
          @Override
          public boolean matches(Class<?> aClass) {
            return aClass.getSimpleName().endsWith("Controller")
                && aClass.getPackageName().startsWith("com.axelor.apps")
                && aClass.getPackageName().endsWith("web");
          }
        },
        new AbstractMatcher<>() {
          @Override
          public boolean matches(Method method) {
            List<Parameter> parameters = Arrays.asList(method.getParameters());
            return parameters.size() == 2
                && parameters.stream()
                    .anyMatch(parameter -> ActionRequest.class.equals(parameter.getType()))
                && parameters.stream()
                    .anyMatch(parameter -> ActionResponse.class.equals(parameter.getType()));
          }
        },
        new ControllerMethodInterceptor());

    bind(AddressService.class).to(AddressServiceImpl.class);
    bind(AdvancedExportService.class).to(AdvancedExportServiceImpl.class);
    bind(UserService.class).to(UserServiceImpl.class);
    bind(MessageServiceImpl.class).to(MessageServiceBaseImpl.class);
    bind(MessageBaseService.class).to(MessageServiceBaseImpl.class);
    bind(MailAccountServiceImpl.class).to(MailAccountServiceBaseImpl.class);
    bind(AccountManagementService.class).to(AccountManagementServiceImpl.class);
    bind(FiscalPositionService.class).to(FiscalPositionServiceImpl.class);
    bind(ProductService.class).to(ProductServiceImpl.class);
    bind(TemplateMessageServiceImpl.class).to(TemplateMessageServiceBaseImpl.class);
    bind(PartnerRepository.class).to(PartnerBaseRepository.class);
    bind(DurationRepository.class).to(DurationBaseRepository.class);
    bind(DurationService.class).to(DurationServiceImpl.class);
    bind(AppBaseService.class).to(AppBaseServiceImpl.class);
    bind(SequenceRepository.class).to(SequenceBaseRepository.class);
    bind(ProductRepository.class).to(ProductBaseRepository.class);
    bind(WeeklyPlanningService.class).to(WeeklyPlanningServiceImp.class);
    bind(MailServiceMessageImpl.class).to(MailServiceBaseImpl.class);
    bind(AddressRepository.class).to(AddressBaseRepository.class);
    bind(YearRepository.class).to(YearBaseRepository.class);
    bind(YearService.class).to(YearServiceImpl.class);
    bind(AppServiceImpl.class).to(AppBaseServiceImpl.class);
    bind(AppService.class).to(AppServiceImpl.class);
    bind(BankService.class).to(BankServiceImpl.class);
    bind(BankRepository.class).to(BankBaseRepository.class);
    bind(CompanyService.class).to(CompanyServiceImpl.class);
    bind(BankAddressRepository.class).to(BankAddressBaseRepository.class);
    bind(UserRepository.class).to(UserBaseRepository.class);
    bind(BankDetailsService.class).to(BankDetailsServiceImpl.class);
    bind(ImportCityService.class).to(ImportCityServiceImpl.class);
    bind(BaseBatchRepository.class).to(BaseBatchBaseRepository.class);
    bind(MailBatchRepository.class).to(MailBatchBaseRepository.class);
    bind(AlarmEngineBatchRepository.class).to(AlarmEngineBatchBaseRepository.class);
    bind(TradingNameService.class).to(TradingNameServiceImpl.class);
    bind(PartnerPriceListService.class).to(PartnerPriceListServiceImpl.class);
    bind(ICalendarEventService.class).to(ICalendarEventServiceImpl.class);
    bind(ICalendarEventRepository.class).to(ICalendarEventManagementRepository.class);
    bind(ProductMultipleQtyService.class).to(ProductMultipleQtyServiceImpl.class);
    bind(BarcodeGeneratorService.class).to(BarcodeGeneratorServiceImpl.class);
    PartnerAddressRepository.modelPartnerFieldMap.put(PartnerAddress.class.getName(), "_parent");
    bind(PeriodService.class).to(PeriodServiceImpl.class);
    bind(ConvertDemoDataFileService.class).to(ConvertDemoDataFileServiceImpl.class);
    bind(ImportDemoDataService.class).to(ImportDemoDataServiceImpl.class);
    bind(MapRestService.class).to(MapRestServiceImpl.class);
    bind(TaxRepository.class).to(TaxBaseRepository.class);
    bind(TeamTaskRepository.class).to(TeamTaskBaseRepository.class);
    bind(TeamTaskService.class).to(TeamTaskServiceImpl.class);
    bind(FrequencyService.class).to(FrequencyServiceImpl.class);
    bind(MailingListMessageRepository.class).to(MailingListMessageBaseRepository.class);
    bind(ABCAnalysisService.class).to(ABCAnalysisServiceImpl.class);
    bind(ABCAnalysisRepository.class).to(ABCAnalysisBaseRepository.class);
    bind(DMSImportWizardService.class).to(DMSImportWizardServiceImpl.class);
    bind(AdvancedImportService.class).to(AdvancedImportServiceImpl.class);
    bind(DataImportService.class).to(DataImportServiceImpl.class);
    bind(FileTabService.class).to(FileTabServiceImpl.class);
    bind(FileFieldService.class).to(FileFieldServiceImpl.class);
    bind(ActionService.class).to(ActionServiceImpl.class);
    bind(PartnerService.class).to(PartnerServiceImpl.class);
    bind(ProductCompanyService.class).to(ProductCompanyServiceImpl.class);
    bind(SearchCallService.class).to(SearchCallServiceImpl.class);
    bind(ProductCategoryService.class).to(ProductCategoryServiceImpl.class);
    bind(GlobalTrackingLogService.class).to(GlobalTrackingLogServiceImpl.class);
    if (AppSettings.get()
        .get("hibernate.session_factory.interceptor", "")
        .equals(GlobalAuditInterceptor.class.getName())) {
      bind(ExportObserver.class);
    }
    bind(ReportGenerator.class).to(BaseReportGenerator.class);
    bind(PrintTemplateService.class).to(PrintTemplateServiceImpl.class);
    bind(PrintService.class).to(PrintServiceImpl.class);
    bind(PrintTemplateLineService.class).to(PrintTemplateLineServiceImpl.class);
    bind(AdvancedImportRepository.class).to(AdvancedImportBaseRepository.class);
    bind(AuthPac4jUserService.class).to(BaseAuthPac4jUserService.class);
    bind(ImportConfigurationRepository.class).to(ImportConfigurationBaseRepository.class);
    bind(PaymentModeService.class).to(PaymentModeServiceImpl.class);
    bind(ModelEmailLinkService.class).to(ModelEmailLinkServiceImpl.class);
    bind(ProductVariantService.class).to(ProductVariantServiceImpl.class);
    bind(ProductCategoryDomainCreatorService.class)
        .to(ProductCategoryDomainCreatorServiceImpl.class);
    bind(FileSourceConnectorService.class).to(FileSourceConnectorServiceImpl.class);
    bind(PricingService.class).to(PricingServiceImpl.class);
    bind(PricedOrderDomainService.class).to(PricedOrderDomainServiceImpl.class);
    bind(InternationalService.class).to(InternationalServiceImpl.class);
    bind(SequenceVersionGeneratorService.class).to(SequenceVersionGeneratorServiceImpl.class);
    bind(SequenceVersionGeneratorQueryService.class)
        .to(SequenceVersionGeneratorQueryServiceImpl.class);
    bind(TranslationRestService.class).to(TranslationRestServiceImpl.class);
    bind(ObjectDataExportService.class).to(ObjectDataExportServiceImpl.class);
    bind(ObjectDataAnonymizeService.class).to(ObjectDataAnonymizeServiceImpl.class);
    bind(DataBackupService.class).to(DataBackupServiceImpl.class);
    bind(ObjectDataConfigExportRepository.class)
        .to(ObjectDataConfigExportManagementRepository.class);
    bind(AnonymizeService.class).to(AnonymizeServiceImpl.class);
    bind(FakerService.class).to(FakerServiceImpl.class);
    bind(DataBackupRepository.class).to(DataBackupManagementRepository.class);
    bind(DataBackupAnonymizeService.class).to(DataBackupAnonymizeServiceImpl.class);
    bind(DataBackupService.class).to(DataBackupServiceImpl.class);
    bind(BankDetailsFullNameComputeService.class).to(BankDetailsFullNameComputeServiceImpl.class);
    bind(BaseServerStartListener.class);
    bind(AosSwagger.class);
    bind(ProductUpdateService.class).to(ProductUpdateServiceImpl.class);
    bind(ProductConversionService.class).to(ProductConversionServiceImpl.class);
    bind(ResponseComputeService.class).to(ResponseComputeServiceImpl.class);
    bind(PrintHtmlGenerationService.class).to(PrintHtmlGenerationServiceImpl.class);
    bind(PrintPdfGenerationService.class).to(PrintPdfGenerationServiceImpl.class);
    bind(TaxEquivService.class).to(TaxEquivServiceImpl.class);
    bind(ProductCompanyRepository.class).to(ProductCompanyBaseRepository.class);
    bind(DMSService.class).to(DMSServiceImpl.class);
  }
}
