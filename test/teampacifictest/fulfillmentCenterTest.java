package teampacifictest;

import TeamPacific.FulfillmentCenter;
import TeamPacific.RobotPlayer;
import TeamPacific.Robot;
import battlecode.common.*;

import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.junit.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class fulfillmentCenterTest {
    static FulfillmentCenter fc;
    static RobotPlayer rp;
    static Robot rb;
    static RobotController rc;
    static Direction dir;

    @Before
    public void setupFulfillmentCenterTest() throws GameActionException {
        rc = mock(RobotController.class);
        rp = new RobotPlayer();
        rb = new FulfillmentCenter(rc);
        fc = new FulfillmentCenter(rc);

        dir = Direction.NORTH;
    }

    @Test
    public void testFulfillmentCenterConstructor() throws GameActionException {
        assertNotNull("Fulfillment Center runs. ", fc);
    }

    @Test
    public void testRunFulfillmentCenter() throws GameActionException {
        when(fc.tryBuild(RobotType.DELIVERY_DRONE, dir)).thenReturn(true);
        when(rb.tryBuild(RobotType.DELIVERY_DRONE, dir)).thenReturn(true);
        fc.run(1);

        assertTrue("Fulfillment Center run builds. ", fc.buildCountF > 0);
    }
}