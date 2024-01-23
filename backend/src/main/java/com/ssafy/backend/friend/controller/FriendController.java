package com.ssafy.backend.friend.controller;

import com.ssafy.backend.alarm.service.AlarmService;
import com.ssafy.backend.common.exception.BaseException;
import com.ssafy.backend.common.exception.MyException;
import com.ssafy.backend.common.response.BaseResponse;
import com.ssafy.backend.common.utils.HttpResponseBody;
import com.ssafy.backend.friend.model.vo.FriendListVO;
import com.ssafy.backend.friend.service.FriendFacade;
import com.ssafy.backend.friend.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

import static com.ssafy.backend.common.response.BaseResponseStatus.*;

@RestController
@RequestMapping("friend")
public class FriendController {

    @Autowired
    FriendService friendService;

    @Autowired
    AlarmService alarmService;

    @Autowired
    FriendFacade friendFacade;

    @PostMapping("")
    public BaseResponse<?> friend(@RequestBody Map<String, String> body, HttpServletRequest request) throws MyException {
        String sign = body.get("sign");
        HttpSession session = request.getSession(false);

        if (sign == null)
            throw new BaseException(EMPTY_SESSION);

        switch (sign) {
            /**
             * [POST] /friend
             * @return ResponseEntity<HttpResponseBody <String>>
             * 친구 요청을 처리
             **/
            case "request":
//                    User user = (User) session.getAttribute("User");
//                    String userId = user.getUserId();
                String userId = "ssafy";  // request session userId
                String userId2 = body.get("userId");  // 요청하고싶은 친구 userId

                friendFacade.requestFriend(userId, userId2);

                return new BaseResponse<>(SUCCESS);
            /**
             * [POST] /friend
             * @return ResponseEntity<HttpResponseBody < String>>
             * 친구 요청에 대해서 승인
             **/
            case "accessFriend":
//                    User user = (User) session.getAttribute("User");
//                    String userId = user.getUserId();
                String accessUserId = "ssafy";  // request session Id
                String accessUserId2 = body.get("userId");

                friendFacade.accessFriend(accessUserId, accessUserId2);

                return new BaseResponse<>(SUCCESS);

            case "quitFriend":
//                    User user = (User) session.getAttribute("User");
//                    String userId = user.getUserId();
                String quitUserId = "ssafy";  // request session Id
                String quitUserId2 = body.get("userId");

                friendService.quitFriend(quitUserId, quitUserId2);

//                return ResponseEntity.ok(new HttpResponseBody<String>("성공", "친구 끊기 성공"));
                return new BaseResponse<>(SUCCESS);

        }
        throw new BaseException(NOT_MATCH_SIGN);
    }

    /**
     * [GET] /friend/count
     *
     * @return ResponseEntity<HttpResponseBody < String>>
     * 친구 요청에 대해서 승인
     **/
    @GetMapping("count")
    public ResponseEntity<HttpResponseBody<?>> countFriend() {

        //        HttpSession session = request.getSession(false);
        // User user = (User) session.getAttribute("User");
        // String userId = user.getUserId();
        String countUserId = "ssafy";  // request session Id

        Integer friends = null;
        try {
            friends = friendService.countFriend(countUserId);
        } catch (MyException e) {
            return new ResponseEntity(new HttpResponseBody<String>("실패", e.getMessage()), e.getStatus());
        }

        return ResponseEntity.ok(new HttpResponseBody<Integer>("성공", friends));
    }

    /**
     * [GET] /friend/list
     *
     * @return ResponseEntity<HttpResponseBody < FriendListVO>>
     * 친구 요청에 대해서 승인
     * 아이디, 닉네임, 상태메시지, 이메일, 랭킹, 총 공부 시간, 모꼬지명
     **/
    @GetMapping("list")
        public ResponseEntity<HttpResponseBody<?>> listFriends() {
//        HttpSession session = request.getSession(false);
            // User user = (User) session.getAttribute("User");
            // String userId = user.getUserId();
            String listUserId = "yj";  // request session Id

            FriendListVO friendListVO = new FriendListVO(friendService.countFriend(listUserId), friendService.listFriends(listUserId));

            return ResponseEntity.ok(new HttpResponseBody<FriendListVO>("성공", friendListVO));

    }


}
