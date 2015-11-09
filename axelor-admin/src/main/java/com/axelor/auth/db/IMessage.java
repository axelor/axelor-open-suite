package com.axelor.auth.db;

public interface IMessage {
	
	/** Common messages **/
	static final public String IMPORT_OK = /*$$(*/ "Import completed succesfully" /*)*/;
	static final public String ERR_IMPORT = /*$$(*/ "Error in import. Please check log" /*)*/;
	
	/** Permission assistant & group menu assistant**/
	
	static final public String BAD_FILE = /*$$(*/ "Bad import file" /*)*/;
	static final public String NO_HEADER = /*$$(*/ "No header row found" /*)*/;
	static final public String BAD_HEADER = /*$$(*/ "Bad header row: " /*)*/;
	static final public String NO_GROUP = /*$$(*/ "Groups not found: %s" /*)*/;
	static final public String NO_OBJECT = /*$$(*/ "Object not found: %s" /*)*/;
	static final public String ERR_IMPORT_WITH_MSG = /*$$(*/ "Error in import: %s. Please check the server log" /*)*/;
	static final public String NO_MENU = /*$$(*/ "Menu not found: %s" /*)*/;
	
}
