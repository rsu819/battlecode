/* TODO:
 - build offensive landscaper (to pile dirt on enemy)
 - refine the flood patrol landscaper
 - refine wall builder to include building wall on tile that landscaper is standing on
 */

package TeamPacific;
import battlecode.common.*;
import static TeamPacific.Blockchain.*;

public class Landscaper extends Robot {
    // data members
    static int MaxSenseRadius;
    static Team myTeam;
    static MapLocation locationHQ = null;
    static MapLocation enemyHq;
    
    public enum LandscaperTask {
        WALL_BUILDER,
        OFFENSE_UNIT,
        FLOOD_PATROL
    }
    
    public Landscaper(RobotController rc) {
        super(rc);
    }

	@Override
	void run(int turnCount) throws GameActionException {
		// TODO Auto-generated method stub
        if (locationHQ == null) {
            locationHQ = getHqLoc(rc.getBlock(1));
        }
		runWallBuilder(turnCount);
	}
    
    public static Direction[] findDigDirs(Direction bldgDir) {
        Direction[] awayFromBldg = {bldgDir.opposite(), bldgDir.opposite().rotateLeft(), bldgDir.opposite().rotateRight()};
        return awayFromBldg;
    }

    static Direction[] findBuildDirs(Direction dir) {
        Direction[] aroundBldg = new Direction[5];
        int index = 0;
        for (Direction d : Direction.allDirections()) {
            if ((Math.abs(d.dx - dir.dx) <= 1 && Math.abs(d.dy - dir.dy) <= 1) && d != dir) {
                aroundBldg[index] = d;
                ++index;
            }
        }
        return aroundBldg;
    }

    public static boolean tryDig(Direction d) throws GameActionException {

        if (rc.isReady() && rc.canDigDirt(d)) {
//            System.out.println("digging dirt");
            rc.digDirt(d);
            return true;
        }
        return false;
    }

    public static boolean buildWall(MapLocation building, int turn) throws GameActionException {
        System.out.println("building wall");
        MapLocation curr = rc.getLocation();
        Direction bldg = curr.directionTo(building);
        Direction[] surround = findBuildDirs(bldg);
        return tryDepositDirt(surround[turn % 5]);
    }

    public static boolean tryDepositDirt(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositDirt(dir)) {
            rc.depositDirt(dir);
            System.out.println("depositing dirt " + dir);
            return true;
        }
        return false;
    }
	
	 // build general tasks / skills for the landscaper
    static void runWallBuilder(int turnCount)  throws GameActionException {

        MapLocation curr = rc.getLocation();
        
        if( locationHQ != null ) {
	
	        if (!curr.isAdjacentTo(locationHQ) && rc.canMove(curr.directionTo(locationHQ))) {
	            tryMove(curr.directionTo(locationHQ));
	        }
	
	        if (rc.getDirtCarrying() < 15  && curr.isAdjacentTo(locationHQ)) {
	            Direction[] awayFromHq = Landscaper.findDigDirs(curr.directionTo(locationHQ));
	            tryDig(awayFromHq[turnCount % 3]);
	        }
	        if (curr.isAdjacentTo(locationHQ)) {
	            if (curr.directionTo(locationHQ) == Direction.EAST ||
	                    curr.directionTo(locationHQ) == Direction.WEST) {
	                buildWall(locationHQ, turnCount);
	            }
	        }
        }

        tryMove(randomDirection());
    }
    // TODO: activate other modes of landscaper action
    static void runFloodPatrol(Landscaper patrol) throws GameActionException {

        MapLocation curr = rc.getLocation();
        Direction d = randomDirection();

        if (!rc.senseFlooding(rc.adjacentLocation(d)) && rc.getDirtCarrying() != RobotType.LANDSCAPER.dirtLimit) {
            patrol.tryDig(d);
        }

        else if (rc.senseFlooding(rc.adjacentLocation(d)) && curr.isWithinDistanceSquared(locationHQ, 18)) {
            if (rc.getDirtCarrying() > 0) {
                patrol.tryDepositDirt(d);
            }
            else
                patrol.tryDig(randomDirection());
        }

        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ) {
                tryMove(curr.directionTo(robot.location));
            }
        }
    }

    static void runOffenseUnit(Landscaper offense) throws GameActionException {

        MapLocation curr = rc.getLocation();

        if (enemyHq == null){
            RobotInfo[] robots = rc.senseNearbyRobots();
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && (robot.team != rc.getTeam())) {
                    enemyHq = robot.location;
                }
            }
        }
        offense.tryDig(randomDirection());
        // TODO: adjust logic for when to dig and when to bury enemy HQ
        if ((rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit) && curr.isAdjacentTo(enemyHq)){
            offense.tryDepositDirt(curr.directionTo(enemyHq));
        } else {
            tryMove(curr.directionTo(enemyHq));
        }


    }

    /*******************************
     GENERAL MOVE METHODS
     ******************************/
    // TODO: refactor to make even more generic a function?
    // uses method from Blockchain class to create Transaction message
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

    static int[] getDistance(MapLocation myLoc, MapLocation myDest) {

        int x = myDest.x - myLoc.x;
        int y = myDest.y - myLoc.y;

        int[] distance = {x, y};
        System.out.println("Distance is [" + distance[0] + "," + distance[1] + "]");
        return distance;
    }
    
    /*****************
    END OF METHODS
    *****************/
}