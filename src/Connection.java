import org.json.JSONException;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import java.util.ArrayList;
import java.util.Queue;
import org.json.JSONObject;

import static java.lang.Thread.sleep;
import java.util.List;
/**
 * Created by kurtisniedling on 2014-11-04.
 */
public class Connection {
    private String matchToken;
    private String serverIP;
    private ZMQ.Socket channel;
    //private String clientToken = "";

    public Connection() {}

    public Connection(String matchToken, String serverIP) {
        this.matchToken = matchToken;
        this.serverIP = serverIP;

        //Make one CommandChannel for commands
        //Command issues a connect to the server, then loops forever
        Context context = ZMQ.context(1);
        CommandChannel cc = new CommandChannel(context, serverIP, matchToken);
        System.out.println("connecting to client..");
        while(cc.clientToken.isEmpty()) {
            try {
                sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("in loop");
        }
        System.out.println("client token: " + cc.clientToken);

        //Now that we have the client token, set up the state channel to monitor the game board state
        //This is a read only channel
        //uses TCP, port 5556
        StateChannel sc = new StateChannel(context, serverIP, matchToken);

        //Our algorithm
        Algo algo = new Algo(cc, sc);
        algo.begin();
        //algo.spinAndShoot();
        //algo.betterAlgorithm();

        //wait for threads to die
//        try {
//            Thread.currentThread().join();
//        }
//        catch (InterruptedException e) {
//            e.printStackTrace();
//            System.exit(3);
//        }
    }

}
