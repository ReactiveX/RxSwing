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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class ListSelectionEventSourceTest {

	@Test
	public void jtableRowSelection_observingSelectionEvents() throws Throwable {
		SwingTestHelper.create().runInEventDispatchThread(new Action0() {

			@Override
			public void call() {
				@SuppressWarnings("unchecked")
				Action1<ListSelectionEvent> action = mock(Action1.class);
				@SuppressWarnings("unchecked")
				Action1<Throwable> error = mock(Action1.class);
				Action0 complete = mock(Action0.class);

				JTable table = createJTable();
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				ListSelectionEventSource
						.fromListSelectionEventsOf(table.getSelectionModel())
						.subscribe(action, error, complete);

				verifyZeroInteractions(action, error, complete);

				table.getSelectionModel().setSelectionInterval(0, 0);

				verify(action, times(1)).call(Mockito.argThat(listSelectionEventMatcher(table.getSelectionModel(), 0, 0, false)));
				verifyNoMoreInteractions(action, error, complete);
			}
		}).awaitTerminal();
	}

	@Test
	public void jtableColumnSelection_observingSelectionEvents() throws Throwable {
		SwingTestHelper.create().runInEventDispatchThread(new Action0() {

			@Override
			public void call() {
				@SuppressWarnings("unchecked")
				Action1<ListSelectionEvent> action = mock(Action1.class);
				@SuppressWarnings("unchecked")
				Action1<Throwable> error = mock(Action1.class);
				Action0 complete = mock(Action0.class);

				JTable table = createJTable();
				table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

				ListSelectionEventSource
						.fromListSelectionEventsOf(table.getColumnModel().getSelectionModel())
						.subscribe(action, error, complete);

				table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);

				verify(action, times(1)).call(Mockito.argThat(listSelectionEventMatcher(table.getColumnModel().getSelectionModel(), 0, 0, false)));
				verifyNoMoreInteractions(action, error, complete);
			}
		}).awaitTerminal();
	}

	@Test
	public void jlistSelection_observingSelectionEvents() throws Throwable {
		SwingTestHelper.create().runInEventDispatchThread(new Action0() {

			@Override
			public void call() {
				@SuppressWarnings("unchecked")
				Action1<ListSelectionEvent> action = mock(Action1.class);
				@SuppressWarnings("unchecked")
				Action1<Throwable> error = mock(Action1.class);
				Action0 complete = mock(Action0.class);

				JList<String> jList = new JList<String>(new String[] {"a", "b", "c", "d", "e", "f"});
				jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				ListSelectionEventSource
						.fromListSelectionEventsOf(jList.getSelectionModel())
						.subscribe(action, error, complete);

				verifyZeroInteractions(action, error, complete);

				jList.getSelectionModel().setSelectionInterval(0, 0);

				verify(action, times(1)).call(Mockito.argThat(listSelectionEventMatcher(jList.getSelectionModel(), 0, 0, false)));
				verifyNoMoreInteractions(action, error, complete);
			}
		}).awaitTerminal();
	}

	@Test
	public void jtableRowSelection_unsubscribe_removesRowSelectionListener() throws Throwable {
		SwingTestHelper.create().runInEventDispatchThread(new Action0() {

			@Override
			public void call() {
				@SuppressWarnings("unchecked")
				Action1<ListSelectionEvent> action = mock(Action1.class);
				@SuppressWarnings("unchecked")
				Action1<Throwable> error = mock(Action1.class);
				Action0 complete = mock(Action0.class);

				JTable table = createJTable();
				int numberOfListenersBefore = getNumberOfRowListSelectionListeners(table);

				Subscription sub = ListSelectionEventSource
						.fromListSelectionEventsOf(table.getSelectionModel())
						.subscribe(action, error, complete);

				sub.unsubscribe();

				table.getSelectionModel().setSelectionInterval(0, 0);

				verifyZeroInteractions(action, error, complete);

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

	private static Matcher<ListSelectionEvent> listSelectionEventMatcher(final Object source,
																		 final int firstIndex,
																		 final int lastIndex,
																		 final boolean isAdjusting) {
		return new ArgumentMatcher<ListSelectionEvent>() {
			@Override
			public boolean matches(Object argument) {
				if (argument.getClass() != ListSelectionEvent.class)
					return false;

				final ListSelectionEvent listSelectionEvent = (ListSelectionEvent) argument;
				return (listSelectionEvent.getSource() == source)
						&& (listSelectionEvent.getFirstIndex() == firstIndex)
						&& (listSelectionEvent.getLastIndex() == lastIndex)
						&& (listSelectionEvent.getValueIsAdjusting() == isAdjusting);
			}

			@Override
			public void describeTo(final Description description) {
				description.appendText("source=" + source);
				description.appendText(" firstIndex=" + firstIndex);
				description.appendText(" lastIndex=" + lastIndex);
				description.appendText(" isAdjusting=" + isAdjusting);
			}
		};
	}
}