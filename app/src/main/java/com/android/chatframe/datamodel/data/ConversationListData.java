/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.chatframe.datamodel.data;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;


import java.util.HashSet;

import com.android.chatframe.datamodel.BoundCursorLoader;
import com.android.chatframe.datamodel.DataModel;
import com.android.chatframe.datamodel.MessagingContentProvider;
import com.android.chatframe.datamodel.binding.BindableData;
import com.android.chatframe.datamodel.binding.BindingBase;
import com.android.chatframe.util.Assert;
import com.android.chatframe.util.LogUtil;

public class ConversationListData extends BindableData
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = LogUtil.BUGLE_DATAMODEL_TAG;
    private static final String BINDING_ID = "bindingId";

    public interface ConversationListDataListener {
        public void onConversationListCursorUpdated(ConversationListData data, Cursor cursor);
        public void setBlockedParticipantsAvailable(boolean blockedAvailable);
    }

    private ConversationListDataListener mListener;
    private final Context mContext;
    private final boolean mArchivedMode;
    private LoaderManager mLoaderManager;

    public ConversationListData(final Context context, final ConversationListDataListener listener,
            final boolean archivedMode) {
        mListener = listener;
        mContext = context;
        mArchivedMode = archivedMode;
    }

    private static final int CONVERSATION_LIST_LOADER = 1;
    private static final int BLOCKED_PARTICIPANTS_AVAILABLE_LOADER = 2;

    private static final int INDEX_BLOCKED_PARTICIPANTS_NORMALIZED_DESTINATION = 1;

    // all blocked participants
    private final HashSet<String> mBlockedParticipants = new HashSet<String>();

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        final String bindingId = args.getString(BINDING_ID);
        Loader<Cursor> loader = null;
        // Check if data still bound to the requesting ui element
        if (isBound(bindingId)) {
            LogUtil.i(TAG,
                    "=====ConversationListData==zhongjihao===onCreateLoader====id: "+id);
            switch (id) {
                case BLOCKED_PARTICIPANTS_AVAILABLE_LOADER:
                    loader = new BoundCursorLoader(bindingId, mContext,
                            MessagingContentProvider.PARTICIPANTS_URI,
                            null,
                           null, null, null);
                    break;
                case CONVERSATION_LIST_LOADER:
                    loader = new BoundCursorLoader(bindingId, mContext,
                            MessagingContentProvider.CONVERSATIONS_URI,
                            null,
                            null,
                            null,       // selection args
                            null);
                    break;
                default:
                    Assert.fail("Unknown loader id");
                    break;
            }
        } else {
            LogUtil.w(TAG, "Creating loader after unbinding list");
        }
        return loader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> generic, final Cursor data) {
        final BoundCursorLoader loader = (BoundCursorLoader) generic;
        LogUtil.i(TAG,
                "=====ConversationListData====zhongjihao===onLoadFinished====id: "+loader.getId()+"   binding: "+loader.getBindingId());
        if (isBound(loader.getBindingId())) {
            switch (loader.getId()) {
                case BLOCKED_PARTICIPANTS_AVAILABLE_LOADER:
                    mBlockedParticipants.clear();
                    if(data != null && data.getCount() > 0){
                        for (int i = 0; i < data.getCount(); i++) {
                            data.moveToPosition(i);
                            mBlockedParticipants.add(data.getString(
                                    INDEX_BLOCKED_PARTICIPANTS_NORMALIZED_DESTINATION));
                        }
                    }
                    mListener.setBlockedParticipantsAvailable(data != null && data.getCount() > 0);
                    break;
                case CONVERSATION_LIST_LOADER:
                    mListener.onConversationListCursorUpdated(this, data);
                    break;
                default:
                    Assert.fail("Unknown loader id");
                    break;
            }
        } else {
            LogUtil.w(TAG, "Loader finished after unbinding list");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> generic) {
        final BoundCursorLoader loader = (BoundCursorLoader) generic;
        if (isBound(loader.getBindingId())) {
            switch (loader.getId()) {
                case BLOCKED_PARTICIPANTS_AVAILABLE_LOADER:
                    mListener.setBlockedParticipantsAvailable(false);
                    break;
                case CONVERSATION_LIST_LOADER:
                    mListener.onConversationListCursorUpdated(this, null);
                    break;
                default:
                    Assert.fail("Unknown loader id");
                    break;
            }
        } else {
            LogUtil.w(TAG, "Loader reset after unbinding list");
        }
    }

    private Bundle mArgs;

    public void init(final LoaderManager loaderManager,
            final BindingBase<ConversationListData> binding) {
        LogUtil.i(TAG,
                "=====ConversationListData==zhongjihao===init====binding: "+binding.getBindingId());
        mArgs = new Bundle();
        mArgs.putString(BINDING_ID, binding.getBindingId());
        mLoaderManager = loaderManager;
        mLoaderManager.initLoader(CONVERSATION_LIST_LOADER, mArgs, this);
        mLoaderManager.initLoader(BLOCKED_PARTICIPANTS_AVAILABLE_LOADER, mArgs, this);
    }

    public void handleMessagesSeen() {
//        BugleNotifications.markAllMessagesAsSeen();
//
//        SmsReceiver.cancelSecondaryUserNotification();
    }

    @Override
    protected void unregisterListeners() {
        // This could be null if we bind but the caller doesn't init the BindableData
        if (mLoaderManager != null) {
            mLoaderManager.destroyLoader(CONVERSATION_LIST_LOADER);
            mLoaderManager.destroyLoader(BLOCKED_PARTICIPANTS_AVAILABLE_LOADER);
            mLoaderManager = null;
        }
        mListener = null;
    }

    public void setScrolledToNewestConversation(boolean scrolledToNewestConversation) {
        DataModel.get().setConversationListScrolledToNewestConversation(
                scrolledToNewestConversation);
        if (scrolledToNewestConversation) {
            handleMessagesSeen();
        }
    }

    public HashSet<String> getBlockedParticipants() {
        return mBlockedParticipants;
    }
}
