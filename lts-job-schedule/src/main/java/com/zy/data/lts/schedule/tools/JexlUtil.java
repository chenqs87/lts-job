package com.zy.data.lts.schedule.tools;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JexlUtil {
    private static final Logger logger = LoggerFactory.getLogger(JexlUtil.class);
    private static final Pattern LTS_PARAM_PATTERN = Pattern.compile("#\\{([^\\r^}]+)}");

    private JexlEngine jexlEngine;

    @PostConstruct
    void init() {
        Map<String, Object> ns = new HashMap<>();
        ns.put("lts", new LtsJexl());
        jexlEngine = new JexlBuilder().namespaces(ns).create();
    }

    public Object execute(String express) {
        try {
            JexlExpression je = jexlEngine.createExpression(express);
            return je.evaluate(null);
        }catch (Exception e) {
            logger.error("Fail to execute jexl [{}]", express, e);
            return express;
        }
    }

    public String format(String value) {
        if(StringUtils.isBlank(value)) {
            return value;
        }

        Matcher matcher = LTS_PARAM_PATTERN.matcher(value);

        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String aa = matcher.group(1);
            matcher.appendReplacement(sb, execute(aa).toString());
        }

        matcher.appendTail(sb);

        return sb.toString();
    }
}
