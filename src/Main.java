/**
 * Created by kurtisniedling on 2014-11-04.
 */
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Main {
    private String clientToken;

    public static void main(String[] args) {
        String teamName = "Tanks But No Tanks";
        String matchToken = "49fd1321-9daa-4810-bd16-4fcb4e1e3cb5";
        String serverIP = "ip-10-136-78-179.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        //TODO: aim and shoot properly
        //TODO: movement properly

        System.out.println("done!");
    }
}
