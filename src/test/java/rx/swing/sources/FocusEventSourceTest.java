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

import org.junit.Test;
import org.mockito.Matchers;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import static org.mockito.Mockito.*;

public class FocusEventSourceTest {
    private Component comp = new JPanel();

    @Test
    public void testObservingFocusEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<FocusEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);

                final FocusEvent event = mock(FocusEvent.class);

                Subscription sub = FocusEventSource.fromFocusEventsOf(comp)
                        .subscribe(action, error, complete);

                verify(action, never()).call(Matchers.<FocusEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();

                fireFocusEvent(event);
                verify(action, times(1)).call(Matchers.<FocusEvent> any());

                fireFocusEvent(event);
                verify(action, times(2)).call(Matchers.<FocusEvent> any());

                sub.unsubscribe();
                fireFocusEvent(event);
                verify(action, times(2)).call(Matchers.<FocusEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
            }

        }).awaitTerminal();
    }

    private void fireFocusEvent(FocusEvent event) {
        for (FocusListener listener : comp.getFocusListeners()) {
            listener.focusGained(event);
        }
    }
}
