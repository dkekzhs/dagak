package com.ssafy.backend.room.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.backend.common.exception.BaseException;
import com.ssafy.backend.room.model.domain.Answer;
import com.ssafy.backend.room.model.domain.Question;
import com.ssafy.backend.room.model.domain.redis.AnswerRedis;
import com.ssafy.backend.room.model.domain.redis.QuestionRedis;
import com.ssafy.backend.room.model.dto.AnswerDTO;
import com.ssafy.backend.room.model.repository.AnswerRepository;
import com.ssafy.backend.room.model.repository.QuestionRepository;
import com.ssafy.backend.room.model.vo.AnswerVO;
import com.ssafy.backend.room.model.vo.ConnectionVO;
import com.ssafy.backend.room.model.dto.QuestionDTO;
import com.ssafy.backend.room.model.dto.EnterRoomDTO;
import com.ssafy.backend.room.model.repository.redis.AnswerRedisRepository;
import com.ssafy.backend.room.model.repository.redis.QuestionRedisRepository;
import com.ssafy.backend.room.model.vo.QuestionVO;
import com.ssafy.backend.user.model.dto.OpenviduRequestDTO;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.ssafy.backend.common.response.BaseResponseStatus.NOT_EXIST_SESSION;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    private final AnswerRedisRepository answerRedisRepository;
    private final QuestionRedisRepository questionRedisRepository;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    private OpenVidu openvidu;

    @PostConstruct
    public void init() {
        this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    @Override
    public String enterMyroom(EnterRoomDTO enterRoomDto) throws Exception {
        openvidu.fetch();
        Session session = openvidu.getActiveSession(enterRoomDto.getSessionName());
        if(session == null){
            HashMap<String,String> SessionPropertyJson = enterRoomDto.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        }
        ConnectionProperties properties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(properties);
        return connection.getToken();
    }

    @Override
    public ConnectionVO enterRandomroom(EnterRoomDTO enterRoomDTO) throws Exception {
        String sessionName = enterRoomDTO.getSessionName();
        Session session;

        if(enterRoomDTO.getPrevSession() == null){
            sessionName = getRandomroom(sessionName);
        }  else { // 기존연결이 있다면 재접속
            sessionName = enterRoomDTO.getPrevSession();
        }
        enterRoomDTO.setSessionName(sessionName);
        openvidu.fetch();
        session = openvidu.getActiveSession(sessionName);

        if(session == null){    // 방이 존재하지 않다면 생성하라
            System.out.println("========================================");
            System.out.println("존재하지 않는 방에 입장합니다: "+sessionName);
//            deletePreviousConnection(enterRoomDTO);
            HashMap<String,String> SessionPropertyJson = enterRoomDTO.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            openvidu.createSession(properties);
        } else{                 // 새로고침할떄 다른 세션에 있는 나의 연결을 삭제한다.
            System.out.println("========================================");
            System.out.println("존재하는 방에 입장합니다: "+sessionName);
//            deletePreviousConnection(enterRoomDTO);
        }

        openvidu.fetch();
        session = openvidu.getActiveSession(sessionName);
        if(session == null){    // 이 세션이 나혼자 있었으면 연결이 끊기면서 방도 사라질수있으므로, 다시 방을 만든다
            HashMap<String,String> SessionPropertyJson = enterRoomDTO.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        }

        ConnectionProperties properties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(properties);
        ConnectionVO connectionVO = new ConnectionVO(connection.getConnectionId(),sessionName,connection.getToken());
        System.out.println("새로운 연결: "+connectionVO.getConnectionId() +" / " + connectionVO.getSession());
        return connectionVO;
    }

    @Override
    public String enterMoccojiroom(EnterRoomDTO enterRoomDTO) throws Exception {
        String SessionName = enterRoomDTO.getSessionName();
        Session session;
        session = openvidu.getActiveSession(SessionName);

        if(session == null){
            HashMap<String,String> SessionPropertyJson = enterRoomDTO.toSessionPropertyJson();
            SessionProperties properties = SessionProperties.fromJson(SessionPropertyJson).build();
            session = openvidu.createSession(properties);
        }

        ConnectionProperties properties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(properties);

        return connection.getToken();
    }

    @Override
    public QuestionVO askQuestion(QuestionDTO questionDTO) throws Exception {
        String sessionId = questionDTO.getSession();
        Session session;

        openvidu.fetch();
        session = openvidu.getActiveSession(sessionId);
        if(session == null){
            throw new BaseException(NOT_EXIST_SESSION);
        }

        // DB에 질문 저장
        QuestionRedis questionRedis = saveQuestion(questionDTO);
        QuestionVO questionVO = new QuestionVO(questionRedis.getQuestionId(),questionDTO.getUserId(),questionDTO.getSession(),questionDTO.getData());

        ObjectMapper om = new ObjectMapper();
        OpenviduRequestDTO openviduRequestDTO = new OpenviduRequestDTO(sessionId,"question",om.writeValueAsString(questionVO));
        URI uri = UriComponentsBuilder
                .fromUriString(OPENVIDU_URL)
                .path("/openvidu/api/signal")
                .encode()
                .build()
                .toUri();

        String secret = "Basic " + OPENVIDU_SECRET;
        secret = Base64.getEncoder().encodeToString(secret.getBytes());

        RequestEntity<String> requestEntity = RequestEntity
                .post(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic T1BFTlZJRFVBUFA6TVlfU0VDUkVU")
                .body(openviduRequestDTO.toJson());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        restTemplate.postForEntity(uri, requestEntity, Object.class);
        return questionVO;
    }

    @Override
    public AnswerVO answerQuestion(AnswerDTO answerDTO) throws Exception {
        String sessionId = answerDTO.getSession();
        Session session;

        openvidu.fetch();
        session = openvidu.getActiveSession(sessionId);
        if(session == null){
            throw new BaseException(NOT_EXIST_SESSION);
        }

        // Redis에 답변 저장
        AnswerRedis answerRedis = saveAnswer(answerDTO);
        System.out.println("AnswerId: "+ answerRedis.getAnswerId());
        AnswerVO answerVO = new AnswerVO(answerRedis.getAnswerId(),answerDTO.getUserId(),answerDTO.getSession(),answerDTO.getData(),answerDTO.getQuestionId());
        ObjectMapper om = new ObjectMapper();

        // RDB에 질문 저장
        QuestionRedis questionRedis = questionRedisRepository.findById(answerDTO.getQuestionId());
        Question question = questionRedis.toEntity();
        questionRepository.save(question);

        //RDB에 답변 저장
        Answer answer = answerRedis.toEntity();
        answerRepository.save(answer);

        OpenviduRequestDTO openviduRequestDTO = new OpenviduRequestDTO(sessionId,"answer",om.writeValueAsString(answerVO));
        URI uri = UriComponentsBuilder
                .fromUriString(OPENVIDU_URL)
                .path("/openvidu/api/signal")
                .encode()
                .build()
                .toUri();

        String secret = "Basic " + OPENVIDU_SECRET;
        secret = Base64.getEncoder().encodeToString(secret.getBytes());

        RequestEntity<String> requestEntity = RequestEntity
                .post(uri)
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic T1BFTlZJRFVBUFA6TVlfU0VDUkVU")
                .body(openviduRequestDTO.toJson());

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        restTemplate.postForEntity(uri, requestEntity, Object.class);

        return answerVO;
    }

    @Override
    public AnswerRedis saveAnswer(AnswerDTO answerDTO) throws Exception {
        AnswerRedis answerRedis = answerDTO.toEntity();
        answerRedis = answerRedisRepository.save(answerRedis);
        return answerRedis;
    }

    @Override
    public QuestionRedis saveQuestion(QuestionDTO questionDTO) throws Exception {
        QuestionRedis questionRedis = questionDTO.toEntity();
        questionRedis = questionRedisRepository.save(questionRedis);
        return questionRedis;
    }

    @Override
    public List<AnswerVO> findAnswerByQuestionId(String questionId) throws Exception {
        List<AnswerRedis> answerRedis = answerRedisRepository.findByQuestionId(questionId);
        List<AnswerVO> answerVOS = answerRedis.stream()
                .map(a -> new AnswerVO(a.getAnswerId(),a.getUserId(),a.getSession(),a.getAnswerContent(),a.getQuestionId()))
                .collect(Collectors.toList());
        return answerVOS;
    }

    public String getRandomroom(String sessionName){
        Random random = new Random();
        int roomNumber = random.nextInt( 3) + 1; // 1-3
        return sessionName+roomNumber;
    }

    public void leaveSession(EnterRoomDTO enterRoomDTO) throws Exception{
        openvidu.fetch();
        List<Session> activeSessions = openvidu.getActiveSessions();
        String prevSession = enterRoomDTO.getPrevSession();
        String prevConnectionId = enterRoomDTO.getPrevConnectionId();
        for(Session s : activeSessions){ // 기존의 연결을 찾아서 삭제한다
            System.out.println("기존 연결끊기를 시도합니다 ... ");
            if(s.getSessionId().equals(prevSession)){ // 세션을 찾았다면
                System.out.println("기존 연결을 찾았습니다 ... ");
                System.out.println("기존 연결 아이디 : "+prevConnectionId);
                try{
                    s.forceDisconnect(prevConnectionId);
                    System.out.println("기존 연결을 성공적으로 끊었습니다.");
                } catch (Exception e){
                    System.out.println("이미 연결이 끊어져있습니다.");
                }
            }
        }
    }
}
