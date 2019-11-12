# 3.0.4 (2019-11-12)
## Bug Fixes
	
- USER: improve full name computation.
- Invoice: fix filter problem on invoice operation type selection.

# 3.0.3 (2017-02-28)
## Bug Fixes
	
- Fixed issue on the method to get the next period
- Changed display of CFONB field, by using nested, on AccontConfig
- Restore client view wizard action
- Updated translations of Invoice and SaleOrder report
- Fixed issue on sequence assignment on SaleOrder
- CRM Calls dashboard fix
- Unused actions removed
- Sequence onNew fix: nextNum removed

# 3.0.2 (2015-09-09)
## Bug Fixes
- Domain on Partner account in Invoice
- JPA context of the project during generation of invoice
- Removed unused selection for social network
- Fixed issue on Paybox WS
- Fill the language on partner/contact when we convert lead
- Improve Business, Project and Task views
- Fixed some issue on Target management
- Fixed some issue on Expense
- Manufacturing dashboard 
- Reset value on AccountEquiv
- Fixed some index name
- Fixed issue with sequence called twice on SaleOrder
- Fixed some issue with conversion of lead
- Contact dashboard

## Improvements
- Sequence management
- Management of number of decimal for unit price
- Company logo became a MetaFile instead of a path
- MoveLine and move is generated during ventilation of invoice only if the amount on line or invoice is not null.
- Controle on sequence and date for customer invoice only
- Improve translations for printing (Purchase Order) and Lead
- Attribute 'x-show-titles' updated in 'editor' according ADK improvement
- Change management of manageCustomerCredit field
- No check of account config if amount if null on a line of invoice during ventilation
- Per default, translation doesn't contains the context, according ADK improvement

# 3.0.1 (2015-05-13)
## Bug Fixes
- Fixed somes issues

## Improvements
- Sequence management
- Message management


# 3.0.0 (2015-01-21)
Fully responsive mobile ready views, gradle based build system and much more.

## Features
- migrated to gradle build system
- fully responsive mobile ready views
- Split object per modules
- Customer Relationship Management
- Sales management
- Financial and cost management
- Human Resource Management
- Project Management
- Inventory and Supply Chain Management
- Production Management
- Multi-company, multi-currency and multi-lingual
