package com.jiaxuan.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jiaxuan.common.BaseContext;
import com.jiaxuan.common.R;
import com.jiaxuan.domain.User;
import com.jiaxuan.service.UserService;
import com.jiaxuan.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 发送短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if (StringUtils.isNotEmpty(phone)){
            //生成随机四位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);


            //调用阿里云短信服务API完成发送短信
            //SMSUtils.sendMessage("reggie外卖","",phone,code);

            //需要将生成的验证码保存到session
            session.setAttribute(phone,code);
            return R.success("短信验证码发送成功");

        }

        return R.error("验证码发送失败！");
    }


    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info("map= {}",map.toString());
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //进行验证码的比对（页面提交验证码和session中保存的验证码）
        if(codeInSession != null && codeInSession.equals(code)){
            //比对成功，登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            //判断当前手机号对应的用户是否为新用户，是则自动注册
            if(user == null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                user.setName("默认"+phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
        return R.error("登录失败！");
    }


    /**
     * 用户退出
     * @param request
     * @return
     */
    @PostMapping("/loginout")
    public R<String> loginOut(HttpServletRequest request){

        //清理服务端用户session
        request.getSession().removeAttribute("user");
        return R.success("用户退出成功！");
    }

}
