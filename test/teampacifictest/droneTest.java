package teampacifictest;

import org.junit.Assert;
import org.junit.BeforeClass;
import battlecode.common.*;
import static TeamPacific.Robot.randomDirection;
import static org.mockito.Mockito.*;
import TeamPacific.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

public class droneTest {

    static MapLocation enemyHqLoc;
    static MapLocation teamHqLoc;
    static RobotController mockRc;
    static Drone drone;
    static Transaction[] block;
    static RobotInfo[] bots;
    static int[] goodMessage;
    static int[] badMessage;

    @BeforeClass
    public static void setupDroneTest() throws GameActionException {

        enemyHqLoc = new MapLocation(45, 48);
//        teamHqLoc = new MapLocation(14, 11);
        int mapWidth = 60;
        int mapHeight = 60;

        goodMessage = new int[]{603720, 2, 2, 14, 11, 0, 0};
        badMessage = new int[]{123, 123, 123, 123, 123, 123, 123};

        // set up expected message
        block = new Transaction[10];
        Transaction teamTransaction = new Transaction(0, goodMessage, 0);
        Transaction notOurTransaction = new Transaction(0, badMessage, 10);
        for (int i = 0; i < 10; i++) {
            if (i == 4) {
                block[i] = teamTransaction;
            } else {
                block[i] = notOurTransaction;
            }
        }


        mockRc = mock(RobotController.class);
        when(mockRc.getTeam()).thenReturn(Team.A);
        when(mockRc.getBlock(1)).thenReturn(block);
        when(mockRc.getMapWidth()).thenReturn(mapWidth);
        when(mockRc.getMapHeight()).thenReturn(mapHeight);

        drone = new Drone(mockRc);
    }

    public void generateRobots(MapLocation nearbyLoc, RobotType type, int radius) {

        int numberOfBots = 6;
        bots = new RobotInfo[numberOfBots];
        Random rand = new Random();
        for (int i = 0; i < numberOfBots; i++) {
            bots[i] = mock(RobotInfo.class);
            if (i == 4) {
                when(bots[i].getLocation()).thenReturn(nearbyLoc);
                when(bots[i].getType()).thenReturn(type);
                when(bots[i].getID()).thenReturn(1);
            }
            else {
                int xCoord = rand.nextInt(radius) + nearbyLoc.x;
                int yCoord = rand.nextInt(radius) + nearbyLoc.y;
                when(bots[i].getLocation()).thenReturn(new MapLocation(xCoord, yCoord));
                when(bots[i].getType()).thenReturn(RobotType.LANDSCAPER);
                when(bots[i].getID()).thenReturn(1);
            }
        }
    }

    @Test
    public void testScan() throws GameActionException{
        when(mockRc.getLocation()).thenReturn(new MapLocation(14, 34));
        when(mockRc.isReady()).thenReturn(true);
        when(mockRc.canMove(mockRc.getLocation().directionTo(new MapLocation(15, 34)))).thenReturn(false);
        when(mockRc.canMove(mockRc.getLocation().directionTo(new MapLocation(15, 35)))).thenReturn(false);
        when(mockRc.canMove(mockRc.getLocation().directionTo(new MapLocation(15, 33)))).thenReturn(true);

        Assert.assertEquals(true, drone.moveToward(Direction.EAST));
    }

    @Test
    public void testGoToFindEnemyHQ() {
        ArrayList<MapLocation> possibleEnemyHq = new ArrayList<MapLocation>() {
            {
                add(new MapLocation(45, 11));
                add(new MapLocation(14, 48));
                add(new MapLocation(45, 48));
            }
        };
        Assert.assertEquals(possibleEnemyHq, drone.enemyHQLoc);
    }

    @Test
    public void findEnemyHqSucceed() throws GameActionException {
        generateRobots(enemyHqLoc, RobotType.HQ, 20);

        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(20);
        when(mockRc.senseNearbyRobots(mockRc.getCurrentSensorRadiusSquared(), mockRc.getTeam().opponent())).thenReturn(bots);
        when(mockRc.getLocation()).thenReturn(new MapLocation(45, 47));
        when(mockRc.canSenseLocation(drone.enemyHQLoc.get(2))).thenReturn(true);

        Assert.assertEquals( 0, drone.findEnemyHQ(2));
    }

    @Test
    public void findEnemyHqFail() throws GameActionException {
        generateRobots(new MapLocation(14, 48), RobotType.MINER, 20);

        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(20);
        when(mockRc.senseNearbyRobots(mockRc.getCurrentSensorRadiusSquared(), mockRc.getTeam().opponent())).thenReturn(bots);
        when(mockRc.getLocation()).thenReturn(new MapLocation(15, 47));
        when(mockRc.canSenseLocation(drone.enemyHQLoc.get(1))).thenReturn(true);

        Assert.assertNotEquals( 0, drone.findEnemyHQ(1));
    }

    @Test
    public void stayInFindState() throws GameActionException {
        when(mockRc.isReady()).thenReturn(true);
        generateRobots(new MapLocation(14, 48), RobotType.MINER, 20);
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(20);
        when(mockRc.senseNearbyRobots(mockRc.getCurrentSensorRadiusSquared(), mockRc.getTeam().opponent())).thenReturn(bots);
        when(mockRc.getLocation()).thenReturn(new MapLocation(15, 47));
        when(mockRc.canSenseLocation(drone.enemyHQLoc.get(1))).thenReturn(true);
        when(mockRc.getRoundNum()).thenReturn(1);

        drone.currentState = Drone.States.FIND;
        drone.confirmedEnemyHq = null;
        drone.checkEnemyHQLocation = 3;

        drone.run(1);

        Assert.assertEquals(Drone.States.FIND, Drone.currentState);
    }


    @Test
    public void switchToPickState() throws GameActionException {
        when(mockRc.isReady()).thenReturn(true);
        generateRobots(enemyHqLoc, RobotType.HQ, 20);
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(20);
        when(mockRc.senseNearbyRobots(mockRc.getCurrentSensorRadiusSquared(), mockRc.getTeam().opponent())).thenReturn(bots);
        when(mockRc.getLocation()).thenReturn(new MapLocation(45, 47));
        when(mockRc.canSenseLocation(drone.enemyHQLoc.get(2))).thenReturn(true);
        when(mockRc.getRoundNum()).thenReturn(1);
        int[] msg = {603720, 3, 3, 45, 48};
        when(mockRc.canSubmitTransaction(msg, 10)).thenReturn(false);
        drone.confirmedEnemyHq = new MapLocation(45, 48);

        drone.run(1);

        Assert.assertEquals(Drone.States.PICK, Drone.currentState);
    }

    @Test
    public void testPickUpLandscaper() throws GameActionException {
        teamHqLoc = new MapLocation(35, 35);
        MapLocation droneLoc = new MapLocation(32, 32);
        when(mockRc.getLocation()).thenReturn(droneLoc);
        when(mockRc.canSenseLocation(teamHqLoc)).thenReturn(true);
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(20);
        generateRobots(droneLoc, RobotType.LANDSCAPER, 8);
        when(mockRc.senseNearbyRobots(20, Team.A)).thenReturn(bots);
        when(mockRc.canPickUpUnit(1)).thenReturn(true);

        Assert.assertEquals(true, drone.findLandscaper(Team.A, teamHqLoc));
    }

    @Test
    public void testDropTeamLandscaper() throws GameActionException {
        when(mockRc.isCurrentlyHoldingUnit()).thenReturn(true);
        when(mockRc.getLocation()).thenReturn(new MapLocation(9, 5));
        drone.confirmedEnemyHq = new MapLocation(6, 5);
        when(mockRc.canSenseLocation(drone.confirmedEnemyHq)).thenReturn(true);
        when(mockRc.canDropUnit(mockRc.getLocation().directionTo(drone.confirmedEnemyHq))).thenReturn(true);
        when(mockRc.senseFlooding(mockRc.getLocation())).thenReturn(false);

        Assert.assertEquals(true, drone.dropTeamLandscaper(drone.confirmedEnemyHq));
    }

    @Test
    public void testFindWater() throws GameActionException {
        Direction dir = randomDirection();
        MapLocation loc = new MapLocation(36, 24);
        when(mockRc.getLocation()).thenReturn(loc);
        MapLocation maybeFlood = loc.translate(dir.getDeltaX(), dir.getDeltaY());
        when(mockRc.canSenseLocation(maybeFlood)).thenReturn(true);
        when(mockRc.senseFlooding(maybeFlood)).thenReturn(true);

        Assert.assertEquals(maybeFlood, drone.findWater());
    }


    @Test
    public void testIsLandscapersAround() throws GameActionException {
        // Create mock locations for testing
        MapLocation currentLoc = new MapLocation(15,15);
        MapLocation[] hqLoc = new MapLocation[] {new MapLocation(20,20), new MapLocation(20,40)};

        // Mock drones functions
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(5);
        when(mockRc.isReady()).thenReturn(true);
        when(mockRc.getLocation()).thenReturn(currentLoc);
        when(mockRc.senseNearbyRobots(5)).thenReturn(new RobotInfo[] {new RobotInfo(3, Team.A, RobotType.LANDSCAPER, 0,
                false, 0, 0, 0, new MapLocation(5,5))});
        boolean result = Drone.isLandscapersAround();
        Assert.assertTrue(result);
    }

    @Test
    public void testPickUpLandscapers() throws GameActionException {
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(5);
        when(mockRc.senseNearbyRobots(5)).thenReturn(new RobotInfo[] {new RobotInfo(3, Team.A, RobotType.LANDSCAPER, 0,
                true, 0, 0, 0, new MapLocation(5,5))});
        when(mockRc.getLocation()).thenReturn(new MapLocation(5,6));
        when(mockRc.canPickUpUnit(3)).thenReturn(true);
        boolean result = Drone.pickUpLandscapers();
        Assert.assertTrue(result);
    }

    @Test
    public void testGoAroundToFind() throws GameActionException {
        MapLocation[] hqLoc = new MapLocation[] {new MapLocation(20,20), new MapLocation(20,40)};
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(5);
        when(mockRc.senseNearbyRobots(5)).thenReturn(new RobotInfo[] {new RobotInfo(3, Team.A, RobotType.LANDSCAPER, 0,
                false, 0, 0, 0, new MapLocation(5,5))});
        when(mockRc.getLocation()).thenReturn(new MapLocation(5,11));
        boolean result = Drone.goAroundToFind(new MapLocation(20,21));
        if(result == false) {
            when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(5);
        } else {
            when(mockRc.canMove(Direction.CENTER)).thenReturn(true);
            mockRc.move(Direction.CENTER);
            when(mockRc.canMove(Direction.SOUTHWEST)).thenReturn(true);
            mockRc.move(Direction.SOUTHWEST);
            when(mockRc.canMove(Direction.SOUTH)).thenReturn(true);
            mockRc.move(Direction.SOUTH);
            when(mockRc.canMove(Direction.SOUTHEAST)).thenReturn(true);
            mockRc.move(Direction.SOUTHEAST);
            Assert.assertTrue(result);
        }
    }

    @Test
    public void testTryMoveTo() throws GameActionException {
        MapLocation currentLoc = new MapLocation(15,15);

        when(mockRc.isReady()).thenReturn(true);
        when(mockRc.canMove(Direction.CENTER)).thenReturn(true);
        when(mockRc.canMove(Direction.SOUTH)).thenReturn(true);
        when(mockRc.canMove(Direction.WEST)).thenReturn(true);
        when(mockRc.canMove(Direction.EAST)).thenReturn(true);
        when(mockRc.canMove(Direction.NORTH)).thenReturn(true);
        when(mockRc.canMove(Direction.NORTHWEST)).thenReturn(true);
        when(mockRc.canMove(Direction.NORTHEAST)).thenReturn(true);
        when(mockRc.canMove(Direction.SOUTHEAST)).thenReturn(true);
        when(mockRc.canMove(Direction.SOUTHWEST)).thenReturn(true);
        when(mockRc.getLocation()).thenReturn(currentLoc);
        boolean result = Drone.tryMoveTo(Direction.CENTER);
        Assert.assertTrue(result);
    }

    @Test
    public void testDropToWater() throws GameActionException {
        MapLocation currentLoc = new MapLocation(15,15);
        when(mockRc.isReady()).thenReturn(true);
        when(mockRc.getLocation()).thenReturn(currentLoc);
        when(mockRc.senseFlooding(mockRc.getLocation())).thenReturn(true);
        boolean result = Drone.dropToWater();

        if(result == false) {
            when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(5);
        } else {
            for (Direction dir : Direction.allDirections()) {
                when(mockRc.canDropUnit(dir)).thenReturn(true);
            }
        Assert.assertTrue(Drone.dropToWater());
        }
    }

    @Test
    public void testPickUpCow() throws GameActionException {
        when(mockRc.getCurrentSensorRadiusSquared()).thenReturn(5);
        when(mockRc.senseNearbyRobots()).thenReturn(new RobotInfo[] {new RobotInfo(3, Team.A, RobotType.COW, 0,
                true, 0, 0, 0, new MapLocation(5,5))});
        when(mockRc.canSenseLocation(any(MapLocation.class))).thenReturn(true);
        when(mockRc.getLocation()).thenReturn(new MapLocation(5,6));
        when(mockRc.canPickUpUnit(3)).thenReturn(true);
        boolean result = Drone.pickUpCow(new MapLocation(5,6));
        Assert.assertTrue(result);
    }
}

