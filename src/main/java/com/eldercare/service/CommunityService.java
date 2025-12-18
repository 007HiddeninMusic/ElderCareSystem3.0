// src/main/java/com/eldercare/service/CommunityService.java
package com.eldercare.service;

import com.eldercare.model.CommunityMessage;
import com.eldercare.util.DataStorageUtil;
import com.eldercare.util.IdGenerator;
import com.eldercare.util.InputValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommunityService {
    private static final CommunityService INSTANCE = new CommunityService();
    private static final String COMMUNITY_DATA_KEY = "community_messages";

    private CommunityService() {}

    public static CommunityService getInstance() {
        return INSTANCE;
    }

    /**
     * 发布新消息
     */
    public void postMessage(CommunityMessage message) throws IOException, ClassNotFoundException {
        InputValidator.validateNotEmpty(message.getElderId(), "发布者ID");
        InputValidator.validateNotEmpty(message.getContent(), "消息内容");

        message.setMessageId(IdGenerator.generateActivityId()); // 复用生成器

        List<CommunityMessage> messages = getAllMessages();
        messages.add(message);

        DataStorageUtil.saveData(COMMUNITY_DATA_KEY, messages);
        System.out.println("[CommunityService] 新消息发布成功：" + message.getElderName());
    }

    /**
     * 获取所有消息（按时间倒序）
     */
    public List<CommunityMessage> getAllMessages() throws IOException, ClassNotFoundException {
        Object data = DataStorageUtil.getData(COMMUNITY_DATA_KEY);
        List<CommunityMessage> messages = data == null ? new ArrayList<>() : (List<CommunityMessage>) data;

        // 按时间倒序排序
        return messages.stream()
                .sorted(Comparator.comparing(CommunityMessage::getCreateTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 获取指定老人的消息
     */
    public List<CommunityMessage> getMessagesByElderId(String elderId) throws IOException, ClassNotFoundException {
        return getAllMessages().stream()
                .filter(msg -> msg.getElderId().equals(elderId))
                .collect(Collectors.toList());
    }

    /**
     * 点赞消息
     */
    public void likeMessage(String messageId) throws IOException, ClassNotFoundException {
        List<CommunityMessage> messages = getAllMessages();
        for (CommunityMessage msg : messages) {
            if (msg.getMessageId().equals(messageId)) {
                msg.addLike();
                break;
            }
        }
        DataStorageUtil.saveData(COMMUNITY_DATA_KEY, messages);
    }

    /**
     * 删除消息
     */
    public boolean deleteMessage(String messageId, String elderId) throws IOException, ClassNotFoundException {
        List<CommunityMessage> messages = getAllMessages();
        boolean removed = messages.removeIf(msg ->
                msg.getMessageId().equals(messageId) && msg.getElderId().equals(elderId));

        if (removed) {
            DataStorageUtil.saveData(COMMUNITY_DATA_KEY, messages);
        }
        return removed;
    }
}