package com.baoshiyun.demo.chat;


import com.baoshiyun.warrior.im.MessageInfo;

import java.util.List;

/**
 * 消息数据适配器
 * Created by ljt on 2020/12/3.
 */
public interface IMessageAdapter {
    /**
     * 批量添加消息
     *
     * @param messages 消息列表
     * @param front    插入到列表头部还是末尾
     */
    void addMessage(List<MessageInfo> messages, boolean front);

    /**
     * 向末尾添加一条消息
     *
     * @param messages 消息
     */
    void addMessage(MessageInfo messages);

    /**
     * 删除一条消息
     *
     * @param message 消息
     */
    void deleteMessage(MessageInfo message);

    /**
     * 更新一条消息
     *
     * @param message 消息
     */
    void updateMessage(MessageInfo message);
}
