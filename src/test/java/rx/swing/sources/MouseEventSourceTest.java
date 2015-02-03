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

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.*;


import javax.swing.JPanel;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Matchers;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

public class MouseEventSourceTest {
    private Component comp = new JPanel();

    @Test
    public void testRelativeMouseMotion() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<Point> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);

                Subscription sub = MouseEventSource.fromRelativeMouseMotion(comp).subscribe(
                        action, error, complete);

                InOrder inOrder = inOrder(action);

                verify(action, never()).call(Matchers.<Point> any());
                verify(error, never()).call(Matchers.<Exception> any());
                verify(complete, never()).call();

                fireMouseMotionEvent(mouseEvent(0, 0, MouseEvent.MOUSE_MOVED));
                verify(action, never()).call(Matchers.<Point> any());

                fireMouseMotionEvent(mouseEvent(10, -5, MouseEvent.MOUSE_MOVED));
                inOrder.verify(action, times(1)).call(new Point(10, -5));

                fireMouseMotionEvent(mouseEvent(6, 10, MouseEvent.MOUSE_MOVED));
                inOrder.verify(action, times(1)).call(new Point(-4, 15));

                sub.unsubscribe();
                fireMouseMotionEvent(mouseEvent(0, 0, MouseEvent.MOUSE_MOVED));
                inOrder.verify(action, never()).call(Matchers.<Point> any());
                verify(error, never()).call(Matchers.<Exception> any());
                verify(complete, never()).call();
            }

        }).awaitTerminal();
    }

    @Test
    public void testMouseEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<MouseEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);

                Subscription sub = MouseEventSource.fromMouseEventsOf(comp)
                        .subscribe(action, error, complete);

                InOrder inOrder = inOrder(action);

                verify(action, never()).call(Matchers.<MouseEvent> any());
                verify(error, never()).call(Matchers.<Exception> any());
                verify(complete, never()).call();

                MouseEvent mouseEvent =
                        mouseEvent(0, 0, MouseEvent.MOUSE_CLICKED);
                fireMouseClickEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                mouseEvent = mouseEvent(300, 200, MouseEvent.MOUSE_CLICKED);
                fireMouseClickEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                mouseEvent = mouseEvent(0, 0,  MouseEvent.MOUSE_CLICKED);
                fireMouseClickEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                sub.unsubscribe();
                fireMouseClickEvent(mouseEvent(0, 0, MouseEvent.MOUSE_CLICKED));
                inOrder.verify(action, never()).call(Matchers.<MouseEvent> any());
                verify(error, never()).call(Matchers.<Exception> any());
                verify(complete, never()).call();
            }

        }).awaitTerminal();
    }

    @Test
    public void testMouseWheelEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                @SuppressWarnings("unchecked")
                Action1<MouseEvent> action = mock(Action1.class);
                @SuppressWarnings("unchecked")
                Action1<Throwable> error = mock(Action1.class);
                Action0 complete = mock(Action0.class);

                Subscription sub = MouseEventSource.fromMouseWheelEvents(comp)
                        .subscribe(action, error, complete);

                InOrder inOrder = inOrder(action);

                verify(action, never()).call(Matchers.<MouseEvent> any());
                verify(error, never()).call(Matchers.<Exception> any());
                verify(complete, never()).call();

                MouseWheelEvent mouseEvent = mouseWheelEvent(0);
                fireMouseWheelEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                mouseEvent = mouseWheelEvent(3);
                fireMouseWheelEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                mouseEvent = mouseWheelEvent(5);
                fireMouseWheelEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                mouseEvent = mouseWheelEvent(1);
                fireMouseWheelEvent(mouseEvent);
                inOrder.verify(action, times(1)).call(mouseEvent);

                sub.unsubscribe();
                fireMouseClickEvent(mouseEvent(0, 0,  MouseEvent.MOUSE_CLICKED));
                inOrder.verify(action, never()).call(Matchers.<MouseEvent> any());
                verify(error, never()).call(Matchers.<Exception> any());
                verify(complete, never()).call();
            }

        }).awaitTerminal();
    }

    private MouseEvent mouseEvent(int x, int y, int mouseEventType) {
        return new MouseEvent(comp, mouseEventType, 1L, 0, x, y, 0,
                false);
    }

    private void fireMouseMotionEvent(MouseEvent event) {
        for (MouseMotionListener listener : comp.getMouseMotionListeners()) {
            listener.mouseMoved(event);
        }
    }

    private void fireMouseClickEvent(MouseEvent event) {
        for (MouseListener listener : comp.getMouseListeners()) {
            listener.mouseClicked(event);
        }
    }

    private MouseWheelEvent mouseWheelEvent(int wheelRotationClicks) {
        int mouseEventType = MouseEvent.MOUSE_WHEEL;
        return new MouseWheelEvent(comp, mouseEventType, 1L, 0, 0, 0, 0,
                false, MouseWheelEvent.WHEEL_BLOCK_SCROLL, 0,
                wheelRotationClicks);
    }

    private void fireMouseWheelEvent(MouseWheelEvent event) {
        for (MouseWheelListener listener : comp.getMouseWheelListeners()) {
            listener.mouseWheelMoved(event);
        }
    }
}
