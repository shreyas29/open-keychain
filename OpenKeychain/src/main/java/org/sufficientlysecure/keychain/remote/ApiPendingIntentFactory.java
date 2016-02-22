/*
 * Copyright (C) 2016 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.remote;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.sufficientlysecure.keychain.provider.KeychainContract;
import org.sufficientlysecure.keychain.remote.ui.RemoteBackupActivity;
import org.sufficientlysecure.keychain.remote.ui.RemoteCreateAccountActivity;
import org.sufficientlysecure.keychain.remote.ui.RemoteErrorActivity;
import org.sufficientlysecure.keychain.remote.ui.RemoteImportKeysActivity;
import org.sufficientlysecure.keychain.remote.ui.RemotePassphraseDialogActivity;
import org.sufficientlysecure.keychain.remote.ui.RemoteRegisterActivity;
import org.sufficientlysecure.keychain.remote.ui.RemoteSecurityTokenOperationActivity;
import org.sufficientlysecure.keychain.remote.ui.RemoteSelectPubKeyActivity;
import org.sufficientlysecure.keychain.remote.ui.SelectAllowedKeysActivity;
import org.sufficientlysecure.keychain.remote.ui.SelectSignKeyIdActivity;
import org.sufficientlysecure.keychain.service.input.CryptoInputParcel;
import org.sufficientlysecure.keychain.service.input.RequiredInputParcel;
import org.sufficientlysecure.keychain.ui.ViewKeyActivity;

import java.util.ArrayList;

public class ApiPendingIntentFactory {

    Context mContext;
    private Intent mPendingIntentData;

    public ApiPendingIntentFactory(Context context, Intent data) {
        mContext = context;
        mPendingIntentData = data;
    }

    PendingIntent requiredInputPi(RequiredInputParcel requiredInput,
                                  CryptoInputParcel cryptoInput) {

        switch (requiredInput.mType) {
            case NFC_MOVE_KEY_TO_CARD:
            case NFC_DECRYPT:
            case NFC_SIGN: {
                return createNfcOperationPendingIntent(requiredInput, cryptoInput);
            }

            case PASSPHRASE: {
                return createPassphrasePendingIntent(requiredInput, cryptoInput);
            }

            default:
                throw new AssertionError("Unhandled required input type!");
        }
    }

    private PendingIntent createNfcOperationPendingIntent(RequiredInputParcel requiredInput, CryptoInputParcel cryptoInput) {
        Intent intent = new Intent(mContext, RemoteSecurityTokenOperationActivity.class);
        // pass params through to activity that it can be returned again later to repeat pgp operation
        intent.putExtra(RemoteSecurityTokenOperationActivity.EXTRA_REQUIRED_INPUT, requiredInput);
        intent.putExtra(RemoteSecurityTokenOperationActivity.EXTRA_CRYPTO_INPUT, cryptoInput);

        return createInternal(intent);
    }

    private PendingIntent createPassphrasePendingIntent(RequiredInputParcel requiredInput, CryptoInputParcel cryptoInput) {
        Intent intent = new Intent(mContext, RemotePassphraseDialogActivity.class);
        // pass params through to activity that it can be returned again later to repeat pgp operation
        intent.putExtra(RemotePassphraseDialogActivity.EXTRA_REQUIRED_INPUT, requiredInput);
        intent.putExtra(RemotePassphraseDialogActivity.EXTRA_CRYPTO_INPUT, cryptoInput);

        return createInternal(intent);
    }

    PendingIntent createSelectPublicKeyPendingIntent(long[] keyIdsArray, ArrayList<String> missingEmails,
                                                     ArrayList<String> duplicateEmails, boolean noUserIdsCheck) {
        Intent intent = new Intent(mContext, RemoteSelectPubKeyActivity.class);
        intent.putExtra(RemoteSelectPubKeyActivity.EXTRA_SELECTED_MASTER_KEY_IDS, keyIdsArray);
        intent.putExtra(RemoteSelectPubKeyActivity.EXTRA_NO_USER_IDS_CHECK, noUserIdsCheck);
        intent.putExtra(RemoteSelectPubKeyActivity.EXTRA_MISSING_EMAILS, missingEmails);
        intent.putExtra(RemoteSelectPubKeyActivity.EXTRA_DUPLICATE_EMAILS, duplicateEmails);

        return createInternal(intent);
    }

    PendingIntent createImportFromKeyserverPendingIntent(long masterKeyId) {
        Intent intent = new Intent(mContext, RemoteImportKeysActivity.class);
        intent.setAction(RemoteImportKeysActivity.ACTION_IMPORT_KEY_FROM_KEYSERVER_AND_RETURN_RESULT);
        intent.putExtra(RemoteImportKeysActivity.EXTRA_KEY_ID, masterKeyId);

        return createInternal(intent);
    }

    PendingIntent createSelectAllowedKeysPendingIntent(String packageName) {
        Intent intent = new Intent(mContext, SelectAllowedKeysActivity.class);
        intent.setData(KeychainContract.ApiApps.buildByPackageNameUri(packageName));

        return createInternal(intent);
    }

    PendingIntent createShowKeyPendingIntent(long masterKeyId) {
        Intent intent = new Intent(mContext, ViewKeyActivity.class);
        intent.setData(KeychainContract.KeyRings.buildGenericKeyRingUri(masterKeyId));

        return createInternal(intent);
    }

    PendingIntent createSelectSignKeyIdPendingIntent(String packageName, String preferredUserId) {
        Intent intent = new Intent(mContext, SelectSignKeyIdActivity.class);
        intent.setData(KeychainContract.ApiApps.buildByPackageNameUri(packageName));
        intent.putExtra(SelectSignKeyIdActivity.EXTRA_USER_ID, preferredUserId);

        return createInternal(intent);
    }

    PendingIntent createBackupPendingIntent(long[] masterKeyIds, boolean backupSecret) {
        Intent intent = new Intent(mContext, RemoteBackupActivity.class);
        intent.putExtra(RemoteBackupActivity.EXTRA_MASTER_KEY_IDS, masterKeyIds);
        intent.putExtra(RemoteBackupActivity.EXTRA_SECRET, backupSecret);

        return createInternal(intent);
    }

    @Deprecated
    PendingIntent createAccountCreationPendingIntent(String packageName, String accountName) {
        Intent intent = new Intent(mContext, RemoteCreateAccountActivity.class);
        intent.putExtra(RemoteCreateAccountActivity.EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(RemoteCreateAccountActivity.EXTRA_ACC_NAME, accountName);

        return createInternal(intent);
    }

    PendingIntent createErrorPendingIntent(String errorMessage) {
        Intent intent = new Intent(mContext, RemoteErrorActivity.class);
        intent.putExtra(RemoteErrorActivity.EXTRA_ERROR_MESSAGE, errorMessage);

        return createInternal(intent);
    }

    private PendingIntent createInternal(Intent intent) {
        // re-attach "data" for pass through. It will be used later to repeat pgp operation
        intent.putExtra(RemoteSecurityTokenOperationActivity.EXTRA_DATA, mPendingIntentData);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //noinspection ResourceType, looks like lint is missing FLAG_IMMUTABLE
            return PendingIntent.getActivity(mContext, 0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(mContext, 0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
    }

    PendingIntent createRegisterPendingIntent(String packageName, byte[] packageCertificate) {
        Intent intent = new Intent(mContext, RemoteRegisterActivity.class);
        intent.putExtra(RemoteRegisterActivity.EXTRA_PACKAGE_NAME, packageName);
        intent.putExtra(RemoteRegisterActivity.EXTRA_PACKAGE_SIGNATURE, packageCertificate);
        intent.putExtra(RemoteRegisterActivity.EXTRA_DATA, mPendingIntentData);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //noinspection ResourceType, looks like lint is missing FLAG_IMMUTABLE
            return PendingIntent.getActivity(mContext, 0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT
                            | PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getActivity(mContext, 0,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }
    }

}
