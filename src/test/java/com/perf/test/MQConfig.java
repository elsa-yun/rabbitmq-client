package com.perf.test;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

public class MQConfig {

    protected final static String host = "172.16.100.43"; // broker host

    protected final static int port = 5672; // broker port

    protected final static String username = "meitun"; // broker username

    protected final static String password = "123456"; // broker password

    protected final static String QUEUE_NAME = "test_confirm"; // test queue name

    protected final static int msglen = 1000; // message length during perf test

    protected final static int consumerCount = 1; // consumer count 100

    protected final static int producerCount = 1; // producer count 100

    protected final static boolean autoAck = false; // consumer auto ack mode

    protected final static BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN; // message persistent mode
    // protected final static BasicProperties prop = MessageProperties.TEXT_PLAIN;

    protected final static int msgcount = 2; // message count during perf test 500000

    protected final static int msgcountPerBatch = 1; // message count per batch in batch confirm mode. 100
}
