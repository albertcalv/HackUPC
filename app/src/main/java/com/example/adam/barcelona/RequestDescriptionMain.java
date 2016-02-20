package com.example.adam.barcelona;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.kontakt.sdk.android.common.model.IDevice;
import com.kontakt.sdk.android.http.HttpResult;
import com.kontakt.sdk.android.http.KontaktApiClient;
import com.kontakt.sdk.android.http.RequestDescription;
import com.kontakt.sdk.android.http.exception.ClientException;
import com.kontakt.sdk.android.http.interfaces.ResultApiCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by adam on 2/20/16.
 */
public class RequestDescriptionMain extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private List<IDevice> allDevices;

    private KontaktApiClient kontaktApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allDevices = new ArrayList<>();
        kontaktApiClient = new KontaktApiClient();
        fetchDevices(0);
    }


    private void fetchDevices(int offset) {
        RequestDescription requestDescription = RequestDescription.start()
                .setStartIndex(offset)
                .build();

        kontaktApiClient.listDevices(requestDescription, new ResultApiCallback<List<IDevice>>() {
            @Override
            public void onSuccess(HttpResult<List<IDevice>> result) {
                if (result.isPresent()) {
                    allDevices.addAll(result.get());
                    int offset = result.getSearchMeta().getOffset();
                    int startIndex = result.getSearchMeta().getStartIndex();
                    if (result.getSearchMeta().hasNextResultsURI()) {
                        fetchDevices(startIndex + offset);
                    } else {
                        Log.d(TAG, "fetched devices count=" + allDevices.size());
                    }
                }
            }

            @Override
            public void onFailure(ClientException e) {
                e.printStackTrace();
            }
        });
    }
}
