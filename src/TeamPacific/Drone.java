package TeamPacific;
import java.util.*;

import battlecode.common.*;

public class Drone extends Robot {
	/* Variable(s) from robot:
	 * - MapLocation teamHqLoc
	 */
    // Drones states
    public enum States {
        FIND, PICK, DROP, OFFENSE;
    }

    public static States currentState = null;
    public static States prevState = null;

    public static Direction tempDirection = null;

    public static MapLocation enemyHQLocation;
    public static MapLocation currentDroneLocation;
    public static MapLocation landscapersLoc = null;
    public static RobotInfo[] nearbyRobots;

    public static boolean findEnemyHQ = false;
    public static boolean enemyLandscapers = false;
    public static boolean haveLandscapers = false;

    public static int checkEnemyHQLocation = 0;
    public ArrayList<MapLocation> enemyHQLoc = new ArrayList<>(3);
    public MapLocation forSureEnemyHQLoc = null;

    public static States getState() {
        return currentState;
    }
    public static void setState(States aState) {
        currentState = aState;
    }

    public Drone(RobotController rc) throws GameActionException {
        super(rc);
    	goToFindEnemyHQ();
        setState(States.FIND);
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        // isReady(): Tests whether the robot can perform an action
    	System.out.println(getState());
        if(rc.isReady()) {
            switch(getState()) {
                case FIND:
                    //nearbyRobots = rc.senseNearbyRobots();
                    //if(nearbyRobots.length > 0) {
                        // Notice and Game map, the location of the enemy HQ has a relationship of
                        // the location of own HQ.
                        // There are three type locations: Horizontal, Vertical, Diagonal
                        //   goToFindOwnHQ();
                        while (forSureEnemyHQLoc == null && checkEnemyHQLocation < 3) {
                            int searching = goToEnemyHQ(checkEnemyHQLocation);
                            switch (searching) {
                                case (-1):
                                    System.out.println("still searching...");
                                    break;
                                case (-2):
                                    System.out.println("not in the first location");
                                    checkEnemyHQLocation++;
                                    break;
                            }
                        }
                        if (forSureEnemyHQLoc != null) {
                            currentState = States.PICK;
                        }

                    //}
                    break;

                case PICK:

                    // either pick up our landscapers or opponent landscapers
                    if (!rc.isCurrentlyHoldingUnit()) {
                        if (rc.canSenseLocation(teamHqLoc)) {
                            RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), ourTeam);
                            if (robots.length > 0) {
                                for (RobotInfo robot : robots) {
                                    if (robot.type == RobotType.LANDSCAPER) {
                                        pickUpLandscapers(ourTeam);
                                    }
                                }
                            }
                        }
                    }
                    else {

                    }
                    // Go to the Enemy HQ
//        	        if (currentDroneLocation.distanceSquaredTo(forSureEnemyHQLoc) >= 20) {
//        	            droneMove(rc.getLocation().directionTo(forSureEnemyHQLoc));
//        	        }
                    // Pick up enemy's landscaper
//                    if (!enemyLandscapers && isLandscapersAround()) {
//                        pickUpLandscapers();
//                    }
                    break;

                case DROP:
                    // Drop the landscapers to the water
                    dropToWater();
                    break;
            }
        }
    }

    public void goToFindEnemyHQ() throws GameActionException {
        //  if(teamHqLoc != null) {
        int teamHqLoc_X = teamHqLoc.x;
        int teamHqLoc_y = teamHqLoc.y;
        int map_width = rc.getMapWidth();
        int map_height = rc.getMapHeight();
        enemyHQLoc.add(new MapLocation(map_width-(teamHqLoc_X+1), teamHqLoc_y));  //Horizontal
        enemyHQLoc.add(new MapLocation(teamHqLoc_X, map_height-(teamHqLoc_y+1))); //Vertical
        enemyHQLoc.add(new MapLocation(map_width-(teamHqLoc_X+1), map_height-(teamHqLoc_y+1))); //Diagonal
        System.out.println("Possible enemy locs: " + enemyHQLoc);
        //  }
    }


    public boolean isLandscapersAround() throws GameActionException {
        // senseNearbyRobots(int radiusSquared): Returns all robots that can be sensed within a certain distance of this robot.
        // getCurrentSensorRadiusSquared(): Returns the robot's current sensor radius squared, which is affected by the
        // current pollution level at the present location.
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared());
        for (RobotInfo bots : robots) {
            if (bots.type == RobotType.LANDSCAPER) {
                // Found the landscaper
                return true;
            }
        }
        return false;
    }

    public boolean pickUpLandscapers(Team team) throws GameActionException {
        // Find landscapers
        int landscaperId = -1;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared());
        for (RobotInfo bots : robots) {
            if (bots.type == RobotType.LANDSCAPER && bots.team == team) {
                // getID(): Returns the ID of the landscaper
                landscaperId = bots.getID();
                // getLocation(): Returns the landscaper's current location
                landscapersLoc = new MapLocation(bots.getLocation().x, bots.getLocation().y);
                if (landscaperId >= 0) {
                    if(rc.getLocation().isAdjacentTo(landscapersLoc) && rc.canPickUpUnit(landscaperId)) {
                        // pickUpUnit(int id): Picks up another unit, the id of the robot to pick up
                        rc.pickUpUnit(landscaperId);
                        haveLandscapers = true;
                        return true;
                    }
                } else {
                    goAroundToFind(landscapersLoc);
                    return false;
                }
            }
        }
        return false;
    }

    public boolean goAroundToFind(MapLocation enemyLandscaperLoc) throws GameActionException {
        // The Direction enumeration represents a direction from one MapLocation to another
        Direction dirToEnemyHQ = rc.getLocation().directionTo(enemyLandscaperLoc);
        // Contain probably direction
        // rotateLeft(): Compute the direction 45 degrees to the left of this one.
        // rotateRight(): Compute the direction 45 degrees to the right of this one.
        Direction[] dir = {
                dirToEnemyHQ,
                dirToEnemyHQ.rotateRight(),
                dirToEnemyHQ.rotateRight().rotateRight(),
                dirToEnemyHQ.rotateRight().rotateRight().rotateRight(),
                dirToEnemyHQ.rotateLeft(),
                dirToEnemyHQ.rotateLeft().rotateLeft(),
                dirToEnemyHQ.rotateLeft().rotateLeft().rotateLeft(),
                dirToEnemyHQ.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
        };
        for (Direction dire : dir) {
            // Try to move on the given direction
            if (tryMoveTo(dire)) {
                return true;
            }
        }
        return false;
    }

    public int goToEnemyHQ(int checkEnemyHQ) throws GameActionException {
        currentDroneLocation = rc.getLocation();
        MapLocation tempLoc = enemyHQLoc.get(checkEnemyHQ);
        System.out.println("Looking for :" + enemyHQLoc);
        if (!rc.canSenseLocation(enemyHQLoc.get(checkEnemyHQ))){
            tryMoveTo(currentDroneLocation.directionTo(tempLoc));
            return -1;
        }
        else {
            RobotInfo[] bots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
            if (bots.length > 0) {
                for(RobotInfo bot : bots) {
                    if (bot.getType() == RobotType.HQ) {
                        findEnemyHQ = true;
                        forSureEnemyHQLoc = bot.location;
                        return 0;
                    }
                }
            }

        }
        return -2;
    }

    public boolean droneMove(Direction dir) throws GameActionException {
        Direction[] dirs = {
                dir,
                dir.rotateRight(),
                dir.rotateRight().rotateRight(),
                dir.rotateRight().rotateRight().rotateRight(),
                dir.rotateLeft(),
                dir.rotateLeft().rotateLeft(),
                dir.rotateLeft().rotateLeft().rotateLeft(),
                dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft()
        };
        for (Direction dire : dirs) {
            if (rc.canMove(dire)) {
                rc.move(dire);
                return true;
            }
        }
        return false;
    }

    public boolean tryMoveTo(Direction dir) throws GameActionException {
        // isReady(): Tests whether the robot can perform an action
        // canMove(Direction dir): Tells whether this robot can move one step in the given direction.
        if (rc.isReady()) {
           if (rc.canMove(dir)) {
               rc.move(dir);
               return true;
           }
           else if (rc.canMove(dir.rotateLeft())) {
               rc.move(dir.rotateLeft());
               return true;
           }
           else if (rc.canMove(dir.rotateRight())) {
               rc.move(dir.rotateRight());
               return true;
           }
        }
        return false;
    }

    public boolean dropToWater() throws GameActionException {
        // Returns whether or not a given location is flooded, if the location is within the sensor radius of the robot.
        if(haveLandscapers) {
            currentDroneLocation = rc.getLocation();
            if (rc.canMove(Direction.CENTER) == true || rc.canMove(Direction.EAST) == true || rc.canMove(Direction.NORTH) == true
                    || rc.canMove(Direction.NORTHEAST) == true || rc.canMove(Direction.NORTHWEST) == true || rc.canMove(Direction.WEST) == true) {
                rc.move(Direction.CENTER);
                currentDroneLocation= rc.getLocation();
                if (rc.senseFlooding(currentDroneLocation) == true && rc.canDropUnit(Direction.CENTER) == true) {
                    rc.dropUnit(Direction.CENTER);
                    haveLandscapers = false;
                    return true;
                }
            }
        }
        return false;
    }
}

