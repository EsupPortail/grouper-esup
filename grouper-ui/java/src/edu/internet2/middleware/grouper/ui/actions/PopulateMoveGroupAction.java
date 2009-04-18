
package edu.internet2.middleware.grouper.ui.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperHelper;
import edu.internet2.middleware.grouper.GrouperSession;

/**
 * @author shilen
 * @version $Id: PopulateMoveGroupAction.java,v 1.1 2009-04-18 16:33:46 shilen Exp $
 */
public class PopulateMoveGroupAction extends GrouperCapableAction {

  static final private String FORWARD_MoveGroup = "MoveGroup";

  public ActionForward grouperExecute(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response,
      HttpSession session, GrouperSession grouperSession)
      throws Exception {
    DynaActionForm groupForm = (DynaActionForm) form;

    // Identify and instantiate group to move
    String curNode = (String)groupForm.get("groupId");
    Group group = GrouperHelper.groupLoadById(grouperSession,
        curNode);
    
    // this is needed to display the current path in the JSP page.
    request.setAttribute("browseParent", GrouperHelper.group2Map(
        grouperSession, group));
    
    // this is needed so that the JSP page can show the saved stems.
    makeSavedStemsAvailable(request);

    // this is needed to show the subtitle on the page.
    session.setAttribute("subtitle", "groups.action.move");

    
    return mapping.findForward(FORWARD_MoveGroup);

  }

}
