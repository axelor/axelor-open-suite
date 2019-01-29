# Changelog

## [Unreleased 5.2.0]
## Features
- Project : Addition of two new boolean fields 'isInvoicingExpenses' and 'isInvoicingPurchases'
- TeamTask : Adding new field 'projInvTypeSelect' and updates in selection list.
- Add new object 'Version' in business support module.
- TeamTask : New fields 'timeToCharge' and 'budgetedTime' added. Also filled default values for new items created from 'projectPlanningTimeList'.
- SaleOrder/Partner : Adding new fields for comments on invoices, sale orders , purchase orders and deliveries.
- Project : Add new object 'Annoucement'.
- Project : Onchange on Project.isInvoicingExpenses and Project.isInvoicingPurchases
- TeamTask : Added new fields "Private","internalDesription" and "orderAccepted".
- Axelor-Business-Support : Addition of new module 'axelor-business-support'.
- TeamTask : Addition of new field 'assignment' in bussiness-support module.
- TeamTask : Added 'Private','internalDesription' and 'orderAccepted' in bussiness-support module.
- Business Project module : Two new fileds in TeamTask and Project (TimeInvoicing / InvoicingType)
- TeamTask : Add new o2m 'projectPlanningTimeSpentList' field.
- Timesheet : Assign Task to lines when generating from Realise Planning. 
- Project : Added new O2M field 'announcementList' in bussiness-support module.
- TeamTask : Addition of new boolean 'isOrderProposed' in business-support module and label on isOrderAccepted.
- TeamTask : Make 'toInvoice' field hidden and set its value automatically from invoiceType.
- Timesheet : Assign toInvoice while generating timesheetLine from Realise Planning.
- TeamTask : Add button to enter spent time.
- TeamTask : Added action onClick of 'acceptOrderBtn' to set value for assignment
- Business Support Module : assigningProvider / assigningCustomer button and label added in TeamTask.
- Business support model : ProjectCategory / TeamTask objects - Adding defaultInvoicing field - Onchange TeamTask.projectCategory 
- TeamTask : Setting panel-mail mail-messages.
- Business Project module : Default value of TimesheetLine.toInvoice At the creation from Timesheet.timesheetLineList
- APP for axelor-business-support with a field 'providerCompany'.
- Stock Correction
- Business support module : New form/grid views of TeamTask for customers.
- Business Support module / Objet TeamTask : Menu entries for customers.Added new role(role.customer) in data-init.
- Employee:Company(Employee) Phonebook 
- User Form : Provide step wise view.
- TeamTask : Update teamtask client view. 
- Project : Default value for project on Wiki/Version/Announcement.
- Project : Divide planned and spent time project planning lines in seprate dashlets.
- Team Task : Add new field 'customerReferral'.
- TeamTask : Assign default value to fields and change in track of object.
- MANUF. APP : new boolean manageCostSheetGroup
- Business Project : new InvoicingProject menu and separate project-task-customer from menu-project-root 
- Project : Addition of M2M 'projectSet' in User object. 
- Purchase Request : Addition of M2M 'purchaseOrderSet'
- Invoicing project : Menu organisation

## Improvements
- TimesheetLine: Adding M2O field TeamTask and integer field timeToCharge.
- Project : adding new O2M field Roadmap and panel Tab Roadmap.
- HR Module : Timesheet - Rename Action Title in "timesheet-form".
- Studio : Allowing to export all data without selecting any app builder.
- Studio: Custom model editor - Added title property for model and removed required condition for AppBuilder. 
- Project : Display task field on logTimesPanel.
- Project : Default value set for ProjectPlanningTime.product.
- TeamTask : Change in team-task-form view. 
- TeamTask object : Change label "Task assigned to the provider".
- MENUS : new organisation in CRM and Sales modules
- Team Task : Change in team-task-form view

## Bug Fixes
- TeamTask : Resolve NPE on save of new team task.
- Studio: Fix import app without image. 
- TeamTask : Resolve NPE by updating action of HR module.
- Generation of Project/Phase from SaleOrder


[Unreleased 5.2.0]: https://github.com/axelor/axelor-business-suite/compare/dev...wip
