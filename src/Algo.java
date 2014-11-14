import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by kurtisniedling on 2014-11-06.
 */
public class Algo {

    CommandChannel cc;
    StateChannel sc;
    List<Tank> myTanks;

    public Algo (CommandChannel cc, StateChannel sc) {
        this.cc = cc;
        this.sc = sc;
    }

    public void begin() {
        System.out.println("starting algorithm..");
        while(true) {
            spinAndShoot();
        }
    }

    public float getAngle() {
        return 3;
    }

    //spin and shoot !
    public void spinAndShoot() {
        /*while(true) {
            String id1 = sc.getID1();
            String id2 = sc.getID2();
            //System.out.println("starting spin & shoot");
            if(!id1.isEmpty()) {
                //System.out.println(sc.ids.get(i));
                //System.out.println("sending fire and rotate with id: " + temp);
                JSONObject attack = null;
                try {
                    attack = new JSONObject();
                    attack.put("tank_id", id1);
                    attack.put("comm_type", "FIRE");
                    attack.put("client_token", cc.clientToken);
                    //System.out.println(attack.toString());
                    cc.addToQueue(attack);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject spin = null;
                try {
                    spin = new JSONObject();
                    spin.put("tank_id", id1);
                    spin.put("comm_type", "ROTATE_TURRET");
                    spin.put("direction", "CCW");
                    spin.put("rads", 1.11);
                    spin.put("client_token", cc.clientToken);
                    cc.addToQueue(spin);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if(!id2.isEmpty()) {
                //System.out.println(sc.ids.get(i));
               // System.out.println("sending fire and rotate with id: " + temp);
                JSONObject attack = null;
                try {
                    attack = new JSONObject();
                    attack.put("tank_id", id2);
                    attack.put("comm_type", "FIRE");
                    attack.put("client_token", cc.clientToken);
                    //System.out.println(attack.toString());
                    cc.addToQueue(attack);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject spin = null;
                try {
                    spin = new JSONObject();
                    spin.put("tank_id", id2);
                    spin.put("comm_type", "ROTATE_TURRET");
                    spin.put("direction", "CCW");
                    spin.put("rads", 1.11);
                    spin.put("client_token", cc.clientToken);
                    cc.addToQueue(spin);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }*/
    }
}
