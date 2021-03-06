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
    private StateChannel sc;
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

    private double track1;
    private double track2;

    private double turret1;
    private double turret2;

    private double enemyAngle1;
    private double enemyAngle2;

    private boolean alive1 = false;
    private boolean alive2 = false;

    private boolean enemyAlive1 = false;
    private boolean enemyAlive2 = false;

    private long pt1 = -1;
    private long pt2 = -1;

    private long mv1 = -1;
    private long mv2 = -1;

    List<Projectile> projectiles;

    public Connection() {}

    public Connection(String matchToken, String serverIP) {
        this.matchToken = matchToken;
        this.serverIP = serverIP;
        rand = new Random(7919);
        projectiles = new ArrayList<Projectile>();
        //rand = new Random(104729);

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
        sc = new StateChannel(context, serverIP, matchToken, cc.clientToken);

        //TODO: write aim function that checks for own tanks
        //TODO: write aim function that checks for barricades / walls
        //TODO: don't run into walls?
        //TODO: check current shots & don't move into them
        //TODO: don't run over other tank

        while (true) {
            String gs = sc.getGameState();
            if (gs == null || gs.isEmpty() || gs.equals("")) {
                continue;
            }

            try {
                JSONObject resp = new JSONObject(gs);
                update(gs);
                //updatePrime(gs);

                if (!resp.get("comm_type").equals("GAMESTATE")) {
                    timeRemaining = 5000000;
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e) {
                    //e.printStackTrace();
                    }
                    continue;
                }
                if (resp.getDouble("timeRemaining") < timeRemaining) {
                    timeRemaining = resp.getDouble("timeRemaining");
                    if (alive1) {
                        cc.rotateTank(id1, "CW", 1.57);
                        moveRandom(id1);
                        aim1Prime();
                    }
                    if (alive2) {
                        cc.rotateTank(id2, "CCW", 1.57);
                        moveRandom(id2);
                        aim2Prime();
                    }
                    cc.fire(id1);
                    cc.fire(id2);
                }
            } catch (JSONException e) {
                //e.printStackTrace();
                //System.out.println("caught json error");
            }
        }
    }

    public void moveRandom(String id) {
        if(id1 != null && id.equals(id1)) {
            long ct1 = System.nanoTime();
            if(ct1 < mv1)
                return;
        }
        else if(id2 != null && id.equals(id2)) {
            long ct2 = System.nanoTime();
            if(ct2 < mv2)
                return;
        }
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
        double r = rand.nextInt(17);
        if(id.equals(id1)) {
            mv1 = System.nanoTime() + (long) (r * 150000000);
        }
        else if(id.equals(id2)) {
            mv2 = System.nanoTime() + (long) (r * 150000000);
        }
        cc.move(id, dir, r);
    }

    //deprecated
    public void aim1() {
        long ct1 = System.nanoTime();
        if(ct1 < pt1)
            return;

        double d1 = Math.sqrt((p1x-e1x)*(p1x-e1x) + (p1y-e1y)*(p1y-e1y));
        double d2 = Math.sqrt((p1x-e2x)*(p1x-e2x) + (p1y-e2y)*(p1y-e2y));
        double angle;
        if(d1 <= d2 && enemyAlive1) {
            angle = Math.atan2(e1y - p1y, e1x - p1x);
        }
        else if(enemyAlive2){
            angle = Math.atan2(e2y - p1y, e2x - p1x);
        }
        else
            return;

        if(angle < 0)
            angle += 2*Math.PI;
        double diff = Math.abs(turret1 - angle);
        if(diff < 0.01) {
            cc.stop(id1, "ROTATE_TURRET");
            return;
        }
        pt1 = System.nanoTime();

        if(angle > turret1) {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CCW: " + Math.toDegrees(diff));
                cc.rotateTurret(id1,"CCW", diff);
                pt1 += (diff/1.5)*150000000;
            }
            else {
                //System.out.println("rotate CW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id1, "CW", (2*Math.PI)-diff);
                pt1 += (diff/(2*Math.PI))*150000000;
            }
        }
        else {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CW " + Math.toDegrees(diff));
                cc.rotateTurret(id1, "CW", diff);
                pt1 += (diff/1.5)*150000000;
            }
            else {
                //System.out.println("rotate CCW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id1, "CCW", (2*Math.PI)-diff);
                pt1 += (diff/(2*Math.PI))*150000000;
            }
        }
    }

    //deprecated
    public void aim2() {
        long ct2 = System.nanoTime();
        if(ct2 < pt2)
            return;

        double d1 = Math.sqrt((p2x-e1x)*(p2x-e1x) + (p2y-e1y)*(p2y-e1y));
        double d2 = Math.sqrt((p2x-e2x)*(p2x-e2x) + (p2y-e2y)*(p2y-e2y));
        double angle;
        if(d1 <= d2 && enemyAlive1) {
            angle = Math.atan2(e1y - p2y, e1x - p2x);
        }
        else if(enemyAlive2) {
            angle = Math.atan2(e2y - p2y, e2x - p2x);
        }
        else
            return;

        if(angle < 0)
            angle += 2*Math.PI;
        double diff = Math.abs(turret2 - angle);
        if(diff < 0.01) {
            cc.stop(id2, "ROTATE_TURRET");
            return;
        }
        if(angle > turret2) {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CCW: " + Math.toDegrees(diff));
                cc.rotateTurret(id2,"CCW", diff);
            }
            else {
                //System.out.println("rotate CW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id2, "CW", (2*Math.PI)-diff);
            }
        }
        else {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CW " + Math.toDegrees(diff));
                cc.rotateTurret(id2, "CW", diff);
            }
            else {
                //System.out.println("rotate CCW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id2, "CCW", (2*Math.PI)-diff);
            }
        }
    }

    //includes check for own player in aiming
    public void aim1Prime() {

        double d1 = Math.sqrt((p1x-e1x)*(p1x-e1x) + (p1y-e1y)*(p1y-e1y));
        double d2 = Math.sqrt((p1x-e2x)*(p1x-e2x) + (p1y-e2y)*(p1y-e2y));
        double d3 = Math.sqrt((p1x-p2x)*(p1x-p2x) + (p1y-p2y)*(p1y-p2y)); //dist to player 3

        double angle;
        if(d1 <= d2 && enemyAlive1) {
            angle = Math.atan2(e1y - p1y, e1x - p1x);
            //if the angles are within 5 rad and our tank is closer
            if(Math.abs(angle - (Math.atan2(p2y - p1y, p2x - p1x))) < 0.176 && d3 < d1) {
                return;
            }
        }
        //0.087
        else if(enemyAlive2){
            angle = Math.atan2(e2y - p1y, e2x - p1x);
            if(Math.abs(angle - (Math.atan2(p2y - p1y, p2x - p1x))) < 0.176 && d3 < d2) {
                return;
            }
        }
        else
            return;

        if(angle < 0)
            angle += 2*Math.PI;
        double diff = Math.abs(turret1 - angle);
        if(diff < 0.01) {
            cc.stop(id1, "ROTATE_TURRET");
            return;
        }
        pt1 = System.nanoTime();

        if(angle > turret1) {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CCW: " + Math.toDegrees(diff));
                cc.rotateTurret(id1,"CCW", diff);
                pt1 += (diff/1.5)*150000000;
            }
            else {
                //System.out.println("rotate CW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id1, "CW", (2*Math.PI)-diff);
                pt1 += (diff/(2*Math.PI))*150000000;
            }
        }
        else {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CW " + Math.toDegrees(diff));
                cc.rotateTurret(id1, "CW", diff);
                pt1 += (diff/1.5)*150000000;
            }
            else {
                //System.out.println("rotate CCW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id1, "CCW", (2*Math.PI)-diff);
                pt1 += (diff/(2*Math.PI))*150000000;
            }
        }
        cc.fire(id1);
    }

    //includes check for own player in aiming
    public void aim2Prime() {
        long ct2 = System.nanoTime();
        if(ct2 < pt2)
            return;

        double d1 = Math.sqrt((p2x-e1x)*(p2x-e1x) + (p2y-e1y)*(p2y-e1y));
        double d2 = Math.sqrt((p2x-e2x)*(p2x-e2x) + (p2y-e2y)*(p2y-e2y));
        double d3 = Math.sqrt((p2x-p1x)*(p2x-p1x) + (p2y-p1y)*(p2y-p1y));
        double angle;

        if(d1 <= d2 && enemyAlive1) {
            angle = Math.atan2(e1y - p2y, e1x - p2x);
            if(Math.abs(angle - (Math.atan2(p1y - p2y, p1x - p2x))) < 0.087 && d3 < d1) {
                //System.out.println("not firing");
                return;
            }
        }
        else if(enemyAlive2){
            angle = Math.atan2(e2y - p2y, e2x - p2x);
            if(Math.abs(angle - (Math.atan2(p1y - p2y, p1x - p2x))) < 0.087 && d3 < d2) {
                //System.out.println("not firing");
                return;
            }
        }
        else
            return;

        if(angle < 0)
            angle += 2*Math.PI;
        double diff = Math.abs(turret2 - angle);
        if(diff < 0.01) {
            cc.stop(id2, "ROTATE_TURRET");
            return;
        }
        if(angle > turret2) {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CCW: " + Math.toDegrees(diff));
                cc.rotateTurret(id2,"CCW", diff);
            }
            else {
                //System.out.println("rotate CW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id2, "CW", (2*Math.PI)-diff);
            }
        }
        else {
            if(Math.toDegrees(diff) <= 180) {
                //System.out.println("rotate CW " + Math.toDegrees(diff));
                cc.rotateTurret(id2, "CW", diff);
            }
            else {
                //System.out.println("rotate CCW: " + (360 - Math.toDegrees(diff)));
                cc.rotateTurret(id2, "CCW", (2*Math.PI)-diff);
            }
        }
        cc.fire(id2);
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
                        /*JSONArray p = tank.getJSONArray("position");
                        for(int j = 0; j < p.length(); j++) {

                        }*/
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

    public void updatePrime(String r) {
        try {
            projectiles.clear();
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
                        track1 = tank.getDouble("tracks");
                    }
                    else {
                        alive1 = false;
                    }
                    JSONArray pos = tank.getJSONArray("projectile");
                    for(int j = 0; j < pos.length(); j++) {
                        JSONObject missile = pos.getJSONObject(i);
                        JSONArray position = missile.getJSONArray("position");
                        Projectile pj = new Projectile(position.getDouble(0), position.getDouble(1), missile.getDouble("direction"));
                        projectiles.add(pj);
                    }

                    tank = tanks.getJSONObject(1); //tank 2
                    if (tank.getBoolean("alive")) {
                        alive2 = true;
                        id2 = tank.getString("id");
                        JSONArray d = tank.getJSONArray("position");
                        p2x = d.getDouble(0);
                        p2y = d.getDouble(1);
                        turret2 = tank.getDouble("turret");
                        track2 = tank.getDouble("tracks");
                    }
                    else {
                        alive2 = false;
                    }
                    pos = tank.getJSONArray("projectile");
                    for(int j = 0; j < pos.length(); j++) {
                        JSONObject missile = pos.getJSONObject(i);
                        JSONArray position = missile.getJSONArray("position");
                        Projectile pj = new Projectile(position.getDouble(0), position.getDouble(1), missile.getDouble("direction"));
                        projectiles.add(pj);
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
                    JSONArray pos = tank.getJSONArray("projectile");
                    for(int j = 0; j < pos.length(); j++) {
                        JSONObject missile = pos.getJSONObject(i);
                        JSONArray position = missile.getJSONArray("position");
                        Projectile pj = new Projectile(position.getDouble(0), position.getDouble(1), missile.getDouble("direction"));
                        projectiles.add(pj);
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
                    pos = tank.getJSONArray("projectile");
                    for(int j = 0; j < pos.length(); j++) {
                        JSONObject missile = pos.getJSONObject(i);
                        JSONArray position = missile.getJSONArray("position");
                        Projectile pj = new Projectile(position.getDouble(0), position.getDouble(1), missile.getDouble("direction"));
                        projectiles.add(pj);
                    }
                }

            }
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.out.println("caught json error in updating tanks");
        }
    }

    public void dodge1() {
        if(!alive1)
            return;
        double angle;
        List<Projectile> incoming = new ArrayList<Projectile>();
        for(Projectile p : projectiles) {
            angle = Math.atan2(p.y - p1y, p.x - p1x);
            if(Math.abs(angle-p.dir) < 0.017) {
                incoming.add(p);
            }
        }
        moveRandom(id1);
        if(incoming.isEmpty()) {
            //moveRandom(id1);
            return;
        }
        double diff = Math.abs(track1 - (incoming.get(0).dir+1.57));

        if(diff > 0.1745) {
            cc.move(id1, "FWD", 5);
        }
        else {
            cc.move(id1, "FWD", 5);
            cc.rotateTank(id1, "CW", 0.5236);
            /*if(incoming.get(0).dir > track2) {
                if(Math.toDegrees(diff) <= 180) {
                    cc.rotateTank(id2,"CCW", diff);
                }
                else {
                    cc.rotateTank(id2, "CW", (2*Math.PI)-diff);
                }
            }
            else {
                if(Math.toDegrees(diff) <= 180) {
                    cc.rotateTank(id2, "CW", diff);
                }
                else {
                    cc.rotateTank(id2, "CCW", (2*Math.PI)-diff);
                }
            }*/
            //moveRandom(id2);
        }
    }

    public void dodge2() {
        if(!alive2)
            return;
        double angle;
        List<Projectile> incoming = new ArrayList<Projectile>();
        for(Projectile p : projectiles) {
            angle = Math.atan2(p.y - p2y, p.x - p2x);
            if(Math.abs(angle-p.dir) < 0.017) {
                incoming.add(p);
            }
        }

        if(incoming.isEmpty()) {
            //moveRandom(id1);
            return;
        }
        double diff = Math.abs(track2 - (incoming.get(0).dir));
        if(diff > 0.1745) {
            cc.move(id2, "REV", 5);
        }
        else {
            cc.move(id2, "REV", 5);
            cc.rotateTank(id2, "CW", 0.5236);
            /*if(incoming.get(0).dir > track2) {
                if(Math.toDegrees(diff) <= 180) {
                    cc.rotateTank(id2,"CCW", diff);
                }
                else {
                    cc.rotateTank(id2, "CW", (2*Math.PI)-diff);
                }
            }
            else {
                if(Math.toDegrees(diff) <= 180) {
                    cc.rotateTank(id2, "CW", diff);
                }
                else {
                    cc.rotateTank(id2, "CCW", (2*Math.PI)-diff);
                }
            }*/
            //moveRandom(id2);
        }

    }
}
