// Running Electronics REBL600FR ( http://www.runele.com/ca1/24/ )
// の初期ファームウェアである。iBeaconのパラメータを変更するアプリケーション
// Programed by Kazuyuki Eguchi
// Copyright(c) Kazuyuki Eguchi

package jp.eguchi.android.beaconsetting2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity
{
	// private String TAG = "BEACONCONT";
	
    // 初期パラメータ
    private String mUUID =  "CDAD3F7F82914B078F1FB36B3A27AD89";
    private String mMajor = "FFFF";
    private String mMinor = "0000";
    private String mPower = "04";
    
    // BL600 iBeaconファームウェアのUUID関連
    private static final String SERVICE_UUID = "569a1900-b87f-490c-92cb-11ba5ea5167c";
    private static final String C_UUID_UUID =  "569a2012-b87f-490c-92cb-11ba5ea5167c";
	private static final String C_MAJOR_UUID = "569a2013-b87f-490c-92cb-11ba5ea5167c";
	private static final String C_MINOR_UUID = "569a2014-b87f-490c-92cb-11ba5ea5167c";
	private static final String C_POWER_UUID = "00002a07-0000-1000-8000-00805f9b34fb";

    private static final long SCAN_PERIOD = 5000;
    
	private Handler mHandler = null;
    private Context mContext = null;
	
	private BluetoothManager bm = null;
	private BluetoothAdapter ba = null;
    private BluetoothDevice bd = null;
    private List<BluetoothGattCharacteristic> mCharacteristics = null;
    
    private Spinner mSpinner = null;
    private Spinner mSpinner2 = null;
    private EditText mText01 = null;
    private EditText mText02 = null;
    private EditText mText03 = null;
    private TextView mMes = null;
    
    private ProgressDialog mDialog = null;
    
    private ArrayList<String> array = new ArrayList<String>();
    
	private boolean mConnected = false;
    private boolean bWrite = false;
    
    private int mPos = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mHandler = new Handler();
		mContext = this;

		setContentView(R.layout.fragment_main);
		
		mSpinner = (Spinner)findViewById(R.id.spinner01);
		mSpinner2 = (Spinner)findViewById(R.id.spinner02);

		ArrayList<String> array2 = new ArrayList<String>();
		
		array2.add("4");
		array2.add("0");
		array2.add("-4");
		array2.add("-8");
		array2.add("-12");
		array2.add("-16");
		array2.add("-20");
		array2.add("-30");
		array2.add("-55");
		
        // String[]に変換する
        String[] lists2 = (String[]) array2.toArray(new String[]{});
        
        // Spinnerを作る
        setSpinner(mSpinner2,lists2);
		
		mMes = (TextView)findViewById(R.id.mes);

		mText01 = (EditText)findViewById(R.id.text01);
		mText01.setText(mUUID);
		
		mText02 = (EditText)findViewById(R.id.text02);
		mText02.setText(mMajor);
		
		mText03 = (EditText)findViewById(R.id.text03);
		mText03.setText(mMinor);
		
		Button btn01 = (Button)findViewById(R.id.button01);
		
		btn01.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mConnected == false)
				{
					scanLeDevice(true);
				}
			}
		});

		Button btn02 = (Button)findViewById(R.id.button02);
		
		btn02.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mSpinner.getSelectedItem() != null)
				{
					// Log.d(TAG,mSpinner.getSelectedItem().toString());
					
					if(mConnected == false)
					{
						connect(mSpinner.getSelectedItem().toString());
					}
					else
					{
						Toast.makeText(mContext, "前の工程が終わっていません。", Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		
		Button btn03 = (Button)findViewById(R.id.button03);
		
		btn03.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mSpinner.getSelectedItem() != null)
				{
					if(mConnected == false)
					{
						// Log.d(TAG,mSpinner.getSelectedItem().toString());
						connect2(mSpinner.getSelectedItem().toString());
					}
					else
					{
						Toast.makeText(mContext, "前の工程が終わっていません。", Toast.LENGTH_LONG).show();
					}
				}
			}
		});
		
		bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        ba = bm.getAdapter();
	}

	private void scanLeDevice(final boolean enable)
	{
		 if (enable)
		 {
			 mHandler.postDelayed(new Runnable()
			 {
				 @Override
				 public void run()
				 {
	                 ba.stopLeScan(mLeScanCallback);
	                 
	                 mDialog.dismiss();
	                 
	                 // 重複を取り除く
	                 HashSet<String> hashSet = new HashSet<String>();
	                 hashSet.addAll(array);
	                 
	                 // String[]に変換する
	                 String[] lists = (String[]) hashSet.toArray(new String[]{});
	                 
	                 // Spinnerを作る
	                 setSpinner(mSpinner,lists);
	             }
	         }, SCAN_PERIOD);

	         if(ba.startLeScan(mLeScanCallback) == true)
	         {
		         mDialog = new ProgressDialog(mContext);
				 mDialog.setMessage("ビーコンを検索中 ...");
				 mDialog.setCancelable(false);
				 mDialog.show();
	         }
	     }
		 else
		 {
	         ba.stopLeScan(mLeScanCallback);
	     }
	}
	
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord)
		{
			if(scanRecord.length > 30)
			{
				// iBeacon互換信号でフィルターする
			    if((scanRecord[5] == (byte)0x4c) && (scanRecord[6] == (byte)0x00) &&
			       (scanRecord[7] == (byte)0x02) && (scanRecord[8] == (byte)0x15))
			    {
			    	// その後はデバイス名でフィルターする
			    	if(device.getName().compareTo("Laird iBeacon") == 0)
			    	{
			    		array.add(device.getAddress().toString());
			    	}
			    }
			}
		}
	};
	
	private void setSpinner(Spinner spinner,String[] lists)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lists);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}
	
	private void connect(String addr)
	{
		bWrite = false;
		
		if(addr != null)
		{
			bd = ba.getRemoteDevice(addr);
			
			if(bd != null)
			{
				bd.connectGatt(getApplicationContext(), false, mGattCallback);
			}
		}
	}
	
	private void connect2(String addr)
	{
		bWrite = true;
		
		if(addr != null)
		{
			bd = ba.getRemoteDevice(addr);
			
			if(bd != null)
			{
				bd.connectGatt(getApplicationContext(), false, mGattCallback);
			}
		}
	}
	
	private BluetoothGattCallback mGattCallback = new BluetoothGattCallback()
	{
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status)
		{
			// Log.d(TAG,"onServicesDiscovered");
			List<BluetoothGattService> lists = gatt.getServices();
			
			for(int i = 0 ; i < lists.size() ; i++)
			{
				BluetoothGattService service = lists.get(i);
				
				// Log.d(TAG,service.getUuid().toString());
				
				if(service.getUuid().compareTo(UUID.fromString(SERVICE_UUID)) == 0)
				{
					mCharacteristics = service.getCharacteristics();
					
					for(int j = 0 ; j < mCharacteristics.size() ; j++)
					{
						BluetoothGattCharacteristic characteristic = mCharacteristics.get(j);
						// Log.d(TAG,characteristic.getUuid().toString());
					}
				}
			}

			mPos = 0;
			
			if(bWrite == false)
			{
				mMajor = null;
				mMinor = null;
				mUUID = null;
				mPower = null;

				ReadParameter(gatt);
			}
			else
			{
				mUUID = mText01.getText().toString();
				mMajor = mText02.getText().toString();
				mMinor = mText03.getText().toString();
				String tmp = mSpinner2.getSelectedItem().toString();
				
				if(tmp == "4")
				{
					mPower = "04";
				}
				else if(tmp == "0")
				{
					mPower = "00";
				}
				else if(tmp == "-4")
				{
					mPower = "FC";
				}
				else if(tmp == "-8")
				{
					mPower = "F8";
				}
				else if(tmp == "-12")
				{
					mPower = "F0";
				}
				else if(tmp == "-16")
				{
					mPower = "F0";
				}
				else if(tmp == "-20")
				{
					mPower = "EC";
				}
				else if(tmp == "-30")
				{
					mPower = "E2";
				}
				else if(tmp == "-55")
				{
					mPower = "C9";
				}
				else
				{
					mPower = "04";
				}
				
				WriteParameter(gatt);
			}
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,int newState)
		{
			switch(newState)
			{
				case BluetoothProfile.STATE_CONNECTED:
					// Log.d(TAG,"onConnectionStateChange STATE_CONNECTED");
					mConnected = true;
					// Log.d(TAG,"discoverServices()");
					if(gatt.discoverServices() == false)
					{
						// Log.d(TAG,"failed");
					}
					break;
					
				case BluetoothProfile.STATE_DISCONNECTED:
					// Log.d(TAG,"onConnectionStateChange STATE_DISCONNECTED");
					mConnected = false;
					break;
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status)
		{
			// Log.d(TAG,"onCharacteristicRead");
			
			byte [] tmp = characteristic.getValue();

			if(characteristic.getUuid().compareTo(UUID.fromString(C_MAJOR_UUID)) == 0)
			{
				mMajor = asHex(tmp);
			}
			
			if(characteristic.getUuid().compareTo(UUID.fromString(C_MINOR_UUID)) == 0)
			{
				mMinor = asHex(tmp);
			}
			
			if(characteristic.getUuid().compareTo(UUID.fromString(C_UUID_UUID)) == 0)
			{
				mUUID = asHex(tmp);
			}

			if(characteristic.getUuid().compareTo(UUID.fromString(C_POWER_UUID)) == 0)
			{
				mPower = asHex(tmp);
			}

			mPos++;
			
			ReadParameter(gatt);
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt,BluetoothGattCharacteristic characteristic, int status) 
		{
			// Log.d(TAG,"onCharacteristicWrite");
			
			mPos++;
			WriteParameter(gatt);
		}
	};
	
	private void ReadParameter(BluetoothGatt gatt)
	{
		if(mPos < mCharacteristics.size())
		{
			BluetoothGattCharacteristic characteristics = mCharacteristics.get(mPos);
			gatt.readCharacteristic(characteristics);
		}
		else
		{
			// Log.d(TAG,"UUID: " + mUUID);
			// Log.d(TAG,"MAJOR: " + mMajor + ", MINIR: " + mMinor);
			// Log.d(TAG,"Power: " + mPower);
			
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					mMes.setText("UUID="+ mUUID + " ,MAJOR=" + mMajor + " ,MINOR" + mMinor + ",Power=" + mPower);
				}
			});

			gatt.disconnect();
		}
	}
	
	private void WriteParameter(BluetoothGatt gatt)
	{
		// Log.d(TAG,"WriteParameter");
		
		if(mPos < mCharacteristics.size())
		{
			BluetoothGattCharacteristic characteristic = mCharacteristics.get(mPos);
			
			byte[] value = null;
			
			if(characteristic.getUuid().compareTo(UUID.fromString(C_MAJOR_UUID)) == 0)
			{
				// Log.d(TAG,"C_MAJOR_UUID");
				value = asByteArray(mMajor);
			}
			else if(characteristic.getUuid().compareTo(UUID.fromString(C_MINOR_UUID)) == 0)
			{
				// Log.d(TAG,"C_MINOR_UUID");
				value = asByteArray(mMinor);
			}
			else if(characteristic.getUuid().compareTo(UUID.fromString(C_UUID_UUID)) == 0)
			{
				// Log.d(TAG,"C_UUID_UUID");
				value = asByteArray(mUUID);
			}
			else if(characteristic.getUuid().compareTo(UUID.fromString(C_POWER_UUID)) == 0)
			{
				// Log.d(TAG,"C_POWER_UUID");
				value = asByteArray(mPower);
			}
			
			if(value != null)
			{
				characteristic.setValue(value);
			
				if(gatt.writeCharacteristic(characteristic) != true)
				{
					// Log.d(TAG,"Error writeCharacteristic");
				}
				
				return;
			}
			
			mPos++;
			WriteParameter(gatt);
		}
		else
		{
			mHandler.post(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(mContext, "書き込み完了!!!", Toast.LENGTH_LONG).show();
				}
			});
			
			gatt.disconnect();
		}
	}
	
	private static String asHex(byte bytes[])
	{
		StringBuffer strbuf = new StringBuffer(bytes.length * 2);

		for (int index = 0; index < bytes.length; index++)
		{
			int bt = bytes[index] & 0xff;

			if (bt < 0x10)
			{
				strbuf.append("0");
			}

			strbuf.append(Integer.toHexString(bt).toUpperCase());
		}

		return strbuf.toString();
	}
	
	private static byte[] asByteArray(String hex)
	{
		byte[] bytes = new byte[hex.length() / 2];

		for (int index = 0; index < bytes.length; index++)
		{
			bytes[index] = (byte) Integer.parseInt(hex.substring(index * 2, (index + 1) * 2),16);
		}

		return bytes;
	}
}
