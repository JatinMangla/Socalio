package models;

public class ModelGroupChatList {

    String groupId,grpDescription,groupTitle,groupIcon,createdBy,timestamp;

    public ModelGroupChatList() {
    }

    public ModelGroupChatList(String groupId, String grpDescription, String groupTitle, String groupIcon, String createdBy, String timestamp) {
        this.groupId = groupId;
        this.grpDescription = grpDescription;
        this.groupTitle = groupTitle;
        this.groupIcon = groupIcon;
        this.createdBy = createdBy;
        this.timestamp = timestamp;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGrpDescription() {
        return grpDescription;
    }

    public void setGrpDescription(String grpDescription) {
        this.grpDescription = grpDescription;
    }

    public String getGroupTitle() {
        return groupTitle;
    }

    public void setGroupTitle(String groupTitle) {
        this.groupTitle = groupTitle;
    }

    public String getGroupIcon() {
        return groupIcon;
    }

    public void setGroupIcon(String groupIcon) {
        this.groupIcon = groupIcon;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
