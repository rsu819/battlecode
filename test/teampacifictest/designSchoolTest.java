package teampacifictest;

import TeamPacific.DesignSchool;
import TeamPacific.RobotPlayer;
import TeamPacific.Robot;
import battlecode.common.*;

import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class designSchoolTest {
    static DesignSchool ds;
    static RobotPlayer rp;
    static Robot rb;
    static RobotController rc;
    static Direction dir;

    @Before
    public void setupDesignSchoolTest() throws GameActionException {
        rc = mock(RobotController.class);
        rp = new RobotPlayer();
        rb = new DesignSchool(rc);
        ds = new DesignSchool(rc);

        dir = Direction.NORTH;
    }

    @Test
    public void testDesignSchoolConstructor() throws GameActionException {
        assertNotNull("Design School runs. ", ds);
    }

    @Test
    public void testRunDesignSchool() throws GameActionException {
        when(ds.tryBuild(RobotType.LANDSCAPER, dir)).thenReturn(true);
        when(rb.tryBuild(RobotType.LANDSCAPER, dir)).thenReturn(true);
        ds.run(1);

        assertTrue("Design School run builds. ", ds.buildCountD > 0);
    }
}