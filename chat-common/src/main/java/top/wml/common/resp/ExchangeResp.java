package top.wml.common.resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ExchangeResp {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long targetId;

    private String targetName;

    private String targetAvatar;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String newMsg;

    @JsonFormat(pattern = "HH:mm")
    private Date msgTime;

    private Integer unreadNum;

    private Byte source;

    public String remark;

    @JsonSerialize(using = ToStringSerializer.class)
    public Long createBy;

    public Date createTime;

    public Long updateBy;

    public Date updateTime;
}
