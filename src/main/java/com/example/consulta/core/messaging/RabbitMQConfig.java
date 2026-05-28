package com.example.consulta.core.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "consulta-facil";
    public static final String DLX = "consulta-facil.dlx";
    public static final String DLQ = "consulta-facil.dead-letter";

    public static final String Q_APPOINTMENTS_CREATED_EMAIL = "appointments.created.email";
    public static final String Q_APPOINTMENTS_CREATED_WHATSAPP = "appointments.created.whatsapp";
    public static final String Q_APPOINTMENTS_CANCELED_EMAIL = "appointments.canceled.email";
    public static final String Q_APPOINTMENTS_CANCELED_WHATSAPP = "appointments.canceled.whatsapp";
    public static final String Q_APPOINTMENTS_CONFIRMED_EMAIL = "appointments.confirmed.email";
    public static final String Q_APPOINTMENTS_CONFIRMED_WHATSAPP = "appointments.confirmed.whatsapp";
    public static final String Q_PAYMENTS_SUCCEEDED_EMAIL = "payments.succeeded.email";
    public static final String Q_PAYMENTS_FAILED_EMAIL = "payments.failed.email";

    public static final String RK_APPOINTMENTS_CREATED = "appointments.created";
    public static final String RK_APPOINTMENTS_CANCELED = "appointments.canceled";
    public static final String RK_APPOINTMENTS_CONFIRMED = "appointments.confirmed";
    public static final String RK_PAYMENTS_SUCCEEDED = "payments.succeeded";
    public static final String RK_PAYMENTS_FAILED = "payments.failed";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(@Autowired(required = false) ConnectionFactory connectionFactory) {
        if (connectionFactory == null) return null;
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            @Autowired(required = false) ConnectionFactory connectionFactory) {
        if (connectionFactory == null) return null;
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 5.0, 25000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public Declarables rabbitDeclarables() {
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        TopicExchange dlx = new TopicExchange(DLX, true, false);
        Queue dlq = QueueBuilder.durable(DLQ).build();

        Queue createdEmail = durableWithDlx(Q_APPOINTMENTS_CREATED_EMAIL);
        Queue createdWhatsApp = durableWithDlx(Q_APPOINTMENTS_CREATED_WHATSAPP);
        Queue canceledEmail = durableWithDlx(Q_APPOINTMENTS_CANCELED_EMAIL);
        Queue canceledWhatsApp = durableWithDlx(Q_APPOINTMENTS_CANCELED_WHATSAPP);
        Queue confirmedEmail = durableWithDlx(Q_APPOINTMENTS_CONFIRMED_EMAIL);
        Queue confirmedWhatsApp = durableWithDlx(Q_APPOINTMENTS_CONFIRMED_WHATSAPP);
        Queue paymentSucceededEmail = durableWithDlx(Q_PAYMENTS_SUCCEEDED_EMAIL);
        Queue paymentFailedEmail = durableWithDlx(Q_PAYMENTS_FAILED_EMAIL);

        return new Declarables(
                exchange, dlx, dlq,
                createdEmail, createdWhatsApp, canceledEmail, canceledWhatsApp,
                confirmedEmail, confirmedWhatsApp, paymentSucceededEmail, paymentFailedEmail,
                bind(createdEmail, exchange, RK_APPOINTMENTS_CREATED),
                bind(createdWhatsApp, exchange, RK_APPOINTMENTS_CREATED),
                bind(canceledEmail, exchange, RK_APPOINTMENTS_CANCELED),
                bind(canceledWhatsApp, exchange, RK_APPOINTMENTS_CANCELED),
                bind(confirmedEmail, exchange, RK_APPOINTMENTS_CONFIRMED),
                bind(confirmedWhatsApp, exchange, RK_APPOINTMENTS_CONFIRMED),
                bind(paymentSucceededEmail, exchange, RK_PAYMENTS_SUCCEEDED),
                bind(paymentFailedEmail, exchange, RK_PAYMENTS_FAILED),
                BindingBuilder.bind(dlq).to(dlx).with("#")
        );
    }

    private Queue durableWithDlx(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", DLX)
                .build();
    }

    private Binding bind(Queue queue, TopicExchange exchange, String routingKey) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
