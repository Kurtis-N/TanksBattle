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
    private volatile String matchToken;

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

        channel.send(matchconnect.toString().getBytes(), 0);
        String r = new String(channel.recv(0));
        //System.out.println("response: " + r);

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

        //get next command from q, execute command
        while(true){
            if(checkEmpty()) {
                try {
                    Thread.sleep(500);
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else
                sendCommand();
        }
    }


    //it is not possible for two invocations of synchronized methods on the same object to interleave
    public synchronized boolean checkEmpty() {
        if(q.isEmpty())
            return true;
        return false;
    }

    public synchronized void addToQueue(JSONObject s) {
        q.add(s);
    }

    public synchronized void sendCommand() {
        JSONObject c = q.poll();
        System.out.println(c.toString());
        channel.send(c.toString().getBytes(), 0);
        String r = new String(channel.recv(0));
        JSONObject resp = null;
        try {
            resp = new JSONObject(r);
           System.out.println("response:\n"+resp.toString());
        }
        catch (JSONException e) {
            //e.printStackTrace();
            //System.exit(2);
        }
    }

    public void rotateTank(String id, String direction, double degree) {
        JSONObject move = null;
        try {
            move = new JSONObject();
            move.put("tank_id", id);
            move.put("comm_type", "ROTATE");
            move.put("direction", direction);
            move.put("rads", Math.toRadians(degree));
            move.put("client_token", clientToken);
            //System.out.println(attack.toString());
            addToQueue(move);
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("rotateTank not properly forming");
        }
    }

    public void rotateTurrent(String id, String direction, double degree) {
        JSONObject move = null;
        try {
            move = new JSONObject();
            move.put("tank_id", id);
            move.put("comm_type", "ROTATE_TURRET");
            move.put("direction", direction);
            move.put("rads", degree);
            move.put("client_token", clientToken);
            //System.out.println(attack.toString());
            addToQueue(move);
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("rotateTurret not properly forming");
        }
    }

    public void fire(String id) {
        JSONObject move = null;
        try {
            move = new JSONObject();
            move.put("tank_id", id);
            move.put("comm_type", "FIRE");
            move.put("client_token", clientToken);
            //System.out.println(attack.toString());
            addToQueue(move);
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("fire not properly forming");
        }
    }

    public void stop(String id, String direction) {
        JSONObject move = null;
        try {
            move = new JSONObject();
            move.put("tank_id", id);
            move.put("comm_type", "STOP");
            move.put("control", "ROTATE");
            move.put("client_token", clientToken);
            //System.out.println(attack.toString());
            addToQueue(move);
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("rotateTurret not properly forming");
        }
    }



}
