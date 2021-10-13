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

public class AsyncReceiver implements MessageListener, ExceptionListener
{
    static QueueConnection queueConn = null;
    public static void main(String[] args) throws Exception
    {
        Properties env = new Properties();
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        env.put(Context.PROVIDER_URL, "tcp://localhost:61616");
        env.put("queue.queueSampleQueue","MyNewQueue");

        // get the initial context
        InitialContext ctx = new InitialContext(env);

        // lookup the queue object
        Queue queue = (Queue) ctx.lookup("queueSampleQueue");

        // lookup the queue connection factory
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

        while (true) {
            if (isStart) {
                System.out.println("Enter close if you want to temporarily close the receiver;");
            } else {
                System.out.println("Enter start if you want to start the receiver;");
            }
            System.out.print("Enter exit if you want to stop the receiver: ");
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
     This method is called asynchronously by JMS when a message arrives
     at the queue. Client applications must not throw any exceptions in
     the onMessage method.
     @param message A JMS message.
     */
    @Override
    public void onMessage(Message message)
    {
        TextMessage msg = (TextMessage) message;
        try {
            if(msg.getText().equals("exit")){
                queueConn.close();
                System.out.println("Application Exits");
            }else{
                System.out.println("received: " + msg.getText());
            }
        } catch (JMSException ex) {
            ex.printStackTrace();
        }
    }

    /**
     This method is called asynchronously by JMS when some error occurs.
     When using an asynchronous message listener it is recommended to use
     an exception listener also since JMS have no way to report errors
     otherwise.
     @param exception A JMS exception.
     */
    @Override
    public void onException(JMSException exception)
    {
        System.err.println("an error occurred: " + exception);
    }
}