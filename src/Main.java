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
        String matchToken = "26d1b212-3aab-4b51-9ac8-884c89b09972";
        String serverIP = "ip-10-136-78-179.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        System.out.println("done!");
    }
}
