import org.json.JSONObject;
import org.json.JSONException;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by kurtisniedling on 2014-11-05.
 */
public class CommandChannel implements Runnable {
    private ZMQ.Socket channel;
    private Queue<JSONObject> q; //change this to String so don't need try/catch for JSONObject creation
    private String matchToken;

    public volatile String clientToken = "";

    public CommandChannel(ZMQ.Context c, String serverIP, String matchToken) {
        this.matchToken = matchToken;
        channel = c.socket(ZMQ.REQ);
        q = new LinkedList<JSONObject>();
        channel.connect("tcp://"+serverIP+":5557");
        System.out.println("connecting to command channel: " + serverIP);

        (new Thread(this)).start();
    }

    public void run() {
        System.out.println("Starting command thread..");

        JSONObject matchconnect = new JSONObject();
        try {
            matchconnect.put("comm_type", "MatchConnect");
            matchconnect.put("match_token", matchToken);
            matchconnect.put("team_name", "Tanks But No Tanks");
            matchconnect.put("password", "dontworryaboutit");
        } catch (JSONException e) {
            e.printStackTrace();
            System.exit(1);
        }
        //System.out.println("matchconnect: " + matchconnect.toString());
        channel.send(matchconnect.toString().getBytes(), 0);
        String r = new String(channel.recv(0));
        System.out.println("response: " + r);

        JSONObject resp = null;
        try {
            resp = new JSONObject(r);
            if(resp.getString("comm_type").equals("MatchConnectResp")){
                clientToken = resp.getString("client_token");
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            System.exit(2);
        }

        //Now in our thread, we should loop forever checking for any JSON commands in the queue
        //while(true){
        //  //execute command
        //}

    }

}
