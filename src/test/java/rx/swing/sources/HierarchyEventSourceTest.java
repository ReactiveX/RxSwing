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

import static org.mockito.Mockito.mock;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
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
import org.mockito.Matchers;
import org.mockito.Mockito;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.SwingObservable;

@RunWith(Parameterized.class)
public class HierarchyEventSourceTest {

    private JPanel rootPanel;
    private JPanel parentPanel;
    private Action1<HierarchyEvent> action;
    private Action1<Throwable> error;
    private Action0 complete;
    private final Func1<Component, Observable<HierarchyEvent>> observableFactory;
    
    public HierarchyEventSourceTest( Func1<Component, Observable<HierarchyEvent>> observableFactory ) {
        this.observableFactory = observableFactory;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList( new Object[][]{ { ObservablefromEventSource() }, 
                                              { ObservablefromSwingObservable() } });
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        rootPanel = new JPanel();
        parentPanel = new JPanel();
        
        action = mock(Action1.class);
        error = mock(Action1.class);
        complete = mock(Action0.class);
    }
    
    @Test
    public void testObservingHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                JPanel childPanel = Mockito.spy(new JPanel());
                parentPanel.add(childPanel);

                Subscription subscription = observableFactory.call(childPanel)
                                                             .subscribe(action, error, complete);

                rootPanel.add(parentPanel);

                Mockito.verify(action).call(Matchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.PARENT_CHANGED, parentPanel, rootPanel)));
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();

                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(childPanel).removeHierarchyListener(Mockito.any(HierarchyListener.class));
                Assert.assertEquals(0, childPanel.getHierarchyListeners().length);

                // Sanity check to verify that no more events are emitted after unsubscribing.
                rootPanel.remove(parentPanel);
                Mockito.verifyNoMoreInteractions(action, error, complete);
            }
        }).awaitTerminal();
    }

    private Matcher<HierarchyEvent> hierarchyEventMatcher(final Component source, final int changeFlags, final Container changed, final Container changedParent) {
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

                return changeFlags == event.getChangeFlags();
            }
        };
    }
    
    private static Func1<Component, Observable<HierarchyEvent>> ObservablefromEventSource()
    {
        return new Func1<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> call(Component component) {
                return HierarchyEventSource.fromHierarchyEventsOf(component);
            }
        };
    }
    
    private static Func1<Component, Observable<HierarchyEvent>> ObservablefromSwingObservable()
    {
        return new Func1<Component, Observable<HierarchyEvent>>() {
            @Override
            public Observable<HierarchyEvent> call(Component component) {
                return SwingObservable.fromHierachyEvents(component);
            }
        };
    }
}
