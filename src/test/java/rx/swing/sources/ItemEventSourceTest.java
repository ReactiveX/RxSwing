package rx.swing.sources;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.event.ItemEvent;

import javax.swing.AbstractButton;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observables.SwingObservable;

public class ItemEventSourceTest
{
    @Test
    public void testObservingItemEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<ItemEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Subscription sub = ItemEventSource.fromItemEventsOf(button).subscribe(action,
                        error, complete);

                verify(action, never()).call(Matchers.<ItemEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();

                button.testSelection();
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));

                button.testSelection();
                verify(action, times(2)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));


                sub.unsubscribe();
                button.testSelection();
                verify(action, times(2)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingItemEventsFilteredBySelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<ItemEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Subscription sub = SwingObservable.fromItemSelectionEvents(button)
                                                  .subscribe(action, error, complete);

                verify(action, never()).call(Matchers.<ItemEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();

                button.testSelection();
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, never()).call(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));


                sub.unsubscribe();
                button.testSelection();
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                verify(action, never()).call(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingItemEventsFilteredByDeSelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<ItemEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);
                
                @SuppressWarnings("serial")
                class TestButton extends AbstractButton {
                    void testSelection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.SELECTED));
                    }
                    void testDeselection() {
                        fireItemStateChanged(new ItemEvent(this, 
                                                           ItemEvent.ITEM_STATE_CHANGED, 
                                                           this, 
                                                           ItemEvent.DESELECTED));
                    }
                }

                TestButton button = new TestButton();
                Subscription sub = SwingObservable.fromItemDeselectionEvents(button)
                                                  .subscribe(action, error, complete);

                verify(action, never()).call(Matchers.<ItemEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();

                button.testSelection();
                verify(action, never()).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                
                button.testDeselection();
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));


                sub.unsubscribe();
                button.testSelection();
                verify(action, never()).call(Mockito.<ItemEvent>argThat(itemEventMatcher(SELECTED)));
                verify(action, times(1)).call(Mockito.<ItemEvent>argThat(itemEventMatcher(DESELECTED)));
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
            }
        }).awaitTerminal();
    }
    
    private Matcher<ItemEvent> itemEventMatcher(final int eventType)
    {
        return new ArgumentMatcher<ItemEvent>() {
            @Override
            public boolean matches(Object argument) {
                if (argument.getClass() !=  ItemEvent.class)
                    return false;
                
                return ((ItemEvent) argument).getStateChange() == eventType;
            }
        };
    }
}
