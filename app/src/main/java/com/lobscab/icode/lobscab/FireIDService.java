package com.lobscab.icode.lobscab;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Icode on 6/15/2017.
 */

public class FireIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String tkn = FirebaseInstanceId.getInstance().getToken();
        //Log.d("Not","Token ["+tkn+"]");

    }
}