package TeamPacific;
import java.util.*;

import battlecode.common.*;

import static TeamPacific.Blockchain.getHqLoc;

public class Drone extends Robot {

    Drone(RobotController rc) throws GameActionException {
        super(rc);
        pacificHQLocation = getHqLoc(rc.getBlock(1));
    	goToFindEnemyHQ();
        setState(States.FIND);
    }

    // Drones states
    enum States {
        FIND, PICK, DROP;
    }

    public static States currentState = null;
    public static States prevState = null;

    public static Direction tempDirection = null;

    public static MapLocation pacificHQLocation;
    public static MapLocation enemyHQLocation;
    public static MapLocation currentDroneLocation;
    public static MapLocation landscapersLoc = null;
    public static RobotInfo[] nearbyRobots;

    public static boolean findEnemyHQ = false;
    public static boolean enemyLandscapers = false;
    public static boolean haveLandscapers = false;

    public static int checkEnemyHQLocation = 0;


    ArrayList<MapLocation> enemyHQLoc = new ArrayList<>(3);
    MapLocation forSureEnemyHQLoc = null;

    static States getState() {
        return currentState;
    }

    static void setState(States aState) {
        currentState = aState;
    }

    @Override
    void run(int turnCount) throws GameActionException {
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
                        
                        if (forSureEnemyHQLoc == null) {
                            goToEnemyHQ(0);
   
                        } else {
                            currentState = States.PICK;
                        }
                    //}
                    break;

                case PICK:
                    // Go to the Enemy HQ
        	        if (currentDroneLocation.distanceSquaredTo(forSureEnemyHQLoc) >= 20) {
        	            droneMove(rc.getLocation().directionTo(forSureEnemyHQLoc));
        	        }
                    // Pick up enemy's landscaper
                    if (!enemyLandscapers && isLandscapersAround()) {
                        pickUpLandscapers();
                    }
                    break;

                case DROP:
                    // Drop the landscapers to the water
                    dropToWater();
                    break;
            }
        }
    }

    public void goToFindOwnHQ() throws GameActionException {
        if (pacificHQLocation == null) {
            // senseNearbyRobots(): Returns all robots within sense radius
            nearbyRobots = rc.senseNearbyRobots();
            for (RobotInfo robot : nearbyRobots) {
                // RobotType: the type of the robot
                // getTeam(): returns this robot's Team
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    // location: The current location of the robot
                    pacificHQLocation = robot.location;
                }
            }
        }
    }

    public void goToFindEnemyHQ() throws GameActionException {
        //  if(pacificHQLocation != null) {
        int pacificHQLocation_X = pacificHQLocation.x;
        int pacificHQLocation_y = pacificHQLocation.y;
        int map_width = rc.getMapWidth();
        int map_height = rc.getMapHeight();
        enemyHQLoc.add(new MapLocation(map_width-pacificHQLocation_X, pacificHQLocation_y));  //Horizontal
        enemyHQLoc.add(new MapLocation(pacificHQLocation_X, map_height-pacificHQLocation_y)); //Vertical
        enemyHQLoc.add(new MapLocation(map_width-pacificHQLocation_X, map_height-pacificHQLocation_y)); //Diagonal
        //System.out.println(enemyHQLoc);
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

    public boolean pickUpLandscapers() throws GameActionException {
        // Find landscapers
        int landscaperId = -1;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared());
        for (RobotInfo bots : robots) {
            if (bots.type == RobotType.LANDSCAPER) {
                // getID(): Returns the ID of the landscaper
                landscaperId = bots.getID();
                // getLocation(): Returns the landscaper's current location
                landscapersLoc = new MapLocation(bots.getLocation().x, bots.getLocation().y);
                if (landscaperId >= 0) {
                    if(rc.getLocation().isAdjacentTo(landscapersLoc) == true && rc.canPickUpUnit(landscaperId) == true) {
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

    public void goToEnemyHQ(int checkEnemyHQ) throws GameActionException {
    	
    	for( int i = 0; i < enemyHQLoc.size(); i++) {
    		
	        // Get the current Drone Location
	        currentDroneLocation = rc.getLocation();
	        // Get the enemy HQ Location, then the drone fly to enemy HQ
	        MapLocation enemyHQLoca = enemyHQLoc.get(i);
	        // Based on the vision radius chart, the greatest number id 20 for every robot can see
	        // If the distance is greater than 20, then drone needs to move closer to the enemy HQ
	        if (currentDroneLocation.distanceSquaredTo(enemyHQLoca) >= 20) {
	            droneMove(rc.getLocation().directionTo(enemyHQLoca));
	        } else {
	            // If the enemy HQ is within drone's vision radius
	            // Then need to check whether enemy HQ is there
	
	            // RobotInfo: Struct that stores basic information that was 'sensed' of another Robot.
	            // senseRobotAtLocation(MapLocation loc): Senses the robot at the given location, or null if there is no robot there.
	            // RobotInfo enemyHQ = rc.senseRobotAtLocation(enemyHQLoca);
	            RobotInfo[] enemyHQNearByRobots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
	            System.out.println("The enemyHQ location: " + enemyHQLoca);
	            for (RobotInfo each : enemyHQNearByRobots) {
	                if (each.getType() == RobotType.HQ) {
	                    findEnemyHQ = true;
	                    forSureEnemyHQLoc = enemyHQLoca;
	                    System.out.println("Find the enemy HQ!!");
	                    break;
	                }
	            }
	            if(forSureEnemyHQLoc != null) {
	            	break;
	            }
        }
        }
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
        if (rc.isReady() && rc.canMove(dir)) {
            // move(Direction dir): Moves one step in the given direction.
            rc.move(dir);
            return true;
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

