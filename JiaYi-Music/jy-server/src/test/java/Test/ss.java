package Test;

import com.jy.JiaYiApplication;
import com.jy.entity.UserEntity;
import com.jy.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = JiaYiApplication.class)
public class ss {
    // 自动注入需要测试的UserMapper（SpringBoot会自动扫描Mapper接口并创建代理对象）
    @Autowired
    private UserMapper userMapper;

    /**
     * 测试根据账号查询用户（含角色信息）
     * 注意：测试前请确保数据库jy-user表中存在【可测试的账号】（如test、admin等，替换为你库中的实际账号）
     */
    @Test
    public void testSelectUserByUserAccount() {
        // 1. 定义要查询的账号（替换为你数据库jy-user表中真实存在的account值，否则查询结果为null）
        String testAccount = "小新"; // 示例：如你的库中有账号"admin"，则改为"admin"

        // 2. 调用Mapper接口的查询方法
        UserEntity user = userMapper.getUserByUserAccount(testAccount);
        System.out.println(user);
        log.info("testSelectUserByUserAccount");
    }
}
