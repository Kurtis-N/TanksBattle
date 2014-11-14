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
        String matchToken = "6d3d66bf-1fdc-4d3b-bd79-f9e12bae95da";
        String serverIP = "ip-10-165-80-48.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        //TODO: aim and shoot properly
        //TODO: movement properly

        System.out.println("done!");
    }
}
