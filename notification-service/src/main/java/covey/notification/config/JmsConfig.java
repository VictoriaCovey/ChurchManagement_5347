package covey.notification.config;

import covey.notification.dto.NotificationMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.Map;

@Configuration
public class JmsConfig {

    /**
     * Reads JMS messages as JSON text, resolving the Java type from the "_type" header
     * ("notification") to this service's own local DTO class - producers (different services,
     * different packages/classes) write the same type id against their own local class.
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(Map.of("notification", NotificationMessage.class));
        return converter;
    }
}
