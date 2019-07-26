package com.ld.system.zuul.filter;

import com.gm.core.base.model.AccessTokenInfoModel;
import com.gm.core.base.utils.TokenUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;

/**
 * url权限过滤器
 *
 * @author 王海龙
 */
@Component
public class AuthUrlFilter extends ZuulFilter {

    /**
     * @return 过滤器类型
     */
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    /**
     * @return 过滤器的级别
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * @return 过滤器是否起作用
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * @return 过滤器的业务逻辑
     * @throws ZuulException 网关异常
     */
    @Override
    public Object run() throws ZuulException {
        try {
            RequestContext context = RequestContext.getCurrentContext();
            HttpServletRequest request = context.getRequest();
            String requestURI = request.getRequestURI();
            if ("/ldApi/login".equals(requestURI)) {
                return null;
            }
            String method = request.getMethod();
            Cookie[] cookies = request.getCookies();
            String systemUserCookie = "";
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if ("systemUser".equals(name)) {
                    systemUserCookie = cookie.getValue();
                }
            }
            if ("".equals(systemUserCookie)) {
                context.setSendZuulResponse(false);
                context.setResponseStatusCode(401);
                context.setResponseBody("cookie不能为空");
                context.getResponse().setContentType("text/html;charset=UTF-8");
                return null;
            }
            // 从cookie中取出用户信息
            AccessTokenInfoModel accessTokenInfoModel = TokenUtils.getAccessTokenInfo(systemUserCookie);
            String userUuid = accessTokenInfoModel.getUserUuid();
            // 验证url权限
            System.out.println("requestURI: " + requestURI + "  method: " + method);

        } catch (Exception e) {
            RequestContext context = RequestContext.getCurrentContext();
            context.set("error.status_code", 401);
            context.set("error.exception", e);
            context.set("error.message", "休息一下");
            return null;
        }
        return null;
    }
}

