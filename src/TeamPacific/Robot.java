package TeamPacific;
import java.util.Arrays;

import battlecode.common.*;

abstract class Robot {
	
    static Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };
	
    static RobotController rc;
    
    Robot(RobotController rc) {
    	this.rc = rc;
    }
    
    abstract void run(int turnCount) throws GameActionException;

    // Will give the nearest object given a list of MapLocations
    static MapLocation findNearest(MapLocation currLoc, MapLocation[] locations) {

        MapLocation tempLocation = new MapLocation( -1, -1);
        int nearest = -1;
        int tempInt = Integer.MAX_VALUE;
        for(int i = 0; i < locations.length; i++) {
            if (currLoc.distanceSquaredTo(locations[i]) < tempInt) {
                tempInt = currLoc.distanceSquaredTo(locations[i]);
                nearest = i;
            }
        }

        if (nearest != -1) {
            tempLocation = locations[nearest];
        }
        return tempLocation;
    }
    
    // Will give the nearest object of a given type(s)
    static MapLocation findNearest(MapLocation currLoc, RobotInfo[] robotList, RobotType[] targetTypes, Team targetTeam) {

        MapLocation tempLocation = new MapLocation( -1, -1);
        int nearest = -1;
        int tempInt = Integer.MAX_VALUE;

        for(int i = 0; i < robotList.length; i++) {

            if( Arrays.asList(targetTypes).contains(robotList[i].getType()) ) {
                if ( (currLoc.distanceSquaredTo(robotList[i].getLocation()) < tempInt) && (robotList[i].getTeam() == targetTeam) ) {
                    tempInt = currLoc.distanceSquaredTo(robotList[i].getLocation());
                    nearest = i;
                }
            }
        }

        if (nearest != -1) {
            tempLocation = robotList[nearest].getLocation();
        }
        return tempLocation;
    }
    
    // Will return a list of all directions in a 180 degree angle of the given direction
    static Direction[] getFrontDirections(Direction aDirection) {

        Direction[] dirList = new Direction[4];

        dirList[0] = aDirection.rotateLeft();
        dirList[1] = dirList[0].rotateLeft();
        dirList[2] = aDirection.rotateRight();
        dirList[3] = dirList[2].rotateRight();

        return dirList;
    }
	
    // Returns a random direction
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }
    
    static boolean tryMove() throws GameActionException {
        for (Direction dir : Direction.values())
            if (tryMove(dir))
                return true;
        return false;
    }
    
    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.isReady() && rc.canMove(dir) && !rc.senseFlooding(rc.adjacentLocation(dir))) {
            rc.move(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to build a given robot in a given direction.
     *
     * @param type The type of the robot to build
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    public static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }
    
    public static void tryBlockchain(int turnCount) throws GameActionException {
        if (turnCount < 3) {
            int[] message = new int[7];
            for (int i = 0; i < 7; i++) {
                message[i] = 123;
            }
            if (rc.canSubmitTransaction(message, 10))
                rc.submitTransaction(message, 10);
        }
        // System.out.println(rc.getRoundMessages(turnCount-1));
    }
	
}
