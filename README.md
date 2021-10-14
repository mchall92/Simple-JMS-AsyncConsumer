## Simple JMS Mail Notification with Async Consumer

To start the simple JMS mail notification producer-consumer, 
please follow the these steps:

1. Download the latest Apache ActiveMQ Java Message broker (https://activemq.apache.org/components/classic/download/)
2. Unzip the downloaded file.
3. For Mac users, open terminal and cd to the /bin subdirectory in the unzipped folder.
4. Enter $ ./activemq console in terminal to execute activemq.
5. Make sure you see that Apache ActiveMQ has started in terminal.
6. Go to src/main/java in this directory.
7. Compile and execute Sender and AsyncReceiver Java files.
<br>
   a. For sender: Enter any non-empty message as mail notification. Enter "exit" if you want to quit the execution.
<br>
<br>
   b. For AsyncReceiver: Connection is automatically started. Enter "start" and "close" to close or restart connection. 
   Mail notifications will be printed out on the terminal with time stamps. Enter "exit" if you want to quit the 
   execution.
