package com.wearableintelligencesystem.androidsmartphone.comms;

import java.net.InetSocketAddress;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.util.Map;
import java.util.UUID;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class AspWebsocketServer extends WebSocketServer {
    //data observable we can send data through
    private static PublishSubject<JSONObject> dataObservable;
    private static Disposable dataSub;

    private static int connected = 0;

    private final String TAG = "WearableAI_AspWebsocketServer";

    private Map<String, WebSocket> clients = new ConcurrentHashMap<>();
    private WebSocket asgConn;

    public AspWebsocketServer(int port)
    {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
    }

    public AspWebsocketServer(InetSocketAddress address)
    {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connected = 2;
        String uniqueID = UUID.randomUUID().toString();
        clients.put(uniqueID, conn);
        asgConn = conn;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        Log.d(TAG, "onClose called");
        for (WebSocket connToCheck : clients.values()){ // there was a race condition where the ASG would recconnect before the socket was marked as closed, so the ASP thought their was no connection (connected = 1), but there actually was a connection, so now we check to see if any of the sockets in the clients hashmap is still open
            if (connToCheck.isOpen()){ //exit, because we have a live connection to the ASG
                Log.d(TAG, "onClose found open connection, so not setting connected=1");
                return;
            }
        }
        Log.d(TAG, "onClose set connected=1");
        connected = 1;
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JSONObject json_obj = new JSONObject(message);
            dataObservable.onNext(json_obj);
        } catch (JSONException e){
            //if we send a string, this will get thrown, all messages should be JSON or byte []
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(WebSocket conn, ByteBuffer message) {
        Log.d(TAG, "GOT MESSAGE BYTES");
    }

    @Override
    public void onError(WebSocket conn, Exception ex)
    {
        ex.printStackTrace();
        if (conn != null) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
    }

    @Override
    public void onStart()
    {
        //LogHelper.e(TAG, "Server started!");
        connected = 1;
        setConnectionLostTimeout(2);
        startConnectionLostTimer();
    }

    public void sendJson(JSONObject data){
        if (connected == 2){
            Log.d(TAG, "SENDING JSON FROM ASP WS");
            asgConn.send(data.toString());
        } else {
            Log.d(TAG, "CANNOT SEND JSON, NOT CONNECTED");
        }

    }

    //receive observable to send and receive data
    public void setObservable(PublishSubject<JSONObject> observable){
        dataObservable = observable;
        dataSub = dataObservable.subscribe(i -> handleDataStream(i));
    }

    //this receives data from the data observable. For now, this class decides what to send and what not to send to the ASG
    private void handleDataStream(JSONObject data){
        //first check if it's a type we should handle
        try{
            String type = data.getString(MessageTypes.MESSAGE_TYPE_LOCAL);
            if (type.equals(MessageTypes.INTERMEDIATE_TRANSCRIPT)){
                Log.d(TAG, "AspWebsocketServer got INTERMEDIATE_TRANSCRIPT, sending to ASG");
                //data.put(MessageTypes.MESSAGE_TYPE_LOCAL, data.getString(MessageTypes.MESSAGE_TYPE_ASG)); //change the type to the type for ASG
                //data.remove(MessageTypes.MESSAGE_TYPE_ASG);
                sendJson(data);
            } else if (type.equals(MessageTypes.FINAL_TRANSCRIPT)){
                Log.d(TAG, "AspWebsocketServer got FINAL_TRANSCRIPT, sending to ASG");
                sendJson(data);
            } else if (type.equals(MessageTypes.VOICE_COMMAND_RESPONSE)){
                Log.d(TAG, "AspWebsocketServer got VOICE_COMMAND_RESPONSE, sending to ASG");
                sendJson(data);
            } else if (type.equals(MessageTypes.FACE_SIGHTING_EVENT)){
                Log.d(TAG, "AspWebsocketServer got FACE_SIGHTING_EVENT, sending to ASG");
                sendJson(data);
            } else if (type.equals(MessageTypes.SEARCH_ENGINE_RESULT)){
                Log.d(TAG, "AspWebsocketServer got SEARCH_ENGINE_RESULT, sending to ASG");
                sendJson(data);
            } else if (type.equals(MessageTypes.ACTION_SWITCH_MODES)){
                Log.d(TAG, "AspWebsocketServer got ACTION_SWITCH_MODES, sending to ASG");
                sendJson(data);
            } else if (type.equals(MessageTypes.VISUAL_SEARCH_RESULT)){
                Log.d(TAG, "AspWebsocketServer got VISUAL_SEARCH_RESULT, sending to ASG");
                sendJson(data);
            } else if (type.equals(MessageTypes.KEYWORD_RESULT)){
                Log.d(TAG, "AspWebsocketServer got KEYWORD_RESULT, sending to ASG");
                sendJson(data);
            }
    } catch (JSONException e){
            e.printStackTrace();
        }
    }


    //need to call this so if we get "Force Stop"ped, we will clean up sockets so we can connect on restart
    public void destroy(){
        Log.d(TAG, "destroying");
        connected = 0;
        dataSub.dispose();

        try{
            stop(400);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        Log.d(TAG, "destroy complete");
    }


}
