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

    private volatile double p1x;
    private volatile double p1y;
    private volatile double p2x;
    private volatile double p2y;

    private volatile double e1x;
    private volatile double e1y;
    private volatile double e2x;
    private volatile double e2y;

    private volatile double angle1;
    private volatile double angle2;

    private volatile double turret1;
    private volatile double turret2;

    private volatile double enemyAngle1;
    private volatile double enemyAngle2;

    private CommandChannel cc;
    private String clientToken;

    public StateChannel(ZMQ.Context context, String serverIP, String matchToken, CommandChannel cc) {
        this.context = context;
        this.serverIP = serverIP;
        this.matchToken = matchToken;
        this.cc = cc;
        clientToken = cc.clientToken;
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

            if(msg != null || !msg.isEmpty() || msg.equals("")) {
                parseMessage(msg);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }

        }
    }

    public void parseMessage(String msg) {
        try {
            JSONObject resp = new JSONObject(msg);
            if (resp.get("comm_type").equals("GAME_END")) {
                return;
            } else if (resp.get("comm_type").equals("GAME_START")) {
                return;
            } else if (resp.get("comm_type").equals("GAMESTATE")) {
                update(msg);
                aimAndShoot();
                //spinAndShoot();
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.out.println("caught json error");
        }
    }


    /*
        Spin and shoot motherfucka!!!
    */
    public void spinAndShoot() {
        String id1 = getID1();
        String id2 = getID2();
        //System.out.println("starting spin & shoot");
        if(!id1.isEmpty()) {
            cc.fire(id1);
            cc.rotateTurrent(id1, "CCW", 35);
        }
        if(!id2.isEmpty()) {
            cc.fire(id2);
            cc.rotateTurrent(id2, "CW", 35);
        }
    }

    /*
       check which tank closer too
       calculate angle to rotate
       rotate that angle
       shoot at that angle
    */
    public void aimAndShoot() {
        //for tank 1
        //Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
        double d1 = Math.abs(Math.sqrt((getP1x() - getE1x()) * (getP1x() - getE1x()) + (getP1y() - getE1y()) * (getP1y() - getE1y())));
        double d2 = Math.abs(Math.sqrt((getP1x()-getE2x())*(getP1x()-getE2x()) + (getP1y()-getE2y())*(getP1y()-getE2y())));
        //which tank to aim at
        //if(d1 >= d2) {
            //whether to rotate cw or ccw
            double angle = Math.atan2(getE1y()-getP1y(),getE1x()-getP1x());
            if(angle < 0) {
                angle += 2*Math.PI;
            }
            System.out.println("angle: " + Math.toDegrees(angle));
            System.out.println("turret: " + turret1);
            double rotate = Math.toRadians(turret1)-angle;
            if(rotate < 0)
                cc.rotateTurrent(getID1(), "CW", Math.abs(rotate));
            else
                cc.rotateTurrent(getID1(), "CCW", Math.abs(rotate));
            cc.fire(getID1());
            System.out.println("aim n shoot : " + Math.toDegrees(Math.abs(rotate)));
//            if(getTurret1() > getEnemyAngle1()) {
//                cc.rotateTurrent(getID1(), "CW", getTurret1() - getEnemyAngle1());
//            }
//            else
//                cc.rotateTurrent(getID1(), "CCW", getTurret1() - getEnemyAngle1());
//            cc.fire(getID1());

        //}
//        else {
////            if(getTurret1() > getEnemyAngle2()) {
////                cc.rotateTurrent(getID1(), "CW", getTurret1() - getEnemyAngle2());
////            }
////            else
////                cc.rotateTurrent(getID1(), "CCW", getTurret1() - getEnemyAngle2());
////            cc.fire(getID1());
//        }

        //for tank2
        d1 = Math.abs(Math.sqrt((getP1x()-getE1x())*(getP1x()-getE1x()) + (getP1y()-getE1y())*(getP1y()-getE1y())));
        d2 = Math.abs(Math.sqrt((getP1x()-getE2x())*(getP1x()-getE2x()) + (getP1y()-getE2y())*(getP1y()-getE2y())));
        /*if(d1 >= d2) {
            if(getTurret2() > getEnemyAngle1()) {
                cc.rotateTurrent(getID2(), "CW", getTurret2() - getEnemyAngle1());
            }
            else
                cc.rotateTurrent(getID2(), "CCW", getTurret2() - getEnemyAngle1());
            cc.fire(getID2());
        }
        else {
            if(getTurret2() > getEnemyAngle2()) {
                cc.rotateTurrent(getID2(), "CW", getTurret2() - getEnemyAngle2());
            }
            else
                cc.rotateTurrent(getID2(), "CCW", getTurret2() - getEnemyAngle2());
            cc.fire(getID2());
        }*/
        angle = Math.atan2(getE2y()-getP2y(),getE2x()-getP2x());
        if(angle < 0) {
            angle += (2*Math.PI);
        }
        //System.out.println("angle: " + Math.toDegrees(angle));
        //System.out.println("turrent: " + turret1);
        rotate = Math.toRadians(turret1)-angle;
        if(rotate < 0)
            cc.rotateTurrent(getID2(), "CW", Math.abs(rotate));
        else
            cc.rotateTurrent(getID2(), "CCW", Math.abs(rotate));
        cc.fire(getID2());
        //System.out.println("aim n shoot : " + Math.abs(rotate)  );
    }



    public  void update(String r) {
        try {
            JSONObject resp = new JSONObject(r);
            JSONArray players = (JSONArray) resp.get("players");
            //For each of our tanks
            for (int i = 0; i < players.length(); i++) {
                JSONObject p = new JSONObject(players.get(i).toString());

                /* update our guys */
                if (p.get("name").equals("Tanks But No Tanks")) {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    //for each tank - check if it's alive - then add the id
                    JSONObject tank = tanks.getJSONObject(0);
                    //if dead remove
                    if (!tank.getBoolean("alive")) {
                        if (getID1().equals(tank.get("id").toString())) {
                            setId1("");
                        }
                    }
                    else {
                        setId1(tank.get("id").toString());
                        JSONArray d = tank.getJSONArray("position");
                        setP1x(d.getDouble(0));
                        setP1y(d.getDouble(1));
                        setAngle1(getAngle(getP1x(), getP1y()));
                        setTurret1(Math.toDegrees(tank.getDouble("turret")));
                    }
                    tank = tanks.getJSONObject(1);
                    //if dead, remove from list
                    if (!tank.getBoolean("alive")) {
                        if (getID2().equals(tank.get("id").toString())) {
                            setId2("");
                        }
                    }
                    else {
                        setId2(tank.get("id").toString());
                        JSONArray d = tank.getJSONArray("position");
                        setE1x(d.getDouble(0));
                        setE1y(d.getDouble(1));
                        setAngle2(getAngle(getP2x(), getP2y()));
                        setTurret2(Math.toDegrees(tank.getDouble("turret")));

                    }
                }

                /* update their guys */
                else {
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
                }

            }
        } catch (JSONException e) {
            //e.printStackTrace();
            System.out.println("caught json error");
        }
    }



    public double getAngle(double x, double y) {
       double angle = Math.toDegrees(Math.atan2(y, x));
       if(angle < 0) {
           angle += 360;
       }
       return angle;
    }

    public String getID1() {
        return id1;
    }

    public String getID2() {
        return id2;
    }

    public void setId1(String id) {
        id1 = id;
    }

    public void setId2(String id) {
        id2 = id;
    }

    public double getP1x() {
        return p1x;
    }

    public void setP1x(double p1) {
        this.p1x = p1x;
    }

    public double getP1y() {
        return p1y;
    }

    public void setP1y(double p1y) {
        this.p1y = p1y;
    }

    public double getP2x() {
        return p2x;
    }

    public void setP2x(double p2x) {
        this.p2x = p2x;
    }

    public double getP2y() {
        return p2y;
    }

    public void setP2y(double p2y) {
        this.p2y = p2y;
    }

    public double getE1x() {
        return e1x;
    }

    public void setE1x(double e1x) {
        this.e1x = e1x;
    }

    public double getE1y() {
        return e1y;
    }

    public void setE1y(double e1y) {
        this.e1y = e1y;
    }

    public double getE2x() {
        return e2x;
    }

    public void setE2x(double e2x) {
        this.e2x = e2x;
    }

    public double getE2y() {
        return e2y;
    }

    public void setE2y(double e2y) {
        this.e2y = e2y;
    }

    public double getAngle1() {
        return angle1;
    }

    public void setAngle1(double angle1) {
        this.angle1 = angle1;
    }

    public double getAngle2() {
        return angle2;
    }

    public void setAngle2(double angle2) {
        this.angle2 = angle2;
    }

    public double getEnemyAngle1() {
        return enemyAngle1;
    }

    public void setEnemyAngle1(double enemyAngle1) {
        this.enemyAngle1 = enemyAngle1;
    }

    public double getEnemyAngle2() {
        return enemyAngle2;
    }

    public void setEnemyAngle2(double enemyAngle2) {
        this.enemyAngle2 = enemyAngle2;
    }

    public double getTurret1() {
        return turret1;
    }

    public void setTurret1(double turret1) {
        this.turret1 = turret1;
    }

    public double getTurret2() {
        return turret2;
    }

    public void setTurret2(double turret2) {
        this.turret2 = turret2;
    }
}
