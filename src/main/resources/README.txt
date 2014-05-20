#  AXELOR ERP DATA IMPORT #

We are using language specific data import. Use following steps to import demo data.

** File organisation **
1. XML configuration file is common for EN/FR data(csv-config_demo.xml). 
2. Some configuration xml file use to update imported data.
   Example: 'csv-config_meta_update.xml'(meta is module name here).
3. All csv files must follow "module_domainNAME.csv" and it should be in their particular directory (demo_EN/demoFR).
   Example: 'base_partner.csv","base_partnerCategory.csv"
   
** Attachments(images/documents) configuration in csv ***
1. Use 'meta_file.csv' to specify attachment's name and path(Under main attachment directory specified in application.properties).
   Example : fileName='20_120px.png'             filePath='partner/20_120px.png'
                      (IMPORTID_RESOLUTIONpx.png)         (OBJECT NAME/FILENAME)
   Naming convention used for for file name and file path are not compulsory.  
   Here full path will be = "file.upload.dir"(from application.properties)+"partner/20_120px.png"
2.To use attachment specified in meta_file.csv use 'metaFileFiled(m2o).fileName'.
   Example: base_partner.csv -> 'partner.fileName'  

** DATA IMPORT ***
1. Go to directory "axelor-erp/axelor-data" from terminal.

2. Specify correct database name in "persistence.xml" of axelor-data module.

3. Run command "./axelor-data.sh -c src/main/resources/config_files/csv-config_demo.xml  -d src/main/resources/data/demo_FR"
   Here -c option specify configuration xml file and -d option is for csv file directory.
   
4. After successful import, install modules. 
   Example: To install module axelor-base and axelor-account use following . 
   mvn -q exec:java -Dexec.mainClass="com.axelor.cli.Main" -Dexec.args="-p persistenceUnit -u -m axelor-base,axelor-account"
 
5. To use/update MetaObjects(MetaMenu,MetaModel..etc) run import with "csv-config_meta_update.xml".
   "./axelor-data.sh -c src/main/resources/config_files/csv-config_meta_update.xml  -d src/main/resources/data/demo_FR"
   
   
   
 