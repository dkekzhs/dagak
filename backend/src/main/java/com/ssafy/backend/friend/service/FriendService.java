package com.ssafy.backend.friend.service;

import com.ssafy.backend.friend.model.vo.FriendVO;

import java.util.List;


public interface FriendService {

    public void requestFriend(String userId, String userId2);

    public void accessFriend(String accessUserId, String accessUuserId2);

    public void quitFriend(String quitUserId, String quitUserId2);

    int countFriend(String countUserId);

    List<FriendVO> listFriends(String listUserId);
}