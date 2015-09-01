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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.beans.PropertyChangeEvent;

import javax.swing.JPanel;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observables.SwingObservable;

public class PropertyChangeEventSourceTest
{
    @Test
    public void testObservingPropertyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call()
            {
                @SuppressWarnings("unchecked")
                Action1<PropertyChangeEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);
                
                Component component = new JPanel();
                
                Subscription subscription = PropertyChangeEventSource.fromPropertyChangeEventsOf(component)
                                                                     .subscribe(action, error, complete);
                
                verify(action, never()).call(Matchers.<PropertyChangeEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
                
                component.setEnabled(false);
                verify(action, times(1)).call(Mockito.argThat(propertyChangeEventMatcher("enabled", true, false)));
                verifyNoMoreInteractions(action, error, complete);
                
                // check that an event is only fired if the value really changes
                component.setEnabled(false);
                verifyNoMoreInteractions(action, error, complete);
                
                component.setEnabled(true);
                verify(action, times(1)).call(Mockito.argThat(propertyChangeEventMatcher("enabled", false, true)));
                verifyNoMoreInteractions(action, error, complete);
                
                // check some arbitrary property
                component.firePropertyChange("width", 200, 300);
                verify(action, times(1)).call(Mockito.argThat(propertyChangeEventMatcher("width", 200l, 300l)));
                verifyNoMoreInteractions(action, error, complete);
                
                // verify no events sent after unsubscribing
                subscription.unsubscribe();
                component.setEnabled(false);
                verifyNoMoreInteractions(action, error, complete);
            }
            
        }).awaitTerminal();
    }
    
    @Test
    public void testObservingFilteredPropertyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call()
            {
                @SuppressWarnings("unchecked")
                Action1<PropertyChangeEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);
                
                Component component = new JPanel();
                
                Subscription subscription = SwingObservable.fromPropertyChangeEvents(component, "enabled")
                                                           .subscribe(action, error, complete);
                
                verify(action, never()).call(Matchers.<PropertyChangeEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
                
                // trigger a bunch of property change events and verify that only the enbled ones are observed
                component.setEnabled(false);
                component.setEnabled(false);
                component.setEnabled(true);
                component.firePropertyChange("width", 200, 300);
                component.firePropertyChange("height", 400, 200);
                component.firePropertyChange("depth", 100, 300);
                verify(action, times(1)).call(Mockito.argThat(propertyChangeEventMatcher("enabled", true, false)));
                verify(action, times(1)).call(Mockito.argThat(propertyChangeEventMatcher("enabled", false, true)));
                verifyNoMoreInteractions(action, error, complete);
                
                subscription.unsubscribe();
            }
            
        }).awaitTerminal();
    }
    
    private static Matcher<PropertyChangeEvent> propertyChangeEventMatcher(final String propertyName, final Object oldValue, final Object newValue)
    {
        return new ArgumentMatcher<PropertyChangeEvent>() {
            @Override
            public boolean matches(Object argument) {
                if (argument.getClass() != PropertyChangeEvent.class) {
                    return false;
                }
                
                PropertyChangeEvent pcEvent = (PropertyChangeEvent) argument;
                
                if(!propertyName.equals(pcEvent.getPropertyName())) {
                    return false;
                }
                
                if (!oldValue.equals(pcEvent.getOldValue())) {
                    return false;
                }
                
                return newValue.equals(pcEvent.getNewValue());
            }
        };
    }
}
