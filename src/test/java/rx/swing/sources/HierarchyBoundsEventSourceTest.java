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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JPanel;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mockito;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.SwingObservable;
import rx.swing.sources.HierarchyEventSource.Predicate;

@RunWith(Parameterized.class)
public class HierarchyBoundsEventSourceTest {

    private JPanel rootPanel;
    private JPanel parentPanel;
    private Action1<HierarchyEvent> action;
    private Action1<Throwable> error;
    private Action0 complete;
    private final Func1<Component, Observable<HierarchyEvent>> observableFactory;
    private JPanel childPanel;
    
    public HierarchyBoundsEventSourceTest( Func1<Component, Observable<HierarchyEvent>> observableFactory ) {
        this.observableFactory = observableFactory;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][]{ { observablefromEventSource() }, 
                                              { observablefromSwingObservable() } });
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        rootPanel = new JPanel();
        
        parentPanel = new JPanel();
        rootPanel.add(parentPanel);
        
        childPanel = Mockito.spy(new JPanel());
        parentPanel.add(childPanel);
        
        action = mock(Action1.class);
        error = mock(Action1.class);
        complete = mock(Action0.class);
    }
    
    @Test
    public void testObservingAnscestorResizedHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                Subscription subscription = observableFactory.call(childPanel)
                                                             .filter(Predicate.ANCESTOR_RESIZED)
                                                             .subscribe(action, error, complete);

                parentPanel.setSize(10, 10);
                parentPanel.setLocation(10, 10); // verifies that ancestor moved events are ignored.

                Mockito.verify(action).call(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_RESIZED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();

                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                parentPanel.setSize(20, 20);
                parentPanel.setLocation(20, 20);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingAnscestorMovedHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                Subscription subscription = observableFactory.call(childPanel)
                                                             .filter(Predicate.ANCESTOR_MOVED)
                                                             .subscribe(action, error, complete);

                parentPanel.setSize(10, 10); // verifies that ancestor resized events are ignored.
                parentPanel.setLocation(10, 10);

                Mockito.verify(action).call(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_MOVED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();

                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                parentPanel.setSize(20, 20);
                parentPanel.setLocation(20, 20);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingAllHierarchyBoundsEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                Subscription subscription = observableFactory.call(childPanel)
                                                             .subscribe(action, error, complete);
                
                InOrder inOrder = inOrder(action);

                parentPanel.setSize(10, 10);
                parentPanel.setLocation(10, 10);

                inOrder.verify(action).call(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_RESIZED, parentPanel, rootPanel)));
                inOrder.verify(action).call(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_MOVED, parentPanel, rootPanel)));
                inOrder.verifyNoMoreInteractions();
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();

                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                parentPanel.setSize(20, 20);
                parentPanel.setLocation(20, 20);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }

    private Matcher<HierarchyEvent> hierarchyEventMatcher(final Component source, final int id, final Container changed, final Container changedParent) {
        return new ArgumentMatcher<HierarchyEvent>() {
            @Override
            public boolean matches(Object argument) {
                if (argument.getClass() != HierarchyEvent.class)
                    return false;

                HierarchyEvent event = (HierarchyEvent) argument;

                if (source != event.getComponent())
                    return false;

                if (changed != event.getChanged())
                    return false;

                if (changedParent != event.getChangedParent())
                    return false;

                return id == event.getID();
            }
        };
    }
    
    private static Func1<Component, Observable<HierarchyEvent>> observablefromEventSource()
    {
        return new Func1<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> call(Component component) {
                return HierarchyEventSource.fromHierarchyBoundsEventsOf(component);
            }
        };
    }
    
    private static Func1<Component, Observable<HierarchyEvent>> observablefromSwingObservable()
    {
        return new Func1<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> call(Component component) {
                return SwingObservable.fromHierachyBoundsEvents(component);
            }
        };
    }
}
