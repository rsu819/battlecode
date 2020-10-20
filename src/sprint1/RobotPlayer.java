package sprint1;
import battlecode.common.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

public strictfp class RobotPlayer {
    static RobotController rc;
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
    static RobotType[] spawnedByMiner = {RobotType.REFINERY, RobotType.VAPORATOR, RobotType.DESIGN_SCHOOL,
            RobotType.FULFILLMENT_CENTER, RobotType.NET_GUN};

    public enum LandscaperTask {
        WALL_BUILDER,
        OFFENSE_UNIT,
        FLOOD_PATROL
    }

    static int turnCount;
    static int minerCount;
    static MapLocation locationHQ;
    static MapLocation refinery;
    static boolean designSchoolCount;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;
        minerCount = 0;
        designSchoolCount = true;


        while (true) {
            turnCount += 1;
            System.out.println("Turncount: " + turnCount);

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 runHQ();                break;
                    case MINER:              runMiner();             break;
                    case REFINERY:           runRefinery();          break;
                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:
                        if (turnCount % 2 == 0)
                            runLandscaper(LandscaperTask.WALL_BUILDER);
                        else
                            runLandscaper(LandscaperTask.FLOOD_PATROL);
                        break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
                    case NET_GUN:            runNetGun();            break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runHQ() throws GameActionException {
        locationHQ = rc.getLocation();
        System.out.println("HQ IS AT " + locationHQ);
        while (minerCount < 4){
            Direction dir = randomDirection();
            if (tryBuild(RobotType.MINER, dir) == true)
                ++minerCount;
        }

    }

    static void runMiner() throws GameActionException {
        //tryBlockchain();
        Direction dir = randomDirection();
        MapLocation[] soup;
        RobotInfo[] robots;

        robots = rc.senseNearbyRobots(-1, rc.getTeam());

        if (locationHQ == null) {
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ) {
                    locationHQ = robot.location;
                }

            }
        }

        if (refinery == null){
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.REFINERY) {
                    refinery = robot.location;
                }
            }
        }


        for (Direction d: directions) {
            tryMine(d);
        }
        if (rc.getSoupCarrying() < 5) {
            tryMove(randomDirection());
        }
        if (rc.getSoupCarrying() < RobotType.MINER.soupLimit) {
            soup = rc.senseNearbySoup();
            for (MapLocation loc : soup) {
                tryMove(rc.getLocation().directionTo(loc));
            }
        }
        if (tryBuild(RobotType.DESIGN_SCHOOL, dir) == true && designSchoolCount == true) {
            System.out.println("Built design school");
            designSchoolCount = false;
        }
        if (tryBuild(RobotType.REFINERY, dir)) {
            System.out.println("built a refinery");
        }
        else {
            tryMove(rc.getLocation().directionTo(locationHQ));
            if (refinery != null) {
                tryMove(rc.getLocation().directionTo(refinery));
            }
        }

        if (rc.getLocation().isAdjacentTo(locationHQ) || rc.getLocation().isAdjacentTo(refinery)) {
            for (Direction d : directions)
                if (tryRefine(d))
                    System.out.println("I refined soup! " + rc.getTeamSoup());
        }




        // tryBuild(randomSpawnedByMiner(), randomDirection());
    }


    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
        for (Direction dir : directions) {
            tryBuild(RobotType.LANDSCAPER, dir);
        }
    }


    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
        randomDirection().rotateLeft();
    }


    static void runLandscaper(LandscaperTask task) throws GameActionException {
        RobotInfo[] robots;
        robots = rc.senseNearbyRobots();

        if (locationHQ == null) {
            for (RobotInfo robot : robots) {
                if (robot.type == RobotType.HQ && robot.team == rc.getTeam()) {
                    locationHQ = robot.location;
                }
            }
            tryMove(randomDirection());
        }

        if (task == LandscaperTask.WALL_BUILDER) {
            runWallBuilder();
        } else if (task == LandscaperTask.FLOOD_PATROL) {
            runFloodPatrol();
        }
    }

    // build general tasks / skills for the landscaper
    static void runWallBuilder()  throws GameActionException {

        Landscaper builder = new Landscaper(rc, locationHQ);

        MapLocation curr = rc.getLocation();

        if (!curr.isAdjacentTo(locationHQ) && rc.canMove(curr.directionTo(locationHQ))) {
            tryMove(curr.directionTo(locationHQ));
        }

        if (rc.getDirtCarrying() < 4 && curr.isAdjacentTo(locationHQ)) {
            Direction[] awayFromHq = Landscaper.digAwayFromBldg(curr.directionTo(locationHQ));
            builder.tryDig(awayFromHq[turnCount % 3]);
        }
        if (curr.isAdjacentTo(locationHQ)) {
            if (curr.directionTo(locationHQ) == Direction.NORTH ||
                    curr.directionTo(locationHQ) == Direction.SOUTH ||
                    curr.directionTo(locationHQ) == Direction.EAST ||
                    curr.directionTo(locationHQ) == Direction.WEST) {
                builder.buildWall(locationHQ, turnCount);
            }
        }

        tryMove(randomDirection());
    }

    static void runFloodPatrol() throws GameActionException {

        Landscaper patrol = new Landscaper(rc, locationHQ);
        MapLocation curr = rc.getLocation();
        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());

        if (rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit) {
            for (Direction d : directions) {
                if (rc.senseFlooding(rc.adjacentLocation(d))) {
                    patrol.tryDepositDirt(d);
                }
            }
        }
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ) {
                tryMove(curr.directionTo(robot.location));
            }
        }
        for (Direction d : directions) {
            patrol.tryDig(d);
        }
    }

    /*******************************
     GENERAL MOVE METHODS
     ******************************/

    static MapLocation findHq() {

        RobotInfo[] robots = rc.senseNearbyRobots();
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ) {
                return robot.location;
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

//    // TODO: if can't move, need to dig to change elevation to build path
//    static MapLocation moveTo(MapLocation dest) throws GameActionException {
//        System.out.println("I am moving to " + dest);
//
//        int deltaX, deltaY, paces;
//        int[] distance;
//        MapLocation current;
//
//        current = rc.getLocation();
//        if (current.equals(dest))
//            return current;
//
//        // getDistance returns x + y coordinate distance from robot to their dest
//        distance = getDistance(current, dest);
//        deltaX = distance[0];
//        deltaY = distance[1];
//
//        // more general movement in that direction instead of a direct shot
//        if (deltaX < 0) {
//
//            if (tryMove(Direction.WEST)) {
//            } else if (tryMove(Direction.NORTHWEST)) {
//            } else if (tryMove(Direction.SOUTHWEST)) {
//            }
//        }
//        else if (deltaX > 0) {
//            if (tryMove(Direction.EAST)) {
//            } else if (tryMove(Direction.NORTHEAST)) {
//            } else if (tryMove(Direction.SOUTHEAST)) {
//            }
//        }
//        else if (deltaY < 0) {
//            if (tryMove(Direction.SOUTH)) {
//            } else if (tryMove(Direction.EAST)) {
//            } else if (tryMove(Direction.WEST)) {
//            }
//        }
//        else if (deltaY > 0) {
//            if (tryMove(Direction.NORTH)) {
//            } else if (tryMove(Direction.NORTHEAST)) {
//            } else if (tryMove(Direction.NORTHWEST)) {
//            }
//        }
//        else
//            tryMove(randomDirection());
//        return rc.getLocation();
//    }

    /*****************
     END OF METHODS
     *****************/

    static void runDeliveryDrone() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        if (!rc.isCurrentlyHoldingUnit()) {
            // See if there are any enemy robots within capturing range
            RobotInfo[] robots = rc.senseNearbyRobots(GameConstants.DELIVERY_DRONE_PICKUP_RADIUS_SQUARED, enemy);

            if (robots.length > 0) {
                // Pick up a first robot within range
                rc.pickUpUnit(robots[0].getID());
                System.out.println("I picked up " + robots[0].getID() + "!");
            }
        } else {
            // No close robots, so search for robots within sight radius
            tryMove(randomDirection());
        }
    }

    static void runNetGun() throws GameActionException {

    }

    /**
     * Returns a random Direction.
     *
     * @return a random Direction
     */
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    /**
     * Returns a random RobotType spawned by miners.
     *
     * @return a random RobotType
     */
    static RobotType randomSpawnedByMiner() {
        return spawnedByMiner[(int) (Math.random() * spawnedByMiner.length)];
    }

    static boolean tryMove() throws GameActionException {
        for (Direction dir : Direction.values())
            if (tryMove(dir))
                return true;
        return false;
        // MapLocation loc = rc.getLocation();
        // if (loc.x < 10 && loc.x < loc.y)
        //     return tryMove(Direction.EAST);
        // else if (loc.x < 10)
        //     return tryMove(Direction.SOUTH);
        // else if (loc.x > loc.y)
        //     return tryMove(Direction.WEST);
        // else
        //     return tryMove(Direction.NORTH);
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
    static boolean tryBuild(RobotType type, Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to mine soup in a given direction.
     *
     * @param dir The intended direction of mining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canMineSoup(dir)) {
            rc.mineSoup(dir);
            return true;
        } else return false;
    }

    /**
     * Attempts to refine soup in a given direction.
     *
     * @param dir The intended direction of refining
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryRefine(Direction dir) throws GameActionException {
        if (rc.isReady() && rc.canDepositSoup(dir)) {
            rc.depositSoup(dir, rc.getSoupCarrying());
            return true;
        } else return false;
    }


    static void tryBlockchain() throws GameActionException {
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
