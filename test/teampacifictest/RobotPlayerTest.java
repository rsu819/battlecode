package teampacifictest;

import TeamPacific.RobotPlayer;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;

public class RobotPlayerTest {

    @Test
    public void testRobotPlayerCreation() {
        RobotPlayer r1 = Mockito.mock(RobotPlayer.class);
        RobotPlayer r2 = Mockito.mock(RobotPlayer.class);
        assertNotEquals(r1, r2);
    }
    /*
    @Test
    public void testRunDesignSchool() throws GameActionException {
        RobotPlayer rb = Mockito.mock(RobotPlayer.class);
        RobotController rc = Mockito.mock(RobotController.class);
        //RobotPlayer rb = new RobotPlayer(rc);// it will probably get fixed if we give it its own class. because the RobotPlayer has no constructor.
        //rb.buildCount = 0;
        //System.out.println(rb.buildCount + " TEST");
        when(rb.tryBuild(RobotType.LANDSCAPER, Direction.NORTH)).thenReturn(true);// Do tryBuild() first so you can copy from there.
        rb.runDesignSchool();// NULL pointer error?
        assertEquals(1, rb.buildCount);
    }

    @Test
    public void testTryBuild() throws GameActionException {
        RobotPlayer rb = Mockito.mock(RobotPlayer.class);
        RobotController rc = Mockito.mock(RobotController.class);
        when(rb.rc).thenReturn(rc);// error: RobotController$$EnhancerByMockitoWithCGLIB$$304e0d94 cannot be returned by shoot() shoot() should return int
        when(rb.rc.isReady()).thenReturn(true);
        when(rb.rc.canBuildRobot(RobotType.LANDSCAPER, Direction.NORTH)).thenReturn(true);
        assertTrue("tryBuild builds", rb.tryBuild(RobotType.LANDSCAPER, Direction.NORTH));
    }*/
}