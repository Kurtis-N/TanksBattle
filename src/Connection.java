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
import java.util.Random;
/**
 * Created by kurtisniedling on 2014-11-04.
 */
public class Connection {
    private String matchToken;
    private String serverIP;
    private ZMQ.Socket channel;
    private CommandChannel cc;
    private double timeRemaining = 5000000;
    Random rand;

    private String id1;
    private String id2;

    private double p1x;
    private double p1y;
    private double p2x;
    private double p2y;

    private double e1x;
    private double e1y;
    private double e2x;
    private double e2y;

    private double angle1;
    private double angle2;

    private double turret1;
    private double turret2;

    private double enemyAngle1;
    private double enemyAngle2;

    private boolean alive1 = false;
    private boolean alive2 = false;

    private boolean enemyAlive1 = false;
    private boolean enemyAlive2 = false;



    public Connection() {}

    public Connection(String matchToken, String serverIP) {
        this.matchToken = matchToken;
        this.serverIP = serverIP;
        rand = new Random(16011992);

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
                if (resp.get("comm_type").equals("GAMESTATE") && resp.getDouble("timeRemaining") < timeRemaining) {
                    update(gs);
                    timeRemaining = resp.getDouble("timeRemaining");
                    if(alive1) {
                        aim1();
                        cc.fire(id1);
                    }
                    if(alive2) {
                        aim2();
                        cc.fire(id2);
                    }
                    moveRandom(id1);
                    moveRandom(id2);
                    //spinAndShoot();
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                //System.out.println("caught json error");
            }
            try {
                Thread.sleep(100);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    //put this in a thread that periodically calls it
    public void moveRandom(String id) {
        int i = rand.nextInt(2);
        String dir = "";
        String facing = "";
        if(i == 0) {
            dir = "FWD";
            facing = "CW";
        }
        else if(i == 1) {
            dir = "REV";
            facing = "CCW";
        }
        cc.rotateTank(id, facing, Math.toRadians(45));
        cc.move(id, dir, rand.nextInt(100));
    }

    //change the aims to whichever is closer or has line of sight
    public void aim1() {
        double angle = Math.atan2(e1y, e1x);
        if(angle < 0) {
            angle += (2*Math.PI);
        }
        double rotate = turret1-angle;
        if(rotate > 0)
            cc.rotateTurrent(id1, "CW", rotate);
        else
            cc.rotateTurrent(id1, "CCW", Math.abs(rotate));

        /*double rotate = angle = turret1;
        if(rotate > 2*Math.acos(-1))
            rotate = rotate-2*Math.acos(-1);
        if(rotate < -(2*Math.acos(-1)))
            rotate = rotate+2*Math.acos(-1);
        if(rotate > Math.acos(-1))
            rotate -= 2*Math.acos(-1);
        else if(rotate < -Math.acos(-1))
            rotate += 2*Math.acos(-1);
        if(rotate > 0)
            cc.rotateTurrent(id1, "CCW", Math.abs(rotate));
        else
            cc.rotateTurrent(id1, "CW", rotate);
        */

        //System.out.println("e1y: " + e1y + " e1x: " + e1x + " angle:" + Math.atan2(e1y, e1x));
        //System.out.println("turret1: " + turret1 +" angle: " + angle + " rotate: " + Math.abs(rotate));
    }

    public void aim2() {
        double angle = Math.atan2(e2y, e2x);
        if(angle < 0) {
            angle += (2*Math.PI);
        }
        double rotate = turret2-angle;
        if(rotate > 0)
            cc.rotateTurrent(id2, "CW", rotate);
        else
            cc.rotateTurrent(id2, "CCW", Math.abs(rotate));


/*        if(rotate > 2*Math.acos(-1))
            rotate = rotate-2*Math.acos(-1);
        if(rotate < -(2*Math.acos(-1)))
            rotate = rotate+2*Math.acos(-1);
        if(rotate > Math.acos(-1))
            rotate -= 2*Math.acos(-1);
        else if(rotate < -Math.acos(-1))
            rotate += 2*Math.acos(-1);
        if(rotate > 0)
            cc.rotateTurrent(id1, "CCW", Math.abs(rotate));
        else
            cc.rotateTurrent(id1, "CW", rotate);
            */
       //    System.out.println("turret2: " + turret2 +" angle: " + angle + " rotate: " + Math.abs(rotate));
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
                        alive1 = true;
                        id1 = tank.getString("id");
                        JSONArray d = tank.getJSONArray("position");
                        p1x = d.getDouble(0);
                        p1y = d.getDouble(1);
                        turret1 = tank.getDouble("turret");

                        cc.fire(id1);
                        cc.rotateTurrent(id1, "CW", Math.toRadians(90));
                    }
                    else {
                        alive1 = false;
                    }

                    tank = tanks.getJSONObject(1); //tank 2
                    if (tank.getBoolean("alive")) {
                        alive2 = true;
                        id2 = tank.getString("id");
                        JSONArray d = tank.getJSONArray("position");
                        p2x = d.getDouble(0);
                        p2y = d.getDouble(1);
                        turret2 = tank.getDouble("turret");

                        cc.fire(id2);
                        cc.rotateTurrent(id2, "CW", Math.toRadians(90));
                    }
                    else {
                        alive2 = false;
                    }
                }

                //update their guys
               else {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    JSONObject tank = tanks.getJSONObject(0);
                    //if dead remove
                    if (tank.getBoolean("alive")) {
                        enemyAlive1 = true;
                        JSONArray d = tank.getJSONArray("position");
                        e1x = d.getDouble(0);
                        e1y = d.getDouble(1);
                        //setEnemyAngle1(getAngle(getE1x(), getE1y()));
                    }
                    //aim at other tank
                    else {
                        enemyAlive1 = false;
                    }

                    tank = tanks.getJSONObject(1);
                    if (tank.getBoolean("alive")) {
                        enemyAlive2 = true;
                        JSONArray d = tank.getJSONArray("position");
                        e2x = d.getDouble(0);
                        e2y = d.getDouble(1);
                        //setEnemyAngle2(getAngle(getE2x(), getE2y()));
                    }
                    else {
                        enemyAlive2 = false;
                    }
                }

            }
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.out.println("caught json error in updating tanks");
        }
    }
}
