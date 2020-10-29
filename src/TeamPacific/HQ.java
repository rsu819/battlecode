package TeamPacific;
import battlecode.common.*;
import static TeamPacific.Blockchain.*;


public class HQ extends Robot {
	
    static int minerCount;
    static int numMiners;

	HQ(RobotController rc) {
		// TODO Auto-generated constructor stub
		super(rc);
        minerCount = 0;
        numMiners = 0;
	}

	@Override
	void run(int turnCount) throws GameActionException {
		// TODO Auto-generated method stub
     int maxMiner = 4;
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

        for (Direction dir : directions) {
            if (numMiners < maxMiner && tryBuild(RobotType.MINER, dir)) {
                ++numMiners;
            }
        }
        //for (Team c : Team.values()) TODO: if miners die make more?
        //System.out.println(c); TODO: optimize miner creation.

        for (RobotInfo kill : rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent())) {
            if (kill.type == RobotType.DELIVERY_DRONE && rc.canShootUnit(kill.ID)) {
                rc.shootUnit(kill.ID);
            }
        }
	}

	static boolean emitHqLocation() throws GameActionException{
	    int[] message;
	    int cost = 10;
	    message = foundResourceMessage(rc.getLocation(), MessageTo.Any, Resource.HomeHQ);
	    if (rc.canSubmitTransaction(message, cost)){
	        rc.submitTransaction(message, cost);
	        return true;
        }
	    return false;
    }


}
