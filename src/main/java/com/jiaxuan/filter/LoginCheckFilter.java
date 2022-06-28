package com.jiaxuan.filter;


import com.alibaba.fastjson.JSON;
import com.jiaxuan.common.BaseContext;
import com.jiaxuan.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否完成登录的过滤器
 */

@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        //1获取本次请求的URI
        String requestURI = httpServletRequest.getRequestURI();

        log.info("拦截到请求 {}",requestURI);

        //2定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**"
        };

        //3判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //4不需要处理则直接放行
        if (check){
            log.info("本次请求不需要处理 {}",requestURI);
            chain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        //5判断登录状态，如果已经登录则直接放行
        if(httpServletRequest.getSession().getAttribute("employee") != null){
            log.info("用户已经登录，用户id为：{}",httpServletRequest.getSession().getAttribute("employee"));

            //使用工具类BaseContext将从session中获取的id存入threadlocal中
            Long empId = (Long) httpServletRequest.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            chain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        //6如果未登录，则返回未登录结果，通过输出流向客户端页面相应数据
        httpServletResponse.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("用户未登录");
        return;

    }


    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI){
        for (String url:urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }












}
