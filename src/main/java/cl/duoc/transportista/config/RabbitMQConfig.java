package cl.duoc.transportista.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracion de RabbitMQ para el microservicio Transportista (PRODUCTOR).
 *
 * Define 2 colas:
 *   - cola.guias.exito   : recibe las guias creadas exitosamente.
 *   - cola.guias.errores : recibe las guias que fallaron al enviarse a la cola 1.
 *
 * Ambas colas estan vinculadas a un Direct Exchange con routing keys distintas.
 * Se usa Jackson para serializar los mensajes como JSON.
 */
@Configuration
public class RabbitMQConfig {

    // Nombres de las colas
    public static final String COLA_EXITO = "cola.guias.exito";
    public static final String COLA_ERRORES = "cola.guias.errores";

    // Exchange
    public static final String EXCHANGE = "guias.exchange";

    // Routing keys
    public static final String ROUTING_KEY_EXITO = "guia.exito";
    public static final String ROUTING_KEY_ERROR = "guia.error";

    // ---------- Colas ----------

    @Bean
    public Queue colaExito() {
        // durable = true para que la cola sobreviva reinicios de RabbitMQ
        return new Queue(COLA_EXITO, true);
    }

    @Bean
    public Queue colaErrores() {
        return new Queue(COLA_ERRORES, true);
    }

    // ---------- Exchange ----------

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(EXCHANGE);
    }

    // ---------- Bindings ----------

    @Bean
    public Binding bindingExito(Queue colaExito, DirectExchange exchange) {
        return BindingBuilder.bind(colaExito).to(exchange).with(ROUTING_KEY_EXITO);
    }

    @Bean
    public Binding bindingErrores(Queue colaErrores, DirectExchange exchange) {
        return BindingBuilder.bind(colaErrores).to(exchange).with(ROUTING_KEY_ERROR);
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