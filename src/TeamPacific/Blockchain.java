package TeamPacific;

import battlecode.common.*;

import static TeamPacific.RobotPlayer.rc;

// class methods for RobotController to use Blockchain.
// helps to construct messages and track blockchain cost
public class Blockchain {

    // change code! something sneakier
    public static int TeamId = 603720;

    public static class MessageTo {
        static int Miner = 1;
        static int Landscaper = 2;
        static int Drone = 3;
        static int Any = 4;
    }

    public static class Resource {
        static int Soup = 1;
        static int HomeHQ = 2;
        static int EnemyHQ = 3;
        static int Cows = 4;
    }


    public static int[] foundResourceMessage(MapLocation loc, int robot, int rsrc, boolean attack) {
        int[] message = {0,0,0,0,0,0,0};
        message[0] = TeamId;
        message[1] = robot;
        message[2] = rsrc;
        message[3] = loc.x;
        message[4] = loc.y;
        if (attack == true) {
            message[5] = 1;
        }

        return message;
    }

    public static int[] readBlockchain(Transaction[] txns, int recipient, int resource) {
        for (Transaction txn : txns) {
            int[] message = txn.getMessage();
            if (message[0] == TeamId && message[1] == recipient && message[2] == resource) {
                return message;
            }
        }
        return null;
    }

    public static MapLocation getHqLoc(Transaction[] txns) throws GameActionException{

        for (Transaction txn : txns) {
            int[] message = txn.getMessage();
            if (message[0] == TeamId && message[2] == Resource.HomeHQ) {
                MapLocation hq = new MapLocation(message[3], message[4]);
                System.out.println("Found HQ: " + hq.x + ", " + hq.y);
                return hq;
            }
        }
        return null;
    }

    public static boolean emitEnemyHq(MapLocation enemy) throws GameActionException{
        int[] message;
        int cost = 10;
        message = foundResourceMessage(enemy, MessageTo.Any, Resource.EnemyHQ, false);
        if (rc.canSubmitTransaction(message, cost)){
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    /* fields for the message:

    int[0] = 55555
    int[1] = 1-miner / 2-landscaper / 3-drone / 4-any
    int[2] = 1-found soup / 2-found home HQ / 3-found enemy HQ / 4-found cows
    int[3] = x coordinate of resource
    int[4] = y coordinate of resource
    int [1-6] == 1: attack!
    int[6] =
    team
    location
    building / robot id (?)
    found soup
    soup location
    who the message is for?

    use turn counts to control when to send out messages - or other metrics

    general uses for blockchain
    locate unit/building
    switch state/action to attack mode
    soup locating
    */


}
