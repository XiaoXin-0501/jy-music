package com.jy.vo.requestVo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserInfoBody {
    //昵称
    private String nickName;
    //性别
    private String sex;
    //email
    private String email;
    //birthday
    private LocalDate birthday;
}
