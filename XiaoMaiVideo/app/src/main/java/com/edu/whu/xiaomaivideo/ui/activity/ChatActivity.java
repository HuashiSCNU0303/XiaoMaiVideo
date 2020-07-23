/**
 * Author: 张俊杰
 * Create Time: 2020/7/20
 * Update Time: 2020/7/22
 */

package com.edu.whu.xiaomaivideo.ui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import com.edu.whu.xiaomaivideo.R;
import com.edu.whu.xiaomaivideo.adapter.ChatAdapter;
import com.edu.whu.xiaomaivideo.databinding.ActivityChatBinding;
import com.edu.whu.xiaomaivideo.model.Message;
import com.edu.whu.xiaomaivideo.model.MessageVO;
import com.edu.whu.xiaomaivideo.model.User;
import com.edu.whu.xiaomaivideo.restcallback.MessageRestCallback;
import com.edu.whu.xiaomaivideo.restcallback.UserRestCallback;
import com.edu.whu.xiaomaivideo.restservice.MessageRestService;
import com.edu.whu.xiaomaivideo.restservice.UserRestService;
import com.edu.whu.xiaomaivideo.util.CommonUtil;
import com.edu.whu.xiaomaivideo.util.Constant;
import com.edu.whu.xiaomaivideo.util.EventBusMessage;
import com.edu.whu.xiaomaivideo.viewModel.ChatViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {
    ChatViewModel chatViewModel;
    ActivityChatBinding activityChatBinding;
    ChatAdapter chatAdapter;
    List<Message> messageList;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatViewModel = new ViewModelProvider(Objects.requireNonNull(this)).get(ChatViewModel.class);
        activityChatBinding= DataBindingUtil.setContentView(this,R.layout.activity_chat);
        activityChatBinding.setViewmodel(chatViewModel);
        activityChatBinding.setLifecycleOwner(this);
        EventBus.getDefault().register(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        // 访问网络，获取消息
        MessageRestService.getMessages(Constant.currentChattingId, new MessageRestCallback() {
            @Override
            public void onSuccess(int resultCode, List<Message> messages) {
                super.onSuccess(resultCode, messages);
                if (messages == null) {
                    messages = new ArrayList<>();
                }
                messageList = messages;
                activityChatBinding.chatUserName.setText(Constant.currentChattingName);
                chatAdapter = new ChatAdapter(ChatActivity.this, messageList);
                setRecyclerView();
                setSubmitListener();
            }
        });
    }

    public void setRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        activityChatBinding.recyclerView.setLayoutManager(linearLayoutManager);
        activityChatBinding.recyclerView.setAdapter(chatAdapter);
        activityChatBinding.recyclerView.scrollToPosition(messageList.size()-1);
    }

    public void setSubmitListener() {
        activityChatBinding.ivConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = activityChatBinding.etInputMessage.getText().toString().trim();
                // 发给服务器
                MessageVO message = new MessageVO();
                message.setMsgType("msg");
                message.setSenderId(Constant.currentUser.getUserId());
                message.setReceiverId(Constant.currentChattingId);
                message.setText(msg);
                EventBus.getDefault().post(new EventBusMessage(Constant.SEND_MESSAGE, JSON.toJSONString(message)));
                imm.showSoftInput(activityChatBinding.etInputMessage, InputMethodManager.SHOW_FORCED);
                imm.hideSoftInputFromWindow(activityChatBinding.etInputMessage.getWindowToken(), 0);
                activityChatBinding.etInputMessage.setText("");
                // 发到界面上
                Message message1 = new Message();
                message1.setText(msg);
                message1.setSender(Constant.currentUser);
                message1.setReceiver(null);
                message1.setMsgType("msg");
                message1.setTime(new Date());
                messageList.add(message1);
                chatAdapter.notifyItemInserted(messageList.size()-1);
                activityChatBinding.recyclerView.scrollToPosition(messageList.size()-1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Constant.currentChattingId = (long) -1;
        Constant.currentChattingName = "";
    }

    @Subscribe
    public void onEventBusMessage(EventBusMessage message) {
        if (message.getType().equals(Constant.RECEIVE_MESSAGE)) {
            // 获取新消息的json串
            JSONObject jsonObject = JSON.parseObject(message.getMessage());
            Message receiveMessage = JSON.toJavaObject(jsonObject, Message.class);
            receiveMessage.setReceiver(null);
            // 加上8个小时
            receiveMessage.setTime(new Date(receiveMessage.getTime().getTime()+8*Constant.HOUR));
            // 添加到Adapter里面去
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    messageList.add(receiveMessage);
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    activityChatBinding.recyclerView.scrollToPosition(messageList.size()-1);
                }
            });
        }
    }
}