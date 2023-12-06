package top.wml.common.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MsgUnreadRecord {
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private Integer unreadNum;

    private Byte source;

    public String remark;

    @JsonSerialize(using = ToStringSerializer.class)
    public Long createBy;

    @TableField(fill = FieldFill.INSERT)
    public Date createTime;

    @JsonSerialize(using = ToStringSerializer.class)
    public Long updateBy;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    public Date updateTime;
}
