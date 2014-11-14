import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kurtisniedling on 2014-11-13.
 */
public class GameState {
    public volatile boolean init = false;

    public volatile String id1;
    public volatile String id2;

    public volatile double p1x;
    public volatile double p1y;
    public volatile double p2x;
    public volatile double p2y;

    public volatile double e1x;
    public volatile double e1y;
    public volatile double e2x;
    public volatile double e2y;

    public volatile double angle1;
    public volatile double angle2;

    public volatile double turret1;
    public volatile double turret2;

    public volatile double enemyAngle1;
    public volatile double enemyAngle2;

    public boolean alive1 = false;
    public boolean alive2 = false;


    public GameState() {

    }

    public void updateState(String r) {
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
                    if (tank.getBoolean("alive")) {
                        alive1 = true;
                        id1 = tank.getString("id");
                        JSONArray d = tank.getJSONArray("position");
                        p1x = d.getDouble(0);
                        p1y = d.getDouble(1);

                    }
                    else {
                        alive1 = false;
                    }

                    tank = tanks.getJSONObject(1);
                    if (tank.getBoolean("alive")) {
                        alive2 = true;
                        id2 = tank.getString("id");
                        JSONArray d = tank.getJSONArray("position");
                        p2x = d.getDouble(0);
                        p2y = d.getDouble(1);
                    }
                    else {
                        alive2 = false;
                    }
                }

                /* update their guys */
                /*else {
                    JSONArray tanks = (JSONArray) p.get("tanks");
                    JSONObject tank = tanks.getJSONObject(0);
                    //if dead remove
                    if (tank.getBoolean("alive")) {
                        JSONArray d = tank.getJSONArray("position");
                        setE1x(d.getDouble(0));
                        setE1y(d.getDouble(1));
                        //setEnemyAngle1(getAngle(getE1x(), getE1y()));
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
                        //setEnemyAngle2(getAngle(getE2x(), getE2y()));
                    }
                    else {
                        setEnemyAngle2(getAngle1());
                    }
                }*/

            }
            init = true;
        } catch (JSONException e) {
            //e.printStackTrace();
            //System.out.println("caught json error in updating tanks");
        }
    }

    public double getP1x() {
        return p1x;
    }

    public void setP1x(double p1x) {
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

    public boolean isAlive1() {
        return alive1;
    }

    public void setAlive1(boolean alive1) {
        this.alive1 = alive1;
    }

    public boolean isAlive2() {
        return alive2;
    }

    public void setAlive2(boolean alive2) {
        this.alive2 = alive2;
    }
}
