/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper.shell;
import  bsh.*;
import  edu.internet2.middleware.grouper.*;

/**
 * Add group.
 * <p/>
 * @author  blair christensen.
 * @version $Id: addGroup.java,v 1.1.1.1 2008-04-27 14:52:17 tzeller Exp $
 * @since   0.0.1
 */
public class addGroup {

  // PUBLIC CLASS METHODS //

  /**
   * Add a group.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   parent      <i>name</i> of parent {@link Group}.
   * @param   extn        <i>extension</i> of {@link Group}.
   * @param   displayExtn <i>displayExtension</i> of {@link Group}.
   * @return  Added {@link Group}.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static Group invoke(
    Interpreter i, CallStack stack, String parent, String extn, String displayExtn
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s   = GrouperShell.getSession(i);
      Stem            ns  = StemFinder.findByName(s, parent);
      return ns.addChildGroup(extn, displayExtn);
    }
    catch (GroupAddException eGA)               {
      GrouperShell.error(i, eGA);
    }
    catch (InsufficientPrivilegeException eIP)  {
      GrouperShell.error(i, eIP);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF);
    }
    return null;
  } // public static Group invoke(i, stack, parent, extn, displayExtn)

} // public class addGroup

