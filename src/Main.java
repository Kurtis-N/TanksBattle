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
        String matchToken = "4664b727-2ca3-4cf0-a357-b5de7ae35b5b";
        String serverIP = "ip-10-234-167-219.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        //TODO: aim and shoot properly
        //TODO: movement properly

        System.out.println("done!");
    }
}
