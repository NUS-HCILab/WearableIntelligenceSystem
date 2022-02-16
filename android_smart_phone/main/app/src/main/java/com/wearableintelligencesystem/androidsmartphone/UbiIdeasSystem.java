package com.wearableintelligencesystem.androidsmartphone;

import android.content.Context;
import android.util.Log;

import com.wearableintelligencesystem.androidsmartphone.comms.MessageTypes;
import com.wearableintelligencesystem.androidsmartphone.database.mediafile.MediaFileRepository;
import com.wearableintelligencesystem.androidsmartphone.nlp.NlpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ArrayBlockingQueue;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class UbiIdeasSystem {
    private static final String TAG = "UbiIdeasSystem_WearableAiService_WIS";

    //receive/send data stream
    PublishSubject<JSONObject> dataObservable;
    Disposable dataSub;
    Context context;

    NlpUtils nlpUtils;

    String transcriptBuffer = "";
    long timeLastSent = 0;
    long sendTimeInterval = 10000; //milliseconds we wait to send new data
    long sendWordInterval = 30; //number words we wait to send new data
    boolean sendTimeIntervalFlag = false;
    boolean sendWordIntervalFlag = true;

    UbiIdeasSystem(Context context, PublishSubject<JSONObject> dataObservable){
        this.context = context;
        timeLastSent = System.currentTimeMillis();
        nlpUtils = NlpUtils.getInstance(context);

        //receive/send data
        this.dataObservable = dataObservable;
        dataSub = this.dataObservable.subscribe(i -> handleDataStream(i));
    }

    //receive audio and send to vosk
    private void handleDataStream(JSONObject data){
        //first check if it's a type we should handle
        try{
            String type = data.getString(MessageTypes.MESSAGE_TYPE_LOCAL);
            if (type.equals(MessageTypes.FINAL_TRANSCRIPT)){
                handleFinalTranscript(data.getString(MessageTypes.TRANSCRIPT_TEXT));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void handleFinalTranscript(String transcript){
        Log.d(TAG, transcript);

        addToTranscriptBuffer(transcript);
        if (sendTimeIntervalFlag){
            long currTime = System.currentTimeMillis();
            if ((currTime - timeLastSent) > sendTimeInterval){
                sendCurrentTranscriptBuffer();
                return;
            }
        }
        if (sendWordIntervalFlag){
            if (nlpUtils.wordsInString(transcriptBuffer) > sendWordInterval){
                sendCurrentTranscriptBuffer();
                return;
            }
        }
    }

    private void sendCurrentTranscriptBuffer(){
        Log.d(TAG, transcriptBuffer);
        //get the buffer to send
        String toSendBuffer = transcriptBuffer;

        //clear the buffer
        transcriptBuffer = "";

        //send off an event so we get keywords for the previous buffer
        try{
            JSONObject toSendKeywordQuery = new JSONObject();
            toSendKeywordQuery.put(MessageTypes.MESSAGE_TYPE_LOCAL, MessageTypes.KEYWORD_QUERY);
            toSendKeywordQuery.put(MessageTypes.TEXT_TO_PROCESS, toSendBuffer);
            dataObservable.onNext(toSendKeywordQuery);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void addToTranscriptBuffer(String transcript){
        transcriptBuffer = transcriptBuffer + " " + transcript;
    }
}
