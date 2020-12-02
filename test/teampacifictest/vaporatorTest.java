package teampacifictest;

import TeamPacific.Vaporator;
import battlecode.common.*;

import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class vaporatorTest {
    static Vaporator vap;
    static RobotController rc;

    @Before
    public void setupVaporatorTest() throws GameActionException {
        rc = mock(RobotController.class);
        vap = new Vaporator(rc);
    }

    @Test
    public void testVaporatorConstructor() throws GameActionException {
        assertNotNull("Vaporator runs. ", vap);
    }

    @Test
    public void testRunVaporator() throws GameActionException {
        vap.run(1);

        assertNotNull("Vaporator run builds. ", vap);
    }
}