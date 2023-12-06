package top.wml.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@TableName("group_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupUser {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long groupId;
    private String groupName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String userNickname;
    private String userAvatar;

    private Byte adminable;

    private Byte source;

    private Byte status;

    private Byte isAttention;

    private String remark;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;






}
