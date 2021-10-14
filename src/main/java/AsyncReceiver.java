import java.sql.Timestamp;
import java.util.Properties;
import java.util.Scanner;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import javax.jms.ExceptionListener;
import javax.jms.QueueSession;
import javax.jms.QueueReceiver;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

/**
 * This is the async receiver(consumer) class.
 */
public class AsyncReceiver implements MessageListener, ExceptionListener {

    static QueueConnection queueConn = null;

    public static void main(String[] args) throws Exception {
        // Set up environment
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
        env.put("queue.mailNotificationQueue","MyQueue");

        // get the initial context
        InitialContext ctx = new InitialContext(env);

        // look up the queue object
        Queue queue = (Queue) ctx.lookup("mailNotificationQueue");

        // look up the queue connection factory
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");

        // create a queue connection
        queueConn = connFactory.createQueueConnection();

        // create a queue session
        QueueSession queueSession = queueConn.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);

        // create a queue receiver
        QueueReceiver queueReceiver = queueSession.createReceiver(queue);

        // set an asynchronous message listener
        AsyncReceiver asyncReceiver = new AsyncReceiver();
        queueReceiver.setMessageListener(asyncReceiver);

        // set an asynchronous exception listener on the connection
        queueConn.setExceptionListener(asyncReceiver);

        queueConn.start();
        boolean isStart = true;

        // let user close/restart the connection
        while (true) {
            if (isStart) {
                System.out.println("Enter close if you want to temporarily close the receiver;");
            } else {
                System.out.println("Enter start if you want to start the receiver;");
            }
            System.out.println("Enter exit if you want to stop the receiver.");
            Scanner sc= new Scanner(System.in);
            String op = sc.nextLine();

            op = op.trim();

            if (op.equalsIgnoreCase("start")) {
                if (isStart) {
                    System.out.println("Receiver already started.");
                } else {
                    queueConn = connFactory.createQueueConnection();
                    queueSession = queueConn.createQueueSession(false,Session.AUTO_ACKNOWLEDGE);
                    queueReceiver = queueSession.createReceiver(queue);
                    queueReceiver.setMessageListener(asyncReceiver);
                    queueConn.setExceptionListener(asyncReceiver);
                    queueConn.start();
                    isStart = true;
                }
            } else if (op.equalsIgnoreCase("close")) {
                if (!isStart) {
                    System.out.println("Receiver already closed.");
                } else {
                    queueConn.close();
                    isStart = false;
                }
            } else if (op.equalsIgnoreCase("exit")) {
                break;
            } else {
                System.out.println("Incorrect command, please enter again.");
            }
        }
    }

    /**
     * This method is called asynchronously when connection starts.
     * Message sent into queue when receiver is closed will be sent when
     * connection restarts.
     * @param message A JMS message.
     */
    @Override
    public void onMessage(Message message)
    {
        TextMessage msg = (TextMessage) message;
        try {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println("Received mail notification: " + msg.getText() + "   || Received at " + timestamp);
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method is called asynchronously by JMS when some error occurs.
     * @param exception A JMS exception.
     */
    @Override
    public void onException(JMSException exception) {
        System.err.println(exception.getMessage());
    }
}