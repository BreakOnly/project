package com.jrmf.filter;

import com.jrmf.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.web.http.DefaultCookieSerializer;


import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JrmfCookieSerializer extends DefaultCookieSerializer {

    private static final Pattern DOMAIN_PATTERN = Pattern.compile("^(.+\\.)?(.+\\.[a-z]+)$");
    private static final Logger LOGGER = LoggerFactory.getLogger(JrmfCookieSerializer.class);

    @Override
    public void writeCookieValue(CookieValue cookieValue) {
        super.writeCookieValue(cookieValue);
        String setCookieHeader = cookieValue.getResponse().getHeader("Set-Cookie");

        String host = cookieValue.getRequest().getHeader("Host");
        LOGGER.info("request Host header is "+host);

        if (StringUtil.isEmpty(host)) {
            LOGGER.error("can not find Host request header");
            return;
        }
        String parentHost = getParentDomain(host);

        setCookieHeader += ";Domain=" + parentHost;
        LOGGER.info("response Set-Cookie header is " + setCookieHeader);
        cookieValue.getResponse().setHeader("Set-Cookie", setCookieHeader);
    }


    private String getParentDomain(String host) {
        Matcher matcher = DOMAIN_PATTERN.matcher(host);

        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }
}
