import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kurtisniedling on 2014-11-06.
 */
public class Algo {

    CommandChannel cc;
    StateChannel sc;

    public Algo (CommandChannel cc, StateChannel sc) {
        this.cc = cc;
        this.sc = sc;
    }

    public float getAngle() {
        return 3;
    }

    //spin and shoot !
    public void spinAndShoot() {
        while(true) {
            System.out.println("starting spin & shoot");
            if(sc.ids.size() > 2)
                sc.ids.clear();
            if(sc.ids.size() > 0) {
                for(int i = 0; i < sc.ids.size(); i++) {
                    System.out.println("sending fire and rotate");
                    //System.out.println(sc.ids.get(i));
                    String temp = sc.ids.get(i);
                    JSONObject attack = null;
                    try {
                        attack = new JSONObject();
                        attack.put("tank_id", temp);
                        attack.put("comm_type", "FIRE");
                        attack.put("client_token", cc.clientToken);
                        System.out.println(attack.toString());
                        cc.addToQueue(attack);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    JSONObject spin = null;
                    try {
                        spin = new JSONObject();
                        spin.put("tank_id", temp);
                        spin.put("comm_type", "ROTATE_TURRET");
                        spin.put("direction", "CCW");
                        spin.put("rads", 1.11);
                        spin.put("client_token", cc.clientToken);
                        cc.addToQueue(spin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else
                System.out.println("nothing in list");
        }
    }
}
