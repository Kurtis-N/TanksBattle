import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by kurtisniedling on 2014-11-05.
 */
public class StateChannel implements Runnable{
    private ZMQ.Context context;
    private String matchToken;
    private String serverIP;
    private ZMQ.Socket channel;
    private Queue<JSONObject> q;

    public StateChannel(ZMQ.Context context, String serverIP, String matchToken) {
        this.context = context;
        this.serverIP = serverIP;
        this.matchToken = matchToken;
        channel = context.socket(ZMQ.SUB);
        q = new LinkedList<JSONObject>();
        channel.connect("tcp://"+serverIP+":5556");
        channel.subscribe(matchToken.getBytes());
        System.out.println("connecting to state channel: " + serverIP);

        (new Thread(this)).start();
    }

    public void run() {
        //check comm_type for:
        //GAME_START, MATCH_END, GAME_END
        System.out.println("Starting state thread..");
        while(true) {
            byte[] m = channel.recv(0);
            String msg = new String(m);
            System.out.println(msg);
            //check if m is equal to the match token, just ignore?
            msg.replace(matchToken, "");

            JSONObject resp = null;
            try {
                resp = new JSONObject(msg);
                System.out.println(resp.toString());
            }
            catch (JSONException e) {
                //should do something here?
                e.printStackTrace();
            }
        }

    }
}
