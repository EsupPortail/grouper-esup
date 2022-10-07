package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningGrouperSyncDao {

  private GrouperProvisioner grouperProvisioner = null;

  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }

  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * add / update / delete sync objects based on real data
   */
  public void fixSyncObjects() {
    ProvisioningSyncResult provisioningSyncResult = this.grouperProvisioner
        .getProvisioningSyncResult();
    
    ProvisioningSyncIntegration.fullSyncMembersForInitialize(this.getGrouperProvisioner(), provisioningSyncResult,
        this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncMembers(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getMemberUuidToProvisioningEntityWrapper());
    ProvisioningSyncIntegration.fullSyncMemberships(provisioningSyncResult,
        this.getGrouperProvisioner().getGcGrouperSync(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncGroups(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncMembers(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncMemberships(),
        this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getGroupUuidMemberUuidToProvisioningMembershipWrapper());

    //    //do we really need to do this now?  maybe just do this at end?
    //    int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
    //    provisioningSyncResult.setSyncObjectStoreCount(objectStoreCount);
    //    
    //    this.grouperProvisioner.getDebugMap().put("fixSyncObjectStoreCount", objectStoreCount);

  }

  /**
   * get sync objects from the database
   */
  public List<GcGrouperSyncGroup> retrieveAllSyncGroups() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    List<GcGrouperSyncGroup> gcGrouperSyncGroups = gcGrouperSync
        .getGcGrouperSyncGroupDao().groupRetrieveAll();

    clearErrorsGroup(gcGrouperSyncGroups);
    return gcGrouperSyncGroups;
  }

  public void clearErrorsGroup(Collection<GcGrouperSyncGroup> gcGrouperSyncGroups) {
    for (GcGrouperSyncGroup gcGrouperSyncGroup : GrouperUtil.nonNull(gcGrouperSyncGroups)) {
      gcGrouperSyncGroup.setErrorCode(null);
      gcGrouperSyncGroup.setErrorMessage(null);
      gcGrouperSyncGroup.setErrorTimestamp(null);
    }
  }
  
  public void clearErrorsMember(Collection<GcGrouperSyncMember> gcGrouperSyncMembers) {
    for (GcGrouperSyncMember gcGrouperSyncMember : GrouperUtil.nonNull(gcGrouperSyncMembers)) {
      gcGrouperSyncMember.setErrorCode(null);
      gcGrouperSyncMember.setErrorMessage(null);
      gcGrouperSyncMember.setErrorTimestamp(null);
    }
  }
  
  public void clearErrorsMembership(Collection<GcGrouperSyncMembership> gcGrouperSyncMemberships) {
    for (GcGrouperSyncMembership gcGrouperSyncMembership : GrouperUtil.nonNull(gcGrouperSyncMemberships)) {
      gcGrouperSyncMembership.setErrorCode(null);
      gcGrouperSyncMembership.setErrorMessage(null);
      gcGrouperSyncMembership.setErrorTimestamp(null);
    }
  }
  
  /**
   * get sync objects from the database
   */
  public List<GcGrouperSyncMember> retrieveAllSyncMembers() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    List<GcGrouperSyncMember> gcGrouperSyncMembers = gcGrouperSync
        .getGcGrouperSyncMemberDao().memberRetrieveAll();
    clearErrorsMember(gcGrouperSyncMembers);

    return gcGrouperSyncMembers;
  }

  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public List<GcGrouperSyncMembership> retrieveAllSyncMemberships() {
    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = gcGrouperSync
        .getGcGrouperSyncMembershipDao().membershipRetrieveAll();

    clearErrorsMembership(gcGrouperSyncMemberships);

    return gcGrouperSyncMemberships;
  }

  /**
   * get sync objects from the database
   */
  public void retrieveIncrementalSyncGroups() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

    List<GcGrouperSyncGroup> gcGrouperSyncGroups = new ArrayList<GcGrouperSyncGroup>();

    Set<String> groupIdsToRetrieve = new HashSet<String>();

    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithoutRecalc().getGroupUuidsForGroupOnly()) {
      String groupId = (String) grouperIncrementalDataItem.getItem();
      groupIdsToRetrieve.add(groupId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithoutRecalc()
        .getGroupUuidsForGroupMembershipSync()) {
      String groupId = (String) grouperIncrementalDataItem.getItem();
      groupIdsToRetrieve.add(groupId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithoutRecalc()
        .getGroupUuidsMemberUuidsForMembershipSync()) {
      String groupId = (String) ((MultiKey) grouperIncrementalDataItem.getItem())
          .getKey(0);
      groupIdsToRetrieve.add(groupId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithRecalc().getGroupUuidsForGroupOnly()) {
      String groupId = (String) grouperIncrementalDataItem.getItem();
      groupIdsToRetrieve.add(groupId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithRecalc()
        .getGroupUuidsForGroupMembershipSync()) {
      String groupId = (String) grouperIncrementalDataItem.getItem();
      groupIdsToRetrieve.add(groupId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithRecalc()
        .getGroupUuidsMemberUuidsForMembershipSync()) {
      String groupId = (String) ((MultiKey) grouperIncrementalDataItem.getItem())
          .getKey(0);
      groupIdsToRetrieve.add(groupId);
    }

    this.getGrouperProvisioner().getDebugMap().put("syncGroupsToQuery",
        GrouperUtil.length(groupIdsToRetrieve));

    if (groupIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncGroup> grouperSyncGroupIdToSyncGroup = gcGrouperSync
          .getGcGrouperSyncGroupDao().groupRetrieveByGroupIds(groupIdsToRetrieve);

      gcGrouperSyncGroups
          .addAll(GrouperUtil.nonNull(grouperSyncGroupIdToSyncGroup).values());
    }

    clearErrorsGroup(gcGrouperSyncGroups);
    this.getGrouperProvisioner().getDebugMap().put("syncGroupsFound",
        GrouperUtil.length(gcGrouperSyncGroups));

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
        .setGcGrouperSyncGroups(gcGrouperSyncGroups);

    debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
    debugMap.put("syncGroupCount", GrouperUtil.length(gcGrouperSyncGroups));
  }

  /**
   * get sync objects from the database
   */
  public void retrieveIncrementalSyncMembers() {

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

    List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();

    Set<String> memberIdsToRetrieve = new HashSet<String>();

    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithoutRecalc()
        .getMemberUuidsForEntityOnly()) {
      String memberId = (String) grouperIncrementalDataItem.getItem();
      memberIdsToRetrieve.add(memberId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithoutRecalc()
        .getMemberUuidsForEntityMembershipSync()) {
      String memberId = (String) grouperIncrementalDataItem.getItem();
      memberIdsToRetrieve.add(memberId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithoutRecalc()
        .getGroupUuidsMemberUuidsForMembershipSync()) {
      String memberId = (String) ((MultiKey) grouperIncrementalDataItem.getItem())
          .getKey(1);
      memberIdsToRetrieve.add(memberId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithRecalc().getMemberUuidsForEntityOnly()) {
      String memberId = (String) grouperIncrementalDataItem.getItem();
      memberIdsToRetrieve.add(memberId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithRecalc()
        .getMemberUuidsForEntityMembershipSync()) {
      String memberId = (String) grouperIncrementalDataItem.getItem();
      memberIdsToRetrieve.add(memberId);
    }
    for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
        .retrieveGrouperProvisioningDataIncrementalInput()
        .getGrouperIncrementalDataToProcessWithRecalc()
        .getGroupUuidsMemberUuidsForMembershipSync()) {
      String memberId = (String) ((MultiKey) grouperIncrementalDataItem.getItem())
          .getKey(1);
      memberIdsToRetrieve.add(memberId);
    }

    this.getGrouperProvisioner().getDebugMap().put("syncMembersToQuery",
        GrouperUtil.length(memberIdsToRetrieve));

    if (memberIdsToRetrieve.size() > 0) {
      GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
      Map<String, GcGrouperSyncMember> grouperSyncMemberIdToSyncMember = gcGrouperSync
          .getGcGrouperSyncMemberDao().memberRetrieveByMemberIds(memberIdsToRetrieve);
      gcGrouperSyncMembers
          .addAll(GrouperUtil.nonNull(grouperSyncMemberIdToSyncMember).values());
    }

    this.getGrouperProvisioner().getDebugMap().put("syncMembersFound",
        GrouperUtil.length(gcGrouperSyncMembers));

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
        .getGcGrouperSyncMembers() != null) {

      Set<GcGrouperSyncMember> gcGrouperSyncMembersSet = new HashSet<GcGrouperSyncMember>(
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
              .getGcGrouperSyncMembers());
      gcGrouperSyncMembersSet.addAll(gcGrouperSyncMembers);

      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncMembers(
              new ArrayList<GcGrouperSyncMember>(gcGrouperSyncMembersSet));
    } else {
      Set<GcGrouperSyncMember> gcGrouperSyncMembersSet = new HashSet<GcGrouperSyncMember>();
      gcGrouperSyncMembersSet.addAll(gcGrouperSyncMembers);

      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncMembers(
              new ArrayList<GcGrouperSyncMember>(gcGrouperSyncMembersSet));
    }

    //    this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().setGcGrouperSyncMembers(gcGrouperSyncMembers);
    clearErrorsMember(gcGrouperSyncMembers);

    debugMap.put("retrieveSyncMembersMillis", System.currentTimeMillis() - start);
    debugMap.put("syncMemberCount", GrouperUtil.length(gcGrouperSyncMembers));

  }

  /**
   * get sync objects from the database.  all records correspond to a sync group and sync member or its skipped
   */
  public void retrieveIncrementalSyncMemberships() {

    GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();

    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    long start = System.currentTimeMillis();

    List<GcGrouperSyncMembership> gcGrouperSyncMemberships = new ArrayList<GcGrouperSyncMembership>();

    // ####  memberships by membership
    {
      Set<MultiKey> groupIdsMemberIdsToRetrieve = new HashSet<MultiKey>();

      for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
          .retrieveGrouperProvisioningDataIncrementalInput()
          .getGrouperIncrementalDataToProcessWithoutRecalc()
          .getGroupUuidsMemberUuidsForMembershipSync()) {
        MultiKey groupIdMemberIdFieldId = (MultiKey) grouperIncrementalDataItem.getItem();
        groupIdsMemberIdsToRetrieve.add(new MultiKey(groupIdMemberIdFieldId.getKey(0),
            groupIdMemberIdFieldId.getKey(1)));
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
          .retrieveGrouperProvisioningDataIncrementalInput()
          .getGrouperIncrementalDataToProcessWithRecalc()
          .getGroupUuidsMemberUuidsForMembershipSync()) {
        MultiKey groupIdMemberIdFieldId = (MultiKey) grouperIncrementalDataItem.getItem();
        groupIdsMemberIdsToRetrieve.add(new MultiKey(groupIdMemberIdFieldId.getKey(0),
            groupIdMemberIdFieldId.getKey(1)));
      }

      if (groupIdsMemberIdsToRetrieve.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsToQuery",
            GrouperUtil.length(groupIdsMemberIdsToRetrieve));
        Map<MultiKey, GcGrouperSyncMembership> grouperSyncMembershipMap = gcGrouperSync
            .getGcGrouperSyncMembershipDao()
            .membershipRetrieveByGroupIdsAndMemberIds(gcGrouperSync.getId(), groupIdsMemberIdsToRetrieve);
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsFromMembership",
            GrouperUtil.length(grouperSyncMembershipMap));
        gcGrouperSyncMemberships
            .addAll(GrouperUtil.nonNull(grouperSyncMembershipMap).values());
      }
    }

    // ####  memberships by group
    {
      Set<String> groupIdsToRetrieve = new HashSet<String>();

      for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
          .retrieveGrouperProvisioningDataIncrementalInput()
          .getGrouperIncrementalDataToProcessWithoutRecalc()
          .getGroupUuidsForGroupMembershipSync()) {
        String groupId = (String) grouperIncrementalDataItem.getItem();
        groupIdsToRetrieve.add(groupId);
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
          .retrieveGrouperProvisioningDataIncrementalInput()
          .getGrouperIncrementalDataToProcessWithRecalc()
          .getGroupUuidsForGroupMembershipSync()) {
        String groupId = (String) grouperIncrementalDataItem.getItem();
        groupIdsToRetrieve.add(groupId);
      }

      if (groupIdsToRetrieve.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsToQueryFromGroup",
            GrouperUtil.length(groupIdsToRetrieve));
        List<GcGrouperSyncMembership> grouperSyncMemberships = gcGrouperSync
            .getGcGrouperSyncMembershipDao()
            .membershipRetrieveByGroupIds(groupIdsToRetrieve);
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsFromGroup",
            GrouperUtil.length(grouperSyncMemberships));
        gcGrouperSyncMemberships.addAll(GrouperUtil.nonNull(grouperSyncMemberships));
      }
    }

    // ####  memberships by member
    {
      Set<String> memberIdsToRetrieve = new HashSet<String>();

      for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
          .retrieveGrouperProvisioningDataIncrementalInput()
          .getGrouperIncrementalDataToProcessWithoutRecalc()
          .getMemberUuidsForEntityMembershipSync()) {
        String memberId = (String) grouperIncrementalDataItem.getItem();
        memberIdsToRetrieve.add(memberId);
      }
      for (GrouperIncrementalDataItem grouperIncrementalDataItem : this.grouperProvisioner
          .retrieveGrouperProvisioningDataIncrementalInput()
          .getGrouperIncrementalDataToProcessWithRecalc()
          .getMemberUuidsForEntityMembershipSync()) {
        String memberId = (String) grouperIncrementalDataItem.getItem();
        memberIdsToRetrieve.add(memberId);
      }

      if (memberIdsToRetrieve.size() > 0) {
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsToQueryFromMember",
            GrouperUtil.length(memberIdsToRetrieve));
        List<GcGrouperSyncMembership> grouperSyncMemberships = gcGrouperSync
            .getGcGrouperSyncMembershipDao()
            .membershipRetrieveByMemberIds(memberIdsToRetrieve);
        this.getGrouperProvisioner().getDebugMap().put("syncMembershipsFromMember",
            GrouperUtil.length(grouperSyncMemberships));
        gcGrouperSyncMemberships.addAll(GrouperUtil.nonNull(grouperSyncMemberships));
      }
    }

    this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
        .setGcGrouperSyncMemberships(gcGrouperSyncMemberships);

    List<String> memberIdsToRetrieve = new ArrayList<String>();

    for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {
      memberIdsToRetrieve.add(gcGrouperSyncMembership.getGrouperSyncMemberId());
    }

    Map<String, GcGrouperSyncMember> memberRetrieveByIds = gcGrouperSync
        .getGcGrouperSyncMemberDao().memberRetrieveByIds(memberIdsToRetrieve);

    if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
        .getGcGrouperSyncMembers() != null) {

      Set<GcGrouperSyncMember> gcGrouperSyncMembersSet = new HashSet<GcGrouperSyncMember>(
          this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
              .getGcGrouperSyncMembers());
      gcGrouperSyncMembersSet.addAll(memberRetrieveByIds.values());

      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncMembers(
              new ArrayList<GcGrouperSyncMember>(gcGrouperSyncMembersSet));
    } else {
      Set<GcGrouperSyncMember> gcGrouperSyncMembershipsSet = new HashSet<GcGrouperSyncMember>();
      gcGrouperSyncMembershipsSet.addAll(memberRetrieveByIds.values());
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncMembers(
              new ArrayList<GcGrouperSyncMember>(gcGrouperSyncMembershipsSet));
    }

    clearErrorsMembership(gcGrouperSyncMemberships);

    debugMap.put("retrieveSyncMembershipsMillis", System.currentTimeMillis() - start);
    debugMap.put("syncMembershipCount", GrouperUtil.length(gcGrouperSyncMemberships));

  }

  public void retrieveSyncDataFull() {
    Map<String, Object> debugMap = this.getGrouperProvisioner().getDebugMap();

    {
      long start = System.currentTimeMillis();
      List<GcGrouperSyncGroup> retrieveAllSyncGroups = grouperProvisioner
          .retrieveGrouperProvisioningSyncDao().retrieveAllSyncGroups();
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncGroups(retrieveAllSyncGroups);

      debugMap.put("retrieveSyncGroupsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncGroupCount",
          GrouperUtil
              .length(this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
                  .getGcGrouperSyncGroups()));
    }
    {
      long start = System.currentTimeMillis();
      List<GcGrouperSyncMember> retrieveAllSyncMembers = grouperProvisioner
          .retrieveGrouperProvisioningSyncDao().retrieveAllSyncMembers();
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncMembers(retrieveAllSyncMembers);
      debugMap.put("retrieveSyncEntitiesMillis", System.currentTimeMillis() - start);
      debugMap.put("syncEntityCount", GrouperUtil.length(this.getGrouperProvisioner()
          .retrieveGrouperProvisioningDataSync().getGcGrouperSyncMembers()));
    }
    {
      long start = System.currentTimeMillis();
      List<GcGrouperSyncMembership> retrieveAllSyncMemberships = grouperProvisioner
          .retrieveGrouperProvisioningSyncDao().retrieveAllSyncMemberships();
      this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
          .setGcGrouperSyncMemberships(
              retrieveAllSyncMemberships);
      debugMap.put("retrieveSyncMshipsMillis", System.currentTimeMillis() - start);
      debugMap.put("syncMshipCount", GrouperUtil.length(retrieveAllSyncMemberships));
    }

  }

  /**
   * update subject link for these members
   * @param gcGrouperSyncMembersToRefreshSubjectLink
   */
  public void updateSubjectLink(
      List<GcGrouperSyncMember> gcGrouperSyncMembersToRefreshSubjectLink) {
    if (GrouperUtil.length(gcGrouperSyncMembersToRefreshSubjectLink) == 0) {
      return;
    }

    // If using subject attributes and those are not in the member sync object, then resolve the subject, and put in the member sync object
    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache0 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[0];
    boolean hasSubjectLinkAttributeValueCache0 = grouperProvisioningConfigurationAttributeDbCache0 != null
        && grouperProvisioningConfigurationAttributeDbCache0.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache0.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache1 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[1];
    boolean hasSubjectLinkAttributeValueCache1 = grouperProvisioningConfigurationAttributeDbCache1 != null
        && grouperProvisioningConfigurationAttributeDbCache1.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache1.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache2 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[2];
    boolean hasSubjectLinkAttributeValueCache2 = grouperProvisioningConfigurationAttributeDbCache2 != null
        && grouperProvisioningConfigurationAttributeDbCache2.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache2.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript());

    GrouperProvisioningConfigurationAttributeDbCache grouperProvisioningConfigurationAttributeDbCache3 = 
        this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getEntityAttributeDbCaches()[3];
    boolean hasSubjectLinkAttributeValueCache3 = grouperProvisioningConfigurationAttributeDbCache3 != null
        && grouperProvisioningConfigurationAttributeDbCache3.getSource() == GrouperProvisioningConfigurationAttributeDbCacheSource.grouper
        && grouperProvisioningConfigurationAttributeDbCache3.getType() == GrouperProvisioningConfigurationAttributeDbCacheType.subjectTranslationScript
        && !StringUtils.isBlank(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript());

    if (!hasSubjectLinkAttributeValueCache0 && !hasSubjectLinkAttributeValueCache1
        && !hasSubjectLinkAttributeValueCache2 && !hasSubjectLinkAttributeValueCache3) {
      return;
    }

    int subjectsCannotFindLinkData = 0;

    Set<MultiKey> sourceIdSubjectIds = new HashSet<MultiKey>();

    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembersToRefreshSubjectLink) {

      ProvisioningSyncIntegration.decorateSyncMemberSubjectInformationIfNull(this.grouperProvisioner, gcGrouperSyncMember, null);
      
      if (!StringUtils.isBlank(gcGrouperSyncMember.getSourceId()) && !StringUtils.isBlank(gcGrouperSyncMember.getSubjectId())) {
        MultiKey sourceIdSubjectId = new MultiKey(gcGrouperSyncMember.getSourceId(),
            gcGrouperSyncMember.getSubjectId());
        sourceIdSubjectIds.add(sourceIdSubjectId);
      }
    }

    Map<MultiKey, Subject> sourceIdSubjectIdToSubject = SubjectFinder
        .findBySourceIdsAndSubjectIds(sourceIdSubjectIds, false, true);

    Set<GcGrouperSyncMember> gcSyncMembersChangedInSubjectLink = new LinkedHashSet<GcGrouperSyncMember>();
    
    for (GcGrouperSyncMember gcGrouperSyncMember : gcGrouperSyncMembersToRefreshSubjectLink) {

      MultiKey sourceIdSubjectId = new MultiKey(gcGrouperSyncMember.getSourceId(),
          gcGrouperSyncMember.getSubjectId());
      Subject subject = sourceIdSubjectIdToSubject.get(sourceIdSubjectId);

      if (subject == null) {
        subjectsCannotFindLinkData++;
        // maybe it didn't get resolved, don't mess with the existing cached data.
        continue;
      }

      Map<String, Object> variableMap = new HashMap<String, Object>();
      variableMap.put("subject", subject);

      if (hasSubjectLinkAttributeValueCache0) {
        String entityAttributeValueCache0Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache0.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache0(entityAttributeValueCache0Value);
      }

      if (hasSubjectLinkAttributeValueCache1) {
        String entityAttributeValueCache1Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache1.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache1(entityAttributeValueCache1Value);
      }

      if (hasSubjectLinkAttributeValueCache2) {
        String entityAttributeValueCache2Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache2.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache2(entityAttributeValueCache2Value);
      }

      if (hasSubjectLinkAttributeValueCache3) {
        String entityAttributeValueCache3Value = GrouperUtil
            .substituteExpressionLanguage(grouperProvisioningConfigurationAttributeDbCache3.getTranslationScript(), variableMap);
        gcGrouperSyncMember.setEntityAttributeValueCache3(entityAttributeValueCache3Value);
      }
      gcSyncMembersChangedInSubjectLink.add(gcGrouperSyncMember);
    }

    if (subjectsCannotFindLinkData > 0) {
      this.grouperProvisioner.getDebugMap().put("subjectsCannotFindLinkData",
          subjectsCannotFindLinkData);
    }
    if (GrouperUtil.length(gcSyncMembersChangedInSubjectLink) > 0) {
      this.getGrouperProvisioner().retrieveGrouperProvisioningObjectLog().debug(GrouperProvisioningObjectLogType.retrieveSubjectLink, gcSyncMembersChangedInSubjectLink);
    }

  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsInsertGroups(List<ProvisioningGroup> grouperTargetGroupsToInsert, boolean includeMembershipsIfApplicable) {
    
    for (ProvisioningGroup grouperTargetGroup : GrouperUtil.nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup.getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.isInsertResultProcessed()) {
        continue;
      }
      
      provisioningGroupWrapper.setInsertResultProcessed(true);
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper.getGcGrouperSyncGroup();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetGroup.getException() == null && GrouperUtil.booleanValue(grouperTargetGroup.getProvisioned(), false)) {
        gcGrouperSyncGroup.setInTarget(true);
        gcGrouperSyncGroup.setInTargetStart(nowTimestamp);
        gcGrouperSyncGroup.setInTargetInsertOrExists(true);
        //gcGrouperSyncGroup.setLastGroupMetadataSync(nowTimestamp);
        gcGrouperSyncGroup.setErrorMessage(null);
        gcGrouperSyncGroup.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.groupAttributes 
              == this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(nowTimestamp,
                grouperTargetGroup);
            
            if (this.getGrouperProvisioner().retrieveGrouperProvisioningDataIncrementalInput().getGrouperIncrementalDataToProcessWithRecalc().getGroupUuidsForGroupMembershipRecalc().contains(gcGrouperSyncGroup.getGroupId())) {
              gcGrouperSyncGroup.setLastGroupSync(new Timestamp(System.currentTimeMillis()));
              gcGrouperSyncGroup.setLastGroupSyncStart(new Timestamp(this.getGrouperProvisioner().retrieveGrouperProvisioningLogic().getRetrieveDataStartMillisSince1970()));
            }
          }
          
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          gcGrouperSync.setGroupCount(GrouperUtil.intValue(gcGrouperSync.getGroupCount(), 0) + 1);
        }
        
      } else {
        gcGrouperSyncGroup.setErrorMessage(GrouperUtil.exception4kZipBase64(GrouperUtil.getFullStackTrace(grouperTargetGroup.getException())));
        gcGrouperSyncGroup.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput().addRecordsWithInsertErrors(1);
      }
    }
  }

  public void processResultsInserts(GrouperProvisioningLists targetObjectInserts) {
    if (targetObjectInserts == null) {
      return;
    }
    processResultsInsertGroups(targetObjectInserts.getProvisioningGroups(), true);
    processResultsInsertEntities(targetObjectInserts.getProvisioningEntities(), true);
    processResultsInsertMemberships(targetObjectInserts.getProvisioningMemberships());
  }

  public void processResultsUpdatesFull(GrouperProvisioningLists targetObjectUpdates) {
    if (targetObjectUpdates == null) {
      return;
    }

    processResultsUpdateGroupsFull(targetObjectUpdates.getProvisioningGroups(), true);
    processResultsUpdateEntitiesFull(targetObjectUpdates.getProvisioningEntities(), true);
    processResultsUpdateMemberships(targetObjectUpdates.getProvisioningMemberships());

  }

  public void processResultsDeletes(GrouperProvisioningLists targetObjectDeletes) {
    if (targetObjectDeletes == null) {
      return;
    }
    processResultsDeleteGroups(targetObjectDeletes.getProvisioningGroups(), true);
    processResultsDeleteEntities(targetObjectDeletes.getProvisioningEntities(), true);
    processResultsDeleteMemberships(targetObjectDeletes.getProvisioningMemberships());

  }

  public void processResultsReplaces(
      GrouperProvisioningReplacesObjects targetObjectReplaces) {
    if (targetObjectReplaces == null) {
      return;
    }

    for (ProvisioningGroup provisioningGroup : targetObjectReplaces
        .getProvisioningMemberships().keySet()) {

      List<GcGrouperSyncMembership> gcGrouperSyncMemberships = this.grouperProvisioner
          .retrieveGrouperProvisioningDataSync().getGcGrouperSyncMemberships();

      List<ProvisioningMembership> provisioningMembershipsForGroup = targetObjectReplaces
          .getProvisioningMemberships().get(provisioningGroup);

      List<GcGrouperSyncMembership> gcGrouperSyncMembershipsForGroup = new ArrayList<GcGrouperSyncMembership>();

      // loop through all sync memberships and find the ones for this provisioningGroup
      for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMemberships) {

        if (StringUtils.equals(gcGrouperSyncMembership.getGrouperSyncGroupId(),
            provisioningGroup.getProvisioningGroupWrapper().getSyncGroupId())) {
          gcGrouperSyncMembershipsForGroup.add(gcGrouperSyncMembership);
        }

      }

      // the ones that exist in provisioningMembershipsForGroup and not in gcGrouperSyncMembershipsForGroup are the inserts
      // or they are in gcGrouperSyncMembershipsForGroup but marked as not in target that means they are inserts

      Map<String, ProvisioningMembership> syncIdToProvisioningMembership = new HashMap<String, ProvisioningMembership>();

      for (ProvisioningMembership grouperTargetMembership : provisioningMembershipsForGroup) {
        ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
            .getProvisioningMembershipWrapper();
        GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
            .getGcGrouperSyncMembership();
        if (gcGrouperSyncMembership != null) {
          syncIdToProvisioningMembership.put(gcGrouperSyncMembership.getId(),
              grouperTargetMembership);
        }

        if (!gcGrouperSyncMembership.isInTarget()) {
          // insert or update

          Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

          if (grouperTargetMembership.getException() == null && GrouperUtil
              .booleanValue(grouperTargetMembership.getProvisioned(), false)) {
            gcGrouperSyncMembership.setInTarget(true);
            gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
            gcGrouperSyncMembership.setInTargetInsertOrExists(true);
            gcGrouperSyncMembership.setErrorMessage(null);
            gcGrouperSyncMembership.setErrorTimestamp(null);
            
            if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
              GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
              gcGrouperSync.setRecordsCount(GrouperUtil.intValue(gcGrouperSync.getRecordsCount(), 0) + 1);
            }
            
          } else {
            gcGrouperSyncMembership
                .setErrorMessage(grouperTargetMembership.getException() == null ? null
                    : GrouperUtil
                        .getFullStackTrace(grouperTargetMembership.getException()));
            gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
            this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
                .addRecordsWithInsertErrors(1);
          }
        }

      }

      for (GcGrouperSyncMembership gcGrouperSyncMembership : gcGrouperSyncMembershipsForGroup) {

        if (!syncIdToProvisioningMembership
            .containsKey(gcGrouperSyncMembership.getId())) {

          ProvisioningMembershipWrapper provisioningMembershipWrapper = this.grouperProvisioner
              .retrieveGrouperProvisioningDataIndex()
              .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper()
              .get(new MultiKey(gcGrouperSyncMembership.getGrouperSyncGroupId(),
                  gcGrouperSyncMembership.getGrouperSyncMemberId()));

          Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
          ProvisioningMembership grouperTargetMembership = provisioningMembershipWrapper == null
              ? null
              : provisioningMembershipWrapper.getTargetMembership();

          if (grouperTargetMembership == null
              || (grouperTargetMembership.getException() == null && GrouperUtil
                  .booleanValue(grouperTargetMembership.getProvisioned(), false))) {
            if (gcGrouperSyncMembership != null) {

              if (gcGrouperSyncMembership.isInTarget()) {
                gcGrouperSyncMembership.setInTarget(false);
                gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
                gcGrouperSyncMembership.setErrorMessage(null);
                gcGrouperSyncMembership.setErrorTimestamp(null);
                
                if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
                  GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
                  gcGrouperSync.setRecordsCount(gcGrouperSync.getRecordsCount() - 1);
                }
                
              }
              
            }
          } else {
            if (gcGrouperSyncMembership != null) {
              gcGrouperSyncMembership
                  .setErrorMessage(grouperTargetMembership.getException() == null ? null
                      : GrouperUtil
                          .getFullStackTrace(grouperTargetMembership.getException()));
              gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
            }
            this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
                .addRecordsWithDeleteErrors(1);
          }

        }

      }

    }

  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsInsertEntities(
      List<ProvisioningEntity> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity
          .getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.isInsertResultProcessed()) {
        continue;
      }
      
      provisioningEntityWrapper.setInsertResultProcessed(true);
      
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetEntity.getException() == null
          && GrouperUtil.booleanValue(grouperTargetEntity.getProvisioned(), false)) {
        gcGrouperSyncMember.setInTarget(true);
        gcGrouperSyncMember.setInTargetStart(nowTimestamp);
        gcGrouperSyncMember.setInTargetInsertOrExists(true);
        //gcGrouperSyncMember.setLastUserMetadataSync(nowTimestamp);
        gcGrouperSyncMember.setErrorMessage(null);
        gcGrouperSyncMember.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.entityAttributes == this.grouperProvisioner
              .retrieveGrouperProvisioningBehavior()
              .getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
                nowTimestamp,
                grouperTargetEntity);
          }
        }
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          gcGrouperSync.setUserCount(GrouperUtil.intValue(gcGrouperSync.getUserCount(), 0) + 1);
        }
        
      } else {
        gcGrouperSyncMember
            .setErrorMessage(grouperTargetEntity.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetEntity.getException()));
        gcGrouperSyncMember.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithInsertErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param grouperTargetMembershipsToInsert
   */
  public void processResultsInsertMemberships(
      List<ProvisioningMembership> grouperTargetMembershipsToInsert) {

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil
        .nonNull(grouperTargetMembershipsToInsert)) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
          .getProvisioningMembershipWrapper();
      
      if (provisioningMembershipWrapper.isInsertResultProcessed()) {
        continue;
      }
      
      provisioningMembershipWrapper.setInsertResultProcessed(true);
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
          .getGcGrouperSyncMembership();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetMembership.getException() == null
          && GrouperUtil.booleanValue(grouperTargetMembership.getProvisioned(), false)) {
        gcGrouperSyncMembership.setInTarget(true);
        gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
        gcGrouperSyncMembership.setInTargetInsertOrExists(true);
        gcGrouperSyncMembership.setErrorMessage(null);
        gcGrouperSyncMembership.setErrorTimestamp(null);
        
        if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
          GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
          gcGrouperSync.setRecordsCount(GrouperUtil.intValue(gcGrouperSync.getRecordsCount(), 0) + 1);
        }
        
      } else {
        gcGrouperSyncMembership
            .setErrorMessage(grouperTargetMembership.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetMembership.getException()));
        gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithInsertErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsUpdateEntitiesFull(
      List<ProvisioningEntity> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity
          .getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.isUpdateResultProcessed()) {
        continue;
      }
      
      provisioningEntityWrapper.setUpdateResultProcessed(true);
      
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetEntity.getException() == null
          && GrouperUtil.booleanValue(grouperTargetEntity.getProvisioned(), false)) {
        //gcGrouperSyncMember.setLastUserMetadataSync(nowTimestamp);
        gcGrouperSyncMember.setErrorMessage(null);
        gcGrouperSyncMember.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.entityAttributes == this.grouperProvisioner
              .retrieveGrouperProvisioningBehavior()
              .getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
                nowTimestamp,
                grouperTargetEntity);
          }
        }
      } else {
        gcGrouperSyncMember
            .setErrorMessage(grouperTargetEntity.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetEntity.getException()));
        gcGrouperSyncMember.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithUpdateErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsUpdateGroupsFull(
      List<ProvisioningGroup> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

    for (ProvisioningGroup grouperTargetGroup : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup
          .getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.isUpdateResultProcessed()) {
        continue;
      }
      
      provisioningGroupWrapper.setUpdateResultProcessed(true);
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
          .getGcGrouperSyncGroup();

      if (grouperTargetGroup.getException() == null
          && GrouperUtil.booleanValue(grouperTargetGroup.getProvisioned(), false)) {
        //gcGrouperSyncGroup.setLastGroupMetadataSync(nowTimestamp);
        gcGrouperSyncGroup.setErrorMessage(null);
        gcGrouperSyncGroup.setErrorTimestamp(null);
        if (includeMembershipsIfApplicable) {
          //see if all attributes were synced
          if (GrouperProvisioningBehaviorMembershipType.groupAttributes == this.grouperProvisioner
              .retrieveGrouperProvisioningBehavior()
              .getGrouperProvisioningBehaviorMembershipType()) {

            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
                nowTimestamp,
                grouperTargetGroup);

            if (this.getGrouperProvisioner()
                .retrieveGrouperProvisioningDataIncrementalInput()
                .getGrouperIncrementalDataToProcessWithRecalc()
                .getGroupUuidsForGroupMembershipRecalc()
                .contains(gcGrouperSyncGroup.getGroupId())) {
              gcGrouperSyncGroup
                  .setLastGroupSync(new Timestamp(System.currentTimeMillis()));
              gcGrouperSyncGroup.setLastGroupSyncStart(new Timestamp(
                  this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                      .getRetrieveDataStartMillisSince1970()));
            }

          }
        }
      } else {
        
        if (GrouperProvisioningBehaviorMembershipType.groupAttributes == this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType()) {
            processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(nowTimestamp, grouperTargetGroup);
        }
        
        gcGrouperSyncGroup
            .setErrorMessage(GrouperUtil.exception4kZipBase64(GrouperUtil.getFullStackTrace(grouperTargetGroup.getException())));
        gcGrouperSyncGroup.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithUpdateErrors(1);
      }
    }
  }

  public void processResultsInsertUpdateProvisioningUpdatableAttributeMemberships(
      Timestamp nowTimestamp,
      ProvisioningUpdatable provisioningUpdatable) {
    boolean fullSyncSuccess = true;
    // see if all attributes were processed
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
        .nonNull(provisioningUpdatable.getInternal_objectChanges())) {
      if (provisioningObjectChange.getException() != null || !GrouperUtil
          .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
        fullSyncSuccess = false;
        break;
      }
    }
    if (fullSyncSuccess) {
      //gcGrouperSyncGroup.setLastGroupSync(nowTimestamp);
    }
    
    String membershipAttributeName = this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
    
    // see if all attributes were processed
    for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
        .nonNull(provisioningUpdatable.getInternal_objectChanges())) {
      if (provisioningObjectChange.getException() != null || !GrouperUtil
          .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
        continue;
      }
      if (StringUtils.equals(membershipAttributeName, provisioningObjectChange.getAttributeName())) {
        
        ProvisioningAttribute provisioningAttribute = provisioningUpdatable
            .getAttributes().get(provisioningObjectChange.getAttributeName());
        Map<Object, ProvisioningMembershipWrapper> valueToProvisioningMembershipWrapper = provisioningAttribute == null ? null : provisioningAttribute
            .getValueToProvisioningMembershipWrapper();
        if (valueToProvisioningMembershipWrapper != null) {
          if (provisioningObjectChange
              .getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.insert) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper
                .get(provisioningObjectChange.getNewValue());
            // if this is a default value there might not be a membership in place
            if (provisioningMembershipWrapper != null) {
              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();
              gcGrouperSyncMembership.setErrorMessage(null);
              gcGrouperSyncMembership.setErrorTimestamp(null);
              gcGrouperSyncMembership.setInTarget(true);
              gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
              gcGrouperSyncMembership.setInTargetInsertOrExists(true);
            }
          } else if (provisioningObjectChange
              .getProvisioningObjectChangeAction() == ProvisioningObjectChangeAction.delete) {
            ProvisioningMembershipWrapper provisioningMembershipWrapper = valueToProvisioningMembershipWrapper
                .get(provisioningObjectChange.getOldValue());
            // if there is a default untracked value, this might be null
            if (provisioningMembershipWrapper != null) {
              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();
              gcGrouperSyncMembership.setErrorMessage(null);
              gcGrouperSyncMembership.setErrorTimestamp(null);
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
            }
          }
        }

      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param grouperTargetMembershipsToInsert
   */
  public void processResultsUpdateMemberships(
      List<ProvisioningMembership> grouperTargetMembershipsToInsert) {

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil
        .nonNull(grouperTargetMembershipsToInsert)) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
          .getProvisioningMembershipWrapper();
      
      if (provisioningMembershipWrapper.isUpdateResultProcessed()) {
        continue;
      }
      
      provisioningMembershipWrapper.setUpdateResultProcessed(true);
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
          .getGcGrouperSyncMembership();
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetMembership.getException() == null
          && GrouperUtil.booleanValue(grouperTargetMembership.getProvisioned(), false)) {
        gcGrouperSyncMembership.setErrorMessage(null);
        gcGrouperSyncMembership.setErrorTimestamp(null);
      } else {
        gcGrouperSyncMembership
            .setErrorMessage(grouperTargetMembership.getException() == null ? null
                : GrouperUtil.getFullStackTrace(grouperTargetMembership.getException()));
        gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithUpdateErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsDeleteEntities(
      List<ProvisioningEntity> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningEntity grouperTargetEntity : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningEntityWrapper provisioningEntityWrapper = grouperTargetEntity
          .getProvisioningEntityWrapper();
      
      if (provisioningEntityWrapper.isDeleteResultProcessed()) {
        continue;
      }
      
      provisioningEntityWrapper.setDeleteResultProcessed(true);
      
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();

      if (gcGrouperSyncMember == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .debugMapAdd("gcGrouperSyncMemberDeleteMissing", 1);
      }

      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetEntity.getException() == null
          && GrouperUtil.booleanValue(grouperTargetEntity.getProvisioned(), false)) {
        if (gcGrouperSyncMember != null) {
          gcGrouperSyncMember.setInTarget(false);
          gcGrouperSyncMember.setInTargetEnd(nowTimestamp);
          //gcGrouperSyncMember.setLastUserMetadataSync(nowTimestamp);
          gcGrouperSyncMember.setErrorMessage(null);
          gcGrouperSyncMember.setErrorTimestamp(null);
          if (includeMembershipsIfApplicable) {
            //see if all attributes were synced
            if (GrouperProvisioningBehaviorMembershipType.entityAttributes == this.grouperProvisioner
                .retrieveGrouperProvisioningBehavior()
                .getGrouperProvisioningBehaviorMembershipType()) {
              boolean fullSyncSuccess = true;
              // see if all attributes were processed
              for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
                  .nonNull(grouperTargetEntity.getInternal_objectChanges())) {
                if (provisioningObjectChange.getException() != null || !GrouperUtil
                    .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
                  fullSyncSuccess = false;
                  break;
                }
              }
              if (fullSyncSuccess) {
                //gcGrouperSyncMember.setLastUserSync(nowTimestamp);
              }
            }
          }
          
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            gcGrouperSync.setUserCount(GrouperUtil.intValue(gcGrouperSync.getUserCount(), 0) - 1);
          }
          
        }
      } else {
        if (gcGrouperSyncMember != null) {
          gcGrouperSyncMember
              .setErrorMessage(grouperTargetEntity.getException() == null ? null
                  : GrouperUtil.getFullStackTrace(grouperTargetEntity.getException()));
          gcGrouperSyncMember.setErrorTimestamp(nowTimestamp);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithDeleteErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param includeMembershipsIfApplicable true if this group includes memberships if 
   * it is even doing memberships as a group attribute
   * @param grouperTargetGroupsToInsert
   */
  public void processResultsDeleteGroups(
      List<ProvisioningGroup> grouperTargetGroupsToInsert,
      boolean includeMembershipsIfApplicable) {

    for (ProvisioningGroup grouperTargetGroup : GrouperUtil
        .nonNull(grouperTargetGroupsToInsert)) {
      ProvisioningGroupWrapper provisioningGroupWrapper = grouperTargetGroup
          .getProvisioningGroupWrapper();
      
      if (provisioningGroupWrapper.isDeleteResultProcessed()) {
        continue;
      }
      
      provisioningGroupWrapper.setDeleteResultProcessed(true);
      
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
          .getGcGrouperSyncGroup();
      if (gcGrouperSyncGroup == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .debugMapAdd("gcGrouperSyncGroupDeleteMissing", 1);
      }
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetGroup.getException() == null
          && GrouperUtil.booleanValue(grouperTargetGroup.getProvisioned(), false)) {
        if (gcGrouperSyncGroup != null) {
          gcGrouperSyncGroup.setInTarget(false);
          gcGrouperSyncGroup.setInTargetEnd(nowTimestamp);
          //gcGrouperSyncGroup.setLastGroupMetadataSync(nowTimestamp);
          gcGrouperSyncGroup.setErrorMessage(null);
          gcGrouperSyncGroup.setErrorTimestamp(null);
          if (includeMembershipsIfApplicable) {
            //see if all attributes were synced
            if (GrouperProvisioningBehaviorMembershipType.groupAttributes == this.grouperProvisioner
                .retrieveGrouperProvisioningBehavior()
                .getGrouperProvisioningBehaviorMembershipType()) {
              boolean fullSyncSuccess = true;
              // see if all attributes were processed
              for (ProvisioningObjectChange provisioningObjectChange : GrouperUtil
                  .nonNull(grouperTargetGroup.getInternal_objectChanges())) {
                if (provisioningObjectChange.getException() != null || !GrouperUtil
                    .booleanValue(provisioningObjectChange.getProvisioned(), false)) {
                  fullSyncSuccess = false;
                  break;
                }
              }

              if (fullSyncSuccess) {
                if (this.getGrouperProvisioner()
                    .retrieveGrouperProvisioningDataIncrementalInput()
                    .getGrouperIncrementalDataToProcessWithRecalc()
                    .getGroupUuidsForGroupMembershipRecalc()
                    .contains(gcGrouperSyncGroup.getGroupId())) {
                  gcGrouperSyncGroup
                      .setLastGroupSync(new Timestamp(System.currentTimeMillis()));
                  gcGrouperSyncGroup.setLastGroupSyncStart(new Timestamp(
                      this.getGrouperProvisioner().retrieveGrouperProvisioningLogic()
                          .getRetrieveDataStartMillisSince1970()));
                }
              }
            }
          }
          
          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            gcGrouperSync.setGroupCount(GrouperUtil.intValue(gcGrouperSync.getGroupCount(), 0) - 1);
          }
          
        }
      } else {
        if (gcGrouperSyncGroup != null) {
          gcGrouperSyncGroup
              .setErrorMessage(GrouperUtil.exception4kZipBase64(GrouperUtil.getFullStackTrace(grouperTargetGroup.getException())));
          gcGrouperSyncGroup.setErrorTimestamp(nowTimestamp);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithDeleteErrors(1);
      }
    }
  }

  /**
   * process the results back to the sync objects
   * @param grouperTargetMembershipsToDelete
   */
  public void processResultsDeleteMemberships(
      List<ProvisioningMembership> grouperTargetMembershipsToDelete) {

    for (ProvisioningMembership grouperTargetMembership : GrouperUtil
        .nonNull(grouperTargetMembershipsToDelete)) {
      ProvisioningMembershipWrapper provisioningMembershipWrapper = grouperTargetMembership
          .getProvisioningMembershipWrapper();
      
      if (provisioningMembershipWrapper.isDeleteResultProcessed()) {
        continue;
      }
      
      provisioningMembershipWrapper.setDeleteResultProcessed(true);
      
      GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
          .getGcGrouperSyncMembership();

      if (gcGrouperSyncMembership == null) {
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .debugMapAdd("gcGrouperSyncMembershipDeleteMissing", 1);
      }

      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      if (grouperTargetMembership.getException() == null
          && GrouperUtil.booleanValue(grouperTargetMembership.getProvisioned(), false)) {
        if (gcGrouperSyncMembership != null) {
          gcGrouperSyncMembership.setInTarget(false);
          gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
          gcGrouperSyncMembership.setErrorMessage(null);
          gcGrouperSyncMembership.setErrorTimestamp(null);

          if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
            GcGrouperSync gcGrouperSync = this.grouperProvisioner.getGcGrouperSync();
            gcGrouperSync.setRecordsCount(GrouperUtil.intValue(gcGrouperSync.getRecordsCount(), 0) - 1);
          }
          
        }
        
        
      } else {
        if (gcGrouperSyncMembership != null) {
          gcGrouperSyncMembership
              .setErrorMessage(grouperTargetMembership.getException() == null ? null
                  : GrouperUtil
                      .getFullStackTrace(grouperTargetMembership.getException()));
          gcGrouperSyncMembership.setErrorTimestamp(nowTimestamp);
        }
        this.getGrouperProvisioner().retrieveGrouperProvisioningOutput()
            .addRecordsWithDeleteErrors(1);
      }
    }
  }

  /**
   * go through what was selected from full and keep track of whats there and what isnt there
   * @param values
   */
  public void processResultsSelectGroupsFull(
      Collection<ProvisioningGroupWrapper> values) {
    for (ProvisioningGroupWrapper provisioningGroupWrapper : GrouperUtil
        .nonNull(values)) {
      
      if (!provisioningGroupWrapper.isRecalcObject()) {
        continue;
      }
      
      ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper
          .getTargetProvisioningGroup();

      boolean exists = targetProvisioningGroup != null;
      GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
          .getGcGrouperSyncGroup();

      if (gcGrouperSyncGroup == null) {
        continue;
      }

      if (exists != gcGrouperSyncGroup.isInTarget()) {

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        gcGrouperSyncGroup.setInTarget(exists);
        if (exists) {
          gcGrouperSyncGroup.setInTargetStart(nowTimestamp);
          if (StringUtils.isBlank(gcGrouperSyncGroup.getInTargetInsertOrExistsDb())) {
            gcGrouperSyncGroup.setInTargetInsertOrExists(false);
          }

        } else {
          gcGrouperSyncGroup.setInTargetEnd(nowTimestamp);
        }
      }

    }
  }

  /**
   * go through what was selected from full and keep track of whats there and what isnt there
   * @param values
   */
  public void processResultsSelectEntitiesFull(
      Collection<ProvisioningEntityWrapper> values) {
    for (ProvisioningEntityWrapper provisioningEntityWrapper : GrouperUtil
        .nonNull(values)) {
      
      if (!provisioningEntityWrapper.isRecalcObject()) {
        continue;
      }
      
      ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper
          .getTargetProvisioningEntity();

      boolean exists = targetProvisioningEntity != null;
      GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
          .getGcGrouperSyncMember();

      if (gcGrouperSyncMember == null) {
        continue;
      }

      if (exists != gcGrouperSyncMember.isInTarget()) {

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        gcGrouperSyncMember.setInTarget(exists);
        if (exists) {
          gcGrouperSyncMember.setInTargetStart(nowTimestamp);
          if (StringUtils.isBlank(gcGrouperSyncMember.getInTargetInsertOrExistsDb())) {
            gcGrouperSyncMember.setInTargetInsertOrExists(false);
          }
        } else {
          gcGrouperSyncMember.setInTargetEnd(nowTimestamp);
        }
      }

    }

  }

  /**
   * go through what was selected from full and keep track of whats there and what isnt there
   * @param provisioningMembershipWrappers
   */
  public void processResultsSelectMembershipsFull(
      Collection<ProvisioningGroupWrapper> provisioningGroupWrappers,
      Collection<ProvisioningEntityWrapper> provisioningEntityWrappers,
      Collection<ProvisioningMembershipWrapper> provisioningMembershipWrappers) {

    GrouperProvisioningBehaviorMembershipType grouperProvisioningBehaviorMembershipType = this.grouperProvisioner
        .retrieveGrouperProvisioningBehavior()
        .getGrouperProvisioningBehaviorMembershipType();
    if (grouperProvisioningBehaviorMembershipType == null) {
      grouperProvisioningBehaviorMembershipType = GrouperProvisioningBehaviorMembershipType.membershipObjects;
    }
    switch (grouperProvisioningBehaviorMembershipType) {
      case groupAttributes:

      {
        String groupAttributeNameForMemberships = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getGroupMembershipAttributeName();

        // get the attribute that holds members
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getTargetGroupAttributeNameToConfig().get(groupAttributeNameForMemberships);

        String translateFromMemberAttribute = grouperProvisioningConfigurationAttribute == null
            ? null
            : grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningEntityField();

        if (StringUtils.isBlank(translateFromMemberAttribute)) {
          this.grouperProvisioner.getDebugMap()
              .put("processResultsSelectMembershipsFullCantUnresolveMemberships", true);

        } else {

          Map<MultiKey, ProvisioningMembershipWrapper> syncGroupIdSyncMemberIdToMembershipWrappersProcessed = new HashMap<MultiKey, ProvisioningMembershipWrapper>(
              this.grouperProvisioner.retrieveGrouperProvisioningDataIndex()
                  .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper());

          Map<String, GcGrouperSyncMember> provisioningAttributeToMember = new HashMap<String, GcGrouperSyncMember>();

          for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappers) {
            GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
                .getGcGrouperSyncMember();
            if (gcGrouperSyncMember != null) {
              Object provisioningAttribute = this.getGrouperProvisioner()
                  .retrieveGrouperProvisioningTranslator().translateFromGrouperProvisioningEntityField(
                      provisioningEntityWrapper, translateFromMemberAttribute);
              String provisioningAttributeString = GrouperUtil
                  .stringValue(provisioningAttribute);
              provisioningAttributeToMember.put(provisioningAttributeString,
                  gcGrouperSyncMember);
            }
          }

          Timestamp now = new Timestamp(System.currentTimeMillis());

          for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {

            GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
                .getGcGrouperSyncGroup();
            if (gcGrouperSyncGroup == null) {
              continue;
            }

            ProvisioningGroup targetProvisioningGroup = provisioningGroupWrapper
                .getTargetProvisioningGroup();
            if (targetProvisioningGroup == null) {
              continue;
            }

            Set<?> membershipAttributes = targetProvisioningGroup
                .retrieveAttributeValueSet(groupAttributeNameForMemberships);

            for (Object object : GrouperUtil.nonNull(membershipAttributes)) {
              String provisioningAttributeString = GrouperUtil.stringValue(object);

              GcGrouperSyncMember gcGrouperSyncMember = provisioningAttributeToMember
                  .get(provisioningAttributeString);
              if (gcGrouperSyncMember == null) {
                continue;
              }

              MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncGroup.getId(),
                  gcGrouperSyncMember.getId());

              ProvisioningMembershipWrapper provisioningMembershipWrapper = syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .get(syncGroupIdSyncMemberId);
              if (provisioningMembershipWrapper == null) {
                continue;
              }

              syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .remove(syncGroupIdSyncMemberId);

              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();

              if (gcGrouperSyncMembership == null) {
                continue;
              }

              // ok, this is in the target :)
              if (!gcGrouperSyncMembership.isInTarget()) {
                gcGrouperSyncMembership.setInTarget(true);
                gcGrouperSyncMembership.setInTargetStart(now);
                // its not an insert
                gcGrouperSyncMembership.setInTargetInsertOrExists(false);
              }
            }

          }

          // remaining ones are not in target
          for (ProvisioningMembershipWrapper provisioningMembershipWrapper : syncGroupIdSyncMemberIdToMembershipWrappersProcessed
              .values()) {
            
            if (!provisioningMembershipWrapper.isRecalcObject()) {
              continue;
            }
            
            GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                .getGcGrouperSyncMembership();

            if (gcGrouperSyncMembership == null) {
              continue;
            }

            // ok, this is not in the target :)
            if (gcGrouperSyncMembership.isInTarget()) {
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(now);
            }

          }

        }
      }

        break;
      case entityAttributes: {
        String entityAttributeNameForMemberships = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getEntityMembershipAttributeName();

        // get the attribute that holds members
        GrouperProvisioningConfigurationAttribute grouperProvisioningConfigurationAttribute = this.grouperProvisioner
            .retrieveGrouperProvisioningConfiguration()
            .getTargetGroupAttributeNameToConfig().get(entityAttributeNameForMemberships);

        String translateFromGroupAttribute = grouperProvisioningConfigurationAttribute == null
            ? null
            : grouperProvisioningConfigurationAttribute.getTranslateFromGrouperProvisioningGroupField();

        if (StringUtils.isBlank(translateFromGroupAttribute)) {
          this.grouperProvisioner.getDebugMap()
              .put("processResultsSelectMembershipsFullCantUnresolveMemberships", true);

        } else {

          Map<MultiKey, ProvisioningMembershipWrapper> syncGroupIdSyncMemberIdToMembershipWrappersProcessed = new HashMap<MultiKey, ProvisioningMembershipWrapper>(
              this.grouperProvisioner.retrieveGrouperProvisioningDataIndex()
                  .getGrouperSyncGroupIdGrouperSyncMemberIdToProvisioningMembershipWrapper());

          Map<String, GcGrouperSyncGroup> provisioningAttributeToGroup = new HashMap<String, GcGrouperSyncGroup>();

          for (ProvisioningGroupWrapper provisioningGroupWrapper : provisioningGroupWrappers) {
            GcGrouperSyncGroup gcGrouperSyncGroup = provisioningGroupWrapper
                .getGcGrouperSyncGroup();
            if (gcGrouperSyncGroup != null) {

              Object provisioningAttribute = this.getGrouperProvisioner()
                  .retrieveGrouperProvisioningTranslator()
                  .translateFromGrouperProvisioningGroupField(provisioningGroupWrapper, translateFromGroupAttribute);
              String provisioningAttributeString = GrouperUtil.stringValue(provisioningAttribute);

              provisioningAttributeToGroup.put(provisioningAttributeString,
                  gcGrouperSyncGroup);
            }
          }

          Timestamp now = new Timestamp(System.currentTimeMillis());

          for (ProvisioningEntityWrapper provisioningEntityWrapper : provisioningEntityWrappers) {

            GcGrouperSyncMember gcGrouperSyncMember = provisioningEntityWrapper
                .getGcGrouperSyncMember();
            if (gcGrouperSyncMember == null) {
              continue;
            }

            ProvisioningEntity targetProvisioningEntity = provisioningEntityWrapper
                .getTargetProvisioningEntity();
            if (targetProvisioningEntity == null) {
              continue;
            }

            Set<?> membershipAttributes = targetProvisioningEntity
                .retrieveAttributeValueSet(entityAttributeNameForMemberships);

            for (Object object : GrouperUtil.nonNull(membershipAttributes)) {
              String provisioningAttributeString = GrouperUtil.stringValue(object);

              GcGrouperSyncGroup gcGrouperSyncGroup = provisioningAttributeToGroup
                  .get(provisioningAttributeString);
              if (gcGrouperSyncGroup == null) {
                continue;
              }

              MultiKey syncGroupIdSyncMemberId = new MultiKey(gcGrouperSyncGroup.getId(),
                  gcGrouperSyncMember.getId());

              ProvisioningMembershipWrapper provisioningMembershipWrapper = syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .get(syncGroupIdSyncMemberId);
              if (provisioningMembershipWrapper == null) {
                continue;
              }

              syncGroupIdSyncMemberIdToMembershipWrappersProcessed
                  .remove(syncGroupIdSyncMemberId);

              GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                  .getGcGrouperSyncMembership();

              if (gcGrouperSyncMembership == null) {
                continue;
              }

              // ok, this is in the target :)
              if (!gcGrouperSyncMembership.isInTarget()) {
                gcGrouperSyncMembership.setInTarget(true);
                gcGrouperSyncMembership.setInTargetStart(now);
                // its not an insert
                gcGrouperSyncMembership.setInTargetInsertOrExists(false);
              }
            }

          }

          // remaining ones are not in target
          for (ProvisioningMembershipWrapper provisioningMembershipWrapper : syncGroupIdSyncMemberIdToMembershipWrappersProcessed
              .values()) {
            
            if (!provisioningMembershipWrapper.isRecalcObject()) {
              continue;
            }
            
            GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
                .getGcGrouperSyncMembership();

            if (gcGrouperSyncMembership == null) {
              continue;
            }

            // ok, this is not in the target :)
            if (gcGrouperSyncMembership.isInTarget()) {
              gcGrouperSyncMembership.setInTarget(false);
              gcGrouperSyncMembership.setInTargetEnd(now);
            }

          }

        }
      }
        break;

      case membershipObjects:

        for (ProvisioningMembershipWrapper provisioningMembershipWrapper : GrouperUtil
            .nonNull(provisioningMembershipWrappers)) {
          
          if (!provisioningMembershipWrapper.isRecalcObject()) {
            continue;
          }
          
          ProvisioningMembership targetProvisioningMembership = provisioningMembershipWrapper
              .getTargetProvisioningMembership();

          boolean exists = targetProvisioningMembership != null;
          GcGrouperSyncMembership gcGrouperSyncMembership = provisioningMembershipWrapper
              .getGcGrouperSyncMembership();

          if (gcGrouperSyncMembership == null) {
            continue;
          }

          if (exists != gcGrouperSyncMembership.isInTarget()) {

            Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
            gcGrouperSyncMembership.setInTarget(exists);
            if (exists) {
              gcGrouperSyncMembership.setInTargetStart(nowTimestamp);
              if (StringUtils
                  .isBlank(gcGrouperSyncMembership.getInTargetInsertOrExistsDb())) {
                gcGrouperSyncMembership.setInTargetInsertOrExists(false);
              }

            } else {
              gcGrouperSyncMembership.setInTargetEnd(nowTimestamp);
            }
          }

        }

        break;

      default:
        throw new RuntimeException(
            "Not expecting: " + grouperProvisioningBehaviorMembershipType);
    }

  }

}
