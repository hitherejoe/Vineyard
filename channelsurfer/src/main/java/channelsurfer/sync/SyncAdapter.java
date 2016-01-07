package channelsurfer.sync;

/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.LongSparseArray;
import android.widget.Toast;

import channelsurfer.LibraryUtils;
import channelsurfer.TvContractUtils;
import channelsurfer.model.Channel;
import channelsurfer.model.Program;
import channelsurfer.service.TvInputProvider;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A SyncAdapter implementation which updates program info periodically.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";

    public static final String BUNDLE_KEY_INPUT_ID = "bundle_key_input_id";
    public static final long SYNC_FREQUENCY_SEC = 60 * 60 * 6;  // 6 hours
    private static final int SYNC_WINDOW_SEC = 60 * 60 * 12;  // 12 hours
    private static final int BATCH_OPERATION_COUNT = 100;
    public static final long FULL_SYNC_FREQUENCY_SEC = 60 * 60 * 24;  // daily
    private static final int FULL_SYNC_WINDOW_SEC = 60 * 60 * 24 * 14;  // 2 weeks
    private static final int SHORT_SYNC_WINDOW_SEC = 60 * 60;  // 1 hour

    private final Context mContext;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContext = context;
    }

    /**
     * Called periodically by the system in every {@code SYNC_FREQUENCY_SEC}.
     */
    private Account account;
    private Bundle extras;
    private String authority;
    private ContentProviderClient provider;
    private SyncResult syncResult;
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        this.account = account;
        this.extras = extras;
        this.authority = authority;
        this.provider = provider;
        this.syncResult = syncResult;

        Log.d(TAG, "Opened SyncAdapter");

        doLocalSync();
    }

    public void doLocalSync() {
        Log.d(TAG, "onPerformSync(" + account + ", " + authority + ", " + extras + ")");
        final String inputId = extras.getString(SyncAdapter.BUNDLE_KEY_INPUT_ID);
        if (inputId == null) {
            Log.e(TAG, "Need a valid input id");
            return;
        }
        //REFRESH CHANNEL DATA FROM SERVICE

        LibraryUtils.getTvInputProvider(mContext, new LibraryUtils.TvInputProviderCallback() {
            @Override
            public void onTvInputProviderCallback(TvInputProvider provider) {
                List<Channel> allChannels = provider.getAllChannels();
                Log.d(TAG, allChannels.toString());
                for (int i = 0; i < allChannels.size(); i++) {
                    if (allChannels.get(i).getOriginalNetworkId() == 0)
                        allChannels.get(i).setOriginalNetworkId(i + 1);
                    if (allChannels.get(i).getTransportStreamId() == 0)
                        allChannels.get(i).setTransportStreamId(i + 1);
                }
                TvContractUtils.updateChannels(getContext(), inputId, allChannels);

                LongSparseArray<Channel> channelMap = TvContractUtils.buildChannelMap(
                        mContext.getContentResolver(), inputId, allChannels);
                if (channelMap == null) {
                    Log.d(TAG, "?");
                    Handler h = new Handler(Looper.getMainLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            Toast.makeText(getContext(), "Couldn't find any channels. Uh-oh.", Toast.LENGTH_SHORT).show();
                        }
                    };
                    h.sendEmptyMessage(0);
                    //Let's not continue running
                    return;
                }
                long startMs = new Date().getTime();
                long endMs = startMs + FULL_SYNC_WINDOW_SEC * 1000;
                Log.d(TAG, "Now start to get programs");
                for (int i = 0; i < channelMap.size(); ++i) {
                    Uri channelUri = TvContract.buildChannelUri(channelMap.keyAt(i));
                    List<Program> programList = provider.getProgramsForChannel(channelUri, channelMap.valueAt(i), startMs, endMs);
                    Log.d(TAG, "For " + channelMap.valueAt(i).toString());
                    Log.d(TAG, programList.toString());
//                    updatePrograms(channelUri, programList);
                    //Let's double check programs
                    Uri programEditor = TvContract.buildProgramsUriForChannel(channelUri);
                    getContext().getContentResolver().delete(programEditor, null, null);
                    for (Program p : programList) {
                        p.setChannelId(channelMap.keyAt(i)); //Make sure you have the correct channel id value, it seems to be a foreign key
                        Uri insert = getContext().getContentResolver().insert(programEditor, p.toContentValues());
                        Log.d(TAG, (insert == null) + " " + p.toString());
                        if (insert != null)
                            Log.d(TAG, insert.toString());
                    }

                    Log.d(TAG, programEditor.toString());
                    String[] projection = {TvContract.Programs.COLUMN_TITLE};
                    try (Cursor c = getContext().getContentResolver().query(programEditor, projection, null, null, null)) {
                        Log.d(TAG, "Found " + c.getCount() + " programs");
                        while (c.moveToNext()) {
                            Log.d(TAG, "Cursor read " + c.getString(c.getColumnIndex(TvContract.Programs.COLUMN_TITLE)));
                        }
                    }
                }
                Log.d(TAG, "Sync performed");
            }
        });
    }

    /**
     * Updates the system database, TvProvider, with the given programs.
     *
     * <p>If there is any overlap between the given and existing programs, the existing ones
     * will be updated with the given ones if they have the same title or replaced.
     *
     * @param channelUri The channel where the program info will be added.
     * @param newPrograms A list of {@link Program} instances which includes program
     *         information.
     */
    private void updatePrograms(final Uri channelUri, final List<Program> newPrograms) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int fetchedProgramsCount = newPrograms.size();
                if (fetchedProgramsCount == 0) {
                    return;
                }
                List<Program> oldPrograms = TvContractUtils.getPrograms(mContext.getContentResolver(),
                        channelUri);
                Program firstNewProgram = newPrograms.get(0);
                int oldProgramsIndex = 0;
                int newProgramsIndex = 0;
                // Skip the past programs. They will be automatically removed by the system.
                for (Program program : oldPrograms) {
                    oldProgramsIndex++;
                    if(program.getEndTimeUtcMillis() > firstNewProgram.getStartTimeUtcMillis()) {
                        break;
                    }
                }
                // Compare the new programs with old programs one by one and update/delete the old one or
                // insert new program if there is no matching program in the database.
                ArrayList<ContentProviderOperation> ops = new ArrayList<>();
                while (newProgramsIndex < fetchedProgramsCount) {
                    Program oldProgram = oldProgramsIndex < oldPrograms.size()
                            ? oldPrograms.get(oldProgramsIndex) : null;
                    Program newProgram = newPrograms.get(newProgramsIndex);
                    boolean addNewProgram = false;
                    if (oldProgram != null) {
                        if (oldProgram.equals(newProgram)) {
                            // Exact match. No need to update. Move on to the next programs.
//                            Log.d(TAG, oldProgram+" is = "+newProgram);
                            oldProgramsIndex++;
                            newProgramsIndex++;
                        } else if (needsUpdate(oldProgram, newProgram)) {
                            // Partial match. Update the old program with the new one.
                            // NOTE: Use 'update' in this case instead of 'insert' and 'delete'. There could
                            // be application specific settings which belong to the old program.
                            ops.add(ContentProviderOperation.newUpdate(
                                    TvContract.buildProgramUri(oldProgram.getProgramId()))
                                    .withValues(newProgram.toContentValues())
                                    .build());
//                            Log.d(TAG, "Updating program "+newProgram);
                            oldProgramsIndex++;
                            newProgramsIndex++;
                        } else if (oldProgram.getEndTimeUtcMillis() < newProgram.getEndTimeUtcMillis()) {
                            // No match. Remove the old program first to see if the next program in
                            // {@code oldPrograms} partially matches the new program.
                            ops.add(ContentProviderOperation.newDelete(
                                    TvContract.buildProgramUri(oldProgram.getProgramId()))
                                    .build());
//                            Log.d(TAG, "Deleting program "+oldProgram);
                            oldProgramsIndex++;
                        } else {
                            // No match. The new program does not match any of the old programs. Insert it
                            // as a new program.
                            addNewProgram = true;
                            newProgramsIndex++;
                        }
                    } else {
                        // No old programs. Just insert new programs.
                        addNewProgram = true;
                        newProgramsIndex++;
                    }
                    if (addNewProgram) {
                        ops.add(ContentProviderOperation
                                .newInsert(TvContract.Programs.CONTENT_URI)
                                .withValues(newProgram.toContentValues())
                                .build());
//                        Log.d(TAG, "Adding in program "+newProgram);
                    }
                    // Throttle the batch operation not to cause TransactionTooLargeException.
                    if (ops.size() > BATCH_OPERATION_COUNT
                            || newProgramsIndex >= fetchedProgramsCount) {
                        try {
                            mContext.getContentResolver().applyBatch(TvContract.AUTHORITY, ops);
                            Log.d(TAG, "Applying batch update");
                        } catch (SecurityException | RemoteException | OperationApplicationException e) {
                            Log.e(TAG, "Failed to insert programs.", e);
                            return;
                        }
                        ops.clear();
                    }
                }
            }
        }).start();
    }
    /**
     * Returns {@code true} if the {@code oldProgram} program needs to be updated with the
     * {@code newProgram} program.
     */
    private boolean needsUpdate(Program oldProgram, Program newProgram) {
        return true;
    }
}