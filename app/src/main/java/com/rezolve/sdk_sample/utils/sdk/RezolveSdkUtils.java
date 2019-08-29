package com.rezolve.sdk_sample.utils.sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rezolve.sdk.RezolveSDK;
import com.rezolve.sdk.core.managers.MerchantManager;
import com.rezolve.sdk.core.managers.ProductManager;
import com.rezolve.sdk.model.network.RezolveError;
import com.rezolve.sdk_sample.R;

public class RezolveSdkUtils {

    // Managers

    public static MerchantManager getMerchantManager(@Nullable RezolveSDK rezolveSdk) {
        return rezolveSdk == null ? null : rezolveSdk.getRezolveSession() == null ? null : rezolveSdk.getRezolveSession().getMerchantManager();
    }

    public static ProductManager getProductManager(@Nullable RezolveSDK rezolveSdk) {
        return rezolveSdk == null ? null : rezolveSdk.getRezolveSession() == null ? null : rezolveSdk.getRezolveSession().getProductManager();
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
