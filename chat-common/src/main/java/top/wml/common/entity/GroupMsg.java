package top.wml.common.entity;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GroupMsg extends Message{
    private Long groupId;

    private String fromUserNickname;

    private String fromUserAvatar;

}
