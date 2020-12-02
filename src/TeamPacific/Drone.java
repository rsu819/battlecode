package TeamPacific;
import java.util.*;

import TeamPacific.Blockchain;
import TeamPacific.Robot;
import battlecode.common.*;

import static TeamPacific.Blockchain.TeamId;
import static TeamPacific.Blockchain.emitEnemyHq;

public class Drone extends Robot {
	/* Variable(s) from robot:
	 * - MapLocation teamHqLoc
	 */
    // Drones states
    public enum States {
        FIND, PICK, DROP, HOVER;
    }

    public int idMod;
    static MapLocation landscapersLoc = null;
    public static States currentState = null;
    public static MapLocation currentDroneLocation;
    public static MapLocation flooding;
    public static boolean findEnemyHQ = false;
    public static boolean haveEnemyBot = false;
    public static boolean haveLandscapers = false;
    public static int checkEnemyHQLocation = 0;
    public ArrayList<MapLocation> enemyHQLoc = new ArrayList<>(3);
    public MapLocation confirmedEnemyHq = null;

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
        idMod = rc.getID() % 3;
    }

    @Override
    public void run(int turnCount) throws GameActionException {
        int round = rc.getRoundNum();
        if (round % 10 == 0 && round > 1 && confirmedEnemyHq != null) {
            emitEnemyHq(confirmedEnemyHq);
        }
        else if (round % 10 == 1 && round > 1 && confirmedEnemyHq == null) {
            currentState = checkDroneState();
            System.out.println("My state is: " + currentState);
        }

        MapLocation current = rc.getLocation();
        System.out.println(getState());
        if(rc.isReady()) {

            switch(getState()) {
                case FIND:
                    while (confirmedEnemyHq == null && checkEnemyHQLocation < 3) {
                        int searching = findEnemyHQ(checkEnemyHQLocation);
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
                    if (confirmedEnemyHq != null) {
                        currentState = States.PICK;
                    }
                    break;
                case PICK:
                    if (!rc.isCurrentlyHoldingUnit()) {
                        if (idMod == 0) {
                            if (round < 450) {
                                currentState = States.HOVER;
                            }
                            // if looking for our landscapers
                            else if (!findLandscaper(ourTeam, teamHqLoc))
                                moveToward(current.directionTo(teamHqLoc));
                        }

                        else if (idMod == 1){
                            if (!pickUpEnemyBot(ourTeam.opponent()))
                                moveToward(current.directionTo(confirmedEnemyHq));
                        }
                        else {
                            if (!rc.canSenseLocation(teamHqLoc)) {
                                tryPickUpRobot(RobotType.COW, Team.NEUTRAL);
                                moveToward(randomDirection());
                            }
                            else
                                moveToward(current.directionTo(teamHqLoc));
                        }
                    } else {
                        currentState = States.DROP;
                    }
                    break;
                case DROP:
                    if (!rc.isCurrentlyHoldingUnit()) {
                        currentState = States.PICK;
                    }
                    if (idMod == 0 && rc.isCurrentlyHoldingUnit()) {
                        if (!dropTeamLandscaper(confirmedEnemyHq)) {
                            moveToward(randomDirection());
                        }
                    }
                    else {
                        if (tryDropEnemyInWater())
                            currentState = States.PICK;
                        else moveToward(randomDirection());
                    }
                    break;
                case HOVER:
                    if (round >= 450) {
                        currentState = States.PICK;
                    }
                    if (!rc.canSenseLocation(teamHqLoc)) {
                        moveToward(current.directionTo(teamHqLoc));
                    }
                    else {
                        tryPickUpRobot(RobotType.COW, Team.NEUTRAL);
                        currentState = States.DROP;
                    }
                default:
                    moveToward(randomDirection());
                    break;
            }
        }
    }

    public void goToFindEnemyHQ() {
        int teamHqLoc_X = teamHqLoc.x;
        int teamHqLoc_y = teamHqLoc.y;
        int map_width = rc.getMapWidth();
        int map_height = rc.getMapHeight();
        enemyHQLoc.add(new MapLocation(map_width-(teamHqLoc_X+1), teamHqLoc_y));  //Horizontal
        enemyHQLoc.add(new MapLocation(teamHqLoc_X, map_height-(teamHqLoc_y+1))); //Vertical
        enemyHQLoc.add(new MapLocation(map_width-(teamHqLoc_X+1), map_height-(teamHqLoc_y+1))); //Diagonal
        System.out.println("Possible enemy locs: " + enemyHQLoc);
    }

    public boolean tryDropEnemyInWater() throws GameActionException {
        MapLocation current = rc.getLocation();
        if (rc.senseFlooding(current) && rc.canDropUnit(Direction.CENTER)) {
            rc.dropUnit(Direction.CENTER);
            return true;
        }
        if (flooding != null && flooding.isAdjacentTo(current) && rc.canDropUnit(current.directionTo(flooding))) {
            rc.dropUnit(current.directionTo(flooding));
            return true;
        }
        else {
            flooding = findWater();
        }
        return false;
    }

    public boolean tryPickUpRobot(RobotType type, Team team) throws GameActionException{
        boolean wallBuilder = false;
        MapLocation curr = rc.getLocation();
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), team);
        if (robots.length > 0) {
            for (RobotInfo robot : robots) {
                if (robot.getType() == type && curr.isAdjacentTo(robot.getLocation()) && rc.canPickUpUnit(robot.getID())) {
                    rc.pickUpUnit(robot.getID());
                    return true;
                }
            }
        }
        return false;
    }

    // pick up landscapers
    public boolean findLandscaper(Team team, MapLocation dest) throws GameActionException {
        MapLocation curr = rc.getLocation();
        if (!rc.canSenseLocation(dest)){
            moveToward(curr.directionTo(dest));
        }
        else {
            if (tryPickUpRobot(RobotType.LANDSCAPER, team)) {
                return true;
            }
        }
        return false;
    }

    // pick up enemies
    public boolean pickUpEnemyBot(Team opponent) throws GameActionException {
        // Find landscapers
        RobotType[] targetBots = {RobotType.LANDSCAPER, RobotType.MINER};
        for (RobotType botType : targetBots) {
            if (tryPickUpRobot(botType, opponent)){
                haveEnemyBot = true;
            }
        }
        moveToward(randomDirection());
        return false;
    }

    public boolean dropTeamLandscaper(MapLocation dest) throws GameActionException {
        MapLocation current = rc.getLocation();
        Direction targetDir = current.directionTo(dest);
        if (!rc.canSenseLocation(dest)) {
            moveToward(targetDir);
        }
        else if (current.compareTo(confirmedEnemyHq) < 20){
            if (rc.canDropUnit(targetDir) && !rc.senseFlooding(current)) {
                MapLocation[] surroundings = squaresAroundLoc(current);
                for (MapLocation square : surroundings) {
                    if (!rc.senseFlooding(square)) {
                        rc.dropUnit(current.directionTo(square));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int findEnemyHQ(int checkEnemyHQ) throws GameActionException {
        currentDroneLocation = rc.getLocation();
        MapLocation tempLoc = enemyHQLoc.get(checkEnemyHQ);
        System.out.println("Looking for :" + enemyHQLoc);
        if (!rc.canSenseLocation(enemyHQLoc.get(checkEnemyHQ))){
            moveToward(currentDroneLocation.directionTo(tempLoc));
            return -1;
        }
        else {
            RobotInfo[] bots = rc.senseNearbyRobots(rc.getCurrentSensorRadiusSquared(), rc.getTeam().opponent());
            if (bots.length > 0) {
                for(RobotInfo bot : bots) {
                    if (bot.getType() == RobotType.HQ) {
                        findEnemyHQ = true;
                        confirmedEnemyHq = bot.location;
                        return 0;
                    }
                }
            }

        }
        return -2;
    }

    public static boolean moveToward(Direction dir) throws GameActionException {
        // isReady(): Tests whether the robot can perform an action
        MapLocation current = rc.getLocation();
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

    public MapLocation findWater() throws GameActionException {
        MapLocation current = rc.getLocation();
        for (Direction dir : directions) {
            MapLocation maybeFlood = new MapLocation(current.x + dir.getDeltaX(), current.y + dir.getDeltaY());
            if (rc.canSenseLocation(maybeFlood) && rc.senseFlooding(maybeFlood) && maybeFlood.compareTo(current) < rc.getCurrentSensorRadiusSquared()) {
                return maybeFlood;
            }
        }
//        moveToward(randomDirection());
        return null;
    }
    // TODO: figure out how to sense water locations around!
    public static boolean dropToWater() throws GameActionException {
        // Returns whether or not a given location is flooded, if the location is within the sensor radius of the robot.
        if(haveEnemyBot) {
            currentDroneLocation = rc.getLocation();
            if (rc.canMove(Direction.CENTER) == true || rc.canMove(Direction.EAST) == true || rc.canMove(Direction.NORTH) == true
                    || rc.canMove(Direction.NORTHEAST) == true || rc.canMove(Direction.NORTHWEST) == true || rc.canMove(Direction.WEST) == true) {
                moveToward(Direction.CENTER);
                currentDroneLocation= rc.getLocation();
                if (rc.senseFlooding(currentDroneLocation) == true && rc.canDropUnit(Direction.CENTER) == true) {
                    rc.dropUnit(Direction.CENTER);
                    haveEnemyBot = false;
                    return true;
                }
            }
        }
        return false;
    }


    public States checkDroneState() throws GameActionException {
        Transaction[] txns = rc.getBlock(rc.getRoundNum() - 1);
        for (Transaction txn : txns) {
            int[] message = txn.getMessage();
            if (message[0] == TeamId && message[1] == Blockchain.MessageTo.Landscaper && message[5] == 1) {
                System.out.println("Attack EnemyHq!");
                return States.PICK;
            }
        }
        return States.FIND;
    }

    public MapLocation getEnemyHQLocation(Transaction[] txns) throws GameActionException {
        for (Transaction txn : txns) {
            int[] message = txn.getMessage();
            if (message[0] == TeamId && message[1] == Blockchain.MessageTo.Drone && message[2] == 3) {
                System.out.println("Found EnemyHq!");
                return new MapLocation(message[3], message[4]);
            }
        }
        return null;
    }

    public static boolean pickUpLandscapers() throws GameActionException {
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

    public static boolean pickUpCow(MapLocation dest) throws GameActionException {
        MapLocation curr = rc.getLocation();
        if (!rc.canSenseLocation(dest)) {
            moveToward(curr.directionTo(dest));
        }
        else if (rc.canSenseLocation(dest)) {
            RobotInfo[] robotList = rc.senseNearbyRobots();
            if ( robotList != null ) {
                for (RobotInfo robot : robotList) {
                    if (robot.getType() == RobotType.COW && curr.isAdjacentTo(robot.getLocation()) && rc.canPickUpUnit(robot.getID())) {
                        rc.pickUpUnit(robot.getID());
                        return true;
                    } else {
                        moveToward(curr.directionTo(robot.getLocation()));
                    }}
            }
        }
        return false;
    }
    public static boolean isLandscapersAround() throws GameActionException {
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

    public static boolean goAroundToFind(MapLocation enemyLandscaperLoc) throws GameActionException {
        for (Direction dire : directions) {
            // Try to move on the given direction
            if (tryMoveTo(dire)) {
                return true;
            }
        }
        return false;
    }

    public static boolean tryMoveTo(Direction dir) throws GameActionException {
        // isReady(): Tests whether the robot can perform an action
        // canMove(Direction dir): Tells whether this robot can move one step in the given direction.
        if (rc.isReady() && rc.canMove(dir)) {
            // move(Direction dir): Moves one step in the given direction.
            rc.move(dir);
            return true;
        }
        return false;
    }

}

