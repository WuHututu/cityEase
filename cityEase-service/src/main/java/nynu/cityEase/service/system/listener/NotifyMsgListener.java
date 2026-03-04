package nynu.cityEase.service.system.listener;

import com.rabbitmq.client.Channel;
import nynu.cityEase.api.vo.constants.MqConstants;
import nynu.cityEase.api.vo.system.NotifyMsgDTO;
import nynu.cityEase.service.system.dict.repository.entity.SysUserMessageDO;
import nynu.cityEase.service.system.notice.service.ISysUserMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NotifyMsgListener {

    private static final Logger log = LoggerFactory.getLogger(NotifyMsgListener.class);

    @Autowired
    private ISysUserMessageService userMessageService;

    @RabbitListener(queues = MqConstants.NOTIFY_QUEUE)
    public void handleNotifyMessage(NotifyMsgDTO msgDTO, Message message, Channel channel) throws IOException {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            log.info("MQ开始消费通知消息: 准备落库, 接收人 [{}], 标题 [{}]", msgDTO.getReceiveUserId(), msgDTO.getTitle());

            SysUserMessageDO messageDO = new SysUserMessageDO();
            messageDO.setReceiveUserId(msgDTO.getReceiveUserId());
            messageDO.setTitle(msgDTO.getTitle());
            messageDO.setContent(msgDTO.getContent());
            messageDO.setRelatedBizId(msgDTO.getRelatedBizId());
            messageDO.setNotifyType(msgDTO.getNotifyType());
            messageDO.setReadStatus(0);

            // 业务落库：将消息正式写入到系统数据库中
            userMessageService.save(messageDO);

            // 确认消息消费成功
            channel.basicAck(deliveryTag, false);

            log.info("消息落库成功，ID: {}", messageDO.getId());

        } catch (Exception e) {
            log.error("处理通知消息发生异常: ", e);
            // 发生异常，拒绝消息并重回队列
            channel.basicNack(deliveryTag, false, true);
        }
    }
}