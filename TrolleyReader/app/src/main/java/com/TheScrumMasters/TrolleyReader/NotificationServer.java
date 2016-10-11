package com.TheScrumMasters.TrolleyReader;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.TheScrumMasters.TrolleyReader.UtilityClasses.Bay;
import com.TheScrumMasters.TrolleyReader.UtilityClasses.ISMSHandler;
import com.TheScrumMasters.TrolleyReader.UtilityClasses.PermissionHandler;
import com.TheScrumMasters.TrolleyReader.UtilityClasses.SMSHandler;
import com.TheScrumMasters.TrolleyReader.UtilityClasses.SMSNotification;

import java.util.HashMap;

public class NotificationServer extends AppCompatActivity implements ISMSHandler
{

    Spinner bayChooser;
    /**
     * This field is the percent of capacity the bays reach before alerting staff
     */
    private final int LOW_BAY = Color.YELLOW;
    private final int NORMAL_BAY = Color.GREEN;


    SMSHandler smsHandler;

    PermissionHandler permissionHandler;
    final int INITIAL_REQUEST_CODE = 0;

    private HashMap<Bay, ProgressBar> bays;
    private HashMap<Bay, TextView> bayTextProgress;//The text progress for bays.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_server);

        smsHandler = new SMSHandler(this,this);

        permissionHandler = new PermissionHandler(this);
        permissionHandler.askPermission(this, permissionHandler.getAllPermissions(), INITIAL_REQUEST_CODE);
        boolean permissionGranted = permissionHandler.getIfPermissionGranted(PermissionHandler.Permissions.SENDSMS);
        if (!permissionGranted)
        {
            Toast.makeText(this,"Can't continue without permissions", Toast.LENGTH_LONG);
            finish();
        }

        bayChooser = ((Spinner) findViewById(R.id.Bay_Spinner));

        //sorry this stuff is all hardcoded.
        Bay bay0 = new Bay(20,20,0.75,0);
        Bay bay1 = new Bay(20,20,0.50,1);
        Bay bay2 = new Bay(20,20,0.20,2);

        ProgressBar progressBar_bay0 = ((ProgressBar) findViewById(R.id.Bay0_ProgressBar));
        ProgressBar progressBar_bay1 = ((ProgressBar) findViewById(R.id.Bay1_ProgressBar));
        ProgressBar progressBar_bay2 = ((ProgressBar) findViewById(R.id.Bay2_ProgressBar));

        TextView bay0Text = ((TextView) findViewById(R.id.Bay0_Progress));
        TextView bay1Text = ((TextView) findViewById(R.id.Bay1_Progress));
        TextView bay2Text = ((TextView) findViewById(R.id.Bay2_Progress));

        Bay[] bayObjects = new Bay[] {bay0, bay1, bay2};

        ProgressBar[] baysArray = new ProgressBar[] {progressBar_bay0,progressBar_bay1,progressBar_bay2};
        TextView[] textViews = new TextView[] {bay0Text,bay1Text,bay2Text};

        String[] baysStringArray = new String[] {"Bay 1", "Bay 2", "Bay 3"};

        bays = new HashMap<>(bayObjects.length);
        bayTextProgress = new HashMap<>(bayObjects.length);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, baysStringArray);
        bayChooser.setAdapter(adapter);

        for(int i=0;i<baysArray.length;i++)
        {
            int max = bayObjects[i].getCapacity();
            int progress = bayObjects[i].getValue();
            baysArray[i].setMax(max);
            baysArray[i].setProgress(progress);

            bays.put(bayObjects[i], baysArray[i]);
            updateBayColours(bayObjects[i]);

            bayTextProgress.put(bayObjects[i],textViews[i]);
            updateProgressText(bayObjects[i]);
        }

    }
    private void updateBayColours(Bay bay)
    {
        ProgressBar bar = bays.get(bay);
        int colour = bay.isLow() ? LOW_BAY : NORMAL_BAY;
        bar.setProgressTintList(ColorStateList.valueOf(colour));
    }

    private void updateProgressText(Bay bay)
    {
        TextView textView = bayTextProgress.get(bay);
        String text = bay.getValue() + "/" + bay.getCapacity();
        textView.setText(text);
    }

    public void addTrolley_onClick(View v)
    {
        int chosenBay = bayChooser.getSelectedItemPosition();
        Bay bay = getBayById(chosenBay);
        ProgressBar progressBar_bay = bays.get(bay);
        if (bay == null)
        {
            Toast.makeText(this, "Bay not selected",Toast.LENGTH_SHORT).show();
            return;
        }
        int newCapacity = bay.getValue() + 1;
        if (newCapacity > bay.getCapacity())
        {
            Toast.makeText(this, "already at max",Toast.LENGTH_SHORT).show();
            return;
        }
        bay.setValue(newCapacity);
        progressBar_bay.setProgress(newCapacity);
        updateProgressText(bay);
        checkForLowBays(bay, chosenBay);
    }

    public void removeTrolley_onClick(View v)
    {
        int chosenBay = bayChooser.getSelectedItemPosition();
        Bay bay = getBayById(chosenBay);
        ProgressBar progressBar_bay = bays.get(bay);
        if (bay == null)
        {
            Toast.makeText(this, "Bay not selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int newCapacity = bay.getValue() - 1;
        if (newCapacity < 0)
        {
            Toast.makeText(this, "already at min", Toast.LENGTH_SHORT).show();
            return;
        }
        bay.setValue(newCapacity);
        progressBar_bay.setProgress(newCapacity);
        updateProgressText(bay);
        checkForLowBays(bay, chosenBay);
    }

    private Bay getBayById(int id)
    {
        for (Bay bay : bays.keySet())
        {
            if (bay.getId() == id)
            {
                return bay;
            }
        }
        return null;
    }


    private void checkForLowBays(Bay bay, int bayPosition)
    {
        if (bay.isLow())
        {
            final String message = SMSNotification.createJSONMessage("1","A Bay is running out of trolleys, would you like to accept a request to fix this?", "Bay" + bayPosition);
            Toast.makeText(this, "Low bay detected sending message", Toast.LENGTH_LONG).show();
            System.out.println(message);
            try
            {
                final String[] staffNumbers = getStaffNumbers();
                if (staffNumbers == null || staffNumbers.length == 0)
                {
                    return;
                }
                smsHandler.sendMessage(staffNumbers[0], message);

            }
            catch (IllegalArgumentException e)
            {
                Toast.makeText(this, "Couldn't send message", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        updateBayColours(bay);
    }

    private String[] getStaffNumbers()
    {
        EditText phoneNumberText = ((EditText) findViewById(R.id.phoneNumber_EditText));
        String phoneNumbers = phoneNumberText.getText().toString();
        if (phoneNumbers.isEmpty())
        {
            Toast.makeText(this, "Phone numbers are empty, please enter atleast 1", Toast.LENGTH_LONG);
            return null;
        }
        //System.out.println("Phone numbers: ");
        //System.out.println(phoneNumbers);
        String[] phoneNumberArray = phoneNumbers.split("/\n/g");
        for (String str : phoneNumberArray)
            System.out.println(str);

        return phoneNumberArray;
    }

    @Override
    protected void onDestroy()
    {
        smsHandler.destroy();
        super.onDestroy();
    }

    @Override
    public void SMSReceived(String message)
    {
        Toast.makeText(this, "message received", Toast.LENGTH_LONG).show();
    }
}
