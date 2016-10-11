package com.TheScrumMasters.TrolleyReader.UtilityClasses;

/**
 * Created by ryan on 30/08/16.
 */
public class Trolley
{
    private String TrolleyID;

    public Trolley(String ID)
    {
        TrolleyID = ID;
    }

    public String toJSON()
    {
        //String str = "{" + "TrolleyID:" + TrolleyID + ",CentreID:" + ShoppingCentreID + "}";
        String str = TrolleyID;
        return str;
    }
}
