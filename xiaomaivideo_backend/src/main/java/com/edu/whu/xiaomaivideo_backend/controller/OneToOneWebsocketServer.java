/**
 * Author: 张俊杰
 * Create Time: 2020/7/15
 * Update Time: 2020/7/18
 */


package com.edu.whu.xiaomaivideo_backend.controller;

import com.edu.whu.xiaomaivideo_backend.model.*;
import com.edu.whu.xiaomaivideo_backend.service.CommentRestService;
import com.edu.whu.xiaomaivideo_backend.service.MessageRestService;
import com.edu.whu.xiaomaivideo_backend.service.MovieRestService;
import com.edu.whu.xiaomaivideo_backend.service.UserRestService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/{userId}")
@Component
@Slf4j
public class OneToOneWebsocketServer {
    public static UserRestService userRestService;


    public static MessageRestService messageRestService;


    public static CommentRestService commentRestService;


    public static MovieRestService movieRestService;

    /**
     * 用于存放所有在线客户端
     */
    private static Map<Long, OneToOneWebsocketServer> clients = new ConcurrentHashMap<>();

    private Gson gson = new Gson();

    private Session session;
    /**接收userId*/
    private Long userId;


    @OnOpen
    public void onOpen(Session session,@PathParam("userId") Long userId) {
        log.info("有新的客户端上线: {}", userId);
        this.session = session;
        this.userId = userId;
        clients.put(userId, this);
        try {
            session.getBasicRemote().sendText("连接成功"+userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose() {

        log.info("有客户端离线: {}", userId);
        clients.remove(userId);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (clients.get(userId) != null) {
            log.info("发生了错误,移除客户端: {}", userId);
            clients.remove(userId);
        }
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message) {
        log.info("收到客户端发来的消息: {}", message);
        MessageVO messageVO= gson.fromJson(message, MessageVO.class);
        User sender = userRestService.getUserById(messageVO.getSenderId());
        User receiver = userRestService.getUserById(messageVO.getReceiverId());
        switch (messageVO.getMsgType()) {
            case "msg":
                //发送私信消息以websocket形式
                Message message1=new Message();
                message1.setSender(sender);
                message1.setReceiver(receiver);
                message1.setText(messageVO.getText());
                message1.setTime(new Date());
                message1.setMsgType(messageVO.getMsgType());
                try {
                    messageRestService.saveMessage(message1);
                    log.info("成功保存信息{}",message1.getText());
                } catch (Exception e){

                }
                break;
            case "at":
                //@
                Message message2=new Message();
                message2.setSender(sender);
                message2.setReceiver(receiver);
                message2.setText(messageVO.getText());
                message2.setTime(new Date());
                message2.setAtMovie(movieRestService.getMovieById(messageVO.getMovieId()));
                message2.setMsgType(messageVO.getMsgType());
                try {
                    messageRestService.saveMessage(message2);
                    log.info("成功保存@信息{}",message2.getText());
                } catch (Exception e){

                }
                break;
            case "like":
                // 点赞使用websocket
                List<Movie> movies = sender.getLikeMovies();
                Movie movie = movieRestService.getMovieById(messageVO.getMovieId());
                movies.add(movie);
                sender.setLikeMovies(movies);
                try {
                    userRestService.saveUser(sender);
                }
                catch (Exception e) {
                }
                break;
            case "unlike":
                // 取消点赞使用websocket
                List<Movie> movies1 = sender.getLikeMovies();
                Movie movie1 = movieRestService.getMovieById(messageVO.getMovieId());
                movies1.remove(movie1);
                sender.setLikeMovies(movies1);
                try {
                    userRestService.saveUser(sender);
                }
                catch (Exception e) {
                }
                break;
            case "comment":
                //comment时，需提交movieId
                Comment comment =new Comment();
                comment.setMsg(messageVO.getText());
                comment.setCommenter(sender);
                comment.setMovie(movieRestService.getMovieById(messageVO.getMovieId()));
                comment.setTime(new Date());
                try{
                    commentRestService.saveComment(comment);
                    log.info("成功保存评论{}", comment.getMsg());
                }catch (Exception e){

                }
                break;
            case "follow":
                //follow时，不需要movieId
                List<User> followerings=sender.getFollowing();
                followerings.add(receiver);
                sender.setFollowing(followerings);
                try{
                    userRestService.saveUser(sender);
                    log.info("成功关注{}", messageVO.getText());
                }catch (Exception e){

                }
                break;
            case "unfollow":
                //unfollow时，不需要movieId
                List<User> followings1=sender.getFollowing();
                followings1.remove(receiver);
                sender.setFollowing(followings1);
                try{
                    userRestService.saveUser(sender);
                    log.info("成功取关{}", messageVO.getText());
                }catch (Exception e){

                }
                break;
        }
        this.sendTo(messageVO);
    }

    private void sendTo(MessageVO message) {
        Session s;
        try{
            s = clients.get(message.getReceiverId()).session;
        }catch (Exception e){
            log.info("当前用户不在线，不做推送:{}",message.getText());
            return;
        }
        if (s != null) {
            try {
                s.getBasicRemote().sendText(gson.toJson(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
