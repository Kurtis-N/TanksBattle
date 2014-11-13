/**
 * Created by kurtisniedling on 2014-11-04.
 */
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

public class Main {
    private String clientToken;

    public static void main(String[] args) {
        String teamName = "Tanks But No Tanks";
        String matchToken = "0124edc5-6b06-4319-a6e8-02592df49d22";
        String serverIP = "ip-10-234-167-219.ec2.internal";

        System.out.println("Starting Tanks..");

        Connection c = new Connection(matchToken, serverIP);

        //TODO
        /*
            - Make CommandChannel repeatedly check queue for new commands and issue those commands in order

            - Make parser for StateChannel
                - once we have the board state how do we want to proceed?
                - check the board state everytime there is an update?

            - Create a new class or use the Connection class to make decisions {movements, attacks, rotations}

            - Make functions for attacking, rotating, moving, etc.. with the proper parameters and enqueues itself
         */



        System.out.println("done!");
    }
}
