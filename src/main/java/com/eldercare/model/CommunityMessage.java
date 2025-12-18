package com.eldercare.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 社区消息模型类
 * 功能：对应老人社区交流中的消息，包含点赞、评论等社交功能
 */
public class CommunityMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    // 消息ID
    private String messageId;
    // 老人ID
    private String elderId;
    // 老人姓名
    private String elderName;
    // 消息内容
    private String content;
    // 创建时间
    private Date createTime;
    // 消息类型："text", "image", "video"
    private String messageType;
    // 点赞数
    private int likeCount;
    // 评论数
    private int commentCount;

    // 构造方法
    public CommunityMessage() {
        this.createTime = new Date();
        this.likeCount = 0;
        this.commentCount = 0;
    }

    // Getter和Setter方法
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getElderId() { return elderId; }
    public void setElderId(String elderId) { this.elderId = elderId; }

    public String getElderName() { return elderName; }
    public void setElderName(String elderName) { this.elderName = elderName; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    /**
     * 点赞
     */
    public void addLike() { this.likeCount++; }

    /**
     * 添加评论
     */
    public void addComment() { this.commentCount++; }

    @Override
    public String toString() {
        return "CommunityMessage{" +
                "messageId='" + messageId + '\'' +
                ", elderId='" + elderId + '\'' +
                ", elderName='" + elderName + '\'' +
                ", content='" + (content.length() > 50 ? content.substring(0, 50) + "..." : content) + '\'' +
                ", createTime=" + createTime +
                ", likeCount=" + likeCount +
                ", commentCount=" + commentCount +
                '}';
    }
}