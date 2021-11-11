package edu.internet2.middleware.grouper.app.provisioning;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveEntityResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.tableSync.ProvisioningSyncIntegration;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.ui.util.ProgressBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncJob;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;

public class GrouperProvisioningDiagnosticsContainer {

  private long started = -1;
  
  /**
   * if in diagnostics
   */
  private boolean inDiagnostics;
  
  /**
   * 
   * @return true if in diagnostics execution
   */
  public boolean isInDiagnostics() {
    return this.inDiagnostics;
  }
  
  /**
   * uniquely identifies this diagnostics request as opposed to other diagnostics in other tabs
   */
  private String uniqueDiagnosticsId;
  
  /**
   * have a progress bean
   */
  private ProgressBean progressBean = new ProgressBean();
  
  private GrouperProvisioner grouperProvisioner;
  
  /**
   * have a progress bean
   * @return the progressBean
   */
  public ProgressBean getProgressBean() {
    return this.progressBean;
  }

  
  public String getUniqueDiagnosticsId() {
    return uniqueDiagnosticsId;
  }

  
  public void setUniqueDiagnosticsId(String uniqueDiagnosticsId) {
    this.uniqueDiagnosticsId = uniqueDiagnosticsId;
  }
  
  public String getReportFinal() {
    return this.report.toString();
  }


  
  public GrouperProvisioner getGrouperProvisioner() {
    return grouperProvisioner;
  }


  
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner) {
    this.grouperProvisioner = grouperProvisioner;
  }

  /**
   * report results
   */
  private StringBuilder report = new StringBuilder();

  /**
   * get report to append.  Assume the output is preformatted
   * @return report
   */
  public StringBuilder getReportInProgress() {
    return this.report;
  }
  
  /**
   * append configuration to diagnostics
   */
  public void appendConfiguration() {
    this.report.append("<h4>Configuration</h4>");
    
    Map<String, String> configuration = new TreeMap<String, String>();
    
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    
    String configPrefix = "provisioner." + this.getGrouperProvisioner().getConfigId() + ".";
    
    ProvisionerConfiguration provisionerConfiguration = this.getGrouperProvisioner().getProvisionerConfiguration();
    Map<String, GrouperConfigurationModuleAttribute> suffixToConfigAttribute = provisionerConfiguration.retrieveAttributes();
    for (String propertyName : grouperLoaderConfig.propertyNames()) {
      if (propertyName.startsWith(configPrefix)) {
        String suffix = GrouperUtil.prefixOrSuffix(propertyName, configPrefix, false);
        String lowerKey = suffix.toLowerCase();
        boolean secret = lowerKey.contains("pass") || lowerKey.contains("secret") || lowerKey.contains("private");
        
        GrouperConfigurationModuleAttribute grouperConfigurationModuleAttribute = suffixToConfigAttribute.get(suffix);
        if (grouperConfigurationModuleAttribute != null) {
          secret = secret || GrouperConfigHibernate.isPassword(
              ConfigFileName.GROUPER_LOADER_PROPERTIES, grouperConfigurationModuleAttribute.getConfigItemMetadata(), 
                propertyName, grouperLoaderConfig.propertyValueString(propertyName), true, null);
        }
        
        configuration.put(propertyName, secret ? "****** (redacted)" : grouperLoaderConfig.propertyValueString(propertyName));
      }
    }

    this.report.append("<pre>");
    for (String propertyName : configuration.keySet()) {
      this.report.append(GrouperUtil.xmlEscape(propertyName + " = " + configuration.get(propertyName))).append("\n");
    }
    this.report.append("</pre>");
    
  }  
  /**
   * run diagnostics
   */
  public void runDiagnostics() {
    this.inDiagnostics = true;
    this.started = System.currentTimeMillis();
    
    Exception exception = null;
    
    try {
      this.report = new StringBuilder();
      
      this.appendConfiguration();

      this.appendExternalSystem();

      this.appendGeneralInfo();
      
      this.appendValidation();
      
      this.appendSelectAllGroups();
      this.appendSelectAllEntities();
      this.appendSelectAllMemberships();

      this.appendSelectGroupFromGrouper();
      this.appendSelectGroupFromTarget();

      this.appendSelectEntityFromGrouper();
      this.appendSelectEntityFromTarget();
      
      this.appendInsertGroupIntoTarget();
      this.appendInsertEntityIntoTarget();

      this.appendInsertGroupAttributesMembershipIntoTarget();

      this.appendDeleteGroupFromTarget();
      this.appendDeleteEntityFromTarget();
    } catch (Exception e) {
      LOG.error("error in diagnostics", e);
      this.report.append("</pre><pre>").append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(e))).append("</pre>");
    } finally {
      this.inDiagnostics = false;

    
    }
    
    {
      Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());

      GcGrouperSyncJob gcGrouperSyncJob = this.grouperProvisioner.getGcGrouperSyncJob();
      gcGrouperSyncJob.setErrorMessage(exception == null ? null : GrouperUtil.getFullStackTrace(exception));
      gcGrouperSyncJob.setErrorTimestamp(exception == null ? null : nowTimestamp);
      gcGrouperSyncJob.setLastSyncTimestamp(nowTimestamp);
      if (this.grouperProvisioner.retrieveGrouperProvisioningDataChanges().wasWorkDone()) {
        gcGrouperSyncJob.setLastTimeWorkWasDone(nowTimestamp);
      }
      gcGrouperSyncJob.setPercentComplete(100);

      // do this in the right spot, after assigning correct sync info about sync
      int objectStoreCount = this.getGrouperProvisioner().getGcGrouperSync().getGcGrouperSyncDao().storeAllObjects();
      this.grouperProvisioner.getProvisioningSyncResult().setSyncObjectStoreCount(objectStoreCount);
  
      this.grouperProvisioner.getDebugMap().put("syncObjectStoreCount", objectStoreCount);
    }

  }

  /**
   * override this to log the external system
   */
  protected void appendExternalSystem() {
    
  }

  private ProvisioningGroupWrapper provisioningGroupWrapper = null;
  
  private ProvisioningEntityWrapper provisioningEntityWrapper = null;
  
  /**
   * select a group from grouper
   */
  private void appendSelectGroupFromGrouper() {

    this.report.append("<h4>Select group from Grouper</h4><pre>");
    
    String groupName = this.getGrouperProvisioningDiagnosticsSettings().getDiagnosticsGroupName();
    if (StringUtils.isBlank(groupName)) {
      this.report.append("<font color='orange'><b>Warning:</b></font> Group name for diagnostics is not set\n");
    } else {
    
      Group group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupName, false);
      if (group == null) {

        this.report.append("<font color='orange'><b>Warning:</b></font> Group '").append(GrouperUtil.xmlEscape(groupName)).append("' does not exist in Grouper\n");
        
      } else {

        this.report.append("<font color='gray'><b>Note:</b></font> Group: ").append(GrouperUtil.xmlEscape(group.toStringDb())).append(this.getCurrentDuration()).append("\n");

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncGroup gcGrouperSyncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveByGroupId(group.getId());
        if (gcGrouperSyncGroup == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncGroup record does not exist in database\n");
          
        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncGroup: ").append(GrouperUtil.xmlEscape(gcGrouperSyncGroup.toString())).append(this.getCurrentDuration()).append("\n");
        }
        
        List<ProvisioningGroup> grouperProvisioningGroups = this.grouperProvisioner.retrieveGrouperDao().retrieveGroups(false, GrouperUtil.toList(group.getId()));
        if (GrouperUtil.length(grouperProvisioningGroups) == 0) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningGroup object, perhaps the group is not marked as provisionable\n");
        } else {
          GrouperUtil.assertion(grouperProvisioningGroups.size() == 1, "Why is size not 1???? " + grouperProvisioningGroups.size());
          
          ProvisioningGroup grouperProvisioningGroup = grouperProvisioningGroups.get(0);
          this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (unprocessed): ").append(GrouperUtil.xmlEscape(grouperProvisioningGroup.toString())).append(this.getCurrentDuration()).append("\n");
         
          this.provisioningGroupWrapper = new ProvisioningGroupWrapper();
          grouperProvisioningGroup.setProvisioningGroupWrapper(this.provisioningGroupWrapper);
          this.provisioningGroupWrapper.setGrouperProvisioner(this.grouperProvisioner);
          this.provisioningGroupWrapper.setGrouperProvisioningGroup(grouperProvisioningGroup);
          this.provisioningGroupWrapper.setGcGrouperSyncGroup(gcGrouperSyncGroup);
          
          List<ProvisioningGroup> grouperTargetGroups = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetGroups(grouperProvisioningGroups, false, false);
          
          if (GrouperUtil.length(grouperTargetGroups) == 0) {
            this.report.append("<font color='gray'><b>Note:</b></font> Cannot find grouperTargetGroup object after translation, perhaps the group is not supposed to translate\n");
          } else {
            GrouperUtil.assertion(grouperTargetGroups.size() == 1, "Why is size not 1???? " + grouperTargetGroups.size());
            ProvisioningGroup grouperTargetGroup = grouperTargetGroups.get(0);
            this.provisioningGroupWrapper.setGrouperTargetGroup(grouperTargetGroup);
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (translated): ").append(GrouperUtil.xmlEscape(grouperTargetGroup.toString())).append("\n");
          
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForGroups(grouperTargetGroups, null);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(grouperTargetGroups, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(grouperTargetGroups);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(grouperTargetGroups);

            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningGroup (filtered, attributes manipulated, matchingId calculated): ").append(GrouperUtil.xmlEscape(grouperTargetGroup.toString())).append("\n");
            
            if (GrouperUtil.isBlank(grouperTargetGroup.getMatchingId())) {
              
              GrouperProvisioningConfigurationAttribute matchingAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveGroupAttributeMatching();
              if (matchingAttribute == null) {
                this.report.append("<font color='red'><b>Error:</b></font> Cannot find the group matching attribute/field\n");
              } else {
                if (!matchingAttribute.isInsert() && !matchingAttribute.isUpdate()) {
                  if (gcGrouperSyncGroup != null && gcGrouperSyncGroup.isInTarget()) {
                    this.report.append("<font color='red'><b>Error:</b></font> Grouper target group matching id is blank and it is currently in target\n");
                  } else {
                    this.report.append("<font color='green'><b>Success:</b></font> Grouper target group matching id is blank but it is not inserted or updated so it probably is not retrieved from target yet\n");
                  }
                } else {
                  this.report.append("<font color='red'><b>Error:</b></font> Grouper target group matching id is blank\n");
                }
              }
              
            }
            
            // validate
            this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateGroups(grouperTargetGroups, false);
            
            if (this.provisioningGroupWrapper.getErrorCode() != null) {
              this.report.append("<font color='red'><b>Error:</b></font> Group is not valid! " + this.provisioningGroupWrapper.getErrorCode() + "\n");
            } else {
              this.report.append("<font color='green'><b>Success:</b></font> Group is valid\n");
            }
          }          
        }
      }
      
    }
    this.report.append("</pre>\n");

  }
  
  /**
   * select an entity from grouper
   */
  private void appendSelectEntityFromGrouper() {

    this.report.append("<h4>Select entity from Grouper</h4><pre>");
    
    String subjectIdOrIdentifier = this.getGrouperProvisioningDiagnosticsSettings().getDiagnosticsSubjectIdOrIdentifier();
    if (StringUtils.isBlank(subjectIdOrIdentifier)) {
      this.report.append("<font color='orange'><b>Warning:</b></font> Subject id or identifier for diagnostics is not set\n");
    } else {
    
      Subject subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);

      if (subject == null) {

        this.report.append("<font color='orange'><b>Warning:</b></font> Subject '").append(GrouperUtil.xmlEscape(subjectIdOrIdentifier)).append("' is not resolvable\n");
        
      } else {

        Member member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, false);
        if (member == null) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Subject '").append(GrouperUtil.xmlEscape(subjectIdOrIdentifier)).append("' is not in the grouper_members table\n");
        } else {
          
          this.report.append("<font color='gray'><b>Note:</b></font> Member: ").append(GrouperUtil.xmlEscape(member.toString())).append(this.getCurrentDuration()).append("\n");
  
          GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
          GcGrouperSyncMember gcGrouperSyncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveByMemberId(member.getId());
          if (gcGrouperSyncMember == null) {
            this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncMember record does not exist in database\n");
          } else {
            this.report.append("<font color='gray'><b>Note:</b></font> GrouperSyncMember: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMember.toString())).append(this.getCurrentDuration()).append("\n");
            List<GcGrouperSyncMember> gcGrouperSyncMembers = new ArrayList<GcGrouperSyncMember>();
            gcGrouperSyncMembers.add(gcGrouperSyncMember);
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().setGcGrouperSyncMembers(gcGrouperSyncMembers);
          }
          
          List<ProvisioningEntity> grouperProvisioningEntities = this.grouperProvisioner.retrieveGrouperDao().retrieveMembers(false, GrouperUtil.toList(member.getId()));
          if (GrouperUtil.length(grouperProvisioningEntities) == 0) {
            this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningEntity object, perhaps entity is not a member of any provisionable groups or in the list of entities to provision\n");
          } else {
            ProvisioningEntity grouperProvisioningEntity = grouperProvisioningEntities.get(0);
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningEntity (unprocessed): ").append(GrouperUtil.xmlEscape(grouperProvisioningEntity.toString())).append(this.getCurrentDuration()).append("\n");
           
            this.provisioningEntityWrapper = new ProvisioningEntityWrapper();
            grouperProvisioningEntity.setProvisioningEntityWrapper(this.provisioningEntityWrapper);
            this.provisioningEntityWrapper.setGrouperProvisioner(this.grouperProvisioner);
            this.provisioningEntityWrapper.setGrouperProvisioningEntity(grouperProvisioningEntity);
            this.provisioningEntityWrapper.setGcGrouperSyncMember(gcGrouperSyncMember);
            
            this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper().put(provisioningEntityWrapper.getMemberId(), provisioningEntityWrapper);
            ProvisioningSyncIntegration.fullSyncMembers(
                this.getGrouperProvisioner().getProvisioningSyncResult(),
                this.getGrouperProvisioner().getGcGrouperSync(),
                this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync().getGcGrouperSyncMembers(),
                this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getMemberUuidToProvisioningEntityWrapper());
            this.grouperProvisioner.retrieveGrouperProvisioningLogic().assignSyncObjectsToWrappers();
            
            List<ProvisioningEntity> grouperTargetEntities = this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetEntities(GrouperUtil.toList(grouperProvisioningEntity), false, false);
            
            if (GrouperUtil.length(grouperTargetEntities) == 0) {
              this.report.append("<font color='gray'><b>Note:</b></font> Cannot find grouperTargetEntity object after translation, perhaps the entity is not supposed to translate\n");
            } else {
              GrouperUtil.assertion(grouperTargetEntities.size() == 1, "Why is size not 1???? " + grouperTargetEntities.size());
              ProvisioningEntity grouperTargetEntity = grouperTargetEntities.get(0);
              this.provisioningEntityWrapper.setGrouperTargetEntity(grouperTargetEntity);
              this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningEntity (translated): ").append(GrouperUtil.xmlEscape(grouperTargetEntity.toString())).append("\n");
            
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().assignDefaultsForEntities(grouperTargetEntities, null);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(grouperTargetEntities, true, false, false);
              this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(grouperTargetEntities);
              this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(grouperTargetEntities);
  
              this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningEntity (filtered, attributes manipulated, matchingId calculated): ").append(GrouperUtil.xmlEscape(grouperTargetEntity.toString())).append("\n");
              
              if (GrouperUtil.isBlank(grouperTargetEntity.getMatchingId())) {
                
                GrouperProvisioningConfigurationAttribute matchingAttribute = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveEntityAttributeMatching();
                if (matchingAttribute == null) {
                  this.report.append("<font color='red'><b>Error:</b></font> Cannot find the entity matching attribute/field\n");
                } else {
                  if (!matchingAttribute.isInsert() && !matchingAttribute.isUpdate()) {
                    if (gcGrouperSyncMember != null && gcGrouperSyncMember.isInTarget()) {
                      this.report.append("<font color='red'><b>Error:</b></font> Grouper target entity matching id is blank and it is currently in target\n");
                    } else {
                      this.report.append("<font color='green'><b>Success:</b></font> Grouper target entity matching id is blank but it is not inserted or updated so it probably is not retrieved from target yet\n");
                    }
                  } else {
                    this.report.append("<font color='red'><b>Error:</b></font> Grouper target entity matching id is blank\n");
                  }
                }
                
              }
              
              // validate
              this.getGrouperProvisioner().retrieveGrouperProvisioningValidation().validateEntities(grouperTargetEntities, false);
              
              if (this.provisioningEntityWrapper.getErrorCode() != null) {
                this.report.append("<font color='red'><b>Error:</b></font> Entity is not valid! " + this.provisioningEntityWrapper.getErrorCode() + "\n");
              } else {
                this.report.append("<font color='green'><b>Success:</b></font> Entity is valid\n");
              }
            }          
          }
        }
      }
      
    }
    this.report.append("</pre>\n");

  }
  
  /**
   * insert entity to group as a group attribute in target
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  private void appendInsertGroupAttributesMembershipIntoTarget() {
    this.report.append("<h4>Add entity to group (groupAttribute)</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupAttributesMembershipInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to add entity to group in target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target since the group does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add entity to group in target since the group has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot add entity to group in target since the entity does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot add entity to group in target since the entity has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
     
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

      Set<MultiKey> groupUuidMemberUuids = new HashSet<MultiKey>();
      MultiKey groupIdMemberId = new MultiKey(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
      groupUuidMemberUuids.add(groupIdMemberId);
      List<ProvisioningMembership> grouperProvisioningMemberships = this.grouperProvisioner.retrieveGrouperDao().retrieveMemberships(false, null, null, groupUuidMemberUuids);
      
      if (GrouperUtil.length(grouperProvisioningMemberships) == 0) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Cannot find ProvisioningMembership object\n");
      } else {
        ProvisioningMembership grouperProvisioningMembership = grouperProvisioningMemberships.get(0);

        GcGrouperSync gcGrouperSync = this.getGrouperProvisioner().getGcGrouperSync();
        GcGrouperSyncMembership gcGrouperSyncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao().membershipRetrieveByGroupIdAndMemberId(this.provisioningGroupWrapper.getGroupId(), this.provisioningEntityWrapper.getMemberId());
        if (gcGrouperSyncMembership == null) {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership record does not exist in database\n");

        } else {
          this.report.append("<font color='gray'><b>Note:</b></font> GcGrouperSyncMembership: ").append(GrouperUtil.xmlEscape(gcGrouperSyncMembership.toString())).append(this.getCurrentDuration()).append("\n");
        }

        ProvisioningMembershipWrapper provisioningMembershipWrapper = new ProvisioningMembershipWrapper();
        grouperProvisioningMembership.setProvisioningMembershipWrapper(provisioningMembershipWrapper);
        provisioningMembershipWrapper.setGrouperProvisioner(this.grouperProvisioner);
        provisioningMembershipWrapper.setGrouperProvisioningMembership(grouperProvisioningMembership);
        provisioningMembershipWrapper.setGcGrouperSyncMembership(gcGrouperSyncMembership);

        this.grouperProvisioner.retrieveGrouperProvisioningDataIndex().getGroupUuidMemberUuidToProvisioningMembershipWrapper().put(groupIdMemberId, provisioningMembershipWrapper);

        ProvisioningSyncIntegration.fullSyncMemberships(
            this.getGrouperProvisioner().getProvisioningSyncResult(),
            this.getGrouperProvisioner().getGcGrouperSync(),
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncGroups(),
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncMembers(),
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataSync()
            .getGcGrouperSyncMemberships(),
            this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
            .getGroupUuidMemberUuidToProvisioningMembershipWrapper());
        this.grouperProvisioner.retrieveGrouperProvisioningLogic().assignSyncObjectsToWrappers();

        grouperProvisioningMembership.setProvisioningGroup(this.provisioningGroupWrapper.getGrouperProvisioningGroup());
        grouperProvisioningMembership.setProvisioningEntity(this.provisioningEntityWrapper.getGrouperProvisioningEntity());
        this.grouperProvisioner.retrieveGrouperProvisioningData().getProvisioningGroupWrappers().add(this.provisioningGroupWrapper);

        this.grouperProvisioner.retrieveGrouperTranslator().translateGrouperToTargetMemberships(GrouperUtil.toList(grouperProvisioningMembership), false);

        List<ProvisioningGroup> grouperTargetGroupsToUpdate = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
        String membershipAttributeName = grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships();
        
        Collection<String> values = (Collection)this.provisioningGroupWrapper.getGrouperTargetGroup().getAttributes().get(membershipAttributeName).getValue();
        String value = values == null || values.size() == 0 ? null : values.iterator().next();
                
        if (values.size() == 0) {
          this.report.append("<font color='red'><b>Error:</b></font> No values to add after translation\n");
        } else if (values.size() > 1) {
          this.report.append("<font color='red'><b>Error:</b></font> Translation resulted in multiple values: " + values + "\n");
        } else if (((Collection)this.provisioningGroupWrapper.getTargetProvisioningGroup().getAttributes().get(membershipAttributeName).getValue()).contains(value)) {
          this.report.append("<font color='orange'><b>Warning:</b></font> Target already contains value: " + value + "\n");
        } else {
          grouperTargetGroupsToUpdate.get(0).addInternal_objectChange(
              new ProvisioningObjectChange(ProvisioningObjectChangeDataType.attribute, null, grouperProvisioner.retrieveGrouperProvisioningConfiguration().getAttributeNameForMemberships(), 
                  ProvisioningObjectChangeAction.insert, null, value)
              );
  
          this.grouperProvisioner.retrieveGrouperProvisioningCompare().removeGroupDefaultMembershipAttributeValueIfAnyAdded(grouperTargetGroupsToUpdate);
  
          for (ProvisioningObjectChange provisioningObjectChange : grouperTargetGroupsToUpdate.get(0).getInternal_objectChanges()) {
            this.report.append("<font color='gray'><b>Note:</b></font> ProvisioningObjectChange: attributeName=" + provisioningObjectChange.getAttributeName() + ", action=" + provisioningObjectChange.getProvisioningObjectChangeAction() + ", oldValue=" + provisioningObjectChange.getOldValue() + ", newValue=" + provisioningObjectChange.getNewValue() + "\n");
          }
          
          RuntimeException runtimeException = null;
          try {
            this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().updateGroups(new TargetDaoUpdateGroupsRequest(grouperTargetGroupsToUpdate));
          } catch (RuntimeException re) {
            runtimeException = re;
          } finally {
            try {
              this.grouperProvisioner.retrieveGrouperSyncDao().processResultsUpdateGroupsFull(grouperTargetGroupsToUpdate, true);
  
            } catch (RuntimeException e) {
              GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
            }
          }
  
          if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Adding entity to group in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
            return;
          }
  
          if (runtimeException != null) {
            this.report.append("<font color='red'><b>Error:</b></font> Adding entity to group in target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
            return;
          }
          this.report.append("<font color='green'><b>Success:</b></font> No error adding entity to group in target\n");
          
          // check target
          TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToUpdate, true));
          
          List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
      
          if (GrouperUtil.length(targetGroups) == 0) {
            this.report.append("<font color='red'><b>Error:</b></font> Cannot find group from target after inserting membership!\n");
          } else if (GrouperUtil.length(targetGroups) > 1) {
            this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups after inserting membership, should be 1!\n");
          } else {
            if (((Collection)targetGroups.get(0).getAttributes().get(membershipAttributeName).getValue()).contains(value)) {
              this.report.append("<font color='green'><b>Success:</b></font> Found membership in target after inserting: " + value + "\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> Did not find membership in target after inserting: " + value + "\n");
            }
          }
        }
      }
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Adding entity to group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));

    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
  }
  

  /**
   * insert group into target
   */
  private void appendInsertGroupIntoTarget() {
    this.report.append("<h4>Insert group into Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to insert group into target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null || this.provisioningGroupWrapper.getGrouperProvisioningGroup() == null 
        || this.provisioningGroupWrapper.getGrouperTargetGroup() == null ) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert group into target since does not exist in Grouper\n");
      this.report.append("</pre>\n");
      return;
    }
    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert group into target since it is already there\n");
      this.report.append("</pre>\n");
      return;
    }
    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() != null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert group into target since it is already there\n");
      this.report.append("</pre>\n");
      return;
    }
    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot insert group into target since it has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

      this.provisioningGroupWrapper.setRecalc(true);
      
      List<ProvisioningGroup> grouperTargetGroupsToInsert = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
      
      // add object change entries
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForGroupsToInsert(grouperTargetGroupsToInsert);
      
      //lets create these
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertGroups(new TargetDaoInsertGroupsRequest(grouperTargetGroupsToInsert));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertGroups(grouperTargetGroupsToInsert, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting group into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting group into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error inserting group into target\n");
      
      //retrieve so we have a copy
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToInsert, true));
      
      List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());
  
      if (GrouperUtil.length(targetGroups) == 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Cannot find group from target after inserting!\n");
        return;
      }
      if (GrouperUtil.length(targetGroups) > 1) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups after inserting, should be 1!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Found group from target after inserting\n");

      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(targetGroups, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(targetGroups);
  
      // index
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(targetGroups);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdGroups();

      this.provisioningGroupWrapper.setTargetProvisioningGroup(targetGroups.get(0));
      this.provisioningGroupWrapper.setCreate(false);
        
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Inserting group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * delete group from target
   */
  private void appendDeleteGroupFromTarget() {
    this.report.append("<h4>Delete group from Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to delete group from target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete group because there's no specified group\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningGroupWrapper != null && this.provisioningGroupWrapper.getTargetProvisioningGroup() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete group from target since it does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningGroupWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot delete group from target since it has an error code: " + this.provisioningGroupWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

      this.provisioningGroupWrapper.setRecalc(true);
      
      List<ProvisioningGroup> grouperTargetGroupsToDelete = GrouperUtil.toList(this.provisioningGroupWrapper.getGrouperTargetGroup());
      
      //lets delete
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().deleteGroups(new TargetDaoDeleteGroupsRequest(grouperTargetGroupsToDelete));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeleteGroups(grouperTargetGroupsToDelete, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningGroupWrapper.getGrouperTargetGroup().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting group from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningGroupWrapper.getGrouperTargetGroup().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting group from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error deleting group from target\n");
      
      TargetDaoRetrieveGroupsResponse targetDaoRetrieveGroupsResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroups(new TargetDaoRetrieveGroupsRequest(grouperTargetGroupsToDelete, true));
      
      List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveGroupsResponse == null ? null : targetDaoRetrieveGroupsResponse.getTargetGroups());

      if (GrouperUtil.length(targetGroups) > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetGroups) + " groups in target after deleting, should be 0!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Did not find group in target after deleting\n");
        
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Deleting group").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * insert entity into target
   */
  private void appendInsertEntityIntoTarget() {
    this.report.append("<h4>Insert entity into Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntityInsert()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to insert entity into target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper == null || this.provisioningEntityWrapper.getGrouperProvisioningEntity() == null 
        || this.provisioningEntityWrapper.getGrouperTargetEntity() == null ) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert entity into target since it is not configured to be provisioned\n");
      this.report.append("</pre>\n");
      return;
    }
    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() != null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot insert entity into target since it is already there\n");
      this.report.append("</pre>\n");
      return;
    }
    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot insert entity into target since it has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

      this.provisioningEntityWrapper.setRecalc(true);

      List<ProvisioningEntity> grouperTargetEntitiesToInsert = GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity());
      
      // add object change entries
      this.grouperProvisioner.retrieveGrouperProvisioningCompare().addInternalObjectChangeForEntitiesToInsert(grouperTargetEntitiesToInsert);
      
      //lets create these
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().insertEntities(new TargetDaoInsertEntitiesRequest(grouperTargetEntitiesToInsert));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperSyncDao().processResultsInsertEntities(grouperTargetEntitiesToInsert, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningEntityWrapper.getGrouperTargetEntity().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting entity into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningEntityWrapper.getGrouperTargetEntity().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Inserting entity into target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error inserting entity into target\n");
      
      //retrieve so we have a copy
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToInsert, true));
      
      List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());
  
      if (GrouperUtil.length(targetEntities) == 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Cannot find entity from target after inserting!\n");
        return;
      }
      if (GrouperUtil.length(targetEntities) > 1) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetEntities) + " entities after inserting, should be 1!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Found entity from target after inserting\n");

      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(targetEntities, true, false, false);
      this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(targetEntities);
  
      // index
      this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(targetEntities);
      this.grouperProvisioner.retrieveGrouperProvisioningMatchingIdIndex().indexMatchingIdEntities();

      this.provisioningEntityWrapper.setTargetProvisioningEntity(targetEntities.get(0));
      this.provisioningEntityWrapper.setCreate(false);
        
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Inserting entity").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * delete entity from target
   */
  private void appendDeleteEntityFromTarget() {
    this.report.append("<h4>Delete entity from Target</h4><pre>");
    
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntityDelete()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to delete entity from target\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete entity because there's no specified entity\n");
      this.report.append("</pre>\n");
      return;
    }

    if (this.provisioningEntityWrapper != null && this.provisioningEntityWrapper.getTargetProvisioningEntity() == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> Cannot delete entity from target since it does not exist there\n");
      this.report.append("</pre>\n");
      return;
    }

    if (null != this.provisioningEntityWrapper.getErrorCode()) {
      this.report.append("<font color='red'><b>Error:</b></font> Cannot delete entity from target since it has an error code: " + this.provisioningEntityWrapper.getErrorCode() + "\n");
      this.report.append("</pre>\n");
      return;
    }
              
    try {
      this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

      this.provisioningEntityWrapper.setRecalc(true);
      
      List<ProvisioningEntity> grouperTargetEntitiesToDelete = GrouperUtil.toList(this.provisioningEntityWrapper.getGrouperTargetEntity());
      
      //lets delete
      RuntimeException runtimeException = null;
      try {
        this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().deleteEntities(new TargetDaoDeleteEntitiesRequest(grouperTargetEntitiesToDelete));
      } catch (RuntimeException re) {
        runtimeException = re;
      } finally {
        try {
          this.grouperProvisioner.retrieveGrouperSyncDao().processResultsDeleteEntities(grouperTargetEntitiesToDelete, false);
          
        } catch (RuntimeException e) {
          GrouperUtil.exceptionFinallyInjectOrThrow(runtimeException, e);
        }
      }
      if (this.provisioningEntityWrapper.getGrouperTargetEntity().getException() != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting entity from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(this.provisioningEntityWrapper.getGrouperTargetEntity().getException())) + "\n");
        return;
      }
  
      if (runtimeException != null) {
        this.report.append("<font color='red'><b>Error:</b></font> Deleting entity from target:\n" + GrouperUtil.xmlEscape(GrouperUtil.getFullStackTrace(runtimeException)) + "\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> No error deleting entity from target\n");
      
      TargetDaoRetrieveEntitiesResponse targetDaoRetrieveEntitiesResponse = 
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveEntities(new TargetDaoRetrieveEntitiesRequest(grouperTargetEntitiesToDelete, true));
      
      List<ProvisioningEntity> targetEntities = GrouperUtil.nonNull(targetDaoRetrieveEntitiesResponse == null ? null : targetDaoRetrieveEntitiesResponse.getTargetEntities());

      if (GrouperUtil.length(targetEntities) > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Found " + GrouperUtil.length(targetEntities) + " entities in target after deleting, should be 0!\n");
        return;
      }
      this.report.append("<font color='green'><b>Success:</b></font> Did not find entity in target after deleting\n");
        
    } catch (RuntimeException re) {
      this.report.append("<font color='red'><b>Error:</b></font> Deleting entity").append(this.getCurrentDuration()).append("\n");
      this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
      
    } finally {
      String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
      debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
      this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
      this.report.append("</pre>\n");
    }
          
  }
  
  /**
   * select a group from target
   */
  private void appendSelectGroupFromTarget() {
    this.report.append("<h4>Select group from Target</h4><pre>");
    
    if (this.provisioningGroupWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> No provisioningGroupWrapper means no group to select from target\n");
    } else {
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroup(), false)
          && !GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveGroups(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve specific group(s)\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroups()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve specific group(s)\n");
      } else if (this.provisioningGroupWrapper.getGrouperTargetGroup() == null) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Grouper target group is null\n");
      } else {

        try {
            
          TargetDaoRetrieveGroupResponse targetDaoRetrieveGroupResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveGroup(
              new TargetDaoRetrieveGroupRequest(this.provisioningGroupWrapper.getGrouperTargetGroup(), true));

          if (targetDaoRetrieveGroupResponse == null) {
            this.report.append("<font color='red'><b>Error:</b></font> TargetDaoRetrieveGroupResponse is null\n");
          } else if (targetDaoRetrieveGroupResponse.getTargetGroup() == null) {
            this.report.append("<font color='gray'><b>Note:</b></font> group is not in target\n");
          } else {
            this.provisioningGroupWrapper.setTargetProvisioningGroup(targetDaoRetrieveGroupResponse.getTargetGroup());
            this.report.append("<font color='gray'><b>Note:</b></font> Target group (unprocessed): ")
              .append(GrouperUtil.xmlEscape(targetDaoRetrieveGroupResponse.getTargetGroup().toString())).append(this.getCurrentDuration()).append("\n");
            
            List<ProvisioningGroup> targetGroupsForOne = GrouperUtil.toList(targetDaoRetrieveGroupResponse.getTargetGroup());
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
                targetGroupsForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
                targetGroupsForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
                targetGroupsForOne);

            this.report.append("<font color='gray'><b>Note:</b></font> Target group (filtered, attributes manipulated, matchingId calculated):\n  ")
              .append(GrouperUtil.xmlEscape(targetDaoRetrieveGroupResponse.getTargetGroup().toString())).append("\n");

            if (GrouperUtil.isBlank(targetDaoRetrieveGroupResponse.getTargetGroup().getMatchingId())) {
              this.report.append("<font color='red'><b>Error:</b></font> Target group matching id is blank\n");
            }
            
            if (!GrouperUtil.equals(this.provisioningGroupWrapper.getGrouperTargetGroup().getMatchingId(), targetDaoRetrieveGroupResponse.getTargetGroup().getMatchingId())) {
              this.report.append("<font color='red'><b>Error:</b></font> Matching id's do not match!\n");
            }
            
          }
          
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting specific group(s)").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    this.report.append("</pre>\n");

  }
  
  /**
   * select an entity from target
   */
  private void appendSelectEntityFromTarget() {
    this.report.append("<h4>Select entity from Target</h4><pre>");
    
    if (this.provisioningEntityWrapper == null) {
      this.report.append("<font color='gray'><b>Note:</b></font> No provisioningEntityWrapper means no entity to select from target\n");
    } else {
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntity(), false)
          && !GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveEntities(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve specific entities(s)\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntities()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve specific entities(s)\n");
      } else if (this.provisioningEntityWrapper.getGrouperTargetEntity() == null) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Grouper target entity is null\n");
      } else {

        try {
            
          TargetDaoRetrieveEntityResponse targetDaoRetrieveEntityResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveEntity(
              new TargetDaoRetrieveEntityRequest(this.provisioningEntityWrapper.getGrouperTargetEntity(), true));

          if (targetDaoRetrieveEntityResponse == null) {
            this.report.append("<font color='red'><b>Error:</b></font> TargetDaoRetrieveEntityResponse is null\n");
          } else if (targetDaoRetrieveEntityResponse.getTargetEntity() == null) {
            this.report.append("<font color='gray'><b>Note:</b></font> entity is not in target\n");
          } else {
            this.provisioningEntityWrapper.setTargetProvisioningEntity(targetDaoRetrieveEntityResponse.getTargetEntity());
            this.report.append("<font color='gray'><b>Note:</b></font> Target entity (unprocessed): ")
              .append(GrouperUtil.xmlEscape(targetDaoRetrieveEntityResponse.getTargetEntity().toString())).append(this.getCurrentDuration()).append("\n");
            
            List<ProvisioningEntity> targetEntitiesForOne = GrouperUtil.toList(targetDaoRetrieveEntityResponse.getTargetEntity());
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
                targetEntitiesForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
                targetEntitiesForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
                targetEntitiesForOne);

            this.report.append("<font color='gray'><b>Note:</b></font> Target entity (filtered, attributes manipulated, matchingId calculated):\n  ")
              .append(GrouperUtil.xmlEscape(targetDaoRetrieveEntityResponse.getTargetEntity().toString())).append("\n");

            if (GrouperUtil.isBlank(targetDaoRetrieveEntityResponse.getTargetEntity().getMatchingId())) {
              this.report.append("<font color='red'><b>Error:</b></font> Target entity matching id is blank\n");
            }
            
            if (!GrouperUtil.equals(this.provisioningEntityWrapper.getGrouperTargetEntity().getMatchingId(), targetDaoRetrieveEntityResponse.getTargetEntity().getMatchingId())) {
              this.report.append("<font color='red'><b>Error:</b></font> Matching id's do not match!\n");
            }
            
          }
          
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting specific entity").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    this.report.append("</pre>\n");

  }
  
  private void appendValidation() {
    
    this.report.append("<h4>Validation</h4><pre>");

    {
      List<String> errorsToDisplay = new ArrayList<String>();
      
      Map<String, String> validationErrorsToDisplay = new LinkedHashMap<String, String>();
      
      this.getGrouperProvisioner().getProvisionerConfiguration().validatePreSave(false, errorsToDisplay, validationErrorsToDisplay);
  
      if (errorsToDisplay.size() > 0 || validationErrorsToDisplay.size() > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Provisioner config JSON rule violations: ")
          .append(errorsToDisplay.size() + validationErrorsToDisplay.size()).append("\n");
        for (String errorToDisplay : errorsToDisplay) {
          this.report.append("<font color='red'><b>Error:</b></font> " + GrouperUtil.xmlEscape(errorToDisplay)).append("\n");
        }
        for (String validationKeyError : validationErrorsToDisplay.keySet()) {
          this.report.append("<font color='red'><b>Error:</b></font> in config item '" + validationKeyError + "': " + GrouperUtil.xmlEscape(validationErrorsToDisplay.get(validationKeyError))).append("\n");
        }
      } else {
        this.report.append("<font color='green'><b>Success:</b></font> Provisioner config satisfies configuration JSON rules\n");
      }
    }

    {
      List<MultiKey> errors = this.getGrouperProvisioner().retrieveGrouperProvisioningConfigurationValidation().validate();
      if (errors.size() > 0) {
        this.report.append("<font color='red'><b>Error:</b></font> Provisioner config validation rule violations: ")
          .append(errors.size()).append("\n");
        for (MultiKey errorMultikey : errors) {
          String error = (String)errorMultikey.getKey(0);
          if (errorMultikey.size() > 1 && !StringUtils.isBlank((String)errorMultikey.getKey(1))) {
            String validationKeyError = (String)errorMultikey.getKey(1);
            this.report.append("<font color='red'><b>Error:</b></font> in config item '" + validationKeyError + "': " + GrouperUtil.xmlEscape(error)).append("\n");
          } else {
            this.report.append("<font color='red'><b>Error:</b></font> " + GrouperUtil.xmlEscape(error)).append("\n");
          }
        }
      } else {
        this.report.append("<font color='green'><b>Success:</b></font> Provisioner config satisfies validation rules\n");
      }
    }
    
    this.report.append("</pre>\n");
    
  }


  private void appendGeneralInfo() {
    this.report.append("<h4>Provisioner</h4><pre>");
    GrouperProvisioningObjectLogType.appendProvisioner(grouperProvisioner, this.report, "Provisioner");
    this.report.append("</pre>\n<h4>Configuration analysis</h4><pre>");
    GrouperProvisioningObjectLogType.appendConfiguration(grouperProvisioner, this.report, "Configuration");
    this.report.append("</pre>\n<h4>Target Dao capabilities</h4><pre>");
    GrouperProvisioningObjectLogType.appendTargetDaoCapabilities(grouperProvisioner, this.report, "Target Dao capabilities");
    this.report.append("</pre>\n<h4>Provisioner behaviors</h4><pre>");
    GrouperProvisioningObjectLogType.appendTargetDaoBehaviors(grouperProvisioner, this.report, "Provisioner behaviors");
    this.report.append("</pre>\n");
    
  }


  /** 
   * get current duration
   * @return duration
   */
  private String getCurrentDuration() {
    return " (elapsed: " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - this.started) + ")";
  }
  
  public void appendSelectAllGroups() {
    this.report.append("<h4>All groups</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsGroupsAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all groups\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllGroups(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all groups\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectGroupsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all groups\n");
      } else {

        try {
            
          this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStart();

          TargetDaoRetrieveAllGroupsResponse targetDaoRetrieveAllGroupsResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllGroups(
              new TargetDaoRetrieveAllGroupsRequest(this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));
          List<ProvisioningGroup> targetGroups = GrouperUtil.nonNull(targetDaoRetrieveAllGroupsResponse == null ? null : targetDaoRetrieveAllGroupsResponse.getTargetGroups());
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningGroups(targetGroups);

          if (GrouperUtil.length(targetGroups) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetGroups) + " groups")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetGroups) + " groups")
              .append(this.getCurrentDuration()).append("\n");
          }

          for (int i=0;i<Math.min(10, GrouperUtil.length(targetGroups)); i++) {
            ProvisioningGroup targetGroup = targetGroups.get(i);

            this.report.append("<font color='gray'><b>Note:</b></font> Group ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetGroups)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");
          }
          this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterGroupFieldsAndAttributes(
              targetGroups, true, false, false);
          this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesGroups(
              targetGroups);
          this.grouperProvisioner.retrieveGrouperTranslator().idTargetGroups(
              targetGroups);
          for (int i=0;i<Math.min(10, GrouperUtil.length(targetGroups)); i++) {
            ProvisioningGroup targetGroup = targetGroups.get(i);

            this.report.append("<font color='gray'><b>Note:</b></font> Group ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetGroups)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetGroup.toString())).append("\n");
          }

          {
            int countWithoutMatchingId = 0;
            for (int i=0;i<GrouperUtil.length(targetGroups);i++) {
              ProvisioningGroup targetGroup = targetGroups.get(i);
              if (GrouperUtil.isBlank(targetGroup.getMatchingId())) {
                countWithoutMatchingId++;
              }
            }
            if (countWithoutMatchingId == 0) {
              this.report.append("<font color='green'><b>Success:</b></font> All target groups have a matching id")
                .append(this.getCurrentDuration()).append("\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> " + countWithoutMatchingId + " target groups do not have a matching id")
                .append(this.getCurrentDuration()).append("\n");
            }
          }
          
          {
            int countWithDuplicateMatchingId = 0;
            Set<Object> matchingIds = new HashSet<Object>();
            Set<Object> firstTen = new HashSet<Object>();
            for (int i=0;i<GrouperUtil.length(targetGroups);i++) {
              ProvisioningGroup targetGroup = targetGroups.get(i);
              if (!GrouperUtil.isBlank(targetGroup.getMatchingId())) {
                if (matchingIds.contains(targetGroup.getMatchingId())) {
                  countWithDuplicateMatchingId++;
                  if (firstTen.size() <= 10) {
                    firstTen.add(targetGroup.getMatchingId());
                  }
                } else {
                  matchingIds.add(targetGroup.getMatchingId());
                }
              }
            }
            if (countWithDuplicateMatchingId == 0) {
              this.report.append("<font color='green'><b>Success:</b></font> All target groups have unique matching ids")
                .append(this.getCurrentDuration()).append("\n");
            } else {
              this.report.append("<font color='red'><b>Error:</b></font> " + countWithDuplicateMatchingId + " target groups have a duplicate matching id, e.g. " + GrouperUtil.toStringForLog(firstTen, 1000))
                .append(this.getCurrentDuration()).append("\n");
            }
          }
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all groups").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        } finally {
          String debugInfo = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().loggingStop();
          debugInfo = StringUtils.defaultString(debugInfo, "None implemented for this DAO");
          this.report.append("<font color='gray'><b>Note:</b></font> Debug info:").append(this.getCurrentDuration()).append(" ").append(GrouperUtil.xmlEscape(StringUtils.trim(debugInfo))).append("\n");
        }
      }
    }
    
    this.report.append("</pre>");
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningDiagnosticsContainer.class);

  /**
   * append this to log, and put a not before each line
   * this will escape html
   * @param string
   */
  public void appendReportLineIfNotBlank(String string) {
    if (!StringUtils.isBlank(string) && this.inDiagnostics) {
      int lineNumber = 0;
      for (String line : GrouperUtil.splitTrim(string, "\n")) {
        if (StringUtils.isBlank(line)) {
          continue;
        }
        if (!line.startsWith("<font color='")) {
          this.report.append("<font color='gray'><b>Note:</b></font> ").append(GrouperUtil.xmlEscape(StringUtils.abbreviate(line, 3000))).append("\n");
        } else {
          this.report.append(line).append("\n");
        }
        if (++lineNumber >= 50) {
          this.report.append("<font color='gray'><b>Note:</b></font> Only showing 50 lines\n");
          break;
        }
      }
    }
    
  }

  /**
   * settings for how diagnostics is going to go
   */
  private GrouperProvisioningDiagnosticsSettings grouperProvisioningDiagnosticsSettings = new GrouperProvisioningDiagnosticsSettings();
  
  /**
   * settings for how diagnostics is going to go
   * @return
   */
  public GrouperProvisioningDiagnosticsSettings getGrouperProvisioningDiagnosticsSettings() {
    return grouperProvisioningDiagnosticsSettings;
  }


  
  public void appendSelectAllEntities() {
    this.report.append("<h4>All entities</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsEntitiesAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all entities\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllEntities(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all entities\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectEntitiesAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all entities\n");
      } else {
  
        try {
            
          TargetDaoRetrieveAllEntitiesResponse targetDaoRetrieveAllEntitiesResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllEntities(
              new TargetDaoRetrieveAllEntitiesRequest(this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()));
          List<ProvisioningEntity> targetEntities = targetDaoRetrieveAllEntitiesResponse == null ? null : targetDaoRetrieveAllEntitiesResponse.getTargetEntities();
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningEntities(targetEntities);
  
          if (GrouperUtil.length(targetEntities) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetEntities) + " entities")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetEntities) + " entities")
              .append(this.getCurrentDuration()).append("\n");
          }
          
          for (int i=0;i<Math.min(10,GrouperUtil.length(targetEntities)); i++) {
            ProvisioningEntity targetEntity = targetEntities.get(i);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Entity ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetEntities)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetEntity.toString())).append("\n");
  
            List<ProvisioningEntity> targetEntitiesForOne = GrouperUtil.toList(targetEntity);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterEntityFieldsAndAttributes(
                targetEntitiesForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesEntities(
                targetEntitiesForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetEntities(
                targetEntitiesForOne);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Entity ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetEntities)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetEntity.toString())).append("\n");
            
          }
  
  
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all entities").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  public void appendSelectAllMemberships() {
    this.report.append("<h4>All memberships</h4>");
    this.report.append("<pre>");
    if (!this.getGrouperProvisioningDiagnosticsSettings().isDiagnosticsMembershipsAllSelect()) {
      this.report.append("<font color='gray'><b>Note:</b></font> Not configured to retrieve all memberships\n");
    } else if (this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() != GrouperProvisioningBehaviorMembershipType.membershipObjects) {
      this.report.append("<font color='gray'><b>Note:</b></font> Membership type is: " + this.grouperProvisioner.retrieveGrouperProvisioningBehavior().getGrouperProvisioningBehaviorMembershipType() + "\n");
    } else {
    
      if (!GrouperUtil.booleanValue(this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().getWrappedDao().getGrouperProvisionerDaoCapabilities().getCanRetrieveAllMemberships(), false)) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Target DAO cannot retrieve all memberships\n");
      } else if (!this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().isSelectMembershipsAll()) {
        this.report.append("<font color='orange'><b>Warning:</b></font> Provisioning behavior is to not retrieve all memberships\n");
      } else {
  
        try {
            
          TargetDaoRetrieveAllMembershipsResponse targetDaoRetrieveAllMembershipsResponse = this.grouperProvisioner.retrieveGrouperTargetDaoAdapter().retrieveAllMemberships(new TargetDaoRetrieveAllMembershipsRequest());
          List<ProvisioningMembership> targetMemberships = targetDaoRetrieveAllMembershipsResponse == null ? null : targetDaoRetrieveAllMembershipsResponse.getTargetMemberships();
          this.grouperProvisioner.retrieveGrouperProvisioningDataTarget().getTargetProvisioningObjects().setProvisioningMemberships(targetMemberships);
  
          if (GrouperUtil.length(targetMemberships) > 0) {
            this.report.append("<font color='green'><b>Success:</b></font> Selected " + GrouperUtil.length(targetMemberships) + " memberships")
              .append(this.getCurrentDuration()).append("\n");
          } else {
            this.report.append("<font color='orange'><b>Warning:</b></font> Selected " + GrouperUtil.length(targetMemberships) + " memberships")
              .append(this.getCurrentDuration()).append("\n");
          }
          
          for (int i=0;i<Math.min(10,GrouperUtil.length(targetMemberships)); i++) {
            ProvisioningMembership targetMembership = targetMemberships.get(i);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Membership ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetMemberships)).append(" (unprocessed):\n  ").append(GrouperUtil.xmlEscape(targetMembership.toString())).append("\n");
  
            List<ProvisioningMembership> targetMembershipsForOne = GrouperUtil.toList(targetMembership);
            
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().filterMembershipFieldsAndAttributes(
                targetMembershipsForOne, true, false, false);
            this.grouperProvisioner.retrieveGrouperProvisioningAttributeManipulation().manipulateAttributesMemberships(
                targetMembershipsForOne);
            this.grouperProvisioner.retrieveGrouperTranslator().idTargetMemberships(
                targetMembershipsForOne);
  
            this.report.append("<font color='gray'><b>Note:</b></font> Membership ").append(i+1).append(" of ")
              .append(GrouperUtil.length(targetMemberships)).append(" (filtered, attributes manipulated, matchingId calculated):\n  ").append(GrouperUtil.xmlEscape(targetMembership.toString())).append("\n");
            
          }
  
  
          
        } catch (RuntimeException re) {
          this.report.append("<font color='red'><b>Error:</b></font> Selecting all memberships").append(this.getCurrentDuration()).append("\n");
          this.report.append(GrouperUtil.xmlEscape(ExceptionUtils.getFullStackTrace(re)));
          
        }
      }
    }
    
    this.report.append("</pre>");
  }

  /**
   * init the config of diagnostics from provisioner configuration
   */
  public void initFromConfiguration() {
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupsAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsGroupsAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsEntitiesAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsEntitiesAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsMembershipsAllSelect(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().isDiagnosticsMembershipsAllSelect());
    this.getGrouperProvisioningDiagnosticsSettings().setDiagnosticsGroupName(this.grouperProvisioner.retrieveGrouperProvisioningConfiguration().getDiagnosticsGroupName());
    
  }
  
  
  
}
