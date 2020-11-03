package TeamPacific;
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
    static int buildCount;

    // Used only for robots that needed their own classes for more complex logic
    static HQ hq;
    static Landscaper landscaper;
    static Miner miner;
    public static MapLocation enemyHq = new MapLocation(33,33);

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
        buildCount = 0;


        //Used to initialize some robots
        switch (rc.getType()) {
        	case HQ:
        		hq = new HQ(rc);
        		break;
        	case LANDSCAPER:
        		landscaper = new Landscaper(rc);
        		break;
        	case MINER:
        		miner = new Miner(rc);
        		break;
        }
        
        while (true) {
            turnCount += 1;
            System.out.println("Turncount: " + turnCount);

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                //System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case HQ:                 hq.run(turnCount);             break;
                    case MINER:              miner.run(turnCount);   		break;
                    case REFINERY:           runRefinery();				 	break;
                    case VAPORATOR:          runVaporator();         		break;
                    case DESIGN_SCHOOL:      runDesignSchool();      		break;
                    case FULFILLMENT_CENTER: runFulfillmentCenter(); 		break;
                    case LANDSCAPER:         landscaper.run(turnCount);     break;
                    case DELIVERY_DRONE:     runDeliveryDrone();     		break;
                    case NET_GUN:            runNetGun();            		break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

    static void runRefinery() throws GameActionException {
        //System.out.println("Pollution: " + rc.sensePollution(rc.getLocation()));
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
    	/*
        for (Direction dir : directions)
            tryBuild(RobotType.DELIVERY_DRONE, dir);
        randomDirection().rotateLeft();*/
    }

    static void runDeliveryDrone() throws GameActionException {
    	/*
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
        }*/
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

    static void runNetGun() throws GameActionException {

    }

}