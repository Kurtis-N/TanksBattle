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
        String matchToken = "3e242be6-2037-4de0-953c-cf09cc621dd3";
        String serverIP = "ip-10-81-166-107.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        //TODO: aim and shoot properly
        //TODO: movement properly

        System.out.println("done!");
    }
}
