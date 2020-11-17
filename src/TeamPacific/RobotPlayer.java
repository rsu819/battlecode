package TeamPacific;
import battlecode.common.*;

import static TeamPacific.Blockchain.TeamId;

public strictfp class RobotPlayer {
    static RobotController rc;

    public static Direction[] directions = {
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
    static int buildCountF;

    // Used only for robots that needed their own classes for more complex logic
    static HQ hq;
    static Landscaper landscaper;
    static Miner miner;
    static Drone drone;
    static NetGun netGun;
    static DesignSchool designSchool;
    static FulfillmentCenter fulfillmentCenter;
    public static MapLocation enemyHq;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        if (rc.getTeam() == Team.A)
            TeamId = 111;
        else
            TeamId = 222;

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;
        buildCountF = 0;

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
            case DELIVERY_DRONE:
                drone = new Drone(rc);
                break;
            case NET_GUN:
                netGun = new NetGun(rc);
                break;
            case DESIGN_SCHOOL:
                designSchool = new DesignSchool(rc);
                break;
            case FULFILLMENT_CENTER:
                fulfillmentCenter = new FulfillmentCenter(rc);
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
                    case HQ:
                        hq.run(turnCount);
                        break;
                    case MINER:
                        miner.run(turnCount);
                        break;
                    case LANDSCAPER:
                        landscaper.run(turnCount);
                        break;
                    case DELIVERY_DRONE:
                        drone.run(turnCount);
                        break;
                    case DESIGN_SCHOOL:
                        designSchool.run(turnCount);
                        break;
                    case FULFILLMENT_CENTER:
                        fulfillmentCenter.run(turnCount);
                        break;
                    case NET_GUN:
                        netGun.run(turnCount);
                        break;
                    case REFINERY:
                        runRefinery();
                        break;
                    case VAPORATOR:
                        runVaporator();
                        break;
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
}
