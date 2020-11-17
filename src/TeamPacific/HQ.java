package TeamPacific;
import battlecode.common.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.enemyHq;


public class HQ extends Robot {

    public static int minerCount;
    public static int numMiners;
    public static int maxMiner = 3;

    public  HQ(RobotController rc) throws GameActionException {
        // TODO Auto-generated constructor stub
        super(rc);
        minerCount = 0;
        numMiners = 0;
        maxMiner = 4;
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        // TODO Auto-generated method stub
        maxMiner = 4;
        switch (turnCount) {
            case 256:                 maxMiner = 5;                break;
            case 677:                 maxMiner = 6;                break;
            case 1210:                maxMiner = 7;                break;
            case 1771:                maxMiner = 8;                break;
            case 2143:                maxMiner = 9;                break;
            case 2348:                maxMiner = 10;               break;
            case 2524:                maxMiner = 11;               break;
            case 3019:                maxMiner = 12;               break;
        }

        if (rc.getRoundNum() == 1) {
            emitHqLocation();
        }

        if ((rc.getRoundNum() % 10 == 0)) {
            int wallHeight = 0;
            for (Direction dir : directions) {
                int elevGain = Math.abs(rc.senseElevation(rc.adjacentLocation(dir)) - rc.senseElevation(rc.getLocation()));
                if (elevGain > 10) {
                    wallHeight++;
                }
            }
            if (wallHeight >= 8) {
                emitAttackMode(enemyHq);
            }
        }

        for (Direction dir : directions) {
            if (numMiners < maxMiner && tryBuild(RobotType.MINER, dir)) {
                ++numMiners;
            }
        }
        //for (Team c : Team.values()) TODO: if miners die make more?
        //System.out.println(c); TODO: optimize miner creation.

        RobotInfo[] kills = findOpponents();
        for (RobotInfo kill : kills) {
            if (kill.getType() == RobotType.DELIVERY_DRONE) {
                shoot(kill);
            }
        }
    }

    public RobotInfo[] findOpponents(){
        return rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent());
    }

    public int shoot(RobotInfo kill) throws GameActionException {
        int shot = 0;
        if (rc.canShootUnit(kill.ID)) {
            rc.shootUnit(kill.ID);
        }
        return shot;
    }

    public static boolean emitHqLocation() throws GameActionException{
        int[] message;
        int cost = 10;
        message = foundResourceMessage(rc.getLocation(), MessageTo.Any, Resource.HomeHQ, false);
        if (rc.canSubmitTransaction(message, cost)){
            rc.submitTransaction(message, cost);
            return true;
        }
        return false;
    }

    public static void emitAttackMode(MapLocation enemyHq) throws GameActionException {
        int[] message;
        int cost = 10;
        message = foundResourceMessage(enemyHq, MessageTo.Landscaper, Resource.EnemyHQ, true);
        if (rc.canSubmitTransaction(message, cost)) {
            rc.submitTransaction(message, cost);
        }
    }

}
