/**
 * Copyright 2014 Netflix, Inc.
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
import rx.functions.Func1;
import rx.schedulers.SwingScheduler;
import rx.subscriptions.Subscriptions;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public enum FocusEventSource { ; // no instances

    /**
     * @see rx.observables.SwingObservable#fromFocusEvents
     */
    public static Observable<FocusEvent> fromFocusEventsOf(final Component component) {
        return Observable.create(new OnSubscribe<FocusEvent>() {
            @Override
            public void call(final Subscriber<? super FocusEvent> subscriber) {
                final FocusListener listener = new FocusListener() {

                    @Override
                    public void focusGained(FocusEvent event) {
                        subscriber.onNext(event);
                    }

                    @Override
                    public void focusLost(FocusEvent event) {
                        subscriber.onNext(event);
                    }
                };
                component.addFocusListener(listener);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        component.removeFocusListener(listener);
                    }
                }));
            }
        }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * Predicates that help with filtering observables for specific focus events.
     */
    public enum Predicate implements Func1<FocusEvent, Boolean> {
        FOCUS_GAINED(FocusEvent.FOCUS_GAINED),
        FOCUS_LOST(FocusEvent.FOCUS_LOST);

        private final int id;

        private Predicate(int id) {
            this.id = id;
        }

        @Override
        public Boolean call(FocusEvent event) {
            return event.getID() == id;
        }
    }
}
