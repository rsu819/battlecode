package teampacifictest;

import TeamPacific.Blockchain;
import TeamPacific.HQ;
import TeamPacific.RobotPlayer;
import TeamPacific.Vaporator;
import battlecode.common.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import static TeamPacific.HQ.*;
import static TeamPacific.Blockchain.*;
import static TeamPacific.RobotPlayer.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class blockchainTest {
    static Vaporator vp;
    static RobotController rc;

    @BeforeClass
    public static void setupBlockchainTest() throws GameActionException {
        rc = mock(RobotController.class);
        vp = new Vaporator(rc);
    }

    public boolean checkMsgEquality(int[] expected, int[] actual) {
        int i;
        for (i = 0; i < 7; i++) {
            Assert.assertEquals(expected[i], actual[i]);
        }
        if (i == 7) {return true;}
        return false;
    }

    /**************
     BEGIN TESTS
     *************/

    @Test
    public void testBlockchainConstructor() throws GameActionException {
        assertEquals("TeamId = ", 603720, TeamId);
    }

    @Test
    public void testMessageToConstructor() throws GameActionException {
        boolean allTrue = false;

        if(Blockchain.MessageTo.Miner == 1 && Blockchain.MessageTo.Landscaper == 2 && Blockchain.MessageTo.Drone == 3 && Blockchain.MessageTo.Any == 4){
            allTrue = true;
        }
        assertTrue("MessageTo works: ", allTrue);
    }

    @Test
    public void testReadBlockchainFail() throws GameActionException {
        Transaction[] txns = null;
        int[] returnValue = Blockchain.readBlockchain(txns, Blockchain.MessageTo.Any);

        assertNull("readBlockchain Fails: ", returnValue);
    }

    @Test
    public void testEmitEnemyHq() throws GameActionException {
        MapLocation enemy = new MapLocation(1, 1);
        int[] message;
        int cost = 10;

        message = foundResourceMessage(enemy, MessageTo.Drone, Resource.EnemyHQ, false);
        when(rc.canSubmitTransaction(message, cost)).thenReturn(Boolean.TRUE);

        boolean returnValue = Blockchain.emitEnemyHq(enemy);

        assertTrue("emitEnemyHq succeeds: " + returnValue, returnValue);
    }

    @Test
    public void testEmitEnemyHqFail() throws GameActionException {
        MapLocation enemy = new MapLocation(1, 1);
        int[] oldMessage = {TeamId, 1, 1, 1, 1, 0, 0};
        int cost = 10;

        when(rc.canSubmitTransaction(oldMessage, cost)).thenReturn(Boolean.FALSE);

        boolean returnValue = Blockchain.emitEnemyHq(enemy);

        assertFalse("emitEnemyHq Fails: " + returnValue, returnValue);
    }
}