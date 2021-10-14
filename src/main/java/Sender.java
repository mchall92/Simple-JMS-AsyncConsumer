import java.sql.Timestamp;
import java.util.Properties;
import java.util.Scanner;

import javax.naming.Context;
import javax.naming.InitialContext;

import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.QueueSender;
import javax.jms.DeliveryMode;
import javax.jms.QueueSession;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;

/**
 * This is a sender(producer) class.
 */
public class Sender {

    public static void main(String[] args) throws Exception {

        // set up environment
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://127.0.0.1:61616");
        env.put("queue.mailNotificationQueue", "MyQueue");

        // get the initial context
        InitialContext ctx = new InitialContext(env);

        // look up the queue object
        Queue queue = (Queue) ctx.lookup("mailNotificationQueue");

        // look up the queue connection factory
        QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup("QueueConnectionFactory");

        // create a queue connection
        QueueConnection queueConn = connFactory.createQueueConnection();

        // create a queue session
        QueueSession queueSession = queueConn.createQueueSession(false, Session.DUPS_OK_ACKNOWLEDGE);

        // create a queue sender
        QueueSender queueSender = queueSession.createSender(queue);
        queueSender.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        // let user create mail notifications as TextMessage
        // empty message is not allowed
        while (true) {
            System.out.print("Please enter an email message or \"exit\" to close the sender: ");
            Scanner sc= new Scanner(System.in);
            String msgText = sc.nextLine();
            if (msgText.trim().length() == 0) {
                System.out.println("Empty notification now allowed");
                continue;
            }
            if (!msgText.equalsIgnoreCase("exit")) {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                TextMessage message = queueSession.createTextMessage(msgText + "  ||  Sent at " + timestamp);
                queueSender.send(message);
                System.out.println("Sent mail notification: " + message.getText() + "   " + timestamp);
            } else {
                break;
            }
        }
        queueConn.close();
    }
}