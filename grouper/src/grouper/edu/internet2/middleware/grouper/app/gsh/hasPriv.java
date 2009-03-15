/*
 * Copyright (C) 2006-2007 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package edu.internet2.middleware.grouper.app.gsh;
import bsh.CallStack;
import bsh.Interpreter;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.StemNotFoundException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Check if subject has privilege.
 * <p/>
 * @author  blair christensen.
 * @version $Id: hasPriv.java,v 1.3 2009-03-15 06:37:23 mchyzer Exp $
 * @since   0.0.1
 */
public class hasPriv {

  // PUBLIC CLASS METHODS //

  /**
   * Check if subject has privilege.
   * <p/>
   * @param   i           BeanShell interpreter.
   * @param   stack       BeanShell call stack.
   * @param   name        Check for privilege on this {@link Group} or {@link Stem}.
   * @param   subjId      Check if this {@link Subject} has privilege.
   * @param   priv        Check this {@link AccessPrivilege}.
   * @return  True if succeeds.
   * @throws  GrouperShellException
   * @since   0.0.1
   */
  public static boolean invoke(
    Interpreter i, CallStack stack, String name,  String subjId, Privilege priv
  ) 
    throws  GrouperShellException
  {
    GrouperShell.setOurCommand(i, true);
    try {
      GrouperSession  s     = GrouperShell.getSession(i);
      Subject         subj  = SubjectFinder.findById(subjId, true);
      if (Privilege.isAccess(priv)) {
        Group g = GroupFinder.findByName(s, name, true);
        if      (priv.equals( AccessPrivilege.ADMIN  )) {
          return g.hasAdmin(subj);
        }
        else if (priv.equals( AccessPrivilege.OPTIN  )) {
          return g.hasOptin(subj);
        }
        else if (priv.equals( AccessPrivilege.OPTOUT )) {
          return g.hasOptout(subj);
        }
        else if (priv.equals( AccessPrivilege.READ   )) {
          return g.hasRead(subj);
        }
        else if (priv.equals( AccessPrivilege.UPDATE )) {
          return g.hasUpdate(subj);
        }
        else if (priv.equals( AccessPrivilege.VIEW   )) {
          return g.hasView(subj);
        }
      }
      else {
        Stem ns = StemFinder.findByName(s, name, true);
        if      (priv.equals( NamingPrivilege.CREATE )) {
          return ns.hasCreate(subj);
        }
        else if (priv.equals( NamingPrivilege.STEM   )) {
          return ns.hasStem(subj);
        }
      }
    }
    catch (GroupNotFoundException eGNF)         {
      GrouperShell.error(i, eGNF);
    }
    catch (StemNotFoundException eNSNF)         {
      GrouperShell.error(i, eNSNF); 
    }
    catch (SubjectNotFoundException eSNF)       {
      GrouperShell.error(i, eSNF); 
    }
    catch (SubjectNotUniqueException eSNU)      {
      GrouperShell.error(i, eSNU); 
    }
    return false;
  } // public static boolean invoke(i, stack, name, subjId, priv)

} // public class hasPriv

