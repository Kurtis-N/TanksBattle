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
        String matchToken = "b4faa9e6-5c1e-4fc1-9ffc-cdbb14323e47";
        String serverIP = "ip-10-234-167-219.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        System.out.println("done!");
        //Establish request/response ZMQ connection to central game channel (command channel)
        //Context context = ZMQ.context(1);
        //ZMQ.Socket channel = context.socket(ZMQ.REQ);


        //Establish pub/sub ZMQ connection to central game channel (state channel)
        //Socket publisher = context.socket(ZMQ.PUB);
    }
}
