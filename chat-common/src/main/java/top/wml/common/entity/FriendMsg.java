package top.wml.common.entity;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FriendMsg extends Message{
    private Long toUserId;
}
