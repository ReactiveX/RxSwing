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

import org.junit.Test;
import rx.Subscription;
import rx.functions.Action0;
import rx.observers.TestSubscriber;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import static junit.framework.Assert.assertEquals;

public class ListSelectionEventSourceTest {

    @Test
    public void jtableRowSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                TestSubscriber<ListSelectionEvent> testSubscriber = TestSubscriber.create();

                JTable table = createJTable();
                ListSelectionEventSource
                        .fromListSelectionEventsOf(table.getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                table.getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(1);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                0 /* end of region with selection changes */,
                                false),
                        testSubscriber.getOnNextEvents().get(0));

                table.getSelectionModel().setSelectionInterval(2, 2);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(2);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                2 /* end of region with selection changes */,
                                false),
                        testSubscriber.getOnNextEvents().get(1));
            }
        }).awaitTerminal();
    }

    @Test
    public void jtableColumnSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                TestSubscriber<ListSelectionEvent> testSubscriber = TestSubscriber.create();

                JTable table = createJTable();
                table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

                ListSelectionEventSource
                        .fromListSelectionEventsOf(table.getColumnModel().getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(1);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getColumnModel().getSelectionModel(),
                                0 /* start of region with selection changes */,
                                0 /* end of region with selection changes */,
                                false),
                        testSubscriber.getOnNextEvents().get(0));

                table.getColumnModel().getSelectionModel().setSelectionInterval(2, 2);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(2);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                table.getColumnModel().getSelectionModel(),
                                0 /* start of region with selection changes */,
                                2 /* end of region with selection changes */,
                                false),
                        testSubscriber.getOnNextEvents().get(1));

            }
        }).awaitTerminal();
    }

    @Test
    public void jlistSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                TestSubscriber<ListSelectionEvent> testSubscriber = TestSubscriber.create();

                JList<String> jList = new JList<String>(new String[]{"a", "b", "c", "d", "e", "f"});
                jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                ListSelectionEventSource
                        .fromListSelectionEventsOf(jList.getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                jList.getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(1);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                jList.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                0 /* end of region with selection changes */,
                                false),
                        testSubscriber.getOnNextEvents().get(0));

                jList.getSelectionModel().setSelectionInterval(2, 2);

                testSubscriber.assertNoErrors();
                testSubscriber.assertValueCount(2);

                assertListSelectionEventEquals(
                        new ListSelectionEvent(
                                jList.getSelectionModel(),
                                0 /* start of region with selection changes */,
                                2 /* end of region with selection changes */,
                                false),
                        testSubscriber.getOnNextEvents().get(1));
            }
        }).awaitTerminal();
    }

    @Test
    public void jtableRowSelectionUnsubscribeRemovesRowSelectionListener() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(new Action0() {

            @Override
            public void call() {
                TestSubscriber<ListSelectionEvent> testSubscriber = TestSubscriber.create();

                JTable table = createJTable();
                int numberOfListenersBefore = getNumberOfRowListSelectionListeners(table);

                Subscription sub = ListSelectionEventSource
                        .fromListSelectionEventsOf(table.getSelectionModel())
                        .subscribe(testSubscriber);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                sub.unsubscribe();

                testSubscriber.assertUnsubscribed();

                table.getSelectionModel().setSelectionInterval(0, 0);

                testSubscriber.assertNoErrors();
                testSubscriber.assertNoValues();

                assertEquals(numberOfListenersBefore, getNumberOfRowListSelectionListeners(table));
            }
        }).awaitTerminal();
    }

    private static int getNumberOfRowListSelectionListeners(final JTable table) {
        return ((DefaultListSelectionModel) table.getSelectionModel()).getListSelectionListeners().length;
    }

    private static JTable createJTable() {
        return new JTable(new Object[][]{
                {"A1", "B1", "C1"},
                {"A2", "B2", "C2"},
                {"A3", "B3", "C3"},
        },
                new String[]{
                        "A", "B", "C"
                });
    }

    private static void assertListSelectionEventEquals(ListSelectionEvent expected, ListSelectionEvent actual) {
        if (expected == null) {
            throw new IllegalArgumentException("missing expected");
        }

        if (actual == null) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual);
        }
        if (!expected.getSource().equals(actual.getSource())) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different source.");
        }
        if (expected.getFirstIndex() != actual.getFirstIndex()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different first index.");
        }
        if (expected.getLastIndex() != actual.getLastIndex()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different last index.");
        }
        if (expected.getValueIsAdjusting() != actual.getValueIsAdjusting()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different ValueIsAdjusting.");
        }
    }
}