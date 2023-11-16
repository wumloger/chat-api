package top.wml.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.Date;

@Data
public class Message {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;

    public String msgContent;

    public String msgType;

    public Long fromUserId;

    public byte status;

    public String time;

    public String remark;

    public Long createBy;

    @TableField(fill = FieldFill.INSERT)
    public Date createTime;

    public Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    public Date updateTime;


}
