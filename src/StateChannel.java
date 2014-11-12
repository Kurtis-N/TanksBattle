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
    private volatile String id1 = "";
    private volatile String id2 = "";

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
            //System.out.println("msg: \n"+msg);

            //ignore the matchTokens
            msg = msg.replace(matchToken, "");
            System.out.println("id1: " + id1);
            System.out.println("id2: " + id2);

            if(msg != null || !msg.isEmpty() || msg.equals("")) {
                parseMessage(msg);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }

        }
    }

    public synchronized String getID1() {
        return id1;
    }

    public synchronized String getID2() {
        return id2;
    }

    public synchronized void setId1(String id) {
        id1 = id;
    }

    public synchronized void setId2(String id) {
        id2 = id;
    }

    public void parseMessage(String msg) {
        try {
            JSONObject resp = new JSONObject(msg);
            if (resp.get("comm_type").equals("GAME_END")) {
                return;
            } else if (resp.get("comm_type").equals("GAME_START")) {
                return;
            } else if (resp.get("comm_type").equals("GAMESTATE")) {
                updatePlayers(msg);
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.out.println("caught json error");
        }
    }

    public  void updatePlayers(String r) {
        try {
            JSONObject resp = new JSONObject(r);
            JSONArray players = (JSONArray) resp.get("players");
            for (int i = 0; i < players.length(); i++) {
                JSONObject p = new JSONObject(players.get(i).toString());

                if (p.get("name").equals("Tanks But No Tanks")) {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    //for each tank - check if it's alive - then check if we already have the id
                    JSONObject tank = tanks.getJSONObject(0);
                    //if dead, remove from list
                    if (!tank.getBoolean("alive")) {
                        if (getID1().equals(tank.get("id").toString())) {
                            System.out.println("id1 set to empty string");
                            setId1("");
                        }
                    }
                    else
                        setId1(tank.get("id").toString());

                    tank = tanks.getJSONObject(1);
                    //if dead, remove from list
                    if (!tank.getBoolean("alive")) {
                        if (getID2().equals(tank.get("id").toString())) {
                            System.out.println("id2 set to empty string");
                            setId2("");
                        }
                    }
                    else
                        setId2(tank.get("id").toString());
                }
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("caught json error");
        }
    }

    public synchronized void aimTanks() {
        if(!id1.isEmpty()) {

        }
        if(!id2.isEmpty()) {

        }
    }
}
