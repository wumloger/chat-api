package top.wml.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MybatisPlus 公共字段自动填充
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     **/
    @Override
    public void insertFill(MetaObject metaObject) {
        this.setFieldValByName("createTime", new Date(), metaObject);
        this.setFieldValByName("updateTime", new Date(), metaObject);

        // SysUserService.getUserId()方法是我获取当前用户id的方法，读者可自行替换
//        this.setFieldValByName("createUserId", "", metaObject);
//        this.setFieldValByName("updateUserId", "", metaObject);
//        this.setFieldValByName("isDeleted", 0, metaObject);

    }

    /**
     * 更新时自动填充
     **/
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
//        this.setFieldValByName("updateUserId", "", metaObject);
    }
}