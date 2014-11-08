import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeromq.ZMQ;

import java.util.LinkedList;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kurtisniedling on 2014-11-05.
 */
public class StateChannel implements Runnable{
    private ZMQ.Context context;
    private String matchToken;
    private String serverIP;
    private ZMQ.Socket channel;
    private Queue<JSONObject> q;
    private boolean flag = false;
    public static volatile ArrayList<String> ids;

    public StateChannel(ZMQ.Context context, String serverIP, String matchToken) {
        this.context = context;
        this.serverIP = serverIP;
        this.matchToken = matchToken;
        channel = context.socket(ZMQ.SUB);
        q = new LinkedList<JSONObject>();
        channel.connect("tcp://"+serverIP+":5556");
        channel.subscribe(matchToken.getBytes());
        System.out.println("connecting to state channel: " + serverIP);
        ids = new ArrayList<String>();

        (new Thread(this)).start();
    }

    public void run() {
        //check comm_type for:
        //GAME_START, MATCH_END, GAME_END
        System.out.println("Starting state thread..");
        while(true) {
            byte[] m = channel.recv(0);
            String msg = new String(m);
            //System.out.println("msg: \n"+msg);
            //check if m is equal to the match token, just ignore?
            msg = msg.replace(matchToken, "");
            //System.out.println("msg: \n"+msg);
            if(msg != null)
                parseMessage(msg);

        }
    }

    public synchronized void parseMessage(String msg) {
        try {
            System.out.println("starting parser");
            JSONObject resp = new JSONObject(msg);
            //JSONObject players = new JSONObject(resp.getString("players"));
            //System.out.println(msg);

            JSONArray players = (JSONArray) resp.get("players");
            //System.out.println("players array made");
            List<String> tankIds = new ArrayList<String>();
            for(int i = 0; i < players.length(); i++) {
                JSONObject p = new JSONObject(players.get(i).toString());
                if(p.get("name").equals("Tanks But No Tanks")) {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    //System.out.println("tanks array: " + tanks.length());
                    for(int j = 0; j < tanks.length(); j++) {
                        JSONObject tank = tanks.getJSONObject(j);
                        //System.out.println("tank: "+j+"\n"+tank.toString());
                        //tankIds.add(tank.get("id").toString());
                        ids.add(tank.get("id").toString());
                    }
                }
            }

            //System.out.println("tankIds: ");
            //for(String s : tankIds) {
                //System.out.println(s);
            //    ids.add(s);
            //}
            //for(String s : ids) {
            //    System.out.println(s);
            //}
        }
        catch (JSONException e) {
            e.printStackTrace();
            System.out.println("caught json error");
        }
        System.out.println("done parsing");
    }
}
