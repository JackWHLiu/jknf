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

import android.os.Bundle;
import android.view.View;
import org.jknf.ioc.SupportActivity;
import org.jknf.ioc.ViewInjector;

public abstract class Activity extends android.app.Activity implements SupportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewInjector.inject(this);
        push();
    }

    @Override
    protected void onDestroy() {
        pop();
        super.onDestroy();
    }

    private void push() {
        if (getApplication() instanceof Application) {
            Application.getInstance().pushTask(this);
        }
    }

    private void pop() {
        if (getApplication() instanceof Application) {
            Application.getInstance().popTask();
        }
    }

    @Override
    public <VIEW extends View> VIEW getView(int id) {
        return (VIEW) findViewById(id);
    }
}
