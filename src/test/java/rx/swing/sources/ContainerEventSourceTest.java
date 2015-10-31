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

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
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
import rx.swing.sources.ContainerEventSource.Predicate;

@RunWith(Parameterized.class)
public class ContainerEventSourceTest {
    
    private final Func1<Container, Observable<ContainerEvent>> observableFactory;
    
    private JPanel panel;
    private Action1<ContainerEvent> action;
    private Action1<Throwable> error;
    private Action0 complete;

    public ContainerEventSourceTest(Func1<Container, Observable<ContainerEvent>> observableFactory) {
        this.observableFactory = observableFactory;
    }
    
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{ { observableFromContainerEventSource() },
                                             { observableFromSwingObservable() } });
    }
    
    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        panel = Mockito.spy(new JPanel());
        action = Mockito.mock(Action1.class);
        error = Mockito.mock(Action1.class);
        complete = Mockito.mock(Action0.class);
    }
    
    @Test
    public void testObservingContainerEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                Subscription subscription = observableFactory.call(panel)
                                                             .subscribe(action, error, complete);
                
                JPanel child = new JPanel();
                panel.add(child);
                panel.removeAll();
                
                InOrder inOrder = Mockito.inOrder(action);
                
                inOrder.verify(action).call(Matchers.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_ADDED)));
                inOrder.verify(action).call(Matchers.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_REMOVED)));
                inOrder.verifyNoMoreInteractions();
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();
                
                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(panel).removeContainerListener(Mockito.any(ContainerListener.class));
                Assert.assertEquals(0, panel.getHierarchyListeners().length);
                
                // Verifies that after unsubscribing events are not emitted.
                panel.add(child);
                Mockito.verifyNoMoreInteractions(action, error, complete);
           }
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingFilteredContainerEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                Subscription subscription = observableFactory.call(panel)
                                                             .filter(Predicate.COMPONENT_ADDED)
                                                             .subscribe(action, error, complete);
                
                JPanel child = new JPanel();
                panel.add(child);
                panel.remove(child); // sanity check to verify that the filtering works.
                
                Mockito.verify(action).call(Matchers.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_ADDED)));
                Mockito.verify(error, Mockito.never()).call(Mockito.any(Throwable.class));
                Mockito.verify(complete, Mockito.never()).call();
                
                // Verifies that the underlying listener has been removed.
                subscription.unsubscribe();
                Mockito.verify(panel).removeContainerListener(Mockito.any(ContainerListener.class));
                Assert.assertEquals(0, panel.getHierarchyListeners().length);
                
                // Verifies that after unsubscribing events are not emitted.
                panel.add(child);
                Mockito.verifyNoMoreInteractions(action, error, complete);
           }
        }).awaitTerminal();
    }

    private static Matcher<ContainerEvent> containerEventMatcher(final Container container, final Component child, final int id) {
        return new ArgumentMatcher<ContainerEvent>() {
            @Override
            public boolean matches(Object argument) {
                if ( argument.getClass() != ContainerEvent.class )
                    return false;
                
                ContainerEvent event = (ContainerEvent) argument;
                
                if (container != event.getContainer())
                    return false;
                
                if (container != event.getSource())
                    return false;
                
                if (child != event.getChild())
                    return false;
                
                return event.getID() == id;
            }
        };
    }

    private static Func1<Container, Observable<ContainerEvent>> observableFromContainerEventSource()
    {
        return new Func1<Container, Observable<ContainerEvent>>(){
            @Override
            public Observable<ContainerEvent> call(Container container) {
                return ContainerEventSource.fromContainerEventsOf(container);
            }
        }; 
    }
    
    private static Func1<Container, Observable<ContainerEvent>> observableFromSwingObservable()
    {
        return new Func1<Container, Observable<ContainerEvent>>(){
            @Override
            public Observable<ContainerEvent> call(Container container) {
                return SwingObservable.fromContainerEvents(container);
            }
        }; 
    }
}
