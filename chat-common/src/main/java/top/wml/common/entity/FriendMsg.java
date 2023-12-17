package top.wml.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FriendMsg implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    public Long id;

    public String msgContent;

    public Byte msgType;

    @JsonSerialize(using = ToStringSerializer.class)
    public Long fromUserId;

    public Byte status;

    public String time;

    public String remark;

    @JsonSerialize(using = ToStringSerializer.class)
    public Long createBy;

    @TableField(fill = FieldFill.INSERT)
    public Date createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    public Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    public Date updateTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long toUserId;
}
