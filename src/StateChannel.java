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
            parseMessage(msg);

        }
    }

    public void parseMessage(String msg) {
        try {
            JSONObject resp = new JSONObject(msg);
            //JSONObject players = new JSONObject(resp.getString("players"));
            //System.out.println(players.toString());

            JSONArray players = (JSONArray) resp.get("players");
            List<String> tankIds = new ArrayList<String>();
            for(int i = 0; i < players.length(); i++) {
                JSONObject p = new JSONObject(players.get(i).toString());
                if(p.get("name").equals("Tanks But No Tanks")) {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    for(int j = 0; j < tanks.length(); j++) {
                        JSONObject tank = new JSONObject(tanks.get(i).toString());
                        tankIds.add(tank.get("id").toString());
                    }
                }
            }
            System.out.println("tankIds: ");
            for(String s : tankIds) {
                System.out.println(s);
            }


        }
        catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
