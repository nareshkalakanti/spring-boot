/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * A thread-safe non-blocking circular buffer with a fixed capacity.
 *
 * @author Andy Wilkinson
 * @param <T> the type of the items in the buffer
 */
public class NonBlockingCircularBuffer<T> {

	private final List<AtomicStampedReference<T>> items;

	private final AtomicLong head = new AtomicLong(0);

	private final int capacity;

	/**
	 * Creates a new buffer with the given {@code capacity}.
	 *
	 * @param capacity the capacity
	 */
	public NonBlockingCircularBuffer(int capacity) {
		this.items = new ArrayList<AtomicStampedReference<T>>();
		this.capacity = capacity;
		for (int i = 0; i < capacity; i++) {
			this.items.add(new AtomicStampedReference<T>(null, -1));
		}
	}

	/**
	 * Adds the given item to the buffer.
	 *
	 * @param item the item
	 */
	public void add(T item) {
		long head = this.head.getAndIncrement();
		AtomicStampedReference<T> itemReference = getItemReference(head);
		int[] stampHolder = new int[1];
		T currentItem = itemReference.get(stampHolder);
		itemReference.compareAndSet(currentItem, item, stampHolder[0], getStamp(head));
	}

	/**
	 * Returns all of the items in the buffer.
	 *
	 * @return the items
	 */
	public List<T> findAll() {
		long end = this.head.get();
		long start = Math.max(0, end - this.capacity);
		List<T> all = new ArrayList<>();
		for (long i = start; i < end; i++) {
			T item = getUpToDateItem(i);
			if (item == null) {
				break;
			}
			all.add(item);
		}
		return all;
	}

	private T getUpToDateItem(long position) {
		AtomicStampedReference<T> itemReference = getItemReference(position);
		int[] stampHolder = new int[1];
		T item = itemReference.get(stampHolder);
		if (item == null) {
			return null;
		}
		int stamp = getStamp(position);
		while (stampHolder[0] < stamp) {
			item = itemReference.get(stampHolder);
		}
		return item;
	}

	private AtomicStampedReference<T> getItemReference(long head) {
		AtomicStampedReference<T> stampedReference = this.items
				.get((int) head % this.capacity);
		return stampedReference;
	}

	private int getStamp(long head) {
		return (int) (head % Integer.MAX_VALUE);
	}

}
