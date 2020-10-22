/* TODO:
 - build offensive landscaper (to pile dirt on enemy)
 - refine the flood patrol landscaper
 - refine wall builder to include building wall on tile that landscaper is standing on
 */

package TeamPacific;
import battlecode.common.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;


public strictfp class RobotPlayer {
    static RobotController rc;
    static int numMiners = 0;

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

    enum States {
        WANDER, MINE, REFINE, MOVETOWARDS, MOVETOWARDSSOUP, MOVETOWARDSREFINERY, MOVETOWARDSHQ, BUILD;
    }

    public enum LandscaperTask {
        WALL_BUILDER,
        OFFENSE_UNIT,
        FLOOD_PATROL
    }

    static int turnCount;
    static int minerCount;
    static MapLocation locationHQ;
    static MapLocation enemyHq;

    static MapLocation refinery;
    static boolean designSchoolCount;

    static Random randomGen = new Random();
    public static States currentState;
    public static States prevState;
    public static Direction tempDir = null;
    public static Direction lastDir = randomDirection();
    public static Direction currDir = lastDir;
    public static MapLocation[] nearbySoup;
    public static MapLocation currentTargetLoc;
    public static MapLocation lastTargetLoc;
    static int buildCount = 0;
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
        buildCount = 0;
        designSchoolCount = true;

        setState(null);

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
                    case LANDSCAPER:         runLandscaper();        break;
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

    static States getState() {
        return currentState;
    }

    static void setState(States aState) {
        currentState = aState;
    }

    static void runMiner() throws GameActionException {

        //tryBlockchain();

        //Used for soup finding
        if(getState() == null) {
            setState(States.WANDER);

            //Find the location of the HQ
            RobotType[] tempArray = {RobotType.HQ};
            locationHQ = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray);
        }

        System.out.println(currentState);
        //System.out.println("My current state is: " + currentState);

        if (rc.isReady()) {

            if ( (turnCount > 200 && turnCount < 300) && (buildCount < 1) ) {
                buildCount++;
                prevState = getState();
                setState(States.BUILD);
                tryBuild(RobotType.DESIGN_SCHOOL, randomDirection());
            }

            switch( getState() ) {
                // Wander around in a (general) straight line until nearby soup is found
                case WANDER:
                    // Check if there is any soup nearby first
                    nearbySoup = rc.senseNearbySoup();
                    //System.out.println(nearbySoup[0]);
                    if (nearbySoup.length > 0) {
                        currentTargetLoc = findNearest(rc.getLocation(), nearbySoup);
                        tempDir = rc.getLocation().directionTo(currentTargetLoc);
                        // Switch to mining state
                        setState(States.MOVETOWARDSSOUP);
                        System.out.println("I'm gonna mine soup: " + tempDir);
                    } else {
                        // Used to prevent robots from shuffling back and forth
                        if (tryMove(currDir)) {
                            System.out.println("I moved!");
                        } else {
                            //Get a list of all "frontal" directions
                            Direction[] dirList = getFrontDirections(currDir);
                            while( !tryMove(currDir) ) {
                                currDir = dirList[randomGen.nextInt(dirList.length)];
                                while ( currDir == lastDir ) {
                                    currDir = randomDirection();
                                }
                                lastDir = currDir;
                            }
                        }
                    }

                    break;

                //Stay put and mine nearby soup
                case MINE:

                    if (rc.getSoupCarrying() >= 100) {
                        lastTargetLoc = currentTargetLoc;
                        setState(States.REFINE);
                    } else {
                        if (tryMine(tempDir)) {
                            System.out.println("I mined soup! " + tempDir + " " + rc.getSoupCarrying());
                        } else { //Check if there is any other soup around to mine
                            nearbySoup = rc.senseNearbySoup(2);
                            if(nearbySoup.length > 0) {
                                tempDir = rc.getLocation().directionTo(findNearest(rc.getLocation(), nearbySoup));
                                System.out.println("I'm gonna mine soup: " + tempDir);
                            } else {
                                setState(States.WANDER);
                            }
                        }
                    }

                    break;

                // Move towards given location naively, will try to stop one tile before location
                case MOVETOWARDSSOUP:
                    System.out.println("I'm moving towards: " + currentTargetLoc);
                    if(rc.adjacentLocation(tempDir).equals(currentTargetLoc)) {
                        setState(States.MINE);
                    } else {
                        // Try the "correct" direction first"
                        if(!tryMove(tempDir)) {
                            //Get a list of all "frontal" directions
                            Direction[] dirList = getFrontDirections(tempDir);
                            //Otherwise continue on whatever path
                            while( !tryMove(lastDir) ) {
                                lastDir = dirList[randomGen.nextInt(dirList.length)];
                            }
                        } else {
                            lastDir = tempDir;
                        }
                        tempDir = rc.getLocation().directionTo(currentTargetLoc);
                    }

                    break;

                case REFINE:

                    // First, see if there is a nearby refinery
                    MapLocation closestRefinery;

                    if(turnCount < 200) {
                        RobotType[] tempArray = {RobotType.HQ, RobotType.REFINERY};
                        closestRefinery = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray);
                    } else {
                        RobotType[] tempArray = {RobotType.REFINERY};
                        closestRefinery = findNearest(rc.getLocation(), rc.senseNearbyRobots(), tempArray);
                    }


                    //There was a refinery found!
                    if (rc.onTheMap(closestRefinery)) {

                        tempDir = rc.getLocation().directionTo(closestRefinery);
                        if (tryRefine(tempDir)) {
                            System.out.println("I refined some soup! " + rc.getSoupCarrying());
                            setState(States.WANDER);
                            System.out.println("I'm gonna wander around!");
                        } else { // Gotta walk
                            currentTargetLoc = closestRefinery;
                            setState(States.MOVETOWARDSREFINERY);
                        }

                    } else {

                        tempDir = randomDirection();
                        //Try and build in a random location
                        if(tryBuild(RobotType.REFINERY, tempDir)) {
                            System.out.println("I built a refinery!");
                        }
                        if (tryRefine(tempDir)) {
                            System.out.println("I refined some soup! " + rc.getSoupCarrying());
                            setState(States.MOVETOWARDSSOUP);
                            currentTargetLoc = lastTargetLoc;
                            System.out.println("I'm gonna move back to mine more soup!");
                        } // Shouldn't be here
                    }

                    break;

                // Move towards given location naively, will try to stop one tile before location
                case MOVETOWARDSREFINERY:
                    System.out.println("I'm moving towards: " + currentTargetLoc);
                    if(rc.adjacentLocation(tempDir).equals(currentTargetLoc)) {
                        setState(States.REFINE);
                    } else {
                        // Try the "correct" direction first"
                        if(!tryMove(tempDir)) {
                            //Get a list of all "frontal" directions
                            Direction[] dirList = getFrontDirections(tempDir);
                            //Otherwise continue on whatever path
                            while( !tryMove(lastDir) ) {
                                lastDir = dirList[randomGen.nextInt(dirList.length)];
                            }
                        } else {
                            lastDir = tempDir;
                        }
                        tempDir = rc.getLocation().directionTo(currentTargetLoc);
                    }
                    break;

                // Used to exclusively build design schools for now
                case BUILD:

                    tempDir = rc.getLocation().directionTo(locationHQ);

                    if ((locationHQ.distanceSquaredTo(rc.adjacentLocation(tempDir.opposite())) >= 4) && (locationHQ.distanceSquaredTo(rc.adjacentLocation(tempDir)) < 16) ){
                        if (tryBuild(RobotType.DESIGN_SCHOOL, tempDir.opposite())) {
                            buildCount++;
                            setState(prevState);
                        } else {
                            setState(prevState);
                        }
                    } else {
                        // Try the "correct" direction first"
                        if(!tryMove(tempDir)) {
                            //Get a list of all "frontal" directions
                            Direction[] dirList = getFrontDirections(tempDir);
                            //Otherwise continue on whatever path
                            while( !tryMove(lastDir) ) {
                                lastDir = dirList[randomGen.nextInt(dirList.length)];
                            }
                        } else {
                            lastDir = tempDir;
                        }
                        tempDir = rc.getLocation().directionTo(locationHQ);
                    }

                    break;

                // Move towards given location naively, will try to stop one tile before location
                case MOVETOWARDSHQ:
                    System.out.println("I'm moving towards: " + locationHQ);
                    if(rc.adjacentLocation(tempDir).equals(locationHQ)) {
                        setState(States.BUILD);
                    } else {
                        // Try the "correct" direction first"
                        if(!tryMove(tempDir)) {
                            //Get a list of all "frontal" directions
                            Direction[] dirList = getFrontDirections(tempDir);
                            //Otherwise continue on whatever path
                            while( !tryMove(lastDir) ) {
                                lastDir = dirList[randomGen.nextInt(dirList.length)];
                            }
                        } else {
                            lastDir = tempDir;
                        }
                        tempDir = rc.getLocation().directionTo(locationHQ);
                    }
                    break;
            }


        }
    }

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
    static MapLocation findNearest(MapLocation currLoc, RobotInfo[] robotList, RobotType[] targetTypes) {

        MapLocation tempLocation = new MapLocation( -1, -1);
        int nearest = -1;
        int tempInt = Integer.MAX_VALUE;

        for(int i = 0; i < robotList.length; i++) {

            if( Arrays.asList(targetTypes).contains(robotList[i].getType()) ) {
                if ( (currLoc.distanceSquaredTo(robotList[i].getLocation()) < tempInt) && (robotList[i].getTeam() == rc.getTeam())) {
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


    static void runRefinery() throws GameActionException {
        System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
    }

    static void runVaporator() throws GameActionException {

    }

    static void runDesignSchool() throws GameActionException {

        if (buildCount < 1) {
            for (Direction dir : directions) {
                if(tryBuild(RobotType.LANDSCAPER, dir)) {
                    buildCount++;
                }
            }
        }
    }


    static void runFulfillmentCenter() throws GameActionException {
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
        randomDirection().rotateLeft();
    }


    static void runLandscaper() throws GameActionException {

        TeamPacific.Landscaper landscaper = new TeamPacific.Landscaper(rc, locationHQ);

        if (locationHQ == null) {
            locationHQ = findHq();
            tryMove(randomDirection());
        }

        runWallBuilder(landscaper);
    }

    // build general tasks / skills for the landscaper
    static void runWallBuilder(TeamPacific.Landscaper builder)  throws GameActionException {

        MapLocation curr = rc.getLocation();

        locationHQ = findHq();

        if (!curr.isAdjacentTo(locationHQ) && rc.canMove(curr.directionTo(locationHQ))) {
            tryMove(curr.directionTo(locationHQ));
        }

        if (rc.getDirtCarrying() < 15  && curr.isAdjacentTo(locationHQ)) {
            Direction[] awayFromHq = TeamPacific.Landscaper.digAwayFromBldg(curr.directionTo(locationHQ));
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

    static void runFloodPatrol(TeamPacific.Landscaper patrol) throws GameActionException {

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

    static void runOffenseUnit(TeamPacific.Landscaper offense) throws GameActionException {

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

        if ((rc.getDirtCarrying() == RobotType.LANDSCAPER.dirtLimit) && curr.isAdjacentTo(enemyHq)){
            offense.tryDepositDirt(curr.directionTo(enemyHq));
        } else {
            tryMove(curr.directionTo(enemyHq));
        }


    }

    /*******************************
     GENERAL MOVE METHODS
     ******************************/

    static MapLocation findHq() {

        RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam());
        for (RobotInfo robot : robots) {
            if (robot.type == RobotType.HQ) {
                System.out.println("HQ is at: " + robot.location);
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

