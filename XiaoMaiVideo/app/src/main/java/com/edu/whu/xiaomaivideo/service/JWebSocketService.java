/**
 * Author: 叶俊豪
 * Create Time: 2020/7/15
 * Update Time: 2020/7/16
 */

package com.edu.whu.xiaomaivideo.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.edu.whu.xiaomaivideo.model.MessageVO;
import com.edu.whu.xiaomaivideo.model.User;
import com.edu.whu.xiaomaivideo.util.Constant;
import com.edu.whu.xiaomaivideo.util.EventBusMessage;
import com.edu.whu.xiaomaivideo.util.JWebSocketClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.java_websocket.handshake.ServerHandshake;
import org.parceler.Parcels;

import java.net.URI;
import java.util.List;

public class JWebSocketService extends Service {

    public JWebSocketClient client;
    private JWebSocketClientBinder mBinder = new JWebSocketClientBinder();
    private final static int GRAY_SERVICE_ID = 1001;


    //灰色保活
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(GRAY_SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


    //用于Activity和service通讯
    public class JWebSocketClientBinder extends Binder {
        public JWebSocketService getService() {
            return JWebSocketService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        EventBus.getDefault().register(this);
        initSocketClient();
        mHandler.postDelayed(heartBeatRunnable, HEART_BEAT_RATE); //开启心跳检测
        return START_STICKY;
    }

    // Service自己是订阅者，别人通知它发消息
    @Subscribe
    public void onEventBusMessage(EventBusMessage message) {
        if (message.getType().equals(Constant.SEND_MESSAGE)) {
            sendMsg(message.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        closeConnect();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public JWebSocketService() {
    }

    private void initSocketClient() {
        URI uri = URI.create(Constant.WS_URL+Constant.CurrentUser.getUserId());
        client = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                Log.e("JWebSocketClientService", "收到的消息：" + message);
                if (message.startsWith("连接成功")) {
                    // 连接成功消息，无视掉
                    return;
                }
                JSONObject jsonObject = JSON.parseObject(message);
                MessageVO messageVO = JSON.toJavaObject(jsonObject, MessageVO.class);
                if (messageVO.getMsgType().equals("like")) {
                    // 点赞，发送点赞通知
                    Log.e("JWebSocketClientService", "收到的消息：点赞");
                }
                else if (messageVO.getMsgType().equals("comment")) {
                    // 评论，发送评论通知
                }
                else if (messageVO.getMsgType().equals("msg")) {
                    // 如果处于聊天状态，就调用下面的代码提醒聊天页面（好像还没做，先不管它）更新消息
                    // EventBus.getDefault().post(new EventBusMessage(Constant.RECEIVE_MESSAGE, message));
                }
                // 消息统一存到本地数据库里，打开消息提醒页面以后再加载
                // messageVO.save();
            }

            @Override
            public void onOpen(ServerHandshake handShakeData) {
                super.onOpen(handShakeData);
                Log.e("JWebSocketClientService", "websocket连接成功");
            }
        };
        connect();
    }

    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    client.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void sendMsg(String msg) {
        if (null != client) {
            Log.e("JWebSocketClientService", "发送的消息：" + msg);
            client.send(msg);
        }
    }

    private void closeConnect() {
        try {
            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
            mHandler.removeCallbacks(heartBeatRunnable);
        }
    }


    //    -------------------------------------websocket心跳检测------------------------------------------------
    private static final long HEART_BEAT_RATE = 10 * 1000; //每隔10秒进行一次对长连接的心跳检测
    private Handler mHandler = new Handler();
    private Runnable heartBeatRunnable = new Runnable() {
        @Override
        public void run() {
            Log.e("JWebSocketClientService", "心跳包检测websocket连接状态");
            if (client != null) {
                if (client.isClosed()) {
                    reconnectWs();
                }
            } else {
                //如果client已为空，重新初始化连接
                client = null;
                initSocketClient();
            }
            //每隔一定的时间，对长连接进行一次心跳检测
            mHandler.postDelayed(this, HEART_BEAT_RATE);
        }
    };

    private void reconnectWs() {
        mHandler.removeCallbacks(heartBeatRunnable);
        new Thread() {
            @Override
            public void run() {
                try {
                    Log.e("JWebSocketClientService", "开启重连");
                    client.reconnectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
