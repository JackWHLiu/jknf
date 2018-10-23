/*
 * Copyright (C) 2017 The JackKnife Open Source Project
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

package org.jknf.app;

import org.jknf.ioc.SupportActivity;

import java.lang.ref.WeakReference;
import java.util.Stack;

public class Application extends android.app.Application {

    private static Application sApp;
    protected Stack<WeakReference<? extends SupportActivity>> mActivityStacks;

    @Override
    public void onCreate() {
        super.onCreate();
        mActivityStacks = new Stack<>();
        sApp = this;
    }

    public static Application getInstance() {
        return sApp;
    }

    public void pushTask(SupportActivity activity) {
        mActivityStacks.add(new WeakReference<>(activity));
    }

    public void popTask() {
        WeakReference<? extends SupportActivity> ref = mActivityStacks.peek();
        if (ref != null) {
            mActivityStacks.pop();
        }
    }

    public void close() {
        for (WeakReference<? extends SupportActivity> ref : mActivityStacks) {
            if (ref != null) {
                SupportActivity activity = ref.get();
                if (activity != null) {
                    activity.finish();
                }
            }
        }
    }

    public void closeRetainBottom() {
        int size = mActivityStacks.size();
        for (int i = size - 1; i > 0; i--) {
            WeakReference<? extends SupportActivity> ref = mActivityStacks.get(i);
            if (ref != null) {
                SupportActivity activity = ref.get();
                if (activity != null) {
                    activity.finish();
                }
            }
        }
    }

    public void forceClose() {
        close();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
