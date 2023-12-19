package top.wml.friend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendVO {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long friendId;

    private String nickname;

    private String avatar;

    private String alphabetic;

    private Byte status;

    private Byte isAttention;

    private String remark;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long createBy;

    private Date createTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long updateBy;

    private Date updateTime;

    private Integer num;
}
