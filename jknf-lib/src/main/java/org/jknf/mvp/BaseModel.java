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

package org.jknf.mvp;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseModel<B> {

    protected List<B> mDatas;

    protected Class<B> mDataClass;

    public BaseModel(Class<B> dataClass) {
        if (dataClass == null) {
            throw new IllegalArgumentException("Unknown bean type.");
        }
        mDataClass = dataClass;
        mDatas = new ArrayList<>();
    }

    public BaseModel put(B bean) {
        mDatas.add(bean);
        return this;
    }

    public BaseModel put(List<B> beans) {
        mDatas.addAll(beans);
        return this;
    }

    public BaseModel clear() {
        mDatas.clear();
        return this;
    }

    public List<B> get() {
        return mDatas;
    }

    protected <E> List<E> extractElement(String elementName, Class<E> elementClass) {
        List<E> elements = new ArrayList<>();
        if (mDatas != null && mDatas.size() > 0) {
            for (B bean : mDatas) {
                Field[] fields = mDataClass.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.getName().equals(elementName)) {
                        try {
                            E element = (E) field.get(bean);
                            elements.add(element);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return elements;
    }
}
