package com.jy.vo.responseVo;

import com.jy.entity.RoleEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInformation {
    private Long id;
    private String nickName;
    private String avatar;
    private String email;
    private String account;
    private String sex;
    private String status;
    private LocalDate birthday;
    private List<RoleEntity> roles;
}
