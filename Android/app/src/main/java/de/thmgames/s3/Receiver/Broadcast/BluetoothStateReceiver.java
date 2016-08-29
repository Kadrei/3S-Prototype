package de.thmgames.s3.Receiver.Broadcast;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Benedikt on 15.12.2014.
 */
public class BluetoothStateReceiver extends BroadcastReceiver {

    public final static String TAG = BluetoothStateChangeListener.class.getName();
    public interface BluetoothStateChangeListener{
        public void onTurningOff();
        public void onTurningOn();
        public void onOn();
        public void onOff();
    }

    private BluetoothStateChangeListener mListener;
    public void setBluetoothStateChangeListener(BluetoothStateChangeListener listener){
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(mListener == null) return;
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    mListener.onOff();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    mListener.onTurningOff();
                    break;
                case BluetoothAdapter.STATE_ON:
                    mListener.onOn();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    mListener.onTurningOn();
                    break;
            }
        }
    }
}
