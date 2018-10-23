/*
 * Copyright (C) 2018 The JackKnife Open Source Project
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

package org.jknf.ioc.inject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.jknf.ioc.SupportActivity;
import org.jknf.ioc.SupportV;
import org.jknf.ioc.annotation.ContentView;
import org.jknf.ioc.annotation.EventBase;
import org.jknf.ioc.annotation.ViewIgnore;
import org.jknf.ioc.annotation.ViewInject;
import org.jknf.ioc.bind.BindEvent;
import org.jknf.ioc.bind.BindLayout;
import org.jknf.ioc.bind.BindView;
import org.jknf.ioc.exception.IllegalXmlMappingException;
import org.jknf.ioc.exception.InjectException;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public abstract class AbstractHandler implements InjectHandler {

    protected abstract String getClassNameSuffix();

    @Override
    public String generateLayoutName(SupportV v) {
        String suffix = getClassNameSuffix();
        StringBuffer sb;
        String layoutName = v.getClass().getSimpleName();
        if (!layoutName.endsWith(suffix)) {
            throw new IllegalXmlMappingException("Class name is not ends with correct suffix.");
        } else {
            String name = layoutName.substring(0, layoutName.length() - suffix.length());
            sb = new StringBuffer(suffix.toLowerCase(Locale.ENGLISH));
            for (int i = 0; i < name.length(); i++) {
                if (name.charAt(i) >= A_INDEX && name.charAt(i) <= Z_INDEX || i == 0) {
                    sb.append(UNDERLINE);
                }
                sb.append(String.valueOf(name.charAt(i)).toLowerCase(Locale.ENGLISH));
            }
        }
        return sb.toString();
    }

    protected abstract SupportActivity getSupportActivity();

    @Override
    public void performInject(BindLayout bindLayout) {
        SupportV v = bindLayout.getTarget();
        String layoutName = generateLayoutName(v);
        Class<? extends SupportV> viewClass = v.getClass();
        SupportActivity activity = getSupportActivity();
        String packageName = activity.getPackageName();
        int layoutId = View.NO_ID;
        try {
            Class<?> layoutClass = Class.forName(packageName + R_LAYOUT);
            Field field = layoutClass.getDeclaredField(layoutName);
            layoutId = field.getInt(v);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        ContentView contentView = v.getClass().getAnnotation(ContentView.class);
        if (contentView != null) {
            layoutId = contentView.value();
        }
        try {
            Method method = viewClass.getMethod(METHOD_SET_CONTENT_VIEW, int.class);
            method.invoke(v, layoutId);
            LayoutInflater inflater = LayoutInflater.from((Context) activity);
            Class<? extends LayoutInflater> inflaterClass = LayoutInflater.class;
            Method inflateMethod = inflaterClass.getDeclaredMethod(METHOD_INFLATE, int.class,
                    ViewGroup.class);
            inflateMethod.invoke(inflater, layoutId, null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void performInject(BindView bindView) {
        SupportV v = bindView.getTarget();
        Class<?> viewClass = v.getClass();
        Field[] viewFields = viewClass.getDeclaredFields();
        SupportActivity activity = getSupportActivity();
        Class<? extends SupportActivity> activityClass = activity.getClass();
        for (Field field : viewFields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            if (View.class.isAssignableFrom(fieldType)) {
                ViewIgnore viewIgnore = field.getAnnotation(ViewIgnore.class);
                if (viewIgnore != null) {
                    continue;
                }
                ViewInject viewInject = field.getAnnotation(ViewInject.class);
                int id = View.NO_ID;
                try {
                    if (viewInject != null) {
                        id = viewInject.value();
                    } else {
                        String packageName = activity.getPackageName();
                        Class<?> idClass = Class.forName(packageName + R_ID);
                        Field idField = idClass.getDeclaredField(field.getName());
                        id = idField.getInt(idField);
                    }
                    Method findViewByIdMethod = activityClass.getMethod(METHOD_FIND_VIEW_BY_ID,
                            int.class);
                    Object view = findViewByIdMethod.invoke(activity, id);
                    if (view != null) {
                        field.set(v, view);
                    }
                } catch (Exception e) {
                    throw new InjectException(fieldType.getName() + " " + field.getName()
                            + " can\'t be injected, at layout(" + generateLayoutName(v) + ".xml), " +
                            "id(" + (id == View.NO_ID ? id : "0x" + Integer.toHexString(id)) + ").");
                }
            }
        }
    }

    @Override
    public void performInject(BindEvent bindEvent) {
        SupportV v = bindEvent.getTarget();
        SupportActivity activity = getSupportActivity();
        Class<?> viewClass = v.getClass();
        Method[] methods = viewClass.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                EventBase eventBase = annotationType.getAnnotation(EventBase.class);
                if (eventBase == null) {
                    continue;
                }
                String listenerSetter = eventBase.listenerSetter();
                Class<?> listenerType = eventBase.listenerType();
                String callbackMethod = eventBase.callbackMethod();
                try {
                    Method valueMethod = annotationType.getDeclaredMethod(METHOD_VALUE);
                    int[] viewIds = (int[]) valueMethod.invoke(annotation);
                    for (int viewId : viewIds) {
                        View view = activity.getView(viewId);
                        if (view == null) {
                            continue;
                        }
                        Method setListenerMethod = view.getClass().getMethod(listenerSetter,
                                listenerType);
                        HashMap<String, Method> map = new HashMap<>();
                        map.put(callbackMethod, method);
                        EventInvocationHandler handler = new EventInvocationHandler(map, v);
                        Object proxy = Proxy.newProxyInstance(listenerType.getClassLoader(),
                                new Class<?>[]{listenerType}, handler);
                        setListenerMethod.invoke(view, proxy);
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class EventInvocationHandler implements InvocationHandler {

        private Map<String, Method> mCallbackMethodMap;
        private SupportV mViewInjected;

        public EventInvocationHandler(HashMap<String, Method> callbackMethodMap, SupportV v) {
            this.mCallbackMethodMap = callbackMethodMap;
            this.mViewInjected = v;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            Method callbackMethod = mCallbackMethodMap.get(name);
            if (callbackMethod != null) {
                return callbackMethod.invoke(mViewInjected, args);
            }
            return method.invoke(proxy, args);
        }
    }
}
