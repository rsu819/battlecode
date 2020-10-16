package sprint1;
import battlecode.common.*;

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
        for (Direction dirs : directions)
            tryBuild(RobotType.DESIGN_SCHOOL, dirs);
        Direction dir = randomDirection();
        if (tryMove(dir))
            System.out.println("I moved to the " + dir);
//
        MapLocation[] soupLocations = rc.senseNearbySoup(34);
        for (MapLocation soup : soupLocations) {
            if (rc.getLocation().isAdjacentTo(soup)) {
                tryMine(rc.getLocation().directionTo(soup));
            }
            else {
                tryMove(dir.opposite());
            }
        }
        if (locationHQ != null){
            moveNextTo(locationHQ);
            if (rc.getLocation().isAdjacentTo(locationHQ)) {
                tryRefine(rc.getLocation().directionTo(locationHQ));
            }
        }


        //        if (tryMove(randomDirection()))
//            System.out.println("I moved!");
        for (Direction dirs : directions)
            if (tryRefine(dirs))
                System.out.println("I refined soup! " + rc.getTeamSoup());
        // build design school near HQ to speed up wall-building activity
        for (Direction dirs : directions)
            tryBuild(RobotType.DESIGN_SCHOOL, dirs);
        // tryBuild(randomSpawnedByMiner(), randomDirection());
        for (Direction dirs : directions)
            tryBuild(RobotType.FULFILLMENT_CENTER, dirs);

    }

    static MapLocation moveNextTo(MapLocation dest) throws GameActionException{
        MapLocation current = rc. getLocation();
        if (current.equals(dest)) {
            return current;
        }
        System.out.println("I am moving to " + dest);
        int deltaX = dest.x - current.x;
        int deltaY = dest.y - current.y;
        for (int i = 0; i < Math.abs(deltaX); i++){
            if (deltaX < 0 && rc.canMove(Direction.WEST)) {
                rc.move(Direction.WEST);
            } else if (deltaX > 0 && rc.canMove(Direction.EAST)) {
                rc.move(Direction.EAST);
            }
        }
        for (int i = 0; i < Math.abs(deltaY); i++) {
            if (deltaY < 0 && rc.canMove(Direction.SOUTH)) {
                rc.move(Direction.SOUTH);
            } else if (deltaY > 0 && rc.canMove(Direction.NORTH)) {
                rc.move(Direction.NORTH);
            }
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
    }

    static void runLandscaper() throws GameActionException {
        Direction rando;
        Landscaper.myTeam = rc.getTeam();
        MapLocation currentLocation = rc.getLocation();
        if (!currentLocation.isAdjacentTo(locationHQ)) {
            moveNextTo(locationHQ);
        }
        if (tryMove(rando = randomDirection())){
            System.out.println("I moved to the: " + rando);
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
        // System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
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
