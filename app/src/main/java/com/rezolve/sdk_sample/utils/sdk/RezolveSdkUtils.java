package com.rezolve.sdk_sample.utils.sdk;

import android.content.Context;
import androidx.annotation.NonNull;

import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.RezolveSession;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.core.managers.ProductManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk_sample.R;

public class RezolveSdkUtils {

    @NonNull
    public static RezolveSDK getRezolveSdk() {
        RezolveSDK result = RezolveSDK.peekInstance();
        if (RezolveSDK.peekInstance() == null) {
            throw new IllegalStateException("You need to initialize the RezolveSDK using `new RezolveSDK.Builder().build()` method!");
        }
        return result;
    }

    @NonNull
    public static RezolveSession getRezolveSession() {
        RezolveSession result = getRezolveSdk().getRezolveSession();
        if (result == null) {
            throw new IllegalStateException("You need to create the Rezolve's session using `getRezolveSdk().createSession(...)` method!");
        }
        return result;
    }

    // Managers

    public static MerchantManager getMerchantManager() {
        return getRezolveSession().getMerchantManager();
    }

    public static ProductManager getProductManager() {
        return getRezolveSession().getProductManager();
    }

    // Errors

    public static String formatedRezolveError(Context context, RezolveError rezolveError) {
        return String.format(
                context.getString(R.string.rezolve_error_format),
                rezolveError.getErrorType().toString(),
                rezolveError.getErrorMessage().toString(),
                rezolveError.getMessage() == null ? "NULL" : rezolveError.getMessage()
        );
    }

    public static RezolveError createCustomRezolveError(String message) {
        return new RezolveError(RezolveError.RezolveErrorType.CUSTOM, RezolveError.RezolveErrorMessage.CUSTOM, message);
    }

    public static RezolveError createMissingMerchantManagerError() {
        return createCustomRezolveError("Missing Merchant Manager");
    }

    public static RezolveError createMissingProductManagerError() {
        return createCustomRezolveError("Missing Product Manager");
    }

    //
    // Interfaces
    //

    public interface ProcessingInterface {
        void processingStarted();

        void processingFinished();

        void onRezolveError(@NonNull RezolveError rezolveError);
    }
}
