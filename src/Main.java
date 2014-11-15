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
        String matchToken = "17733c24-e85c-4ee2-8c5d-241524820a3a";
        String serverIP = "ip-10-169-99-210.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        //TODO: aim and shoot properly
        //TODO: movement properly

        System.out.println("done!");
    }
}
