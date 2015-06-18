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

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;

import static org.mockito.Mockito.*;

public class WindowEventSourceTest {

    @Test
    public void testObservingWindowEvents() throws Throwable {
        if (GraphicsEnvironment.isHeadless())
            return;
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {
            @Override
            public void call() {
                JFrame owner = new JFrame();
                Window window = new Window(owner);

                @SuppressWarnings("unchecked")
                Action1<WindowEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);

                final WindowEvent event = mock(WindowEvent.class);

                Subscription sub = WindowEventSource.fromWindowEventsOf(window)
                        .subscribe(action, error, complete);

                verify(action, never()).call(Matchers.<WindowEvent>any());
                verify(error, never()).call(Matchers.<Throwable>any());
                verify(complete, never()).call();

                fireWindowEvent(window, event);
                verify(action, times(1)).call(Matchers.<WindowEvent>any());

                fireWindowEvent(window, event);
                verify(action, times(2)).call(Matchers.<WindowEvent> any());

                sub.unsubscribe();
                fireWindowEvent(window, event);
                verify(action, times(2)).call(Matchers.<WindowEvent> any());
                verify(error, never()).call(Matchers.<Throwable> any());
                verify(complete, never()).call();
            }

        }).awaitTerminal();
    }

    private void fireWindowEvent(Window window, WindowEvent event) {
        for (WindowListener listener : window.getWindowListeners()) {
            listener.windowClosed(event);
        }
    }
}

