/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.exception;

/**
 * Exception specific to Axelor.
 */
public class AxelorException extends Exception {
	
	private static final long serialVersionUID = 1028105628735355226L;
	
	private int category;

	/**
	 * Default constructor
	 */
	public AxelorException() {
	}

	/**
	 * Create an exception with his message and his type.
	 * 
	 * @param message
	 * 		The exception message
	 * @param category
	 * <ul>
	 * <li>1: Missing field</li>
	 * <li>2: No unique key</li>
	 * <li>3: No value</li>
	 * <li>4: configuration error</li>
	 * <li>5: Inconsistency</li>
	 * </ul>
	 */
	public AxelorException(String message, int category, Object... messageArgs) {
		super(formatMessage(message, messageArgs));
		
		this.category = category;
	}

	
	/**
	 * Format the message with the arguments passed in parameters
	 * 
	 * @param message
	 * 			The message to format
	 * @param messageArgs
	 * 			The arguments
	 * @return
	 */
	public static String formatMessage(String message, Object... messageArgs)  {
		
		if(messageArgs.length > 0)  {
			return String.format(message, messageArgs);
		}
		
		return message;
	}
	
	
	/**
	 *  Create an exception with his cause and his type.	
	 *    
	 * @param cause
	 * 		The exception cause
	 * @param category
	 * <ul>
	 * <li>1: Missing field</li>
	 * <li>2: No unique key</li>
	 * <li>3: No value</li>
	 * <li>4: configuration error</li>
	 * <li>5: Inconsistency</li>
	 * </ul>
	 * 
	 * @see Throwable
	 */
	public AxelorException(Throwable cause, int category) {
		super(cause);
		this.category = category;
	}

	/**
	 *  Create an exception with his message, his cause and his type.	  
	 * 
	 * @param message
	 * 		The exception message
	 * @param cause
	 * 		The exception cause
	 * @param category
	 * 		The exception category
	 * <ul>
	 * <li>1: Missing field</li>
	 * <li>2: No unique key</li>
	 * <li>3: No value</li>
	 * <li>4: configuration error</li>
	 * <li>5: Inconsistency</li>
	 * </ul>
	 * 
	 * @see Throwable
	 */
	public AxelorException(String message, Throwable cause, int category, Object... messageArgs) {
		super( String.format(message, messageArgs), cause);
		this.category = category;
	}
	
	/**
	 * Get the category of exception
	 * 
	 * @return
	 * <ul>
	 * <li>1: Missing field</li>
	 * <li>2: No unique key</li>
	 * <li>3: No value</li>
	 * <li>4: configuration error</li>
	 * <li>5: Inconsistency</li>
	 * </ul>
	 */
	public int getcategory(){
		
		return this.category;
		
	}

}
