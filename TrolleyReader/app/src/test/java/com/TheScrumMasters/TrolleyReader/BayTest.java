package com.TheScrumMasters.TrolleyReader;

import com.TheScrumMasters.TrolleyReader.UtilityClasses.Bay;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class BayTest {
    Bay bay;

    @Before
    public void init()
    {
        bay = new Bay(20,20,0.25,1);
    }

    /***
     * This tests if the bay method, BayisLow can detect if the bay is low in trolleys.
     * @throws Exception
     */
    @Test
    public void BayisLow() throws Exception {
        assertEquals(bay.isLow(), false);
        int newVal = (int) (bay.getLowerThreshold() * bay.getCapacity() - 1);
        bay.setValue(newVal);
        assertEquals(bay.isLow(), true);
    }
}