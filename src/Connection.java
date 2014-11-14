import org.json.JSONArray;
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
    private CommandChannel cc;
    //private String clientToken = "";

    public Connection() {}

    public Connection(String matchToken, String serverIP) {
        this.matchToken = matchToken;
        this.serverIP = serverIP;

        //Make one CommandChannel for commands
        //Command issues a connect to the server, then loops forever
        Context context = ZMQ.context(1);
        cc = new CommandChannel(context, serverIP, matchToken);
        System.out.println("connecting to client..");
        while (cc.clientToken.isEmpty()) {
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
        StateChannel sc = new StateChannel(context, serverIP, matchToken, cc.clientToken);

        while (true) {
            String gs = sc.getGameState();
            if (gs == null || gs.isEmpty() || gs.equals("")) {
                continue;
            }
            try {
                JSONObject resp = new JSONObject(gs);
                if (resp.get("comm_type").equals("GAME_END")) {
                    return;
                }
                if (resp.get("comm_type").equals("GAMESTATE")) { // && resp.getDouble("timeRemaining") < timeRemaining) {
                    update(gs);
                    //timeRemaining = resp.getDouble("timeRemaining");
                    //update(msg);
                    //if(alive1)
                    //    aimAndShoot1();
                    //if(alive2)
                    //   aimAndShoot2();
                    //spinAndShoot();
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                //System.out.println("caught json error");
            }


        }
    }

    public void update(String r) {
        try {
            JSONObject resp = new JSONObject(r);
            JSONArray players = (JSONArray) resp.get("players");
            //For each of our tanks
            for (int i = 0; i < players.length(); i++) {
                JSONObject p = new JSONObject(players.get(i).toString());

                // update our guys
                if (p.get("name").equals("Tanks But No Tanks")) {
                    JSONArray tanks = (JSONArray) p.get("tanks");

                    JSONObject tank = tanks.getJSONObject(0); //tank 1
                    if (tank.getBoolean("alive")) {
                        cc.fire(tank.getString("id"));
                        cc.rotateTurrent(tank.getString("id"), "CW", Math.toRadians(90));
                    }
                    else {
                    }

                    tank = tanks.getJSONObject(1); //tank 2
                    if (tank.getBoolean("alive")) {
                        cc.fire(tank.getString("id"));
                        cc.rotateTurrent(tank.getString("id"), "CW", Math.toRadians(90));
                    }
                    else {
                    }
                }

                //update their guys
               /* else {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    JSONObject tank = tanks.getJSONObject(0);
                    //if dead remove
                    if (tank.getBoolean("alive")) {
                        JSONArray d = tank.getJSONArray("position");
                        setE1x(d.getDouble(0));
                        setE1y(d.getDouble(1));
                        setEnemyAngle1(getAngle(getE1x(), getE1y()));
                    }
                    //aim at other tank
                    else {
                        setEnemyAngle1(getEnemyAngle2());
                    }
                    tank = tanks.getJSONObject(1);

                    if (!tank.getBoolean("alive")) {
                        JSONArray d = tank.getJSONArray("position");
                        setE2x(d.getDouble(0));
                        setE2y(d.getDouble(1));
                        setEnemyAngle2(getAngle(getE2x(), getE2y()));
                    }
                    else {
                        setEnemyAngle2(getAngle1());
                    }
                }*/

            }
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.out.println("caught json error in updating tanks");
        }
    }
}
