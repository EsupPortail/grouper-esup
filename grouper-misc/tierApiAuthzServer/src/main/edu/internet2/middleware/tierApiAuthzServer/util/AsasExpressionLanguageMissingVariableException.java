package edu.internet2.middleware.tierApiAuthzServer.util;


/**
 * @author mchyzer This class is thrown there is a missing variable in EL
 * @version $Id: NotConcurrentRevisionException.java,v 1.1 2004/05/02 05:14:59
 *          mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AsasExpressionLanguageMissingVariableException extends RuntimeException {

  /**
   *  
   */
  public AsasExpressionLanguageMissingVariableException() {
    super();
  }

  /**
   * @param s
   */
  public AsasExpressionLanguageMissingVariableException(String s) {
    super(s);
  }

  /**
   * @param message
   * @param cause
   */
  public AsasExpressionLanguageMissingVariableException(String message, Throwable cause) {
    super(message, cause);
    
  }

  /**
   * @param cause
   */
  public AsasExpressionLanguageMissingVariableException(Throwable cause) {
    super(cause);
    
  }
}