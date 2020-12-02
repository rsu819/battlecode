package teampacifictest;

import TeamPacific.NetGun;
import battlecode.common.*;

import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class netGunTest {
    static NetGun ng;
    static RobotController rc;
    static RobotInfo kill;
    static RobotInfo[] RI;
    static int senseRadius;

    @Before
    public void setupNetGunTest() throws GameActionException {
        rc = mock(RobotController.class);
        ng = new NetGun(rc);

        kill = new RobotInfo(5,
                Team.B, RobotType.DELIVERY_DRONE, 0, false, 0, 0,
                0, new MapLocation(1, 1));
        RI = new RobotInfo[] {kill};

        senseRadius = GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED;
    }

    @Test
    public void testNetGunConstructor() throws GameActionException {
        assertTrue("HQ found no opponents", ng.shot == 0);
    }

    @Test
    public void testRunNetGun() throws GameActionException {
        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getTeam().opponent()).thenReturn(Team.B);
        ng.run(1);

        assertTrue("HQ found no opponents", ng.shot >= 0);
    }

    @Test
    public void testFindNoOpponents() {
        boolean noKill = false;
        NetGun ng = Mockito.mock(NetGun.class);
        if(ng.findOpponents(senseRadius) == null){noKill = true;}
        assertTrue("HQ found no opponents", noKill);
    }

    @Test
    public void testFindOpponents() {
        boolean kills = false;

        when(rc.getTeam()).thenReturn(Team.A);
        when(rc.getTeam().opponent()).thenReturn(Team.B);
        when(rc.senseNearbyRobots(senseRadius, rc.getTeam().opponent())).thenReturn(RI);
        if(ng.findOpponents(senseRadius) != null){kills = true;}

        assertTrue("HQ found opponents", kills);
    }

    @Test
    public void testShootNothing() throws GameActionException {
        boolean shot = false;
        RobotInfo kill = null;
        NetGun ng = Mockito.mock(NetGun.class);
        if(ng.shoot(kill) == 0){shot = true;}
        assertTrue("HQ shoots nothing", shot);
    }
    /*
    @Test
    public void testShootSomething() throws GameActionException {
        boolean shot = false;

        when(rc.canShootUnit(kill.getID())).thenReturn(Boolean.TRUE);

        if(ng.shoot(kill) > 0){shot = true;}
        assertTrue("Can the HQ shoot: " + shot, shot);
    }*/
}