package com.example.tanktoo.eventreportapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by tanktoo on 10.07.2016.
 */
public class MessageBus {

//    final static String HOST = "131.227.92.55";
//    final static int PORT = 8007;
    private String host = "131.227.92.55";
    private int port = 8007;
    final static String USERNAME = "guest";
    final static String PASSWORD = "guest";

    Thread subscribeThread;
    Thread publishThread;

    private BlockingDeque<String> queue = new LinkedBlockingDeque<String>();
    ConnectionFactory factory = new ConnectionFactory();

    public MessageBus(String host, int port){
        System.out.println("########### Create MessageBus");
        this.host = host;
        this.port = port;
        this.setupConnectionFactory();
    }

    protected void destroy() {
        publishThread.interrupt();
        subscribeThread.interrupt();
    }




    void publishMessage(String message) {
        //Adds a message to internal blocking queue
        try {
            Log.d("","[q] " + message);
            queue.putLast(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private void setupConnectionFactory() {
        System.out.println("############# setupConnectionFactory");
        this.factory.setAutomaticRecoveryEnabled(false);
        this.factory.setHost(this.host);
        this.factory.setPort(this.port);
        this.factory.setUsername(USERNAME);
        this.factory.setPassword(PASSWORD);
    }

    public void subscribe(final Handler handler)
    {
        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel();
                        channel.basicQos(1);
                        Map<String, Object> args = new HashMap<String, Object>();
                        args.put("x-message-ttl", 60000);
                        Boolean durable = true;
                        Boolean autodelete = true;
                        AMQP.Queue.DeclareOk q = channel.queueDeclare("repeatable_events_queue", durable, false, autodelete, args);
                        channel.queueBind(q.getQueue(), "events", "#");
//                        channel.queueBind(q.getQueue(), "repeatable_events", "#");
                        QueueingConsumer consumer = new QueueingConsumer(channel);
                        channel.basicConsume(q.getQueue(), true, consumer);

                        // Process deliveries
                        while (true) {
                            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                            String message = new String(delivery.getBody());
                            Log.d("","[r] " + message);

                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();

                            bundle.putString("msg", message);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e1) {
                        Log.d("", "Connection broken: " + e1.getClass().getName());
                        try {
                            Thread.sleep(4000); //sleep and then try again
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        });
        subscribeThread.start();
    }

    public void publishToAMQP()
    {
        System.out.println("s######## start thread");
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        System.out.println("#####host" + factory.getHost());
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();
                        ch.exchangeDeclare("repeatable_events", "topic");

                        while (true) {
                            System.out.println("running");
                            String message = queue.takeFirst();
                            try{
                                AMQP.BasicProperties properties = new AMQP.BasicProperties
                                        .Builder()
                                        .expiration("60000")
                                        .build();
                                //basicPublish(java.lang.String exchange, java.lang.String routingKey, AMQP.BasicProperties props, byte[] body)
                                ch.basicPublish("repeatable_events", "test", properties, message.getBytes());
                                //ch.basicPublish("amq_fanout", "test", properties, message.getBytes());
                                Log.d("", "[s] " + message);
                                ch.waitForConfirmsOrDie();
                            } catch (Exception e){
                                Log.d("","[f] " + message);
                                queue.putFirst(message);
                                throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        System.out.println("######## error 1");
                        break;
                    } catch (Exception e) {
                        System.out.println("######## error 2");
                        Log.d("", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;
                        }
                    }
                }
            }
        });
        publishThread.start();
    }
}
