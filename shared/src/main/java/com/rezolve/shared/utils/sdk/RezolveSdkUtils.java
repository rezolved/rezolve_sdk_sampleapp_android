package com.rezolve.shared.utils.sdk;


import androidx.annotation.NonNull;

import com.rezolve.sdk.model.network.RezolveError;

public class RezolveSdkUtils {
    //
    // Interfaces
    //
    //TODO check

    public interface ProcessingInterface {
        void processingStarted();

        void processingFinished();

        void onRezolveError(@NonNull RezolveError rezolveError);
    }
}
