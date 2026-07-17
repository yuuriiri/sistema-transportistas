package cl.duoc.transportista.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para el microservicio Transportista (PRODUCTOR).
 *
 * La cola principal esta vinculada a un Dead Letter Exchange (DLX).
 * Cuando el consumidor rechaza un mensaje (NACK), RabbitMQ lo redirige
 * automaticamente a la DLQ. El productor NO envia a la DLQ manualmente.
 */
@Configuration
public class RabbitMQConfig {

    // Cola principal
    public static final String COLA_PRINCIPAL = "cola.guias.principal";
    public static final String EXCHANGE = "guias.exchange";
    public static final String ROUTING_KEY = "guia.principal";

    // Dead Letter Queue (DLQ)
    public static final String DLQ = "cola.guias.dlq";
    public static final String DLX_EXCHANGE = "guias.dlx.exchange";
    public static final String DLX_ROUTING_KEY = "guia.dlq";

    // ---------- Cola principal con argumentos DLX ----------

    @Bean
    public Queue colaPrincipal() {
        return QueueBuilder.durable(COLA_PRINCIPAL)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding bindingPrincipal(Queue colaPrincipal, DirectExchange exchange) {
        return BindingBuilder.bind(colaPrincipal).to(exchange).with(ROUTING_KEY);
    }

    // ---------- DLQ y su exchange ----------

    @Bean
    public Queue colaDlq() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }

    @Bean
    public Binding bindingDlq(Queue colaDlq, DirectExchange dlxExchange) {
        return BindingBuilder.bind(colaDlq).to(dlxExchange).with(DLX_ROUTING_KEY);
    }

    // ---------- Conversor JSON ----------

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}