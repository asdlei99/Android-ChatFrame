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

package com.android.chatframe.datamodel;

import com.android.chatframe.Factory;
import com.android.chatframe.datamodel.action.Action;
import com.android.chatframe.datamodel.action.ActionService;
import com.android.chatframe.datamodel.action.BackgroundWorker;
import com.android.chatframe.datamodel.data.ConversationData;
import com.android.chatframe.datamodel.data.ConversationListData;
import com.android.chatframe.datamodel.data.ConversationListData.ConversationListDataListener;
import com.android.chatframe.datamodel.data.MediaPickerData;
import com.android.chatframe.util.Assert.DoesNotRunOnMainThread;
import com.android.chatframe.util.ConnectivityUtil;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;


public abstract class DataModel {
    private String mFocusedConversation;
    private boolean mConversationListScrolledToNewestConversation;

    public static DataModel get() {
        return Factory.get().getDataModel();
    }

    public static final void startActionService(final Action action) {
        get().getActionService().startAction(action);
    }

    public static final void scheduleAction(final Action action,
            final int code, final long delayMs) {
        get().getActionService().scheduleAction(action, code, delayMs);
    }

    public abstract ConversationListData createConversationListData(final Context context,
            final ConversationListDataListener listener, final boolean archivedMode);

    public abstract ConversationData createConversationData(final Context context, final String conversationId);

   public abstract MediaPickerData createMediaPickerData(final Context context);


    public abstract ActionService getActionService();

    public abstract BackgroundWorker getBackgroundWorkerForActionService();

    @DoesNotRunOnMainThread
    public abstract DatabaseWrapper getDatabase();

    // Allow DataModel to coordinate with activity lifetime events.
    public abstract void onActivityResume();

    abstract void onCreateTables(final SQLiteDatabase db);

    public void setFocusedConversation(final String conversationId) {
        mFocusedConversation = conversationId;
    }

    public boolean isFocusedConversation(final String conversationId) {
        return !TextUtils.isEmpty(mFocusedConversation)
                && TextUtils.equals(mFocusedConversation, conversationId);
    }

    public void setConversationListScrolledToNewestConversation(
            final boolean scrolledToNewestConversation) {
        mConversationListScrolledToNewestConversation = scrolledToNewestConversation;
    }

    public boolean isConversationListScrolledToNewestConversation() {
        return mConversationListScrolledToNewestConversation;
    }

    /**
     * If a new message is received in the specified conversation, will the user be able to
     * observe it in some UI within the app?
     * @param conversationId conversation with the new incoming message
     */
    public boolean isNewMessageObservable(final String conversationId) {
        return isConversationListScrolledToNewestConversation()
                || isFocusedConversation(conversationId);
    }

    public abstract void onApplicationCreated();

    public abstract ConnectivityUtil getConnectivityUtil();

}
