package rx.swing.sources;

import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action0;
import rx.observables.SwingObservable;
import rx.subscriptions.SwingSubscriptions;

public enum ItemEventSource { ; // no instances

    public static Observable<ItemEvent> fromItemEventsOf(final ItemSelectable itemSelectable) {
        return Observable.create(new OnSubscribe<ItemEvent>() {
            @Override
            public void call(final Subscriber<? super ItemEvent> subscriber) {
                SwingObservable.assertEventDispatchThread();
                final ItemListener listener = new ItemListener()
                {
                    @Override
                    public void itemStateChanged( ItemEvent event )
                    {
                        subscriber.onNext(event);
                    }
                };
                itemSelectable.addItemListener(listener);
                subscriber.add(SwingSubscriptions.unsubscribeInEventDispatchThread(new Action0() {
                    @Override
                    public void call() {
                        itemSelectable.removeItemListener(listener);
                    }
                }));
            }
        });
    }
}
