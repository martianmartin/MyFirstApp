package com.example.marzipan.myfirstapp;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

// Main Activity for disovering local peers
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class FindOpponentActivity extends AppCompatActivity {

    // Intent filter will be used for processing Android WiFi P2P framework intents
    IntentFilter intentFilter = new IntentFilter();
    // Channel objects is returned by WifiP2pManager initialize() method
    WifiP2pManager.Channel mChannel;
    // Main API objects for accessing Wifi framework
    WifiP2pManager mManager;
    // receiver defines behavior upon android wifi framwork issuing wifi intents
    WiFiDirectBroadcastReceiver receiver;
    TextView textView;
    // Used to store updated list of discovered wifi peers
    private List peers = new ArrayList();
    String hostAddress;
    ServerSocket serverSocket;
    String tcp_input;
    Boolean tcp_accepted = false;
    Boolean tcp_started = false;
    WifiP2pInfo thisConnInfo;
    // peerListListener is used as argument to mManager.requestPeers, defines what to do with updated peerlist
    private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers.clear();
            peers.addAll(peerList.getDeviceList());

        }
    };

    // called when a new peer is available to connect to
    public void connect() {
        // Picking the first device found on the network.
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
                textView.append("Connected!");

                mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {

                    public void onConnectionInfoAvailable(WifiP2pInfo info) {
                        thisConnInfo = info;
                        if (info.groupOwnerAddress != null) {
                            textView.append("go: " + info.groupOwnerAddress.getHostAddress());
                            if (info.isGroupOwner) textView.append("_is owner_");
                            else textView.append("_is not owner_");
                            hostAddress = info.groupOwnerAddress.getHostAddress();
                        } else {
                            textView.append("no group info yet...");
                        }
                    }

                    ;
                });

            }

            @Override
            public void onFailure(int reason) {
                textView.append("failed to connect!");
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_opponent);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        textView = new TextView(this);
        textView.setTextSize(10);
        textView.setText("Searching for nearby opponents...");
        LinearLayout layout = (LinearLayout) findViewById(R.id.find_ops_content);
        layout.addView(textView);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        // This must be the first WiFi p2p method called before any other operations
        mChannel = mManager.initialize(this, getMainLooper(), null);
        // handles android wifi intents
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

        // Initiates Wifi scanning, remain active until connection requested or group formed
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // Code for when the discovery initiation is successful goes here.
                // No services have actually been discovered yet, so this method
                // can often be left blank.  Code for peer discovery goes in the
                // onReceive method, detailed below.
                textView.append("initialized P2P...");
            }

            @Override
            public void onFailure(int reasonCode) {
                // Code for when the discovery initiation fails goes here.
                // Alert the user that something went wrong.
                textView.append("failed P2P init...");
            }
        });


    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager thisManager;
        private WifiP2pManager.Channel thisChannel;
        private Context thisContext;

        public WiFiDirectBroadcastReceiver(WifiP2pManager myWifiManager, WifiP2pManager.Channel myWifiChannel, Context myContext) {
            this.thisManager = myWifiManager;
            this.thisChannel = myWifiChannel;
            this.thisContext = myContext;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            ////////////////////////////////////////////////////////////////////////////////////////
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Determine if Wifi P2P mode is enabled or not, alert
                // the Activity.
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    textView.append("P2P enabled");
                } else {
                    textView.append("P2P disabled");
                }
            }
            ////////////////////////////////////////////////////////////////////////////////////////
            else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                // Request available peers from the wifi p2p manager. This is an
                // asynchronous call and the calling activity is notified with a
                // callback on PeerListListener.onPeersAvailable()
                if (this.thisManager != null) {
                    this.thisManager.requestPeers(this.thisChannel, peerListListener);
                }

                textView.append("pc: ");
                Integer numPeers = peers.size();
                textView.append(numPeers.toString());


            }
            ////////////////////////////////////////////////////////////////////////////////////////
            else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

                // Connection state changed!  We should probably do something about
                // that.

            }
            ////////////////////////////////////////////////////////////////////////////////////////
            else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

//                DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
//                        .findFragmentById(R.id.frag_list);
//                fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
//                        WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

            }
        }
    }

    public void request_peers(View view) {
        // get a print channel properties, this should update peers
        mManager.requestPeers(mChannel, peerListListener);
        textView.append("pc: ");
        Integer numPeers = peers.size();
        textView.append(numPeers.toString());
        if (numPeers > 0) {
            WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
            textView.append("name: " + device.deviceName);
            String myIP = getDottedDecimalIP(getLocalIPAddress());
            textView.append("myIP: " + myIP);

            connect();
        }
    }

    public void start_server(View view) {

        if (!tcp_started) {
            tcp_started = true;
            Thread serverThread = new Thread() {
                @Override
                public void run() {
                    try {

                        serverSocket = new ServerSocket(6666);
                        serverSocket.setSoTimeout(0); //3 second timeout

                        Socket client = serverSocket.accept();
                        tcp_accepted = true;

//                  // should block on accept

//
                        InputStream inputStream = client.getInputStream();
//
                        java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
                        tcp_input = s.hasNext() ? s.next() : "";
//


                        serverSocket.close();
                    } catch (IOException e) {
                        //textView.append("its over");
                    }
                }


            };

            serverThread.start();
            textView.append("threadON");
        }
        textView.append("ss: "+ serverSocket.getLocalPort()+"||");

    }

    public void send_data(View view) {

        Thread clientThread = new Thread() {
            @Override
            public void run() {

                int port = 6666;//serverSocket.getLocalPort();
                Socket socket = new Socket();

        byte buf[] = new byte[1024];
        buf[0] = 0x73;
        buf[1] = 0x68;
        buf[2] = 0x69;
        buf[3] = 0x74;
        buf[4] = 0x73;
                buf[5] = 0x73;
                buf[6] = 0x73;
                buf[7] = 0x73;
//
                try {
//            /**
//             * Create a client socket with the host,
//             * port, and timeout information.
//             */
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(thisConnInfo.groupOwnerAddress.getHostAddress(), port)), 500);

            OutputStream outputStream = socket.getOutputStream();


            outputStream.write(buf, 0, 10);


            outputStream.close();
//
//
                } catch (FileNotFoundException e) {
//            //catch logic
                } catch (IOException e) {
//            //catch logic
                }
//
///**
// * Clean up any open sockets when done
// * transferring or if an exception occurred.
                finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
//                        //catch logic
                            }
                        }
                    }
                }

            }
        };
        clientThread.start();
    }

    private byte[] getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return inetAddress.getAddress();
                        }
                        //return inetAddress.getHostAddress().toString(); // Galaxy Nexus returns IPv6
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        } catch (NullPointerException ex) {
            //Log.e("AndroidNetworkAddressFactory", "getLocalIPAddress()", ex);
        }
        return null;
    }

    private String getDottedDecimalIP(byte[] ipAddr) {
        //convert to dotted decimal notation:
        String ipAddrStr = "";
        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }

    public void printTcpBuf(View view) {
        textView.append("tcp_buf: " + tcp_input + "xx");
        if (tcp_accepted)textView.append("ACCEPTED!"); else {textView.append("NOT accepted!");}
    }

}