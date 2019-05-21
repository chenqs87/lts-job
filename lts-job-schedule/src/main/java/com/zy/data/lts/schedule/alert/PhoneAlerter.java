package com.zy.data.lts.schedule.alert;

import com.zy.data.lts.core.dao.AlertConfigDao;
import com.zy.data.lts.core.dao.FlowDao;
import com.zy.data.lts.core.entity.AlertConfig;
import com.zy.data.lts.core.entity.Flow;
import com.zy.data.lts.core.entity.FlowTask;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author chenqingsong
 * @date 2019/5/20 14:12
 */
@Component
public class PhoneAlerter implements IAlerter {
    private static Logger logger = LoggerFactory.getLogger(PhoneAlerter.class);

    @Value("${lts.alerter.phone.url}")
    private String url;

    @Value("${lts.alerter.phone.default-list}")
    private String defaultList;


    private IPhoneAlerterApi api;

    @Autowired
    FlowDao flowDao;

    @Autowired
    AlertConfigDao alertConfigDao;

    @PostConstruct
    void init() {
        try {
            api = Feign.builder()
                    .encoder(new GsonEncoder())
                    .decoder(new GsonDecoder())
                    .target(IPhoneAlerterApi.class, url);
        } catch (Exception e) {
            logger.error("Fail to create Phone Http Alert Client!!!", e);
        }
    }

    @Override
    public void success(FlowTask message) {

    }

    @Override
    public void failed(FlowTask error) {
        if(api == null) {
            return;
        }
        try {
            Flow flow = flowDao.findById(error.getFlowId());
            AlertConfig config = alertConfigDao.findByFlowId(error.getFlowId());

            String msg = "作业执行失败, ID:" + flow.getId() + ", Name:" + flow.getName();
            logger.warn(msg);
            String phoneList = StringUtils.isNotBlank(config.getPhoneList()) ?
                    defaultList + config.getPhoneList() : defaultList;
            api.send(phoneList, msg);
        } catch (Exception e) {
            logger.error("Fail to send msg to phone!!", e);
        }
    }

    public interface IPhoneAlerterApi {
        @RequestLine("GET /notify/sms/send_warn_sms?mobile={phone}&content={content}&biz_type=bi_warn")
        @Headers("Content-Type: application/json")
        PhoneSendResponse send(@Param("phone")String phone, @Param("content") String msg);
    }

    public class PhoneSendResponse {
        private int code;
        private String msg;
        private String body;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
