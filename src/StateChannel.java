import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kurtisniedling on 2014-11-05.
 */
public class StateChannel {
    private ZMQ.Context context;
    private String matchToken;
    private String serverIP;
    private ZMQ.Socket channel;
    private Queue<JSONObject> q;
    private boolean flag = false;
    public static volatile ArrayList<String> ids;
    private String clientToken;

    public StateChannel(ZMQ.Context context, String serverIP, String matchToken, String clientToken) {
        this.context = context;
        this.serverIP = serverIP;
        this.matchToken = matchToken;
        this.clientToken = clientToken;
        channel = context.socket(ZMQ.SUB);
        q = new LinkedList<JSONObject>();
        channel.connect("tcp://"+serverIP+":5556");
        channel.subscribe(matchToken.getBytes());
        System.out.println("connecting to state channel: " + serverIP);

        //
        // (new Thread(this)).start();
    }

    public String getGameState() {
        byte[] m = channel.recv(0);
        String msg = new String(m);

        //ignore the matchToken
        msg = msg.replace(matchToken, "");
        //System.out.println("msg: \n"+msg);
        return msg;
    }
}
