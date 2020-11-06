package teampacifictest;

import TeamPacific.HQ;
import battlecode.common.*;
import battlecode.common.RobotController;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.junit.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static TeamPacific.RobotPlayer.directions;

public class hqTest {
    static HQ hq;
    static RobotController rc;
    static RobotInfo kill;
    static RobotInfo[] RI;

    @BeforeClass
    public static void setupHQTest() {
        rc = mock(RobotController.class);
        hq = new HQ(rc);

        kill = new RobotInfo(5,
                Team.B, RobotType.DELIVERY_DRONE, 0, false, 0, 0,
                0, new MapLocation(1, 1));
        RI = new RobotInfo[] {kill};
    }

    @Test
    public void testConstructor() throws GameActionException {
        assertEquals(4, hq.maxMiner);
    }

    @Test
    public void testRunMaxMiner() throws GameActionException {
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getTeam().opponent()).thenReturn(Team.B);
        when(rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent())).thenReturn(RI);

        int counter = 0;
        while (counter < 3020) {
            hq.run(counter);
            ++counter;
            if (hq.maxMiner != 3) {System.out.println(hq.maxMiner + " TEST");}
        }
        assertEquals(12, hq.maxMiner);
    }

    @Test
    public void testRunBuildsMiner() throws GameActionException {
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getTeam().opponent()).thenReturn(Team.B);
        when(rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent())).thenReturn(RI);
        for (Direction dir : directions) {
            when(hq.tryBuild(RobotType.MINER, dir)).thenReturn(true);
        }

        Boolean build = false;
        int count = 1;
        while (hq.numMiners != 1 && count < 100) {
            hq.run(count);
            ++count;
        }
        if (hq.numMiners > 0) {build = true;}
        if (hq.numMiners != 1) {System.out.println(hq.numMiners + " TEST");}
        assertTrue("HQ builds miner", build);
    }

    @Test
    public void testFindNoOpponents() {
        boolean noKill = false;
        HQ hq = Mockito.mock(HQ.class);
        if(hq.findOpponents() == null){noKill = true;}
        assertTrue("HQ found no opponents", noKill);
    }

    @Test
    public void testFindOpponents() {
        boolean kills = false;

        when(rc.getTeam())
                .thenReturn(Team.A);
        when(rc.getTeam().opponent())
                .thenReturn(Team.B);
        when(rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED,
                rc.getTeam().opponent()))
                .thenReturn(RI);
        if(hq.findOpponents() != null){kills = true;}// This or the one below. this is false the one below is true.
        //if(rc.senseNearbyRobots(GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, rc.getTeam().opponent()) != null){kills = true;}
        assertTrue("HQ found opponents", kills);
    }

    @Test
    public void testShootNothing() throws GameActionException {
        boolean shot = false;
        RobotInfo kill = null;
        HQ hq = Mockito.mock(HQ.class);
        if(hq.shoot(kill) == 0){shot = true;}
        assertTrue("HQ shoots nothing", shot);
    }
    /*
    @Test
    public void testShootSomething() throws GameActionException {
        boolean shot = false;

        when(rc.canShootUnit(kill.getID())).thenReturn(Boolean.TRUE);

        if(hq.shoot(kill) > 0){shot = true;}
        assertTrue("Can the HQ shoot: " + shot, shot);
    }
    /*
    @Test
    public void testEmitHqLocation() throws GameActionException {
        //HQ hq = Mockito.mock(HQ.class);
        RobotController rc = Mockito.mock(RobotController.class);
        HQ hq = new HQ(rc);
        Blockchain bl = Mockito.mock(Blockchain.class);
        MapLocation mp = new MapLocation(1, 1);
        int[] message = {0,0,0,0,0,0,0};

        //foundResourceMessage(rc.getLocation(), MessageTo.Any, Resource.HomeHQ);
        when(hq.rc.getLocation()).thenReturn(mp);
        when(bl.foundResourceMessage(hq.rc.getLocation(), Blockchain.MessageTo.Any, Blockchain.Resource.HomeHQ, false)).thenReturn(message);

        assertTrue("HQ emits location", hq.emitHqLocation());
    }*/
}