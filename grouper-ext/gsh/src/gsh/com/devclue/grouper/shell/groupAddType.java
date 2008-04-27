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
 * Add a {@link GroupType} to a {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: groupAddType.java,v 1.1.1.1 2008-04-27 14:52:17 tzeller Exp $
 * @since   0.1.0
 */
public class groupAddType {

  // PUBLIC CLASS METHODS //

  /**
   * Add a {@link GroupType} to a {@link Group}.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Name of {@link Group}.
   * @param   type        Name of {@link GroupType}.
   * @return  True if {@link GroupType} added to {@link Group}.
   * @throws  GrouperShellException
   * @since   0.1.0
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name, String type
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      Group     g = GroupFinder.findByName(GrouperShell.getSession(i), name);
      GroupType t = GroupTypeFinder.find(type);
      g.addType(t);
      return true;
    }
    catch (GroupModifyException eGM)    {
      GrouperShell.error(i, eGM);
    }
    catch (GroupNotFoundException eGNF) {
      GrouperShell.error(i, eGNF);
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperShell.error(i, eIP);
    }
    catch (SchemaException eS)          {
      GrouperShell.error(i, eS);
    }
    return false;
  } // public static boolean invoke(i, stack, name, type)

} // public class groupAddType

