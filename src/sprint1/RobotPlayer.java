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

    static int turnCount;
    static int minerCount;
    static MapLocation locationHQ = new MapLocation(35, 26);

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
        System.out.println("I'm a " + rc.getType() + " and I just got created!");
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
//                    case VAPORATOR:          runVaporator();         break;
                    case DESIGN_SCHOOL:      runDesignSchool();      break;
//                    case FULFILLMENT_CENTER: runFulfillmentCenter(); break;
                    case LANDSCAPER:         runLandscaper();        break;
//                    case DELIVERY_DRONE:     runDeliveryDrone();     break;
//                    case NET_GUN:            runNetGun();            break;
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

        while (minerCount < 7){
            Direction dir = randomDirection();
            if (tryBuild(RobotType.MINER, dir) == true)
                ++minerCount;
        }

    }

    static void runMiner() throws GameActionException {
//        tryBlockchain();

        tryBlockchain();
        tryMove(randomDirection());
        if (tryMove(randomDirection()))
            System.out.println("I moved!");
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dir : directions)
            tryBuild(RobotType.DESIGN_SCHOOL, dir);
        for (Direction dir : directions)
            if (tryRefine(dir))
                System.out.println("I refined soup! " + rc.getTeamSoup());
        for (Direction dir : directions)
            if (tryMine(dir))
                System.out.println("I mined soup! " + rc.getSoupCarrying());
    }

    static int[] getDistance(MapLocation myLoc, MapLocation myDest) {

        int x = myDest.x - myLoc.x;
        int y = myDest.y - myLoc.y;

        int[] distance = {x, y};
        return distance;
    }

    static MapLocation moveTo(MapLocation dest) throws GameActionException{
        System.out.println("I am moving to " + dest);

        int deltaX, deltaY, paces;
        int[] distance;
        MapLocation current;

        current = rc.getLocation();
        if (current.equals(dest))
            return current;

        // getDistance returns x + y coordinate distance from robot to their dest
        distance = getDistance(current, dest);
        deltaX = distance[0];
        deltaY = distance[1];
        paces = Math.abs(deltaX) + Math.abs(deltaY);

        while (paces > 0) {
            if (deltaX < 0) {
                if (tryMove(Direction.WEST)) {
                }
                else if (tryMove(Direction.NORTHWEST)) { }
                else if (tryMove(Direction.SOUTHWEST)) { }
            }
            if (deltaX > 0) {
                if (tryMove(Direction.EAST)) {
                }
                else if (tryMove(Direction.NORTHEAST)) { }
                else if (tryMove(Direction.SOUTHEAST)) { }
            }
            if (deltaY < 0) {
                if (tryMove(Direction.SOUTH)) {
                }
                else if (tryMove(Direction.SOUTHEAST)) { }
                else if (tryMove(Direction.SOUTHWEST)) { }
            }
            if (deltaY > 0) {
                if (tryMove(Direction.NORTH)) {
                }
                else if (tryMove(Direction.NORTHEAST)) { }
                else if (tryMove(Direction.NORTHWEST)) { }
            }
            --paces;

        }
        String output = String.format("Robot ID: %d is now at " + rc.getLocation(), rc.getID());
        System.out.println(output);
        current = rc.getLocation();
        if (current.isAdjacentTo(dest)){
            System.out.println("Success!");
        } else {
            System.out.println("Nope, we are at " + current);
        }
        // add conditionals for avoiding elevation/pollution/water/etc
        return dest;
    }


    static void runRefinery() throws GameActionException {
        // System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {
        for (Direction dir: directions) {
            tryBuild(RobotType.LANDSCAPER, dir);
        }

    }

    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
        randomDirection().rotateLeft();
    }

    static void runLandscaper() throws GameActionException {
        // moving dirt
        Direction rando;
        Landscaper.myTeam = rc.getTeam();
        MapLocation currentLocation = rc.getLocation();
        if (!currentLocation.isAdjacentTo(locationHQ)) {
            moveTo(locationHQ);
        }
        for (Direction dir : directions) {
            if (rc.canDigDirt(dir)){
                rc.digDirt(dir);
                System.out.println("Dug dirt to the: " + dir);
            }
        }
        if (tryMove(rando = randomDirection())){
            System.out.println("I moved to the: " + rando);
        }
        for (Direction dir: directions) {
            if (rc.canDepositDirt(dir)) {
                rc.depositDirt(dir);
                System.out.println("Deposited dirt to the: " + dir);
            }
        }
    }

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
        for (Direction dir : directions)
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
        if (rc.isReady() && rc.canMove(dir)) {
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
