package com.nva.server.services;

import com.nva.server.entities.Conversation;

import java.util.List;
import java.util.Map;

public interface ConversationService {
    Conversation saveConversation(Conversation conversation);
    Conversation editConversation(Conversation Conversation);
    void removeConversation(Conversation Conversation);
    List<Conversation> getConversation(Map<String, Object> params);
    long getConversationCount(Map<String, Object> params);
}
