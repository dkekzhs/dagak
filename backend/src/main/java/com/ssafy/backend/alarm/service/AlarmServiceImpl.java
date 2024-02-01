package com.ssafy.backend.alarm.service;

import com.ssafy.backend.alarm.model.domain.Alarm;
import com.ssafy.backend.alarm.model.dto.CheckAlarmDto;
import com.ssafy.backend.alarm.model.dto.ReqestAlarmDto;
import com.ssafy.backend.alarm.model.repository.AlarmRepository;
import com.ssafy.backend.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ssafy.backend.common.response.BaseResponseStatus.*;

@Service
@Slf4j
public class AlarmServiceImpl implements  AlarmService {

    @Autowired
    AlarmRepository alarmRepository;

    @Override
    public Integer findAlarmId(String userId, String requestedUserId, int tagId) {
        return alarmRepository.findAlarmByUserIdAndRequestedUserIdAndTagId(userId, requestedUserId, tagId).orElseThrow(
                () -> {throw new BaseException(NOT_EXIST_ALARM_ID);}
        ).getAlarmId();
    }

    @Override
    public void requestAlarm(ReqestAlarmDto reqestAlarmDto) {
        alarmRepository.save(
                Alarm.builder()
                        .tagId(reqestAlarmDto.getTagId())
                        .userId(reqestAlarmDto.getUserId())
                        .isChecked(0)
                        .requestedUserId(reqestAlarmDto.getRequestedUserId())
                        .build());
    }
    @Override
    public void checkAlarm(CheckAlarmDto checkAlarmDto) {
        Alarm alarm = alarmRepository.findById(checkAlarmDto.getAlarmId()).orElseThrow(() -> new BaseException(NOT_EXIST_ALARM_ID));
        alarm.setIsChecked(1);

//        log.info("Alarm id : {}", alarm.getAlarmId());
        alarmRepository.save(alarm);

    }

    @Override
    public List<Alarm> listofAllAlarm(String userId) {

//        Sort sort = Sort.by(
//                Sort.Order.asc("isChecked"),
//                Sort.Order.desc("createdDate")
//                );
//        log.info("====== sort : {}", sort);

        List<Alarm> allByUserId = alarmRepository.findAllByUserIdOrderByIsCheckedAscCreatedDateDesc(userId);

        return allByUserId;
    }

    @Override
    public List<Alarm> listOfUncheckedAlarm(String userId) {

        List<Alarm> byUserIdAndIsChecked = alarmRepository.findByUserIdAndIsChecked(userId, 0);

        return byUserIdAndIsChecked;
    }

    @Override
    public void aVoidDuplicateAlaram(ReqestAlarmDto reqestAlarmDto) {
        alarmRepository.findAlarmByUserIdAndRequestedUserIdAndTagId(
                reqestAlarmDto.getUserId(),
                reqestAlarmDto.getRequestedUserId(),
                reqestAlarmDto.getTagId()).ifPresent(
                        e -> {
                            throw new BaseException(AVOID_DUPLICATE_ALARM);
                        });
    }

    @Override
    public void deleteAlarm(ReqestAlarmDto alarmDto) {
        System.out.println(alarmDto);
        Alarm alarm = alarmRepository.findAlarmByUserIdAndRequestedUserIdAndTagId(
                alarmDto.getUserId(),
                alarmDto.getRequestedUserId(),
                alarmDto.getTagId()
        ).orElseThrow(() -> new BaseException(ALREADY_DELETE_ALARM));
        alarmRepository.delete(alarm);

    }

    @Override
    public boolean isAlreadyRequestFriend(String userId, String requestedUserId) {
        Alarm byUserIdAndRequestedUserIdAndTagIdAndIsChecked
                = alarmRepository.findByUserIdAndRequestedUserIdAndTagIdAndIsChecked(userId, requestedUserId, 4, 0);

        if(byUserIdAndRequestedUserIdAndTagIdAndIsChecked != null)
            return true;
        return false;
    }
}
