package cl.duoc.consumidor.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.duoc.consumidor.dto.GuiaMessageDTO;

/**
 * Configuracion de RabbitMQ para el microservicio Consumidor.
 * Declara las mismas colas y exchange que el productor para que
 * ambos microservicios se conecten a la misma infraestructura.
 *
 * IMPORTANTE: Se configura un ClassMapper para que Jackson pueda
 * deserializar los mensajes enviados por el productor, que usa
 * un package distinto (cl.duoc.transportista.dto.GuiaMessageDTO)
 * al del consumidor (cl.duoc.consumidor.dto.GuiaMessageDTO).
 */
@Configuration
public class RabbitMQConfig {

    public static final String COLA_EXITO = "cola.guias.exito";
    public static final String COLA_ERRORES = "cola.guias.errores";
    public static final String EXCHANGE = "guias.exchange";
    public static final String ROUTING_KEY_EXITO = "guia.exito";
    public static final String ROUTING_KEY_ERROR = "guia.error";

    @Bean
    public Queue colaExito() {
        return new Queue(COLA_EXITO, true);
    }

    @Bean
    public Queue colaErrores() {
        return new Queue(COLA_ERRORES, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingExito(Queue colaExito, DirectExchange exchange) {
        return BindingBuilder.bind(colaExito).to(exchange).with(ROUTING_KEY_EXITO);
    }

    @Bean
    public Binding bindingErrores(Queue colaErrores, DirectExchange exchange) {
        return BindingBuilder.bind(colaErrores).to(exchange).with(ROUTING_KEY_ERROR);
    }

    // ---------- Mapeo de clases entre microservicios ----------

    @Bean
    public DefaultClassMapper classMapper() {
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");

        // Mapea la clase del PRODUCTOR a la clase del CONSUMIDOR
        // El productor envia como "cl.duoc.transportista.dto.GuiaMessageDTO"
        // El consumidor lo recibe como "cl.duoc.consumidor.dto.GuiaMessageDTO"
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("cl.duoc.transportista.dto.GuiaMessageDTO", GuiaMessageDTO.class);
        classMapper.setIdClassMapping(idClassMapping);

        return classMapper;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        converter.setClassMapper(classMapper());
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}