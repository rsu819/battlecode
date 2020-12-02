package teampacifictest;

import TeamPacific.Refinery;
import battlecode.common.*;

import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class refineryTest {
    static Refinery ref;
    static RobotController rc;

    @Before
    public void setupRefineryTest() throws GameActionException {
        rc = mock(RobotController.class);
        ref = new Refinery(rc);
    }

    @Test
    public void testRefineryConstructor() throws GameActionException {
        assertNotNull("Refinery runs. ", ref);
    }

    @Test
    public void testRunRefinery() throws GameActionException {
        ref.run(1);

        assertNotNull("Refinery run builds. ", ref);
    }
}