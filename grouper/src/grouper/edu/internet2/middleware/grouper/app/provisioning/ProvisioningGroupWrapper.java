package edu.internet2.middleware.grouper.app.provisioning;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;

public class ProvisioningGroupWrapper extends ProvisioningUpdatableWrapper {
  
  private ProvisioningStateGroup provisioningStateGroup = new ProvisioningStateGroup();


  public ProvisioningStateGroup getProvisioningStateGroup() {
    return provisioningStateGroup;
  }

  private boolean grouperTargetGroupFromCacheInitted = false;
  private ProvisioningGroup grouperTargetGroupFromCache;

  //TODO finish this for cached objects
  public ProvisioningGroup getGrouperTargetGroupFromCache() {
    if (grouperTargetGroupFromCacheInitted 
        || this.gcGrouperSyncGroup == null || this.getGrouperProvisioner() == null) {
      return grouperTargetGroupFromCache;
    }
    
    // see if there is an object cached
    for (GrouperProvisioningConfigurationAttributeDbCache cache :
      this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().getGroupAttributeDbCaches()) {
      if (cache == null 
          || cache.getSource() != GrouperProvisioningConfigurationAttributeDbCacheSource.grouper 
          || cache.getType() != GrouperProvisioningConfigurationAttributeDbCacheType.object) {
        continue;
      }
      
    }
    return grouperTargetGroupFromCache;
  }

  private boolean targetProvisioningGroupFromCacheInitted = false;
  private ProvisioningGroup targetProvisioningGroupFromCache;

  
    
  public ProvisioningGroup getTargetProvisioningGroupFromCache() {
    return targetProvisioningGroupFromCache;
  }

  private String groupId;
  
  
  
  
  public String getGroupId() {
    return groupId;
  }




  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  private String syncGroupId;
  
  


  
  public String getSyncGroupId() {
    return syncGroupId;
  }




  
  public void setSyncGroupId(String syncGroupId) {
    this.syncGroupId = syncGroupId;
  }




  public ProvisioningGroupWrapper() {
    super();
    this.provisioningStateGroup.setProvisioningGroupWrapper(this);
  }

  /**
   * this is the representation of grouper side that grouper retrieves from its database 
   */
  private ProvisioningGroup grouperProvisioningGroup;

  /**
<<<<<<< GROUPER_5_BRANCH
   * this is what is retrieved from the target and structured in the target representation
   */
<<<<<<< GROUPER_5_BRANCH
=======
  private boolean delete;
  
  
  /**
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public boolean isDelete() {
    return delete;
  }




  /**
   * if the grrouperProvisioningGroup side is for a delete.  includes things that are known 
   * to be needed to be deleted.  This is used to retrieve the correct
   * incremental state from the target
   * @param delete
   */
  public void setDelete(boolean delete) {
    this.delete = delete;
  }
  
  
  /**
   * if the grrouperProvisioningGroup side is for an update.  includes things that are known 
   * to be needed to be updated.  This is used to retrieve the correct
   * incremental state from the target
   */
  private boolean update;
  
  
  /**
   * if the grrouperProvisioningGroup side is for an update.  includes things that are known 
   * to be needed to be updated.  This is used to retrieve the correct
   * incremental state from the target
   * @return
   */
  public boolean isUpdate() {
    return this.update;
  }

  /**
   * if the grrouperProvisioningGroup side is for an update.  includes things that are known 
   * to be needed to be updated.  This is used to retrieve the correct
   * incremental state from the target
   * @param update
   */
  public void setUpdate(boolean update) {
    this.update = update;
  }

  /**
=======
>>>>>>> 3c25747 Provisioning related changes - make incremental sync more robust
   * this is what is retrieved from the target and structured in the target representation
   */
>>>>>>> 37c6ac9 Remedy V2 digital marketplace provisioner
  private ProvisioningGroup targetProvisioningGroup;
  
  /**
   * grouper side translated for target
   */
  private ProvisioningGroup grouperTargetGroup;

  /**
<<<<<<< GROUPER_5_BRANCH
   * this comes from the commands class and is target specific bean
   */
<<<<<<< GROUPER_5_BRANCH
=======
  private boolean create;
  
  /**
   * if this is for a create in target
   * @return
   */
  public boolean isCreate() {
    return create;
  }

  /**
   * if this is for a create in target
   * @param create
   */
  public void setCreate(boolean create) {
    this.create = create;
  }

  /**
=======
>>>>>>> 252ebc1 restructure how state is stored in provisioning wrappers
   * this comes from the commands class and is target specific bean
   */
>>>>>>> 37c6ac9 Remedy V2 digital marketplace provisioner
  private Object targetNativeGroup;
  
  private GcGrouperSyncGroup gcGrouperSyncGroup;

  
  public ProvisioningGroup getGrouperProvisioningGroup() {
    return grouperProvisioningGroup;
  }

  private void calculateGroupId() {
    this.groupId = null;
    if (this.grouperProvisioningGroup != null) {
      this.groupId = this.grouperProvisioningGroup.getId();
    } else if (this.gcGrouperSyncGroup != null) {
      this.groupId = this.gcGrouperSyncGroup.getGroupId();
    }
  }
  
  public void setGrouperProvisioningGroup(ProvisioningGroup grouperProvisioningGroup) {
    if (this.grouperProvisioningGroup == grouperProvisioningGroup) {
      return;
    }
    ProvisioningGroup oldGrouperProvisioningGroup = this.grouperProvisioningGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldGrouperProvisioningGroup == null ? null : oldGrouperProvisioningGroup.getProvisioningGroupWrapper();

    this.grouperProvisioningGroup = grouperProvisioningGroup;
    
    if (this.grouperProvisioningGroup != null) {
      this.grouperProvisioningGroup.setProvisioningGroupWrapper(this);
    }

    if (oldGrouperProvisioningGroup != null) {
      oldGrouperProvisioningGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.grouperProvisioningGroup = null;
    }
    this.calculateGroupId();
  }

  
  public ProvisioningGroup getTargetProvisioningGroup() {
    return targetProvisioningGroup;
  }

  
  public void setTargetProvisioningGroup(ProvisioningGroup targetProvisioningGroup) {
    
    if (this.targetProvisioningGroup == targetProvisioningGroup) {
      return;
    }
    
    ProvisioningGroup oldTargetProvisioningGroup = this.targetProvisioningGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldTargetProvisioningGroup == null ? null : oldTargetProvisioningGroup.getProvisioningGroupWrapper();

    this.targetProvisioningGroup = targetProvisioningGroup;
    
    if (this.targetProvisioningGroup != null) {
      
      ProvisioningGroupWrapper newTargetGroupOldWrapper = this.targetProvisioningGroup.getProvisioningGroupWrapper();
      
      this.targetProvisioningGroup.setProvisioningGroupWrapper(this);
      
      if (newTargetGroupOldWrapper != null && newTargetGroupOldWrapper.getProvisioningStateGroup().isSelectResultProcessed()) {
        this.getProvisioningStateGroup().setSelectResultProcessed(true);
      }
      if (newTargetGroupOldWrapper != null && newTargetGroupOldWrapper.getProvisioningStateGroup().isSelectAllMembershipResultProcessed()) {
        this.getProvisioningStateGroup().setSelectAllMembershipResultProcessed(true);
      }
      
    }

    if (oldTargetProvisioningGroup != null) {
      oldTargetProvisioningGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.targetProvisioningGroup = null;
    }

  }

  
  public ProvisioningGroup getGrouperTargetGroup() {
    return grouperTargetGroup;
  }

  
  public void setGrouperTargetGroup(ProvisioningGroup grouperTargetGroup) {
    
    if (this.grouperTargetGroup == grouperTargetGroup) {
      return;
    }
    
    ProvisioningGroup oldGrouperTargetGroup = this.grouperTargetGroup;
    ProvisioningGroupWrapper oldProvisioningGroupWrapper = oldGrouperTargetGroup == null ? null : oldGrouperTargetGroup.getProvisioningGroupWrapper();

    this.grouperTargetGroup = grouperTargetGroup;
    
    if (this.grouperTargetGroup != null) {
      this.grouperTargetGroup.setProvisioningGroupWrapper(this);
    }

    if (oldGrouperTargetGroup != null) {
      oldGrouperTargetGroup.setProvisioningGroupWrapper(null);
    }
    if (oldProvisioningGroupWrapper != null && oldProvisioningGroupWrapper != this) {
      oldProvisioningGroupWrapper.grouperTargetGroup = null;
    }
  }

  
  public Object getTargetNativeGroup() {
    return targetNativeGroup;
  }

  
  public void setTargetNativeGroup(Object targetNativeGroup) {
    this.targetNativeGroup = targetNativeGroup;
  }

  
  public GcGrouperSyncGroup getGcGrouperSyncGroup() {
    return gcGrouperSyncGroup;
  }

  
  public void setGcGrouperSyncGroup(GcGrouperSyncGroup gcGrouperSyncGroup) {
    this.gcGrouperSyncGroup = gcGrouperSyncGroup;
    if (this.gcGrouperSyncGroup != null) {
      this.syncGroupId = this.getGcGrouperSyncGroup().getId();
    }
    this.calculateGroupId();

  }
  
  public String toString() {
    return "GroupWrapper@" + Integer.toHexString(hashCode());
  }
  
  public String toStringForError() {
    
    if (this.grouperTargetGroup != null) {
      return "grouperTargetGroup: " + this.grouperTargetGroup;
    }

    if (this.grouperProvisioningGroup != null) {
      return "grouperProvisioningGroup: " + this.grouperProvisioningGroup;
    }

    if (this.targetProvisioningGroup != null) {
      return "targetProvisioningGroup: " + this.targetProvisioningGroup;
    }
    
    if (this.provisioningStateGroup != null) {
      return "provisioningStateGroup: " + this.provisioningStateGroup;
    }

    return this.toString();
  }

  @Override
  public String objectTypeName() {
    return "group";
  }
  

}
