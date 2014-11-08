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

        /*List<String> tankIds = sc.ids;
        while(tankIds.isEmpty()) {
            tankIds = sc.ids;
        }
        for(String id : tankIds)
                System.out.println(id);*/

        //Our algorithm
        //Algo algo = new Algo(cc, sc);
        //algo.start();

        //spin and shoot !
        while(true) {
            System.out.println("starting spin & shoot");
            if(sc.ids.size() > 2)
                sc.ids.clear();
            if(sc.ids.size() > 0) {
                for(int i = 0; i < sc.ids.size(); i++) {
                    System.out.println("sending fire and rotate");
                    //System.out.println(sc.ids.get(i));
                    String temp = sc.ids.get(i);
                    JSONObject attack = null;
                    try {
                        attack = new JSONObject();
                        attack.put("tank_id", temp);
                        attack.put("comm_type", "FIRE");
                        attack.put("client_token", cc.clientToken);
                        System.out.println(attack.toString());
                        cc.addToQueue(attack);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONObject spin = null;
                    try {
                        spin = new JSONObject();
                        spin.put("tank_id", temp);
                        spin.put("comm_type", "ROTATE_TURRET");
                        spin.put("direction", "CCW");
                        spin.put("rads", 1.11);
                        spin.put("client_token", cc.clientToken);
                        cc.addToQueue(spin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
                System.out.println("nothing in list");
        }





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
