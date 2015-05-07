/**
 * Copyright 2015 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.swing.sources;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.SwingScheduler;
import rx.subscriptions.Subscriptions;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public enum ItemEventSource { ; // no instances

    public static Observable<ItemEvent> fromItemEventsOf(final ItemSelectable itemSelectable) {
        return Observable.create(new OnSubscribe<ItemEvent>() {
            @Override
            public void call(final Subscriber<? super ItemEvent> subscriber) {
                final ItemListener listener = new ItemListener() {
                    @Override
                    public void itemStateChanged( ItemEvent event ) {
                        subscriber.onNext(event);
                    }
                };
                itemSelectable.addItemListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        itemSelectable.removeItemListener(listener);
                    }
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }
}
