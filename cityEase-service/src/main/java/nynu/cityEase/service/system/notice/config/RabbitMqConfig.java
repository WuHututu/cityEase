package nynu.cityEase.service.system.notice.config;

import nynu.cityEase.api.vo.constants.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(MqConstants.NOTIFY_EXCHANGE, true, false);
    }

    @Bean
    public Queue notifyQueue() {
        return new Queue(MqConstants.NOTIFY_QUEUE, true);
    }

    @Bean
    public Binding notifyBinding(Queue notifyQueue, DirectExchange notifyExchange) {
        return BindingBuilder.bind(notifyQueue)
                .to(notifyExchange)
                .with(MqConstants.NOTIFY_ROUTING_KEY);
    }
}