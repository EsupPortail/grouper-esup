/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.exception;

/**
 * Exception thrown when an {link Owner} is not found within the Groups
 * Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: OwnerNotFoundException.java,v 1.2 2009-03-15 06:37:23 mchyzer Exp $
 */
public class OwnerNotFoundException extends RuntimeException {
  private static final long serialVersionUID = -2251334941955222491L;
  public OwnerNotFoundException() { 
    super(); 
  }
  public OwnerNotFoundException(String msg) { 
    super(msg); 
  }
  public OwnerNotFoundException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  public OwnerNotFoundException(Throwable cause) { 
    super(cause); 
  }
}

