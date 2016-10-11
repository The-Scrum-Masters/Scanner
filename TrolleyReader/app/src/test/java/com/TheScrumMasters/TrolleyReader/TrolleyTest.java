package com.TheScrumMasters.TrolleyReader;

import com.TheScrumMasters.TrolleyReader.UtilityClasses.Trolley;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by ryan on 11/10/16.
 */

public class TrolleyTest {
    Trolley trolley;
    String ID = "testTrolley";
    @Before
    public void init()
    {
        trolley = new Trolley(ID);
    }

    @Test
    public void IDMatches()
    {
        assertEquals(trolley.toJSON().contains(ID), true);
    }

}
